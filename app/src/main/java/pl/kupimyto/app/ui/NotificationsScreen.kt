package pl.kupimyto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(vm: AppViewModel) {
    if (vm.notifications.isEmpty()) {
        EmptyState("Brak powiadomień")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(vm.notifications.sortedByDescending { it.scheduledAt ?: it.createdAt }, key = { it.id }) { n ->
            Card(
                onClick = { if (!n.isRead) vm.markRead(n) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (n.isRead) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.secondaryContainer
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        n.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (n.isRead) FontWeight.Normal else FontWeight.Bold,
                    )
                    n.message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    (n.scheduledAt ?: n.createdAt)?.let {
                        Text(it.format(UiFmt.display), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
