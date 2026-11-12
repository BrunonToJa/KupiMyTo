package pl.kupimyto.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: AppViewModel) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var page by rememberSaveable { mutableStateOf<Page?>(null) }
    val currentPage = page

    // Systemowy "wstecz": podstrona -> strona główna -> wyjście z aplikacji
    BackHandler(enabled = currentPage != null || tab != Tab.HOME) {
        if (currentPage != null) page = null else tab = Tab.HOME
    }

    // Strona główna i lista zakupów mają własny nagłówek, reszta dostaje pasek z tytułem
    val title: String? = when {
        currentPage == Page.SHOPPING -> null
        currentPage != null -> currentPage.title
        tab != Tab.HOME -> tab.label
        else -> null
    }

    Scaffold(
        topBar = {
            if (title != null) {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (currentPage != null) {
                            IconButton(onClick = { page = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            }
        },
        bottomBar = {
            AppBottomBar(
                selected = tab,
                onSelect = {
                    page = null
                    tab = it
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (currentPage != null) {
                when (currentPage) {
                    Page.PANTRY, Page.RANKING, Page.ALARM -> ComingSoonScreen(currentPage.title, currentPage.icon)
                    Page.SHOPPING -> ShoppingScreen(vm, onBack = { page = null })
                    Page.TASKS -> TasksScreen(vm)
                    Page.FAMILY -> FamilyMembersScreen(vm)
                    Page.NOTIFICATIONS -> NotificationsScreen(vm)
                }
            } else {
                when (tab) {
                    Tab.HOME -> HomeScreen(
                        vm = vm,
                        onOpenPage = { page = it },
                        onOpenCalendar = { tab = Tab.CALENDAR },
                        onOpenProfile = { tab = Tab.MORE },
                    )
                    Tab.CALENDAR -> CalendarScreen(vm)
                    Tab.CHAT -> ComingSoonScreen(Tab.CHAT.label, Tab.CHAT.icon)
                    Tab.MORE -> MoreScreen(vm, onOpenNotifications = { page = Page.NOTIFICATIONS })
                }
            }
            if (vm.loading) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }
    }
}