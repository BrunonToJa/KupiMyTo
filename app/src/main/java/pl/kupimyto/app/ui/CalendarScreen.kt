package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val plLocale = Locale("pl", "PL")
private val monthFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", plLocale)
private val dayNameFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", plLocale)
private val fullDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", plLocale)
private val calTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val weekdays = listOf("Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nd")

/** Czy wpis zaczynający się w dniu [start] (z cyklem [rec]) wypada w dniu [day]. */
internal fun occursOn(start: LocalDate, rec: Recurrence?, day: LocalDate): Boolean {
    if (day == start) return true
    if (rec == null || day.isBefore(start)) return false
    return when (rec) {
        Recurrence.DAILY -> true
        Recurrence.WEEKLY -> start.dayOfWeek == day.dayOfWeek
        Recurrence.MONTHLY -> start.dayOfMonth == day.dayOfMonth
    }
}

private data class CalEntry(
    val time: LocalTime,
    val icon: EntryIcon,
    val title: String,
    val subtitle: String,
    val done: Boolean,
    val task: Task?,
    val event: Event?,
)

private fun entriesOn(day: LocalDate, tasks: List<Task>, events: List<Event>, members: List<FamilyMember>): List<CalEntry> {
    val out = mutableListOf<CalEntry>()
    tasks.forEach { t ->
        val due = t.dueDate ?: return@forEach
        if (occursOn(due.toLocalDate(), t.recurrence, day)) {
            val who = members.firstOrNull { it.userId == t.assignedTo }?.user?.firstName
            val sub = listOfNotNull(
                "Zadanie", due.format(calTimeFmt), who,
                if (t.points > 0) "${t.points} pkt" else null,
                t.recurrence?.label(),
            ).joinToString(" · ")
            out += CalEntry(due.toLocalTime(), t.icon ?: EntryIcon.TASK, t.title, sub, t.status == TaskStatus.DONE, t, null)
        }
    }
    events.forEach { e ->
        if (occursOn(e.startDate.toLocalDate(), e.recurrence, day)) {
            val sub = listOfNotNull(
                "Wydarzenie", e.startDate.format(calTimeFmt),
                e.location?.takeIf { it.isNotBlank() },
                e.recurrence?.label(),
            ).joinToString(" · ")
            out += CalEntry(e.startDate.toLocalTime(), e.icon ?: EntryIcon.CALENDAR, e.title, sub, false, null, e)
        }
    }
    return out.sortedBy { it.time }
}

private sealed interface Target {
    data object New : Target
    data class EditTask(val task: Task) : Target
    data class EditEvent(val event: Event) : Target
}

@Composable
fun CalendarScreen(vm: AppViewModel) {
    val cs = MaterialTheme.colorScheme
    val today = LocalDate.now()
    var selected by rememberSaveable { mutableStateOf(today) }
    var month by rememberSaveable { mutableStateOf(YearMonth.from(today)) }
    var target by remember { mutableStateOf<Target?>(null) }

    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1 // poniedziałek = 0
    val total = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    val cells = remember(month) { List(total) { first.plusDays((it - offset).toLong()) } }
    val iconsByDay = remember(vm.tasks, vm.events, month) {
        cells.associateWith { d -> entriesOn(d, vm.tasks, vm.events, emptyList()).take(3).map { it.icon } }
    }
    val dayEntries = entriesOn(selected, vm.tasks, vm.events, vm.members)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        // --- nawigacja po miesiącach ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Poprzedni miesiąc")
            }
            Text(
                month.format(monthFmt),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 170.dp),
            )
            IconButton(onClick = { month = month.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Następny miesiąc")
            }
        }

        // --- dni tygodnia ---
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
            weekdays.forEach {
                Text(
                    it, Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                )
            }
        }

        // --- siatka dni ---
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { d ->
                    DayCell(
                        day = d,
                        inMonth = YearMonth.from(d) == month,
                        selected = d == selected,
                        isToday = d == today,
                        icons = iconsByDay[d].orEmpty(),
                        onClick = {
                            selected = d
                            if (YearMonth.from(d) != month) month = YearMonth.from(d)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // --- wybrany dzień ---
        Spacer(Modifier.height(24.dp))
        val heading = (if (selected == today) "Dziś, " else
            selected.format(dayNameFmt).replaceFirstChar { it.uppercase() } + ", ") + selected.format(fullDateFmt)
        Text(heading, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Surface(shape = RoundedCornerShape(20.dp), color = cs.surfaceContainerLowest, modifier = Modifier.fillMaxWidth()) {
            if (dayEntries.isEmpty()) {
                Text(
                    "Brak wpisów w tym dniu.",
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                )
            } else {
                Column(Modifier.padding(vertical = 6.dp)) {
                    dayEntries.forEach { e ->
                        EntryRow(e) {
                            target = if (e.task != null) Target.EditTask(e.task) else Target.EditEvent(e.event!!)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF4A7A50), Color(0xFF2B4D33))))
                .clickable { target = Target.New },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Dodaj wydarzenie", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    target?.let { t ->
        EntrySheet(
            initialTask = (t as? Target.EditTask)?.task,
            initialEvent = (t as? Target.EditEvent)?.event,
            defaultKind = EntryKind.EVENT,
            defaultDate = selected,
            members = vm.members,
            onDismiss = { target = null },
            onSaveTask = { vm.saveTask(it); target = null },
            onSaveEvent = { vm.saveEvent(it); target = null },
            onDelete = when (t) {
                Target.New -> null
                is Target.EditTask -> ({ vm.deleteTask(t.task); target = null })
                is Target.EditEvent -> ({ vm.deleteEvent(t.event); target = null })
            },
        )
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    isToday: Boolean,
    icons: List<EntryIcon>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    val textColor = when {
        selected -> cs.onPrimary
        inMonth -> cs.onSurface
        else -> cs.onSurfaceVariant.copy(alpha = 0.45f)
    }
    Box(modifier.height(56.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .width(42.dp)
                .fillMaxHeight()
                .clip(shape)
                .background(if (selected) cs.primary else Color.Transparent)
                .then(if (isToday && !selected) Modifier.border(1.5.dp, cs.primary, shape) else Modifier)
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, color = textColor)
            if (icons.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    icons.forEach {
                        Icon(
                            it.vector(), null, Modifier.size(11.dp),
                            tint = if (selected) cs.onPrimary else if (inMonth) cs.primary else cs.primary.copy(alpha = 0.4f),
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(11.dp))
            }
        }
    }
}

@Composable
private fun EntryRow(e: CalEntry, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(e.icon.vector(), null, Modifier.size(22.dp), tint = cs.onPrimaryContainer) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                e.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (e.done) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                e.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}