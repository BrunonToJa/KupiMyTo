package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.FamilyRole

private fun FamilyRole.label() = when (this) {
    FamilyRole.OWNER -> "Właściciel"
    FamilyRole.ADULT -> "Dorosły"
    FamilyRole.CHILD -> "Dziecko"
    FamilyRole.MEMBER -> "Członek rodziny"
}

@Composable
fun FamilyMembersScreen(vm: AppViewModel) {
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                vm.family?.name.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(vm.members, key = { it.id }) { m ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLowest).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(cs.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        m.user?.firstName?.take(1)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(m.user?.fullName ?: "Użytkownik #${m.userId}", style = MaterialTheme.typography.titleMedium)
                    Text(m.role.label(), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}