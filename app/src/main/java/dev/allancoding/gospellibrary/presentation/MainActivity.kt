package dev.allancoding.gospellibrary.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.beust.klaxon.Klaxon
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
import dev.allancoding.gospellibrary.R
import dev.allancoding.gospellibrary.presentation.theme.GospelLibraryTheme
import androidx.compose.ui.platform.LocalContext

data class Volume(val _id: String, val title: String, val books: List<Book>)
data class Book(val _id: String, val title: String, val chapters: List<Chapter>)
data class Chapter(val _id: String, val chapter: ChapterDetails)
data class ChapterDetails(val number: Int, val verses: List<Verse>)
data class Verse(val text: String)
data class JsonStructure(val volumes: List<Volume>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
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
fun getVerse(context: Context, bookTitle: String, chapterNumber: Int, verseIndex: Int): String? {
    val jsonContent = context.assets.open("scriptures.json").bufferedReader().use { it.readText() }
    val result = Klaxon().parse<JsonStructure>(jsonContent)

    result?.let { jsonStructure ->
        jsonStructure.volumes.forEach { volume ->
            volume.books.forEach { book ->
                if (book.title.equals(bookTitle, ignoreCase = true)) {
                    book.chapters.forEach { chapter ->
                        if (chapter.chapter.number == chapterNumber) {
                            if (verseIndex >= 0 && verseIndex < chapter.chapter.verses.size) {
                                return chapter.chapter.verses[verseIndex].text
                            }
                        }
                    }
                }
            }
        }
    }
    return null
}

@Composable
fun WearApp(settings: SharedPreferences, context: Context) {
    val navController = rememberSwipeDismissableNavController()
    GospelLibraryTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
            composable("menu") {
                AppScaffold(timeText = true) {
                    HomeScreen(
                        onShowBooksList = { navController.navigate("list") },
                        onShowSettingsList = { navController.navigate("settings") }
                    )
                }
            }
            composable("list") {
                AppScaffold(timeText = true) {
                    ListBooksScreen(context)
                }
            }
            composable("settings") {
                AppScaffold(timeText = true) {
                    ListSettingsScreen(
                        onAboutPage = { navController.navigate("aboutPage") },
                        onLangSelect = { navController.navigate("langSelect") },
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


@Composable
fun Home() {
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

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeScreen(onShowBooksList: () -> Unit, onShowSettingsList: () -> Unit) {
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
                Home()
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
                ), onClick = onShowBooksList)

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
fun ListBooksScreen(context: Context) {
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
                    Text(text = stringResource(R.string.Scriptures), fontSize = 18.sp)
                }
            }
            item {
                TitleCard(title = { Text("Verse") }, onClick = { }) {
                    Text(getVerse(context, "Genesis", 1, 0).toString())
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

@WearPreviewDevices
@Composable
fun HomeScreenPreview() {
    HomeScreen(onShowBooksList = {}, onShowSettingsList = {})
}

@WearPreviewDevices
@Composable
fun ListBooksScreenPreview() {
    val context = LocalContext.current
    ListBooksScreen(context)
}