package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TaskFilter(val label: String) {
    ALL("Wszystkie"),
    TODO("Do zrobienia"),
    IN_PROGRESS("W toku"),
    DONE("Zrobione"),
    MINE("Moje"),
}

private val taskTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun dueLabel(due: LocalDateTime): String {
    val today = LocalDate.now()
    return when (due.toLocalDate()) {
        today -> "Dziś, ${due.format(taskTimeFmt)}"
        today.plusDays(1) -> "Jutro, ${due.format(taskTimeFmt)}"
        else -> due.format(UiFmt.display)
    }
}

@Composable
fun TasksScreen(vm: AppViewModel) {
    val cs = MaterialTheme.colorScheme
    var filter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }
    var editing by remember { mutableStateOf<Task?>(null) }
    var creating by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Task?>(null) }

    val myId = vm.user?.id
    fun matches(f: TaskFilter, t: Task): Boolean = when (f) {
        TaskFilter.ALL -> true
        TaskFilter.TODO -> t.status == TaskStatus.TODO
        TaskFilter.IN_PROGRESS -> t.status == TaskStatus.IN_PROGRESS
        TaskFilter.DONE -> t.status == TaskStatus.DONE
        TaskFilter.MINE -> myId != null && t.assignedTo == myId
    }

    val visible = vm.tasks
        .filter { matches(filter, it) }
        .sortedWith(
            compareBy<Task>(
                { it.status == TaskStatus.DONE },
                { it.dueDate ?: LocalDateTime.MAX },
                { -it.priority.ordinal },
            )
        )
    val doneCount = vm.tasks.count { it.status == TaskStatus.DONE }

    Box(Modifier.fillMaxSize()) {
        if (vm.tasks.isEmpty()) {
            EmptyState("Brak zadań.\nDodaj pierwsze przyciskiem +")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { ProgressCard(done = doneCount, total = vm.tasks.size) }
                item {
                    ChipRow(
                        options = TaskFilter.entries.map { f -> "${f.label} ${vm.tasks.count { matches(f, it) }}" to f },
                        selected = filter,
                        onSelect = { filter = it },
                    )
                }
                if (visible.isEmpty()) {
                    item {
                        Text(
                            "Nic tu nie ma w tej kategorii.",
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    items(visible, key = { it.id }) { t ->
                        TaskCard(
                            task = t,
                            assignee = t.assignedTo?.let { id ->
                                vm.members.firstOrNull { it.userId == id }?.user?.firstName ?: "?"
                            },
                            onToggle = { vm.toggleTaskDone(t) },
                            onClick = { editing = t },
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { creating = true },
            containerColor = cs.primary,
            contentColor = cs.onPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Default.Add, "Dodaj zadanie") }
    }

    if (creating || editing != null) {
        val initial = editing
        EntrySheet(
            initialTask = initial,
            defaultKind = EntryKind.TASK,
            defaultDate = LocalDate.now(),
            members = vm.members,
            onDismiss = { creating = false; editing = null },
            onSaveTask = { vm.saveTask(it); creating = false; editing = null },
            onSaveEvent = { vm.saveEvent(it); creating = false; editing = null },
            onDelete = if (initial != null) ({ vm.deleteTask(initial); creating = false; editing = null }) else null,
        )
    }

    toDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Usunąć zadanie?") },
            text = { Text("„${t.title}” zostanie usunięte.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteTask(t)
                    toDelete = null
                    editing = null
                    creating = false
                }) { Text("Usuń", color = cs.error) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Anuluj") } },
        )
    }
}

/** Rząd chipów do wyboru jednej opcji; przewijany poziomo. Używany też w formularzu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChipRow(options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (label, value) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = cs.primaryContainer,
                    selectedLabelColor = cs.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun ProgressCard(done: Int, total: Int) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLowest).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Zrobione $done z $total", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text("${done * 100 / total}%", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { done.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = cs.primary,
            trackColor = cs.primaryContainer,
        )
    }
}

@Composable
private fun TaskCard(task: Task, assignee: String?, onToggle: () -> Unit, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val done = task.status == TaskStatus.DONE
    val overdue = !done && task.dueDate?.isBefore(LocalDateTime.now()) == true

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DoneToggle(done = done, onClick = onToggle)
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (done) cs.onSurfaceVariant else cs.onSurface,
                textDecoration = if (done) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            task.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (task.status == TaskStatus.IN_PROGRESS) {
                    Pill("W toku", cs.secondaryContainer, cs.onSecondaryContainer)
                }
                task.dueDate?.let {
                    Pill(
                        dueLabel(it),
                        if (overdue) cs.errorContainer else cs.primaryContainer,
                        if (overdue) cs.onErrorContainer else cs.onPrimaryContainer,
                    )
                }
                when (task.priority) {
                    TaskPriority.HIGH -> Pill("Wysoki", cs.errorContainer, cs.onErrorContainer)
                    TaskPriority.LOW -> Pill("Niski", cs.surfaceVariant, cs.onSurfaceVariant)
                    TaskPriority.MEDIUM -> Unit
                }
            }
        }
        if (assignee != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(cs.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    assignee.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DoneToggle(done: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (done) cs.primary else Color.Transparent)
                .border(2.dp, if (done) cs.primary else cs.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Filled.Check, contentDescription = "Zrobione", modifier = Modifier.size(16.dp), tint = cs.onPrimary)
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}