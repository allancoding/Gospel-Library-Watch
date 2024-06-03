package dev.allancoding.gospellibrary.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.jayway.jsonpath.ReadContext
import com.jayway.jsonpath.JsonPath
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle

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
                        ReadChapter(context, volumeId, bookId, chapterId)
                    }
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

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ReadChapter(context: Context, volume: String, book: String, chapter: String){
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
                                    Text(text = text, fontFamily = ensign)
                                }
                                item {
                                    Spacer(modifier = Modifier.height(5.dp))
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
            }
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            for (i in 0..<getJson(context, "$.chapter.verses","$volume/$book/$chapter.json", 2).toString().toInt()) {
                val verse = (i + 1).toString()
                val spaces = "  ".repeat(verse.length)
                item {
                    Text(text = verse, fontFamily = ensign, fontWeight = FontWeight.Bold)
                    Text(text = "$spaces " + getJson(context, "$.chapter.verses[$i].text","$volume/$book/$chapter.json", 0).toString(), fontFamily = ensign)
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

@OptIn(ExperimentalHorologistApi::class)
@WearPreviewDevices
@Composable
fun TestPreview() {
    val columnState = rememberResponsiveColumnState(
        verticalArrangement = Arrangement.spacedBy(space = 0.dp, alignment = Alignment.Top),
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = "Test", fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
            val numberOfItems = 130
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
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
