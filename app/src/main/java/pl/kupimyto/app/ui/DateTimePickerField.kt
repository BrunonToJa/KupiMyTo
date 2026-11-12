package pl.kupimyto.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/** Pole terminu: klik -> wybór daty -> wybór godziny. X czyści termin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(label: String, value: LocalDateTime?, onChange: (LocalDateTime?) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, cs.outline, shape)
            .clickable { showDate = true }
            .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = cs.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            value?.format(UiFmt.display) ?: label,
            modifier = Modifier.weight(1f),
            color = if (value != null) cs.onSurface else cs.onSurfaceVariant,
        )
        if (value != null) {
            IconButton(onClick = { onChange(null) }) { Icon(Icons.Filled.Close, "Usuń termin") }
        } else {
            Spacer(Modifier.height(48.dp))
        }
    }

    if (showDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (value?.toLocalDate() ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        pickedDate = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        showDate = false
                        showTime = true
                    }
                }) { Text("Dalej") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Anuluj") } },
        ) { DatePicker(state = state) }
    }

    if (showTime) {
        val state = rememberTimePickerState(
            initialHour = value?.hour ?: 12,
            initialMinute = value?.minute ?: 0,
            is24Hour = true,
        )
        Dialog(
            onDismissRequest = { showTime = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
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
                            pickedDate?.let { d ->
                                onChange(LocalDateTime.of(d, LocalTime.of(state.hour, state.minute)))
                            }
                            showTime = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}