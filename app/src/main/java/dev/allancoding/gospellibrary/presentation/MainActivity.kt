package dev.allancoding.gospellibrary.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.ButtonSize
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.ReadContext
import dev.allancoding.gospellibrary.R
import dev.allancoding.gospellibrary.presentation.theme.GospelLibraryTheme
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.navigation.NavOptions
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import com.google.android.horologist.compose.material.ToggleChip
import kotlin.math.abs
import com.google.android.horologist.compose.material.ToggleChipToggleControl


class MainActivity : ComponentActivity() {
    private val sharedPreferences by lazy {
        getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp(sharedPreferences, this)
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

@Composable
fun WearApp(settings: SharedPreferences, context: MainActivity) {
    val navController = rememberSwipeDismissableNavController()
    GospelLibraryTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
            composable("menu") {
                AppScaffold(timeText = true) {
                    HomeScreen(
                        onShowBooksList = { navController.navigate("list") },
                        onShowSettingsList = { navController.navigate("settings") },
                        settings,
                        context,
                        onShowRead = { volumeId, bookId, chapterId ->
                            val saveLocation = "books/$volumeId/$bookId/$chapterId"
                            settingsSetValue(settings, "saveLocation", saveLocation)
                            navController.navigate(saveLocation)
                        }
                    )
                }
            }
            composable("list") {
                AppScaffold(timeText = true) {
                    ListBooksScreen(context, onShowListBooks = { bookId ->
                        navController.navigate("books/$bookId")
                    }, onShowListChapters = { bookId, chapterId ->
                        navController.navigate("books/$bookId/$chapterId")
                    })
                }
            }
            composable("books/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")
                AppScaffold(timeText = true) {
                    if (bookId != null) {
                        ListBooks(context, bookId, onShowListChapters = { bookId, chapterId ->
                            navController.navigate("books/$bookId/$chapterId")
                        })
                    }
                }
            }
            composable("books/{bookId}/{chapterId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")
                val chapterId = backStackEntry.arguments?.getString("chapterId")
                AppScaffold(timeText = true) {
                    if (bookId != null && chapterId != null) {
                        ListChapters(context, bookId, chapterId, onShowRead = { volumeId, bookId, chapterId ->
                            navController.navigate("books/$volumeId/$bookId/$chapterId")
                        })
                    }
                }
            }
            composable("books/{volumeId}/{bookId}/{chapterId}") { backStackEntry ->
                val volumeId = backStackEntry.arguments?.getString("volumeId")
                val bookId = backStackEntry.arguments?.getString("bookId")
                val chapterId = backStackEntry.arguments?.getString("chapterId")
                AppScaffold(timeText = false) {
                    if (volumeId != null && bookId != null && chapterId != null) {
                        settingsSetValue(settings, "saveLocation", "books/$volumeId/$bookId/$chapterId")
                        ReadChapter(context, volumeId, bookId, chapterId, onShowRead = { volumeId, bookId, chapterId ->
                            navController.navigate(
                                route = "books/$volumeId/$bookId/$chapterId",
                                navOptions = NavOptions.Builder()
                                    .setPopUpTo("books/$volumeId/$bookId/$chapterId", inclusive = true)
                                    .build()
                            )
                        }, settings)
                    }
                }
            }
            composable("settings") {
                AppScaffold(timeText = true) {
                    ListSettingsScreen(
                        onAboutPage = { navController.navigate("aboutPage") },
                        onLangSelect = { navController.navigate("langSelect") },
                        onCleanHistory = { navController.navigate("cleanHistory") },
                        settings
                    )
                }
            }
            composable("langSelect") {
                AppScaffold(timeText = false) {
                    LangSelect(
                        onShowSettingsList = {
                            navController.popBackStack("settings", false)
                        },
                        settings
                    )
                }
            }
            composable("cleanHistory") {
                AppScaffold(timeText = true) {
                    ClearHistoryScreen(
                        onShowSettingsList = {
                        navController.popBackStack("settings", false)
                    }, settings)
                }
            }
            composable("aboutPage") {
                AppScaffold(timeText = true) {
                    AboutScreen()
                }
            }
        }
    }

}

