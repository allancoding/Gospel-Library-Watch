package dev.allancoding.gospellibrary.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.WatchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Confirmation
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberPickerState
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl
import dev.allancoding.gospellibrary.R
import kotlin.math.abs


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeScreen(onShowBooksList: () -> Unit, onShowSettingsList: () -> Unit, onShowOfTheDay: (type: String) -> Unit, settings: SharedPreferences, context: Context, onShowRead: (volume: String, books: String, chapter: String) -> Unit) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    var quoteOfTheDay by remember { mutableStateOf<Map<String, String>?>(null) }
    var verseOfTheDay by remember { mutableStateOf<Map<String, String>?>(null) }
    var quoteTitle by remember { mutableStateOf("") }
    var verseTitle by remember { mutableStateOf("") }
    var ofDay by remember { mutableStateOf(settingsGetValue(settings, "apiDate", "null").toString()) }

    LaunchedEffect(settings) {
        getOfTheDay(settings, context) {
            ofDay = settingsGetValue(settings, "apiDate", "null").toString()
            if (ofDay != "null") {
                quoteOfTheDay = getQuoteOfTheDay(context)
                quoteOfTheDay?.let {
                    quoteTitle = it["title"] ?: ""
                    quoteTitle = buildAnnotatedString { append(quoteTitle.replace("&nbsp;", "\u00A0")) }.toString()
                }
                verseOfTheDay = getVerseOfTheDay(context)
                verseOfTheDay?.let {
                    verseTitle = it["title"] ?: ""
                    verseTitle = buildAnnotatedString { append(verseTitle.replace("&nbsp;", "\u00A0")) }.toString()
                }
            }
            saveQuoteImageList(context)
        }
    }
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
            var saveLocation = settingsGetValue(settings, "saveLocation", "null").toString()
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
            if (ofDay != "null") {
                item {
                    Chip(label = stringResource(R.string.QuoteOfTheDay),
                        secondaryLabel = quoteTitle,
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF6D0C32),
                            secondaryContentColor = Color(0xFFF8A0B2)
                        ),
                        modifier = Modifier.fillMaxWidth(), onClick = {onShowOfTheDay("quote")})
                }
                item {
                    Chip(label = stringResource(R.string.VerseOfTheDay),
                        secondaryLabel = verseTitle,
                        colors = ChipDefaults.gradientBackgroundChipColors(
                            startBackgroundColor = Color(0xFF1D4F73),
                            endBackgroundColor = Color(0xFF122F57),
                            gradientDirection = LayoutDirection.Ltr,
                            secondaryContentColor = Color(0xFFAFEEFC),
                        ),
                        modifier = Modifier.fillMaxWidth(), onClick = {onShowOfTheDay("verse")})
                }
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
fun OfTheDayScreen(context: Context, type: String) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    val ensign = FontFamily(
        Font(R.font.mckaybroldslat_regular, FontWeight.Normal),
        Font(R.font.mckaybroldslat_bold, FontWeight.Bold),
        Font(R.font.mckaybroldslat_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.mckaybroldslat_bolditalic, FontWeight.Bold, FontStyle.Italic)
    )
    if (type == "quote") {
        ScreenScaffold(
            scrollState = columnState,
            modifier = Modifier.background(Color(0xFF6D0C32))
        ) {
            ScalingLazyColumn(
                columnState = columnState,
                modifier = Modifier.fillMaxSize()
            ) {
                val quoteOfTheDay = getQuoteOfTheDay(context)
                var quoteTitle = ""
                var quoteText = ""
                var quoteImgUrl = ""
                var quoteUrl = ""
                quoteOfTheDay?.let {
                    quoteTitle = it["title"] ?: ""
                    quoteText = it["text"] ?: ""
                    quoteImgUrl = it["imageAssetId"] ?: ""
                    quoteUrl = it["uri"] ?: ""
                    quoteTitle = buildAnnotatedString {
                        append(
                            quoteTitle.replace(
                                "&nbsp;",
                                "\u00A0"
                            )
                        )
                    }.toString()
                    quoteText = buildAnnotatedString {
                        append(
                            quoteText.replace(
                                "&nbsp;",
                                "\u00A0"
                            )
                        )
                    }.toString()
                    if (quoteImgUrl != "") {
                        quoteImgUrl = "https://www.churchofjesuschrist.org/imgs/$quoteImgUrl/full/%21500%2C/0/default"
                    }
                    quoteUrl = "https://www.churchofjesuschrist.org$quoteUrl"
                }
                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Text(
                            text = stringResource(R.string.VerseOfTheDay),
                            fontSize = 18.sp,
                            color = Color(0xFFF8A0B2)
                        )
                    }
                }
                if (quoteImgUrl != "") {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val showShimmer = remember { mutableStateOf(true) }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(quoteImgUrl)
                                    .crossfade(1000)
                                    .build(),
                                error = painterResource(id = R.drawable.no_internet),
                                contentDescription = quoteTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .width(225.dp)
                                    .height(126.5.dp)
                                    .background(
                                        shimmerBrush(
                                            targetValue = 1300f,
                                            showShimmer = showShimmer.value
                                        )
                                    ),
                                onError = { showShimmer.value = false },
                                onSuccess = { showShimmer.value = false },
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = quoteText,
                        fontFamily = ensign,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
                item {
                    Text(
                        text = quoteTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        color = Color(0xFFF8A0B2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 5.dp)
                    )
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        var showTrueDialog by remember { mutableStateOf(false) }
                        var showFalseDialog by remember { mutableStateOf(false) }
                        CompactChip(label = {
                            Text(
                                text = stringResource(R.string.OpenOnPhone),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }, colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFFA6014E),
                        ), onClick = {
                            openUrlOnPhone(context, quoteUrl) { success ->
                                if (success) {
                                    showTrueDialog = true
                                } else {
                                    showFalseDialog = true
                                }
                            }
                        })
                        Dialog(
                            showDialog = showTrueDialog,
                            onDismissRequest = { showTrueDialog = false }) {
                            Confirmation(
                                onTimeout = { showTrueDialog = false },
                                icon = {
                                    Icon(
                                        Icons.Default.PhonelinkRing,
                                        contentDescription = "Phone"
                                    )
                                },
                                durationMillis = 3000,
                            ) {
                                Text(
                                    text = stringResource(R.string.TrueOpenOnPhone),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Dialog(
                            showDialog = showFalseDialog,
                            onDismissRequest = { showFalseDialog = false }) {
                            Confirmation(
                                onTimeout = { showFalseDialog = false },
                                icon = {
                                    Icon(
                                        Icons.Default.PhonelinkRing,
                                        contentDescription = "Phone"
                                    )
                                },
                                durationMillis = 4000,
                            ) {
                                Text(
                                    text = stringResource(R.string.FalseOpenOnPhone),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            TimeText()
        }
    } else if (type == "verse") {
        ScreenScaffold(
            scrollState = columnState, modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D4F73),
                        Color(0xFF122F57)
                    )
                )
            )
        ) {
            ScalingLazyColumn(
                columnState = columnState,
                modifier = Modifier.fillMaxSize()
            ) {
                val verseOfTheDay = getVerseOfTheDay(context)
                var verseTitle = ""
                var verseText = ""
                var verseUrl = ""
                verseOfTheDay?.let {
                    verseTitle = it["title"] ?: ""
                    verseText = it["text"] ?: ""
                    verseUrl = it["uri"] ?: ""
                    verseTitle = buildAnnotatedString {
                        append(
                            verseTitle.replace(
                                "&nbsp;",
                                "\u00A0"
                            )
                        )
                    }.toString()
                    verseText = buildAnnotatedString {
                        append(
                            verseText.replace(
                                "&nbsp;",
                                "\u00A0"
                            )
                        )
                    }.toString()
                    verseUrl = "https://www.churchofjesuschrist.org$verseUrl"
                }
                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Text(
                            text = stringResource(R.string.VerseOfTheDay),
                            fontSize = 18.sp,
                            color = Color(0xFFAFEEFC)
                        )
                    }
                }
                item {
                    Text(text = verseText, fontFamily = ensign, fontSize = 16.sp)
                }
                item {
                    Text(
                        text = verseTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        color = Color(0xFFAFEEFC),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 5.dp)
                    )
                }
                item {
                    var showTrueDialog by remember { mutableStateOf(false) }
                    var showFalseDialog by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        CompactChip(label = {
                            Text(
                                text = stringResource(R.string.OpenOnPhone),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }, colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF226083),
                        ), onClick = {
                            openUrlOnPhone(context, verseUrl) { success ->
                                if (success) {
                                    showTrueDialog = true
                                } else {
                                    showFalseDialog = true
                                }
                            }
                        })
                        Dialog(
                            showDialog = showTrueDialog,
                            onDismissRequest = { showTrueDialog = false }) {
                            Confirmation(
                                onTimeout = { showTrueDialog = false },
                                icon = {
                                    Icon(
                                        Icons.Default.PhonelinkRing,
                                        contentDescription = "Phone"
                                    )
                                },
                                durationMillis = 3000,
                            ) {
                                Text(
                                    text = stringResource(R.string.TrueOpenOnPhone),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Dialog(
                            showDialog = showFalseDialog,
                            onDismissRequest = { showFalseDialog = false }) {
                            Confirmation(
                                onTimeout = { showFalseDialog = false },
                                icon = {
                                    Icon(
                                        Icons.Default.PhonelinkRing,
                                        contentDescription = "Phone"
                                    )
                                },
                                durationMillis = 5000,
                            ) {
                                Text(
                                    text = stringResource(R.string.FalseOpenOnPhone),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            TimeText()
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListBooksScreen(
    context: Context,
    onShowListBooks: (volumes: String) -> Unit,
    onShowListChapters: (volume: String, books: String) -> Unit
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
                    Text(text = stringResource(R.string.Scriptures), fontSize = 18.sp)
                }
            }
            for (i in 0..<getJson(context, "$.volumes", "volumes.json", 2).toString().toInt()) {
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
                            text = getJson(
                                context,
                                "$.volumes[$i].title",
                                "volumes.json",
                                0
                            ).toString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                        modifier = Modifier.fillMaxSize(),
                        colors = ChipDefaults.imageBackgroundChipColors(
                            backgroundImagePainter = painterResource(id = background)
                        ),
                        onClick = {
                            if (i == 3) {
                                onShowListChapters(
                                    getJson(
                                        context,
                                        "$.volumes[$i]['_id']",
                                        "volumes.json",
                                        0
                                    ).toString(), "doctrineandcovenants"
                                )
                            } else {
                                onShowListBooks(
                                    getJson(
                                        context,
                                        "$.volumes[$i]['_id']",
                                        "volumes.json",
                                        0
                                    ).toString()
                                )
                            }
                        })
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListBooks(
    context: Context,
    volume: String,
    onShowListChapters: (volume: String, books: String) -> Unit
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
                    Text(
                        text = getJson(
                            context,
                            "$.titleOfficial",
                            "$volume.json",
                            0
                        ).toString(), fontSize = 18.sp, textAlign = TextAlign.Center
                    )
                }
            }
            for (i in 0..<getJson(context, "$.books", "$volume.json", 2).toString().toInt()) {
                item {
                    Chip(
                        label = {
                            Text(
                                text = getJson(
                                    context,
                                    "$.books[$i].title",
                                    "$volume.json",
                                    0
                                ).toString()
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ChipDefaults.secondaryChipColors(),
                        onClick = {
                            onShowListChapters(
                                volume,
                                getJson(
                                    context,
                                    "$.books[$i]['_id']",
                                    "$volume.json",
                                    0
                                ).toString()
                            )
                        })
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListChapters(
    context: Context,
    volume: String,
    book: String,
    onShowRead: (volume: String, books: String, chapter: String) -> Unit,
    settings: SharedPreferences
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        ),
        verticalArrangement = Arrangement.spacedBy(space = 0.dp, alignment = Alignment.Top),
    )
    val type = getJson(context, "$.chapterDelineation", "$volume/$book.json", 0).toString()
    var numberOfItems = 1
    if (type == "Chapter" || type == "Section") {
        numberOfItems =
            getJson(context, "$.chapters", "$volume/$book.json", 2).toString().toInt()
    }
    val items = (1..numberOfItems).map { it.toString() }
    val state = rememberPickerState(initialNumberOfOptions = items.size, repeatItems = false)
    if (type == "Chapter" || type == "Section") {
        if (items.size == 1) {
            onShowRead(
                volume,
                book,
                getJson(
                    context,
                    "$.chapters[0]['_id']",
                    "$volume/$book.json",
                    0
                ).toString()
            )
        } else {
            val grid = settingsGetValue(settings, "grid", "false").toString().toBoolean()
            if (grid) {
                ScreenScaffold(scrollState = columnState) {
                    ScalingLazyColumn(
                        columnState = columnState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            ResponsiveListHeader(contentPadding = firstItemPadding()) {
                                Text(
                                    text = getJson(
                                        context,
                                        "$.title",
                                        "$volume/$book.json",
                                        0
                                    ).toString(), fontSize = 18.sp, textAlign = TextAlign.Center
                                )
                            }
                        }
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
                                                label = {
                                                    Text(
                                                        (itemIndex + 1).toString(),
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                modifier = Modifier.padding(0.dp),
                                                contentPadding = PaddingValues(padding),
                                                colors = ChipDefaults.secondaryChipColors(),
                                                onClick = {
                                                    onShowRead(
                                                        volume,
                                                        book,
                                                        getJson(
                                                            context,
                                                            "$.chapters[$itemIndex]['_id']",
                                                            "$volume/$book.json",
                                                            0
                                                        ).toString()
                                                    )
                                                }
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.width(31.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(top = 30.dp),
                        text = getJson(
                            context,
                            "$.title",
                            "$volume/$book.json",
                            0
                        ).toString(), fontSize = 18.sp, textAlign = TextAlign.Center
                    )
                    val contentDescription by remember { derivedStateOf { "${state.selectedOption + 1}" } }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Picker(
                            modifier = Modifier.size(100.dp, 100.dp),
                            state = state,
                            contentDescription = contentDescription
                        ) {
                            Text(
                                items[it],
                                color = if (state.selectedOption == it) MaterialTheme.colors.primary else MaterialTheme.colors.onSurfaceVariant,
                                fontSize = 17.sp
                            )
                        }
                        Button(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp),
                            buttonSize = ButtonSize.Small,
                            imageVector = Icons.Default.Check,
                            contentDescription = "OK",
                            onClick = {
                                onShowRead(
                                    volume,
                                    book,
                                    getJson(
                                        context,
                                        "$.chapters[${state.selectedOption}]['_id']",
                                        "$volume/$book.json",
                                        0
                                    ).toString()
                                )
                            }
                        )
                    }
                }
            }
        }
    } else if (type == "Page") {
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(
                columnState = columnState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Text(
                            text = getJson(
                                context,
                                "$.title",
                                "$volume/$book.json",
                                0
                            ).toString(), fontSize = 18.sp, textAlign = TextAlign.Center
                        )
                    }
                }
                for (i in 0..<getJson(context, "$.pages", "$volume/$book.json", 2).toString()
                    .toInt()) {
                    item {
                        val chapter = getJson(
                            context,
                            "$.pages[$i]['_id']",
                            "$volume/$book.json",
                            0
                        ).toString()
                        Chip(label = {
                            Text(
                                text = getJson(
                                    context,
                                    "$.chapter.title",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString()
                            )
                        },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 2.dp),
                            colors = ChipDefaults.secondaryChipColors(),
                            onClick = { onShowRead(volume, book, chapter) })
                    }
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ReadChapter(
    context: Context,
    window: Window,
    volume: String,
    book: String,
    chapter: String,
    settings: SharedPreferences,
    onShowFootnote: (volume: String, books: String, chapter: String, verse: String, footnote: String) -> Unit,
    onShowReadMenu: (volume: String, books: String, chapter: String) -> Unit
) {
    val timeOut = settingsGetValue(settings, "screen", "false").toString().toBoolean()
    if (timeOut) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        DisposableEffect(Unit) {
            onDispose {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
    val haptics = LocalHapticFeedback.current
    Box(modifier = Modifier
        .fillMaxSize()
        .customLongPressGesture(longPressDurationMillis = 1000L) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onShowReadMenu(
                volume,
                book,
                chapter)
        }
    ) {
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
                columnState = columnState
            ) {
                val type = getJson(
                    context,
                    "$.chapter.delineation",
                    "$volume/$book/$chapter.json",
                    0
                ).toString()
                val number = getJson(
                    context,
                    "$.chapter.number",
                    "$volume/$book/$chapter.json",
                    1
                ).toString().toInt()
                if (type == "Chapter" || type == "Section") {
                    if (number == 1 && volume == "bookofmormon") {
                        val subtitle = getJson(
                            context,
                            "$.subtitle",
                            "$volume/$book.json",
                            0
                        ).toString()
                        val summary = getJson(
                            context,
                            "$.summary",
                            "$volume/$book.json",
                            0
                        ).toString()
                        item {
                            Text(
                                text = getJson(
                                    context,
                                    "$.titleOfficial",
                                    "$volume/$book.json",
                                    0
                                ).toString().uppercase(),
                                fontFamily = ensign,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (subtitle != "") {
                            item {
                                Text(
                                    text = subtitle.uppercase(),
                                    fontFamily = ensign,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
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
                        val chapterAugmentations = getJson(
                            context,
                            "$.chapter.chapterAugmentations",
                            "$volume/$book/$chapter.json",
                            2
                        ).toString().toInt()
                        if (chapterAugmentations > 0) {
                            for (i in 0..<chapterAugmentations) {
                                val text = getJson(
                                    context,
                                    "$.chapter.chapterAugmentations[$i].text",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString()
                                val subtext = getJson(
                                    context,
                                    "$.chapter.chapterAugmentations[$i].subtext",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString()
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
                                            Text(
                                                text = subtext,
                                                fontFamily = ensign,
                                                fontStyle = FontStyle.Italic
                                            )
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
                        Text(
                            text = "${type.uppercase()} $number",
                            fontFamily = ensign,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(25.dp))
                    }
                    item {
                        Text(
                            text = getJson(
                                context,
                                "$.chapter.summary",
                                "$volume/$book/$chapter.json",
                                0
                            ).toString(), fontFamily = ensign, fontStyle = FontStyle.Italic
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    val addFootnotes =
                        settingsGetValue(settings, "footnote", "true").toString()
                    for (i in 0..<getJson(
                        context,
                        "$.chapter.verses",
                        "$volume/$book/$chapter.json",
                        2
                    ).toString().toInt()) {
                        val verse = (i + 1).toString()
                        val fixVerse = buildAnnotatedString {
                            append("$verse ")
                            addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                0,
                                verse.length
                            )
                            val text = getJson(
                                context,
                                "$.chapter.verses[$i].text",
                                "$volume/$book/$chapter.json",
                                0
                            ).toString()
                            if (addFootnotes == "true") {
                                val footnote = getJson(
                                    context,
                                    "$.chapter.verses[$i].footnotes",
                                    "$volume/$book/$chapter.json",
                                    2
                                ).toString().toInt()
                                if (footnote > 0) {
                                    var textI = 0
                                    for (ii in 0..<footnote) {
                                        val start = getJson(
                                            context,
                                            "$.chapter.verses[$i].footnotes[$ii].start",
                                            "$volume/$book/$chapter.json",
                                            1
                                        ).toString().toInt()
                                        val end = getJson(
                                            context,
                                            "$.chapter.verses[$i].footnotes[$ii].end",
                                            "$volume/$book/$chapter.json",
                                            1
                                        ).toString().toInt()
                                        append(text.substring(textI, start))
                                        textI = end
                                        pushStyle(
                                            SpanStyle(
                                                baselineShift = BaselineShift.Superscript,
                                                fontSize = 14.sp * 0.75,
                                                fontStyle = FontStyle.Italic,
                                                color = Color.hsl(187F, 0.62F, 0.74F)
                                            )
                                        )
                                        val letter = ('a'.code + (ii % 26)).toChar()
                                        append(letter)
                                        pop()
                                        pushStyle(
                                            SpanStyle(
                                                color = Color.hsl(
                                                    190F,
                                                    0.76F,
                                                    0.59F
                                                )
                                            )
                                        )
                                        val note = text.substring(start, end)
                                        withLink(
                                            LinkAnnotation.Clickable(
                                                "footnote",
                                                linkInteractionListener = {
                                                    onShowFootnote(
                                                        volume,
                                                        book,
                                                        chapter,
                                                        i.toString(),
                                                        ii.toString()
                                                    )
                                                })
                                        ) {
                                            append(note)
                                        }
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
                            Text(text = fixVerse, fontFamily = ensign)
                        }
                    }
                } else if (type == "Page") {
                    val typeOfPage = getJson(
                        context,
                        "$.chapter.title",
                        "$volume/$book/$chapter.json",
                        0
                    ).toString()
                    if (typeOfPage == "Title Page" || typeOfPage == "The Book of Mormon") {
                        item {
                            Text(
                                text = getJson(
                                    context,
                                    "$.volume.titleOfficial",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString().uppercase(),
                                fontFamily = ensign,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        item {
                            Text(
                                text = getJson(
                                    context,
                                    "$.chapter.subtitle",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString().uppercase(),
                                fontFamily = ensign,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = getJson(
                                    context,
                                    "$.chapter.title",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString().uppercase(),
                                fontFamily = ensign,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    for (i in 0..<getJson(
                        context,
                        "$.chapter.body",
                        "$volume/$book/$chapter.json",
                        2
                    ).toString().toInt()) {
                        val image = getJson(
                            context,
                            "$.chapter.body[$i].type",
                            "$volume/$book/$chapter.json",
                            0
                        ).toString()
                        if (image == "image") {
                            val title = getJson(
                                context,
                                "$.chapter.body[$i].title",
                                "$volume/$book/$chapter.json",
                                0
                            ).toString()
                            val author = getJson(
                                context,
                                "$.chapter.body[$i].author",
                                "$volume/$book/$chapter.json",
                                0
                            ).toString()
                            val caption = getJson(
                                context,
                                "$.chapter.body[$i].caption",
                                "$volume/$book/$chapter.json",
                                0
                            ).toString()
                            val src = context.resources.getIdentifier(
                                getJson(
                                    context,
                                    "$.chapter.body[$i].image",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString().replace(".webp", ""),
                                "drawable",
                                context.packageName
                            )
                            item {
                                Image(
                                    painter = painterResource(id = src),
                                    contentDescription = title
                                )
                            }
                            item {
                                Text(
                                    text = title,
                                    fontFamily = ensign,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Left
                                )
                            }
                            item {
                                Text(
                                    text = author,
                                    fontFamily = ensign,
                                    textAlign = TextAlign.Left
                                )
                            }
                            if (caption != "") {
                                item {
                                    Text(
                                        text = caption,
                                        fontFamily = ensign,
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else {
                            item {
                                Text(
                                    text = getJson(
                                        context,
                                        "$.chapter.body[$i]",
                                        "$volume/$book/$chapter.json",
                                        0
                                    ).toString(),
                                    fontFamily = ensign,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                    }
                    item {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    for (i in 0..<getJson(
                        context,
                        "$.chapter.footer",
                        "$volume/$book/$chapter.json",
                        2
                    ).toString().toInt()) {
                        item {
                            Text(
                                text = getJson(
                                    context,
                                    "$.chapter.footer[$i]",
                                    "$volume/$book/$chapter.json",
                                    0
                                ).toString(),
                                fontFamily = ensign,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ReadMenu(
    context: Context,
    settings: SharedPreferences,
    volume: String,
    book: String,
    chapter: String,
    onShowRead: (volume: String, books: String, chapter: String) -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Text
        ),
        verticalArrangement = Arrangement.spacedBy(space = 0.dp, alignment = Alignment.Top),
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {

            }
        }
        val scrollState = rememberScrollableState { delta ->
            if (abs(delta) > 0) {
                val nextChapterId = getJson(context, "$.nextChapterId", "$volume/$book/$chapter.json", 0).toString()
                val prevChapterId = getJson(context, "$.prevChapterId", "$volume/$book/$chapter.json", 0).toString()
                val nextChapterPath = getJson(context, "$.$nextChapterId.path", "search.json", 0).toString()
                val prevChapterPath = getJson(context, "$.$prevChapterId.path", "search.json", 0).toString()
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
            delta
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ShowFootnote(
    context: Context,
    volume: String,
    book: String,
    chapter: String,
    verse: String,
    footnote: String
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Text
        ),
        verticalArrangement = Arrangement.spacedBy(space = 0.dp, alignment = Alignment.Top),
    )
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.fillMaxSize()
        ) {
            val title = getJson(
                context,
                "$.chapter.verses[${verse.toInt()}].footnotes[${footnote.toInt()}].footnote",
                "$volume/$book/$chapter.json",
                0
            ).toString()
            item {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 15.dp),
                    text = title,
                    fontSize = 18.sp,
                )
                Log.d("foot", parseScriptureReferences(title).toString())
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListSettingsScreen(
    onAboutPage: () -> Unit,
    onLangSelect: () -> Unit,
    onCleanHistory: () -> Unit,
    context: Context,
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
                Chip(
                    label = {
                        Text(text = stringResource(R.string.Language))
                    },
                    secondaryLabel = {
                        val resourceId = context.resources.getIdentifier(
                            items[settingsGetValue(
                                settings,
                                "languages_array",
                                0
                            ).toString().toInt()], "string", context.packageName
                        )
                        val text = stringResource(id = resourceId)
                        Text(text = text)
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ChipDefaults.secondaryChipColors(),
                    icon = {
                        Icon(Icons.Default.Language, contentDescription = null)
                    },
                    onClick = onLangSelect
                )
            }
            item {
                var onFootnote by remember {
                    mutableStateOf(
                        settingsGetValue(settings, "footnote", "true").toString().toBoolean()
                    )
                }
                ToggleChip(
                    label = stringResource(R.string.Footnotes),
                    secondaryLabel = stringResource(R.string.FootnoteLabel),
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
                    icon = Icons.AutoMirrored.Filled.Notes
                )
            }
            item {
                var onGrid by remember {
                    mutableStateOf(
                        settingsGetValue(settings, "grid", "false").toString().toBoolean()
                    )
                }
                var gridLabel by remember { mutableStateOf("") }
                var icon by remember { mutableStateOf(Icons.Default.GridOn) }
                val gridText = stringResource(R.string.Grid)
                val spinText = stringResource(R.string.Spin)
                val gridLabelText = stringResource(R.string.GridLabel)
                gridLabel = if (onGrid) gridText else spinText
                icon = if (onGrid) Icons.Default.GridOn else Icons.AutoMirrored.Filled.List
                ToggleChip(
                    label = gridLabel,
                    secondaryLabel = gridLabelText,
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedToggleControlColor = MaterialTheme.colors.primary,
                        uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
                    ),
                    checked = onGrid,
                    onCheckedChanged = { isChecked ->
                        onGrid = isChecked
                        settingsSetValue(settings, "grid", isChecked)
                        gridLabel = if (isChecked) gridText else spinText
                        icon =
                            if (isChecked) Icons.Default.GridOn else Icons.AutoMirrored.Filled.List
                    },
                    toggleControl = ToggleChipToggleControl.Switch,
                    icon = icon
                )
            }
            item {
                var onScreen by remember {
                    mutableStateOf(
                        settingsGetValue(settings, "screen", "true").toString().toBoolean()
                    )
                }
                ToggleChip(
                    label = stringResource(R.string.Screen),
                    secondaryLabel = stringResource(R.string.ScreenLabel),
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedToggleControlColor = MaterialTheme.colors.primary,
                        uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
                    ),
                    checked = onScreen,
                    onCheckedChanged = { isChecked ->
                        onScreen = isChecked
                        settingsSetValue(settings, "screen", isChecked)
                    },
                    toggleControl = ToggleChipToggleControl.Switch,
                    icon = Icons.Default.WatchOff
                )
            }
            item {
                Chip(
                    label = {
                        Text(text = stringResource(R.string.ClearHistory))
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ChipDefaults.secondaryChipColors(),
                    icon = {
                        Icon(Icons.Default.CleaningServices, contentDescription = null)
                    },
                    onClick = onCleanHistory
                )
            }
            item {
                Chip(
                    label = {
                        Text(text = stringResource(R.string.About))
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ChipDefaults.secondaryChipColors(),
                    icon = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    onClick = onAboutPage
                )
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

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun LangSelect(onShowSettingsList: () -> Unit, context: Context, settings: SharedPreferences) {
    val items = getLang()
    val state = rememberPickerState(
        initialNumberOfOptions = items.size,
        initiallySelectedOption = settingsGetValue(settings, "languages_array", 0).toString()
            .toInt(),
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
            val resourceId =
                context.resources.getIdentifier(items[it], "string", context.packageName)
            val text = stringResource(id = resourceId)
            Text(
                text = text,
                color = if (state.selectedOption == it) MaterialTheme.colors.primary else MaterialTheme.colors.onSurfaceVariant,
                fontSize = 17.sp
            )
        }
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            buttonSize = ButtonSize.Small,
            imageVector = Icons.Default.Check,
            contentDescription = "OK",
            onClick = {
                settingsSetValue(settings, "languages_array", state.selectedOption)
                settingsSetValue(settings, "language", items[state.selectedOption])
                onShowSettingsList()
            }
        )
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ClearHistoryScreen(onShowSettingsList: () -> Unit, settings: SharedPreferences) {
    Alert(
        icon = { Icon(Icons.Default.CleaningServices, contentDescription = "Clear") },
        title = { Text("Are you Sure?", textAlign = TextAlign.Center) },
        negativeButton = {
            Button(
                buttonSize = ButtonSize.Small,
                imageVector = Icons.Default.Close,
                colors = ButtonDefaults.secondaryButtonColors(),
                contentDescription = "Cancel",
                onClick = onShowSettingsList
            )
        },
        positiveButton = {
            Button(
                buttonSize = ButtonSize.Small,
                imageVector = Icons.Default.Check,
                colors = ButtonDefaults.primaryButtonColors(),
                contentDescription = "Ok",
                onClick = {
                    settingsSetValue(settings, "saveLocation", "null")
                    onShowSettingsList()
                }
            )
        },
        contentPadding =
        PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
    ) {
        Text(
            text = "Do you want to clear your reading history?",
            textAlign = TextAlign.Center
        )
    }
}