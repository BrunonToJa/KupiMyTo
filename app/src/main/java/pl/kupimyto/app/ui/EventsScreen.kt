package pl.kupimyto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.Event
import java.time.LocalDateTime

@Composable
fun EventsScreen(vm: AppViewModel) {
    var editing by remember { mutableStateOf<Event?>(null) }
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (vm.events.isEmpty()) {
            EmptyState("Brak wydarzeń. Dodaj pierwsze przyciskiem +")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(vm.events.sortedBy { it.startDate }, key = { it.id }) { e ->
                    Card(onClick = { editing = e }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(e.title, style = MaterialTheme.typography.titleMedium)
                            val when_ = buildString {
                                append(e.startDate.format(UiFmt.display))
                                e.endDate?.let { append(" – ").append(it.format(UiFmt.display)) }
                            }
                            Text(when_, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            e.location?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { creating = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Default.Add, "Dodaj wydarzenie") }
    }

    if (creating || editing != null) {
        val initial = editing
        EventDialog(
            initial = initial,
            onDismiss = { creating = false; editing = null },
            onSave = { vm.saveEvent(it); creating = false; editing = null },
            onDelete = initial?.let { e -> { vm.deleteEvent(e); editing = null } },
        )
    }
}

@Composable
private fun EventDialog(
    initial: Event?,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var start by remember { mutableStateOf(initial?.startDate) }
    var end by remember { mutableStateOf(initial?.endDate) }

    val s: LocalDateTime? = start
    val e: LocalDateTime? = end
    val valid = title.isNotBlank() && s != null && (e == null || !e.isBefore(s))

    FormDialog(
        title = if (initial == null) "Nowe wydarzenie" else "Edytuj wydarzenie",
        onDismiss = onDismiss,
        confirmEnabled = valid,
        onConfirm = {
            val base = initial ?: Event(familyId = 0, title = "", startDate = s ?: LocalDateTime.now())
            onSave(
                base.copy(
                    title = title.trim(),
                    description = desc.ifBlank { null },
                    location = location.ifBlank { null },
                    startDate = s ?: return@FormDialog,
                    endDate = e,
                )
            )
        },
        onDelete = onDelete,
    ) {
        AppTextField(title, { title = it }, "Tytuł")
        AppTextField(desc, { desc = it }, "Opis", singleLine = false)
        AppTextField(location, { location = it }, "Miejsce")
        DateTimePickerField("Ustaw początek", start) { start = it }
        DateTimePickerField("Ustaw koniec (opcjonalnie)", end) { end = it }
    }
}