@Composable
fun AppScaffold(
    timeText: Boolean = true,
    content: @Composable () -> Unit
) {
    Scaffold {
        if (timeText) {
            TimeText()
        }
        content()
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeScreen(onShowBooksList: () -> Unit, onShowSettingsList: () -> Unit, settings: SharedPreferences, context: Context, onShowRead: (volume: String, books: String, chapter: String) -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary,
                        text = stringResource(R.string.app_name),
                        fontSize = 19.sp,
                    )
                }
            }
            var saveLocation = settingsGetValue(settings,"saveLocation", "null").toString()
            if (saveLocation != "null") {
                item {
                    saveLocation = saveLocation.removePrefix("books/")
                    val locations = saveLocation.split("/")
                    var volume = ""
                    var book = ""
                    var chapter = ""
                    for (i in locations.indices) {
                        when (i) {
                            0 -> {
                                volume = locations[i]
                            }
                            1 -> {
                                book = locations[i]
                            }
                            2 -> {
                                chapter = locations[i]
                            }
                        }
                    }
                    val type = getJson(context, "$.chapter.delineation", "$saveLocation.json", 0).toString()
                    val page = if (type == "Page") {
                        getJson(context, "$.chapter.title", "$saveLocation.json", 0).toString()
                    } else {
                        getJson(context, "$.chapter.bookTitle", "$saveLocation.json", 0).toString() + " " + getJson(context, "$.chapter.number", "$saveLocation.json", 1).toString()
                    }
                    Chip(label = "Continue Reading",
                        secondaryLabel = page,
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth(), onClick = { onShowRead(volume, book, chapter) })
                }
            }
            item {
                Chip(label = {
                    Text(
                        text = stringResource(R.string.Scriptures),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }, colors = ChipDefaults.imageBackgroundChipColors(
                    backgroundImagePainter = painterResource(id = R.drawable.scriptures_cover)
                ), modifier = Modifier.fillMaxWidth(),
                    onClick = onShowBooksList)
            }
            item {
                CompactChip(label = {
                    Text(
                        text = stringResource(R.string.Settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }, colors = ChipDefaults.secondaryChipColors(), onClick = onShowSettingsList)
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListBooksScreen(context: Context, onShowListBooks: (volumes: String) -> Unit, onShowListChapters: (volume: String, books: String) -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = stringResource(R.string.Scriptures), fontSize = 18.sp)
                }
            }
            for (i in 0..<getJson(context, "$.volumes","volumes.json", 2).toString().toInt()) {
                item {
                    var background = R.drawable.scriptures_cover
                    when (i) {
                        0 -> {
                            background = R.drawable.scriptures_old_testament
                        }
                        1 -> {
                            background = R.drawable.scriptures_new_testament
                        }
                        2 -> {
                            background = R.drawable.scripture_cover_book_mormon
                        }
                        3 -> {
                            background = R.drawable.scriptures_doctrine_and_covenants
                        }
                        4 -> {
                            background = R.drawable.scriptures_pearl_of_great_price
                        }
                    }
                    Chip(label = {
                        Text(
                            text = getJson(context, "$.volumes[$i].title","volumes.json", 0).toString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }, modifier = Modifier.fillMaxSize() ,colors = ChipDefaults.imageBackgroundChipColors(
                        backgroundImagePainter = painterResource(id = background)
                    ), onClick = {
                        if (i == 3) {
                            onShowListChapters(getJson(context, "$.volumes[$i]['_id']","volumes.json", 0).toString(), "doctrineandcovenants")
                        } else {
                            onShowListBooks(getJson(context, "$.volumes[$i]['_id']","volumes.json", 0).toString())
                        }
                    })
                }
            }
        }
    }
}
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListBooks(context: Context, volume: String, onShowListChapters: (volume: String, books: String) -> Unit){
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = getJson(context, "$.titleOfficial","$volume.json", 0).toString(), fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
            for (i in 0..<getJson(context, "$.books","$volume.json", 2).toString().toInt()) {
                item {
                    Chip(label = {
                        Text(text = getJson(context, "$.books[$i].title","$volume.json", 0).toString())
                    }, modifier = Modifier.fillMaxSize(), colors = ChipDefaults.secondaryChipColors(), onClick = { onShowListChapters(volume, getJson(context, "$.books[$i]['_id']", "$volume.json", 0).toString()) })
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListChapters(context: Context, volume: String, book: String, onShowRead: (volume: String, books: String, chapter: String) -> Unit){
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        ),
        verticalArrangement = Arrangement.spacedBy(space = 0.dp, alignment = Alignment.Top),
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = getJson(context, "$.title","$volume/$book.json", 0).toString(), fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
            val type = getJson(context, "$.chapterDelineation","$volume/$book.json", 0).toString()
            if (type == "Chapter" || type == "Section") {
                val numberOfItems = getJson(context, "$.chapters","$volume/$book.json", 2).toString().toInt()
                val itemsPerRow = 5
                val numberOfRows = (numberOfItems + itemsPerRow - 1) / itemsPerRow

                for (rowIndex in 0 until numberOfRows) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp)
                                .height(47.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (colIndex in 0 until itemsPerRow) {
                                val itemIndex = rowIndex * itemsPerRow + colIndex
                                if (itemIndex < numberOfItems) {
                                    val padding = when (itemIndex.toString().length) {
                                        1 -> 10.dp
                                        2 -> 8.dp
                                        3 -> 5.dp
                                        else -> 0.dp
                                    }
                                    CompactChip(
                                        label = { Text((itemIndex + 1).toString(), fontSize = 11.sp) },
                                        modifier = Modifier.padding(0.dp),
                                        contentPadding = PaddingValues(padding),
                                        colors = ChipDefaults.secondaryChipColors(),
                                        onClick = { onShowRead(volume, book, getJson(context, "$.chapters[$itemIndex]['_id']", "$volume/$book.json", 0).toString()) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(31.dp))
                                }
                            }
                        }
                    }
                }
            } else if (type == "Page") {
                for (i in 0..<getJson(context, "$.pages","$volume/$book.json", 2).toString().toInt()) {
                    item {
                        val chapter = getJson(context, "$.pages[$i]['_id']","$volume/$book.json", 0).toString()
                        Chip(label = {
                            Text(text = getJson(context, "$.chapter.title","$volume/$book/$chapter.json", 0).toString())
                        }, modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 2.dp), colors = ChipDefaults.secondaryChipColors(), onClick = { onShowRead(volume, book, chapter) })
                    }
                }
            }
        }
    }
}

