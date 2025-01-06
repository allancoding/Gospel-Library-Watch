package dev.allancoding.gospellibrary.presentation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.wear.remote.interactions.RemoteActivityHelper
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.ReadContext
import dev.allancoding.gospellibrary.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

fun getOfTheDay(sharedPreferences: SharedPreferences, context: Context, callback: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val ofDayApi = "https://multimedia-audience-delivery.churchofjesuschrist.org/ws/mobile-mad/v1/general"
        val currentDate = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val lang = settingsGetValue(sharedPreferences, "language", "eng").toString()
        val savedLang = settingsGetValue(sharedPreferences, "apiLanguage", "eng").toString()
        val quoteApi = "$ofDayApi/QuoteOfTheDay/$lang/"
        val verseApi = "$ofDayApi/ScriptureOfTheDay/$lang/nomarkup/"
        val tryDates: Array<Array<String>> = Array(3) { Array(2) { "" } }
        val today = currentDate.format(dateFormatter)
        val yesterday = currentDate.minusDays(1).format(dateFormatter)
        val yestdate = currentDate.minusDays(2).format(dateFormatter)
        val todayRange = currentDate.plusDays(14).format(dateFormatter)
        val yesterdayRange = currentDate.plusDays(13).format(dateFormatter)
        val yestdateRange = currentDate.plusDays(12).format(dateFormatter)
        tryDates[0] = arrayOf(today, todayRange)
        tryDates[1] = arrayOf(yesterday, yesterdayRange)
        tryDates[2] = arrayOf(yestdate, yestdateRange)
        val savedDateString = settingsGetValue(sharedPreferences, "apiDate", "1970-01-01").toString()
        val savedDate = LocalDate.parse(savedDateString, dateFormatter)
        val thresholdDate = currentDate.minusDays(7)
        if (!savedDate.isBefore(thresholdDate) && lang == savedLang) {
            withContext(Dispatchers.Main) {
                callback()
            }
            return@launch
        }
        var tryDate = 0
        while (true) {
            try {
                val url = URL("$quoteApi/${tryDates[tryDate][0]}/${tryDates[tryDate][1]}")
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        connection.inputStream.use { input ->
                            FileOutputStream(File(context.filesDir, "quotes.json")).use { output ->
                                input.copyTo(output)
                            }
                        }
                        connection.disconnect()
                        val verseUrl = URL("$verseApi/${tryDates[tryDate][0]}/${tryDates[tryDate][1]}")
                        val verseConnection = verseUrl.openConnection() as HttpURLConnection
                        verseConnection.connect()
                        if (verseConnection.responseCode == HttpURLConnection.HTTP_OK) {
                            verseConnection.inputStream.use { input ->
                                FileOutputStream(File(context.filesDir, "verses.json")).use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } else {
                            println("Error downloading second file: HTTP ${verseConnection.responseCode}")
                        }
                        verseConnection.disconnect()
                        println("Files downloaded successfully.")
                        settingsSetValue(sharedPreferences, "apiDate", tryDates[tryDate][0])
                        settingsSetValue(sharedPreferences, "apiLanguage", lang)
                        break
                    }
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        println("Error: HTTP 403. Incrementing tryDate to retry.")
                        tryDate++
                        if (tryDate >= tryDates.size) {
                            println("All tryDates exhausted. Exiting.")
                            break
                        }
                    }
                    else -> {
                        println("Error: Server returned HTTP ${connection.responseCode}")
                        connection.disconnect()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("File download failed: ${e.message}")
                break
            }
        }
        withContext(Dispatchers.Main) {
            callback()
        }
    }
}

fun settingsGetValue(sharedPreferences: SharedPreferences, key: String, defaultVal: Any): Any {
    return sharedPreferences.getString(key, defaultVal.toString()) ?: ""
}

fun settingsSetValue(sharedPreferences: SharedPreferences, key: String, value: Any) {
    sharedPreferences.edit().putString(key, value.toString()).apply()
}

