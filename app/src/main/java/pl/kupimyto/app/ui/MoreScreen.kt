package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.ThemeMode

@Composable
fun MoreScreen(vm: AppViewModel, onOpenNotifications: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLowest).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(cs.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    vm.user?.firstName?.take(1)?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = cs.onTertiaryContainer,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(vm.user?.fullName.orEmpty(), style = MaterialTheme.typography.titleMedium)
                Text(vm.user?.email.orEmpty(), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))

        // --- motyw ---
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLowest).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = cs.onPrimaryContainer)
                Spacer(Modifier.width(16.dp))
                Text("Motyw", style = MaterialTheme.typography.bodyLarge)
            }
            ChipRow(
                options = ThemeMode.entries.map { it.label to it },
                selected = vm.themeMode,
                onSelect = vm::changeTheme,            )
        }

        MoreRow(Icons.Outlined.Notifications, "Powiadomienia", badge = vm.unreadCount, onClick = onOpenNotifications)
        MoreRow(Icons.Outlined.Refresh, "Odśwież dane", onClick = vm::refresh)
        MoreRow(Icons.Outlined.SwapHoriz, "Zmień rodzinę", onClick = vm::leaveFamily)
        MoreRow(Icons.AutoMirrored.Outlined.Logout, "Wyloguj", onClick = vm::logout)
    }
}

@Composable
private fun MoreRow(icon: ImageVector, label: String, badge: Int = 0, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = cs.onPrimaryContainer)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (badge > 0) Badge { Text("$badge") }
    }
}