fun handleReadScroll(delta: Float, volume: String, book: String, chapter: String, onShowRead: (volume: String, books: String, chapter: String) -> Unit, context: Context) {
    val nextChapterId = getJson(context, "$.nextChapterId", "$volume/$book/$chapter.json", 0).toString()
    val prevChapterId = getJson(context, "$.prevChapterId", "$volume/$book/$chapter.json", 0).toString()
    val nextChapterPath = getJson(context, "$.$nextChapterId.path", "search.json", 0).toString()
    val prevChapterPath = getJson(context, "$.$prevChapterId.path", "search.json", 0).toString()
    Log.d("json", "$volume - $book - $chapter")
    if (abs(delta) > 0) {
        var newLocation = nextChapterPath.split("/")
        if (delta > 0) {
            newLocation = prevChapterPath.split("/")
        }
        var newVolume = ""
        var newBook = ""
        var newChapter = ""
        for (i in newLocation.indices) {
            when (i) {
                0 -> {
                    newVolume = newLocation[i]
                }
                1 -> {
                    newBook = newLocation[i]
                }
                2 -> {
                    newChapter = newLocation[i]
                }
            }
        }
        onShowRead(newVolume, newBook, newChapter)
    }
}
@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ReadChapter(context: Context, volume: String, book: String, chapter: String, onShowRead: (volume: String, books: String, chapter: String) -> Unit, settings: SharedPreferences){
    val ensign = FontFamily(
        Font(R.font.mckaybroldslat_regular, FontWeight.Normal),
        Font(R.font.mckaybroldslat_bold, FontWeight.Bold),
        Font(R.font.mckaybroldslat_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.mckaybroldslat_bolditalic, FontWeight.Bold, FontStyle.Italic)
    )
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Text
        ),
    )
    val scrollState = rememberScrollableState { delta ->
        handleReadScroll(delta, volume, book, chapter, onShowRead, context)
        delta
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(
                orientation = Orientation.Horizontal,
                state = scrollState
            )
    ) {
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(
                columnState = columnState,
                modifier = Modifier.fillMaxSize()
            ) {
                val type = getJson(context, "$.chapter.delineation", "$volume/$book/$chapter.json", 0).toString()
                val number = getJson(context, "$.chapter.number","$volume/$book/$chapter.json", 1).toString().toInt()
                if (type == "Chapter" || type == "Section") {
                    if (number == 1 && volume == "bookofmormon") {
                        val subtitle = getJson(context, "$.subtitle", "$volume/$book.json", 0).toString()
                        val summary = getJson(context, "$.summary", "$volume/$book.json", 0).toString()
                        item {
                            Text(text = getJson(context, "$.titleOfficial", "$volume/$book.json", 0).toString().uppercase(), fontFamily = ensign, fontSize = 20.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (subtitle != "") {
                            item {
                                Text(text = subtitle.uppercase(), fontFamily = ensign, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        if (summary != "") {
                            item {
                                Text(text = summary, fontFamily = ensign)
                            }
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                    if (volume == "bookofmormon") {
                        val chapterAugmentations = getJson(context, "$.chapter.chapterAugmentations","$volume/$book/$chapter.json", 2).toString().toInt()
                        if (chapterAugmentations > 0) {
                            for (i in 0..<chapterAugmentations) {
                                val text = getJson(context, "$.chapter.chapterAugmentations[$i].text","$volume/$book/$chapter.json", 0).toString()
                                val subtext = getJson(context, "$.chapter.chapterAugmentations[$i].subtext","$volume/$book/$chapter.json", 0).toString()
                                if (text != "") {
                                    item {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    item {
                                        Text(text = text, fontFamily = ensign)
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    if (subtext != "") {
                                        item {
                                            Text(text = subtext, fontFamily = ensign, fontStyle = FontStyle.Italic)
                                        }
                                        item {
                                            Spacer(modifier = Modifier.height(5.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text(text = "${type.uppercase()} $number", fontFamily = ensign, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(25.dp))
                    }
                    item {
                        Text(text = getJson(context, "$.chapter.summary", "$volume/$book/$chapter.json", 0).toString(), fontFamily = ensign, fontStyle = FontStyle.Italic)
                    }
                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    val addFootnotes = settingsGetValue(settings,"footnote", "true").toString()
                    for (i in 0..<getJson(context, "$.chapter.verses","$volume/$book/$chapter.json", 2).toString().toInt()) {
                        val verse = (i + 1).toString()
                        val fixVerse = buildAnnotatedString {
                            append("$verse ")
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, verse.length)
                            val text = getJson(context, "$.chapter.verses[$i].text","$volume/$book/$chapter.json", 0).toString()
                            if (addFootnotes == "true") {
                                val footnote = getJson(context, "$.chapter.verses[$i].footnotes","$volume/$book/$chapter.json", 2).toString().toInt()
                                if (footnote > 0) {
                                    var textI = 0
                                    for (ii in 0..<footnote) {
                                        val start = getJson(context, "$.chapter.verses[$i].footnotes[$ii].start","$volume/$book/$chapter.json", 1).toString().toInt()
                                        val end = getJson(context, "$.chapter.verses[$i].footnotes[$ii].end","$volume/$book/$chapter.json", 1).toString().toInt()
                                        append(text.substring(textI, start))
                                        textI = end
                                        pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 14.sp * 0.75, fontStyle = FontStyle.Italic, color = Color.hsl(187F, 0.62F, 0.74F)))
                                        var letter = ii
                                        while (letter > 25) {
                                            letter -= 26
                                        }
                                        append(('a'.code + letter).toChar())
                                        pop()
                                        pushStyle(SpanStyle(color = Color.hsl(187F, 0.62F, 0.74F)))
                                        val note = text.substring(start, end)
                                        pushStringAnnotation(tag = "footnote/$ii", annotation = note)
                                        append(note)
                                        pop()
                                    }
                                    append(text.substring(textI, text.length))
                                } else {
                                    append(text)
                                }
                            } else {
                                append(text)
                            }
                        }
                        item {
                            Text(text = fixVerse, fontFamily = ensign, modifier = Modifier.clickable {
                                // Find if the click was on the annotated text
                                val annotation = fixVerse.getStringAnnotations(tag = "URL", start = 6, end = 10)
                                if (annotation.isNotEmpty()) {
                                    // Perform the action you want with the URL
                                    val url = annotation[0].item
                                    println("Clicked URL: $url")
                                }
                            })
                        }
                    }
                } else if (type == "Page") {
                    val typeOfPage = getJson(context, "$.chapter.title", "$volume/$book/$chapter.json", 0).toString()
                    if (typeOfPage == "Title Page" || typeOfPage == "The Book of Mormon") {
                        item {
                            Text(text = getJson(context, "$.volume.titleOfficial", "$volume/$book/$chapter.json", 0).toString().uppercase(), fontFamily = ensign, fontSize = 22.sp, textAlign = TextAlign.Center)
                        }
                        item {
                            Text(text = getJson(context, "$.chapter.subtitle", "$volume/$book/$chapter.json", 0).toString().uppercase(), fontFamily = ensign, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        item {
                            Text(text = getJson(context, "$.chapter.title", "$volume/$book/$chapter.json", 0).toString().uppercase(), fontFamily = ensign, fontSize = 16.sp, textAlign = TextAlign.Center)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    for (i in 0..<getJson(context, "$.chapter.body", "$volume/$book/$chapter.json", 2).toString().toInt()) {
                        val image = getJson(context, "$.chapter.body[$i].type", "$volume/$book/$chapter.json", 0).toString()
                        if (image == "image") {
                            val title = getJson(context, "$.chapter.body[$i].title", "$volume/$book/$chapter.json", 0).toString()
                            val author = getJson(context, "$.chapter.body[$i].author", "$volume/$book/$chapter.json", 0).toString()
                            val caption = getJson(context, "$.chapter.body[$i].caption", "$volume/$book/$chapter.json", 0).toString()
                            val src = context.resources.getIdentifier(getJson(context, "$.chapter.body[$i].image", "$volume/$book/$chapter.json", 0).toString().replace(".webp", ""), "drawable", context.packageName)
                            item {
                                Image(painter = painterResource(id = src), contentDescription = title)
                            }
                            item {
                                Text(text = title, fontFamily = ensign, fontStyle = FontStyle.Italic, textAlign = TextAlign.Left)
                            }
                            item {
                                Text(text = author, fontFamily = ensign, textAlign = TextAlign.Left)
                            }
                            if (caption != "") {
                                item {
                                    Text(text = caption, fontFamily = ensign, textAlign = TextAlign.Left)
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else {
                            item {
                                Text(text = getJson(context, "$.chapter.body[$i]", "$volume/$book/$chapter.json", 0).toString(), fontFamily = ensign, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }

                    }
                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    for (i in 0..<getJson(context, "$.chapter.footer", "$volume/$book/$chapter.json", 2).toString().toInt()) {
                        item {
                            Text(text = getJson(context, "$.chapter.footer[$i]", "$volume/$book/$chapter.json", 0).toString(), fontFamily = ensign, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListSettingsScreen(
    onAboutPage: () -> Unit,
    onLangSelect: () -> Unit,
    onCleanHistory: () -> Unit,
    settings: SharedPreferences
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = stringResource(R.string.Settings), fontSize = 18.sp)
                }
            }
            item {
                val items = getLang()
                Chip(label = {
                    Text(text = stringResource(R.string.Language))
                }, secondaryLabel = {
                    Text(text = items[settingsGetValue(settings,"lang", 0).toString().toInt()])
                }, modifier = Modifier.fillMaxSize(), colors = ChipDefaults.secondaryChipColors(), icon = {
                    Icon(Icons.Default.Language, contentDescription = null)
                }, onClick = onLangSelect)
            }
            item {
                var onFootnote by remember {
                    mutableStateOf(settingsGetValue(settings, "footnote", "true").toString().toBoolean())
                }
                ToggleChip(label = "Footnotes",
                    secondaryLabel = "Speeds up rendering",
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedToggleControlColor = MaterialTheme.colors.primary,
                        uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
                    ),
                    checked = onFootnote,
                    onCheckedChanged = { isChecked ->
                        onFootnote = isChecked
                        settingsSetValue(settings, "footnote", isChecked)
                    },
                    toggleControl = ToggleChipToggleControl.Switch,
                    icon = Icons.AutoMirrored.Filled.Notes)
            }
            item {
                Chip(label = {
                    Text(text = stringResource(R.string.ClearHistory))
                }, modifier = Modifier.fillMaxSize(), colors = ChipDefaults.secondaryChipColors(), icon = {
                    Icon(Icons.Default.CleaningServices, contentDescription = null)
                }, onClick = onCleanHistory)
            }
            item {
                Chip(label = {
                    Text(text = stringResource(R.string.About))
                }, modifier = Modifier.fillMaxSize(), colors = ChipDefaults.secondaryChipColors(), icon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                }, onClick = onAboutPage)
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun AboutScreen() {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.SingleButton
        )
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = stringResource(R.string.About), fontSize = 18.sp)
                }
            }
            item {
                ListItem(stringResource(R.string.Version), stringResource(R.string.app_version))
            }
            item {
                Column {
                    ListItem(stringResource(R.string.Copyright), "")
                    Text(
                        text = stringResource(R.string.copyright),
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            }
        }
    }
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
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LangSelect(onShowSettingsList: () -> Unit, settings: SharedPreferences) {
    val items = getLang()
    val state = rememberPickerState(
        initialNumberOfOptions = items.size,
        initiallySelectedOption = settingsGetValue(settings, "lang", 0).toString().toInt(),
        repeatItems = false
    )
    val contentDescription by remember { derivedStateOf { "${state.selectedOption}" } }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 15.dp),
            text = "Language",
            fontSize = 18.sp,
        )
        Picker(
            modifier = Modifier.size(100.dp, 100.dp),
            state = state,
            contentDescription = contentDescription,
            gradientColor = Color.Black,
        ) {
            Text(items[it], color = if (state.selectedOption == it) MaterialTheme.colors.primary else MaterialTheme.colors.onSurfaceVariant, fontSize = 17.sp)
        }
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            buttonSize = ButtonSize.Small,
            imageVector = Icons.Default.Check,
            contentDescription = "OK",
            onClick = {
                settingsSetValue(settings, "lang", state.selectedOption)
                onShowSettingsList()
            }
        )
    }
}

@Composable
fun getLang(): List<String> {
    return stringArrayResource(id = R.array.languages_array).toList()
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ClearHistoryScreen(onShowSettingsList: () -> Unit, settings: SharedPreferences) {
    Alert(
        icon = { Icon(Icons.Default.CleaningServices, contentDescription = "Clear") },
        title = { Text("Are you Sure?", textAlign = TextAlign.Center) },
        negativeButton = { Button(
            buttonSize = ButtonSize.Small,
            imageVector = Icons.Default.Close,
            colors = ButtonDefaults.secondaryButtonColors(),
            contentDescription = "Cancel",
            onClick = onShowSettingsList
        )},
        positiveButton = { Button(
            buttonSize = ButtonSize.Small,
            imageVector = Icons.Default.Check,
            colors = ButtonDefaults.primaryButtonColors(),
            contentDescription = "Ok",
            onClick = {
                settingsSetValue(settings, "saveLocation", "null")
                onShowSettingsList()
            }
        )},
        contentPadding =
        PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
    ) {
        Text(
            text = "Do you want to clear your reading history?",
            textAlign = TextAlign.Center
        )
    }
}
