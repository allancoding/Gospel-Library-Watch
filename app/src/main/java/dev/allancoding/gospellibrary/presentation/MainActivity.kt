package dev.allancoding.gospellibrary.presentation

import android.app.LauncherActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import dev.allancoding.gospellibrary.R
import dev.allancoding.gospellibrary.presentation.theme.GospelLibraryTheme
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.rememberPickerState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import com.google.android.horologist.compose.material.ButtonSize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    GospelLibraryTheme {
        AppScaffold {
            SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
                composable("menu") {
                    HomeScreen(
                        onShowBooksList = { navController.navigate("list") },
                        onShowSettingsList = { navController.navigate("settings") }
                    )
                }
                composable("list") {
                    ListBooksScreen()
                }
                composable("settings") {
                    ListSettingsScreen(
                        onLangSelect = { navController.navigate("langSelect")},
                        onAboutPage = { navController.navigate("aboutPage")},
                    )
                }
                composable("langSelect") {
                    LangSelect(
                        onShowSettingsList = { navController.navigate("settings")}
                    )
                }
                composable("aboutPage") {
                    AboutScreen()
                }
            }
        }
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
fun HomeScreen(onShowBooksList: () -> Unit, onShowSettingsList: () -> Unit,) {
    val scrollState = rememberScrollState()
    TimeText()
    ScreenScaffold(scrollState = scrollState) {
        val padding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .rotaryWithScroll(scrollState)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Home()
            Spacer(Modifier.height(5.dp))
            Chip(label = {
                Text(
                    text = stringResource(R.string.Scriptures),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }, colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.scriptures_cover)
            ), onClick = onShowBooksList)
            Spacer(Modifier.height(5.dp))
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

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListBooksScreen() {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.SingleButton
        )
    )

    ScreenScaffold(scrollState = columnState) {
        /*
         * The Horologist [ScalingLazyColumn] takes care of the horizontal and vertical
         * padding for the list, so there is no need to specify it, as in the [HomeScreen]
         * composable.
         */
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(text = "Header")
                }
            }
            item {
                TitleCard(title = { Text("Example Title") }, onClick = { }) {
                    Text("Example Content\nMore Lines\nAnd More")
                }
            }
            item {
                Chip(label = "Example Chip", onClick = { })
            }
            item {

            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListSettingsScreen(onLangSelect: () -> Unit, onAboutPage: () -> Unit) {
    TimeText()
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Text,
            last = ItemType.Text
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
                    Text(text = stringResource(R.string.Settings), fontSize = 18.sp,)
                }
            }
            item {
                val items = getLang()
                val state = rememberPickerState(items.size, repeatItems = false)
                Chip(label = {
                    Text(text = stringResource(R.string.Language))
                }, secondaryLabel = {
                    Text(text = items[state.selectedOption])
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
    TimeText()
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
                    Text(text = stringResource(R.string.About), fontSize = 18.sp,)
                }
            }
            item {
                ListItem(stringResource(R.string.Version), stringResource(R.string.app_version))
            }
            item {
                Column() {
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
fun LangSelect(onShowSettingsList: () -> Unit) {
    val items = getLang()
    val state = rememberPickerState(items.size, repeatItems = false)
    val contentDescription by remember { derivedStateOf { "${state.selectedOption + 1}" } }
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
            onSelected = {

            }
        ) {
            Text(items[it], color = if (state.selectedOption == it) MaterialTheme.colors.primary else MaterialTheme.colors.onSurfaceVariant, fontSize = 17.sp,)
        }
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            buttonSize = ButtonSize.Small,
            imageVector = Icons.Default.Check,
            contentDescription = "OK",
            onClick = onShowSettingsList
        )
    }
}

@Composable
fun getLang(): List<String> {
    return listOf(stringResource(R.string.English))
}

@WearPreviewDevices
@Composable
fun HomeScreenPreview() {
    HomeScreen(onShowBooksList = {}, onShowSettingsList = {})
}

@WearPreviewDevices
@Composable
fun ListBooksScreenPreview() {
    ListBooksScreen()
}

@WearPreviewDevices
@Composable
fun TestScreenPreview() {
    ListSettingsScreen(onLangSelect = { }) {
        
    }
}