fun getJson(context: Context, path: String, file: String, type: Int): Any? {
    val jsonContent = context.assets.open("scriptures/$file").bufferedReader().use { it.readText() }
    val jsonContext: ReadContext = JsonPath.parse(jsonContent)
    return try {
        when (type) {
            0 -> {
                jsonContext.read<String>(path)
            }
            1 -> {
                jsonContext.read<Int>(path)
            }
            2 -> {
                val array: List<Any> = jsonContext.read(path)
                array.size
            }
            else -> {
                jsonContext.read<String>(path)
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        if (type == 2) {
            -1
        } else {
            null
        }
    }
}

fun getQuoteOfTheDay(context: Context): Map<String, String>? {
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val jsonText = File(context.filesDir, "quotes.json").readText()
    val path = "$[*]"
    val verses: List<Map<String, String>> = JsonPath.parse(jsonText).read(path)
    return verses.find { it["date"] == date }
}

fun getVerseOfTheDay(context: Context): Map<String, String>? {
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val jsonText = File(context.filesDir, "verses.json").readText()
    val path = "$[*]"
    val verses: List<Map<String, String>> = JsonPath.parse(jsonText).read(path)
    return verses.find { it["date"] == date }
}

fun saveQuoteImageList(context: Context) {
    thread(start = true) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.now().format(dateFormatter)

        val jsonText = File(context.filesDir, "quotes.json").readText()
        val jsonArray = JSONArray(jsonText)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val objDate = LocalDate.parse(obj.getString("date"), dateFormatter)
            if (objDate.isAfter(LocalDate.parse(date, dateFormatter)) || objDate.isEqual(LocalDate.parse(date, dateFormatter))) {
                try {
                    val imageUrl = obj.getString("imageAssetId")
                    val dateUrl = obj.getString("date")
                    prefetchImage(context, "https://www.churchofjesuschrist.org/imgs/$imageUrl/full/%21500%2C/0/default", dateUrl)
                } catch (e: Exception) {
                    println("No Image for quote $objDate")
                }
            }
        }
        removeExpiredCache(context)
    }
}

fun openUrlOnPhone(context: Context, url: String, callback: (Boolean) -> Unit) {
    val remoteActivityHelper = RemoteActivityHelper(context)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            remoteActivityHelper.startRemoteActivity(intent).await()
            withContext(Dispatchers.Main) {
                callback(true)
            }
        } catch (e: RemoteActivityHelper.RemoteIntentException) {
            println("Error: RemoteIntentException occurred - ${e.message}")
            withContext(Dispatchers.Main) {
                callback(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                callback(false)
            }
        }
    }
}

fun prefetchImage(context: Context, imageUrl: String, date: String) {
    if (!isImageCached(context, imageUrl)) {
        val imageLoader = ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .diskCachePolicy(CachePolicy.ENABLED)
            .listener(
                onSuccess = { _, _ ->
                    markImageAsCached(context, imageUrl, date)
                }
            )
            .build()
        imageLoader.enqueue(request)
    }
}

fun isImageCached(context: Context, imageUrl: String): Boolean {
    val prefs = context.getSharedPreferences("image_cache", Context.MODE_PRIVATE)
    return prefs.contains(imageUrl)
}

fun markImageAsCached(context: Context, imageUrl: String, date: String) {
    Log.d("ImageCache", "Marking image as cached: $imageUrl")
    val prefs = context.getSharedPreferences("image_cache", Context.MODE_PRIVATE)
    prefs.edit().putString(imageUrl, date).apply()
}

fun removeExpiredCache(context: Context) {
    val prefs = context.getSharedPreferences("image_cache", Context.MODE_PRIVATE)
    val currentDate = LocalDate.now()
    val cachedImages = prefs.all
    cachedImages.forEach { (imageUrl, cachedDateString) ->
        val cachedDate = LocalDate.parse(cachedDateString as String)
        if (cachedDate.isBefore(currentDate)) {
            prefs.edit().remove(imageUrl).apply()
        }
    }
}

fun Modifier.customLongPressGesture(
    longPressDurationMillis: Long = 500L,
    onLongPress: () -> Unit
): Modifier = this.pointerInput(Unit) {
    coroutineScope {
        detectTapGestures(
            onPress = {
                val job = launch {
                    delay(longPressDurationMillis)
                    onLongPress()
                }
                try {
                    awaitRelease()
                } finally {
                    job.cancel()
                }
            }
        )
    }
}

fun parseScriptureReferences(input: String): Array<String> {
    val splitReferences = input.split("; ")
    val cleanedReferences = splitReferences.map { it.trim() }
    val referencesArray = cleanedReferences.toTypedArray()
    return referencesArray
}

@Composable
fun ListItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun getLang(): List<String> {
    return stringArrayResource(id = R.array.languages_array).toList()
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true,targetValue:Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )
        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Suppress("unused")
@Composable
fun BlackScreenWithResettableTimeout(time: Long, content: @Composable () -> Unit) {
    var showBlackScreen by remember { mutableStateOf(false) }
    var interactionDetected by remember { mutableStateOf(false) }
    LaunchedEffect(interactionDetected) {
        if (interactionDetected) {
            Log.d("BlackScreen", "Interaction detected, resetting timeout.")
            interactionDetected = false
        }
        showBlackScreen = false
        delay(time)
        showBlackScreen = true
        Log.d("BlackScreen", "Timeout reached, showing black screen.")
    }
    content()
    if (showBlackScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures {
                        interactionDetected = true
                        Log.d("BlackScreen", "User interaction captured?")
                    }
                }
        )
    }
}