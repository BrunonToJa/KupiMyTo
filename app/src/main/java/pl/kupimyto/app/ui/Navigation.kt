package pl.kupimyto.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/** Zakładki dolnego paska. */
enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Strona główna", Icons.Outlined.Home),
    CALENDAR("Kalendarz", Icons.Outlined.CalendarMonth),
    CHAT("Czat", Icons.Outlined.ChatBubbleOutline),
    MORE("Więcej", Icons.Outlined.MoreHoriz),
}

/** Podstrony otwierane z kafelków (i z "Więcej"). */
enum class Page(val title: String, val icon: ImageVector) {
    PANTRY("Spiżarnia", Icons.Outlined.Inventory2),
    RANKING("Ranking dzieci", Icons.Outlined.EmojiEvents),
    SHOPPING("Lista zakupów", Icons.Outlined.ShoppingCart),
    ALARM("Budzik", Icons.Outlined.Schedule),
    TASKS("Zadania", Icons.Outlined.CheckBox),
    FAMILY("Rodzina", Icons.Outlined.Groups),
    NOTIFICATIONS("Powiadomienia", Icons.Outlined.Notifications);

    companion object {
        /** Kolejność kafelków na ekranie głównym (3 x 2). */
        val homeTiles = listOf(PANTRY, RANKING, SHOPPING, ALARM, TASKS, FAMILY)
    }
}