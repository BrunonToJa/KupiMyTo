package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.TaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private data class TodayItem(
    val icon: ImageVector,
    val title: String,
    val time: String?,
    val onClick: () -> Unit,
)

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onOpenPage: (Page) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val today = LocalDate.now()

    val todayItems = buildList<TodayItem> {
        vm.events.filter { it.startDate.toLocalDate() == today }.sortedBy { it.startDate }.forEach { e ->
            add(TodayItem(Icons.Outlined.CalendarMonth, e.title, e.startDate.format(timeFmt)) { onOpenCalendar() })
        }
        vm.tasks.filter { it.status != TaskStatus.DONE && it.dueDate?.toLocalDate() == today }
            .sortedBy { it.dueDate }.forEach { t ->
                add(TodayItem(Icons.Outlined.CheckBox, t.title, t.dueDate?.format(timeFmt)) { onOpenPage(Page.TASKS) })
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // --- nagłówek: logo + nazwa + awatar ---
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppLogo()
            Spacer(Modifier.width(8.dp))
            Text("Domownik", style = MaterialTheme.typography.titleLarge, color = cs.onBackground, modifier = Modifier.weight(1f))
            Avatar(initial = vm.user?.firstName?.take(1)?.uppercase() ?: "?", onClick = onOpenProfile)
        }

        Spacer(Modifier.height(28.dp))
        Text("Cześć!", style = MaterialTheme.typography.displaySmall, color = cs.onBackground)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Wszystko pod kontrolą", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
        }

        // --- 6 kafelków (3 x 2) ---
        Spacer(Modifier.height(24.dp))
        Page.homeTiles.chunked(3).forEach { rowPages ->
            Row(Modifier.fillMaxWidth()) {
                rowPages.forEach { page ->
                    HomeTile(page, onClick = { onOpenPage(page) }, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // --- Dzisiaj ---
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Dzisiaj", style = MaterialTheme.typography.titleMedium, color = cs.onBackground, modifier = Modifier.weight(1f))
            Row(Modifier.clickable(onClick = onOpenCalendar), verticalAlignment = Alignment.CenterVertically) {
                Text("Zobacz wszystko", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(16.dp), tint = cs.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(20.dp), color = cs.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
            if (todayItems.isEmpty()) {
                Text(
                    "Brak nadchodzących wydarzeń i zadań.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                )
            } else {
                Column(Modifier.padding(vertical = 8.dp)) {
                    todayItems.take(5).forEach { TodayRow(it) }
                }
            }
        }

        // --- motto ---
        Spacer(Modifier.height(24.dp))
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(cs.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Eco, null, Modifier.size(26.dp), tint = cs.onPrimaryContainer) }
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
private fun AppLogo() {
    val green = MaterialTheme.colorScheme.primary
    Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(32.dp).rotate(45f).border(2.dp, green, RoundedCornerShape(9.dp)))
        Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(20.dp), tint = green)
    }
}

@Composable
private fun Avatar(initial: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(40.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(cs.tertiaryContainer)
            .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(initial, style = MaterialTheme.typography.titleMedium, color = cs.onTertiaryContainer) }
}

@Composable
private fun HomeTile(page: Page, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(cs.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(page.icon, contentDescription = page.title, modifier = Modifier.size(30.dp), tint = cs.onPrimaryContainer) }
        Spacer(Modifier.height(8.dp))
        Text(
            page.title,
            style = MaterialTheme.typography.labelMedium,
            color = cs.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TodayRow(item: TodayItem) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable(onClick = item.onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(item.icon, null, Modifier.size(20.dp), tint = cs.onPrimaryContainer)
        Spacer(Modifier.width(12.dp))
        Text(item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        item.time?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant) }
    }
}