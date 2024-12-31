package dev.allancoding.gospellibrary.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavOptions
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.allancoding.gospellibrary.presentation.theme.GospelLibraryTheme

class MainActivity : ComponentActivity() {
    private val sharedPreferences by lazy {
        getSharedPreferences("app_preferences", MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp(sharedPreferences, this, window)
        }
    }
}

@Composable
fun WearApp(settings: SharedPreferences, context: MainActivity, window: Window) {
    val navController = rememberSwipeDismissableNavController()
    GospelLibraryTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
            composable("menu") {
                AppScaffold(timeText = true) {
                    HomeScreen(
                        onShowBooksList = { navController.navigate("list") },
                        onShowSettingsList = { navController.navigate("settings") },
                        onShowOfTheDay = { type ->
                            navController.navigate("ofTheDay/$type")
                        },
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
            composable("ofTheDay/{type}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                AppScaffold(timeText = false) {
                    if (type != null) {
                        OfTheDayScreen(context, type)
                    }
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
                        }, settings)
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
                        ReadChapter(context, window, volumeId, bookId, chapterId, settings,
                            onShowFootnote = { volumeId, bookId, chapterId, verseId, footnoteId ->
                                navController.navigate("books/$volumeId/$bookId/$chapterId/$verseId/$footnoteId")
                            }, onShowReadMenu = { volumeId, bookId, chapterId ->
                                navController.navigate("books/$volumeId/$bookId/$chapterId/menu")
                            }
                        )
                    }
                }
            }
            composable("books/{volumeId}/{bookId}/{chapterId}/{verseId}/{footnoteId}") { backStackEntry ->
                val volumeId = backStackEntry.arguments?.getString("volumeId")
                val bookId = backStackEntry.arguments?.getString("bookId")
                val chapterId = backStackEntry.arguments?.getString("chapterId")
                val verseId = backStackEntry.arguments?.getString("verseId")
                val footnoteId = backStackEntry.arguments?.getString("footnoteId")
                AppScaffold(timeText = true) {
                    if (bookId != null && chapterId != null && volumeId != null && verseId != null && footnoteId != null) {
                        ShowFootnote(context, volumeId, bookId, chapterId, verseId, footnoteId)
                    }
                }
            }
            composable("books/{volumeId}/{bookId}/{chapterId}/{verseId}/menu") { backStackEntry ->
                val volumeId = backStackEntry.arguments?.getString("volumeId")
                val bookId = backStackEntry.arguments?.getString("bookId")
                val chapterId = backStackEntry.arguments?.getString("chapterId")
                AppScaffold(timeText = true) {
                    if (bookId != null && chapterId != null && volumeId != null ) {
                        ReadMenu(context, settings, volumeId, bookId, chapterId,
                            onShowRead = { volumeId, bookId, chapterId ->
                                navController.navigate(
                                    route = "books/$volumeId/$bookId/$chapterId",
                                    navOptions = NavOptions.Builder()
                                        .setPopUpTo("books/$volumeId/$bookId/$chapterId", inclusive = true)
                                        .build()
                                )
                            })
                    }
                }
            }
            composable("settings") {
                AppScaffold(timeText = true) {
                    ListSettingsScreen(
                        onAboutPage = { navController.navigate("aboutPage") },
                        onLangSelect = { navController.navigate("langSelect") },
                        onCleanHistory = { navController.navigate("cleanHistory") },
                        context,
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
                        context,
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
        content()
        if (timeText) {
            TimeText()
        }
    }
}
