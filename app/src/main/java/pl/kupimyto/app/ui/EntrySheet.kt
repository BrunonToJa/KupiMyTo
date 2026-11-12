package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.kupimyto.app.data.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class EntryKind { EVENT, TASK }

fun EntryIcon.vector(): ImageVector = when (this) {
    EntryIcon.CALENDAR -> Icons.Outlined.CalendarMonth
    EntryIcon.TASK -> Icons.Outlined.CheckBox
    EntryIcon.FAMILY -> Icons.Outlined.Groups
    EntryIcon.SHOPPING -> Icons.Outlined.ShoppingCart
}

fun Recurrence.label(): String = when (this) {
    Recurrence.DAILY -> "Codziennie"
    Recurrence.WEEKLY -> "Co tydzień"
    Recurrence.MONTHLY -> "Co miesiąc"
}

private fun EntryKind.defaultIcon() = if (this == EntryKind.TASK) EntryIcon.TASK else EntryIcon.CALENDAR

private val sheetDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val sheetTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Wspólny formularz "Dodaj do kalendarza" dla zadań i wydarzeń.
 * Nowy wpis: przełącznik Wydarzenie/Zadanie. Edycja: podaj initialTask albo initialEvent.
 * onDelete jest wołane po potwierdzeniu usunięcia (potwierdzenie robi sam formularz).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    initialTask: Task? = null,
    initialEvent: Event? = null,
    defaultKind: EntryKind = EntryKind.TASK,
    defaultDate: LocalDate = LocalDate.now(),
    members: List<FamilyMember>,
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit,
    onSaveEvent: (Event) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val fieldShape = RoundedCornerShape(14.dp)
    val editing = initialTask != null || initialEvent != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var kind by remember {
        mutableStateOf(
            when {
                initialTask != null -> EntryKind.TASK
                initialEvent != null -> EntryKind.EVENT
                else -> defaultKind
            }
        )
    }
    var title by remember { mutableStateOf(initialTask?.title ?: initialEvent?.title ?: "") }
    var desc by remember { mutableStateOf(initialTask?.description ?: initialEvent?.description ?: "") }
    val initialDateTime: LocalDateTime? = initialTask?.dueDate ?: initialEvent?.startDate
    var date by remember { mutableStateOf(initialDateTime?.toLocalDate() ?: defaultDate) }
    var time by remember {
        mutableStateOf(initialDateTime?.toLocalTime()?.withSecond(0)?.withNano(0) ?: LocalTime.of(12, 0))
    }
    var recurrence by remember { mutableStateOf(initialTask?.recurrence ?: initialEvent?.recurrence) }
    var icon by remember { mutableStateOf(initialTask?.icon ?: initialEvent?.icon) }
    var points by remember { mutableStateOf((initialTask?.points ?: 0).toString()) }
    var assigned by remember { mutableStateOf(initialTask?.assignedTo) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val shownIcon = icon ?: kind.defaultIcon()
    val people = buildList<Pair<String, Int?>> {
        add("Nikt" to null)
        members.forEach { add((it.user?.firstName ?: "#${it.userId}") to it.userId) }
    }

    fun save() {
        val dt = LocalDateTime.of(date, time)
        val d = desc.ifBlank { null }
        if (kind == EntryKind.TASK) {
            val base = initialTask ?: Task(familyId = 0, title = "")
            onSaveTask(
                base.copy(
                    title = title.trim(), description = d, assignedTo = assigned, dueDate = dt,
                    icon = shownIcon, points = points.toIntOrNull() ?: 0, recurrence = recurrence,
                )
            )
        } else {
            val base = initialEvent ?: Event(familyId = 0, title = "", startDate = dt)
            onSaveEvent(
                base.copy(
                    title = title.trim(), description = d, startDate = dt,
                    icon = shownIcon, recurrence = recurrence,
                )
            )
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = cs.surfaceContainerLowest,
        unfocusedContainerColor = cs.surfaceContainerLowest,
        focusedBorderColor = cs.primary,
        unfocusedBorderColor = cs.outlineVariant,
        focusedPlaceholderColor = cs.onSurfaceVariant.copy(alpha = 0.7f),
        unfocusedPlaceholderColor = cs.onSurfaceVariant.copy(alpha = 0.7f),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.background,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // --- nagłówek ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        initialTask != null -> "Edytuj zadanie"
                        initialEvent != null -> "Edytuj wydarzenie"
                        else -> "Dodaj do kalendarza"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(cs.primaryContainer).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Close, "Zamknij", Modifier.size(20.dp), tint = cs.onPrimaryContainer)
                }
            }

            // --- rodzaj wpisu (tylko przy dodawaniu) ---
            if (!editing) {
                LabeledField("Co chcesz dodać?") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KindButton("Wydarzenie", Icons.Outlined.CalendarMonth, kind == EntryKind.EVENT, { kind = EntryKind.EVENT }, Modifier.weight(1f))
                        KindButton("Zadanie", Icons.Outlined.CheckBox, kind == EntryKind.TASK, { kind = EntryKind.TASK }, Modifier.weight(1f))
                    }
                }
            }

            LabeledField("Nazwa") {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text(if (kind == EntryKind.TASK) "Np. posprzątać pokój" else "Np. urodziny babci") },
                    singleLine = true, shape = fieldShape, colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            LabeledField("Opis") {
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    placeholder = { Text("Dodaj szczegóły") },
                    minLines = 4, shape = fieldShape, colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Data", Modifier.weight(1f)) {
                    PickerBox(date.format(sheetDateFmt), Icons.Outlined.CalendarMonth) { showDate = true }
                }
                LabeledField("Godzina", Modifier.weight(1f)) {
                    PickerBox(time.format(sheetTimeFmt), Icons.Outlined.Schedule) { showTime = true }
                }
            }

            // --- powtarzalne ---
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceVariant).padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Powtarzalne", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Ustaw cykl wydarzenia lub zadania",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = recurrence != null,
                        onCheckedChange = { recurrence = if (it) Recurrence.WEEKLY else null },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = cs.primary,
                            uncheckedTrackColor = cs.outlineVariant,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
                val rec = recurrence
                if (rec != null) {
                    Spacer(Modifier.height(10.dp))
                    ChipRow(
                        options = Recurrence.entries.map { it.label() to it },
                        selected = rec,
                        onSelect = { recurrence = it },
                    )
                }
            }

            // --- tylko zadania ---
            if (kind == EntryKind.TASK) {
                LabeledField("Punkty za wykonanie") {
                    OutlinedTextField(
                        value = points, onValueChange = { points = it.filter(Char::isDigit).take(4) },
                        singleLine = true, shape = fieldShape, colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                LabeledField("Przypisane do") {
                    ChipRow(options = people, selected = assigned, onSelect = { assigned = it })
                    Text(
                        "Po wykonaniu zadania punkty zostaną doliczone do rankingu.",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                    )
                }
            }

            // --- ikona ---
            LabeledField("Wybierz ikonę") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EntryIcon.entries.forEach { ic ->
                        val sel = ic == shownIcon
                        Box(
                            Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(fieldShape)
                                .background(if (sel) cs.primary else cs.surfaceContainerLowest)
                                .border(1.dp, if (sel) cs.primary else cs.outlineVariant, fieldShape)
                                .clickable { icon = ic },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(ic.vector(), null, tint = if (sel) cs.onPrimary else cs.onSurface)
                        }
                    }
                }
            }

            // --- przyciski ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(0.8f).height(52.dp),
                ) { Text("Anuluj") }
                Button(
                    onClick = ::save,
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1.2f).height(52.dp),
                ) {
                    Text(
                        when {
                            editing -> "Zapisz zmiany"
                            kind == EntryKind.TASK -> "Dodaj zadanie"
                            else -> "Dodaj wydarzenie"
                        }
                    )
                }
            }
            if (editing && onDelete != null) {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (kind == EntryKind.TASK) "Usuń zadanie" else "Usuń wydarzenie", color = cs.error)
                }
            }
        }
    }

    // --- wybór daty ---
    if (showDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Anuluj") } },
        ) { DatePicker(state = state) }
    }

    // --- wybór godziny ---
    if (showTime) {
        val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
        Dialog(onDismissRequest = { showTime = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = cs.surfaceContainerHigh,
                modifier = Modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min),
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Wybierz godzinę",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    )
                    TimePicker(state = state)
                    Row(Modifier.fillMaxWidth().height(40.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTime = false }) { Text("Anuluj") }
                        TextButton(onClick = {
                            time = LocalTime.of(state.hour, state.minute)
                            showTime = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    // --- potwierdzenie usunięcia ---
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (kind == EntryKind.TASK) "Usunąć zadanie?" else "Usunąć wydarzenie?") },
            text = { Text("„${title}” zostanie usunięte.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete?.invoke() }) {
                    Text("Usuń", color = cs.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }
}

@Composable
private fun LabeledField(label: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        content()
    }
}

@Composable
private fun KindButton(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val fg = if (selected) cs.onPrimary else cs.onSurface
    Row(
        modifier
            .height(52.dp)
            .clip(shape)
            .background(if (selected) cs.primary else cs.surfaceContainerLowest)
            .border(1.dp, if (selected) cs.primary else cs.outlineVariant, shape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = fg)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = fg)
    }
}

@Composable
private fun PickerBox(text: String, icon: ImageVector, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(cs.surfaceContainerLowest)
            .border(1.dp, cs.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(icon, null, Modifier.size(18.dp), tint = cs.onSurface)
    }
}