package pl.kupimyto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.TaskPriority
import pl.kupimyto.app.data.model.TaskStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object UiFmt {
    val input: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val display: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("pl", "PL"))
}

fun Double.pretty(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

fun TaskStatus.label() = when (this) {
    TaskStatus.TODO -> "Do zrobienia"
    TaskStatus.IN_PROGRESS -> "W toku"
    TaskStatus.DONE -> "Zrobione"
}

fun TaskPriority.label() = when (this) {
    TaskPriority.LOW -> "Niski"
    TaskPriority.MEDIUM -> "Średni"
    TaskPriority.HIGH -> "Wysoki"
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier,
    )
}

@Composable
fun <T> DropdownField(
    label: String,
    selectedText: String,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selectedText", maxLines = 1)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (text, value) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { open = false; onSelect(value) })
            }
        }
    }
}

/** Pole daty i godziny. TODO: zamienić na DatePicker/TimePicker zgodnie z makietą. */
@Composable
fun DateTimeField(label: String, value: LocalDateTime?, onChange: (LocalDateTime?) -> Unit) {
    var text by remember { mutableStateOf(value?.format(UiFmt.input) ?: "") }
    fun parse(s: String): LocalDateTime? = runCatching { LocalDateTime.parse(s.trim(), UiFmt.input) }.getOrNull()
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onChange(parse(it)) },
        label = { Text(label) },
        placeholder = { Text("2026-11-30 18:00") },
        isError = text.isNotBlank() && parse(text) == null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    confirmText: String = "Zapisz",
    onDelete: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        },
        confirmButton = { TextButton(onClick = onConfirm, enabled = confirmEnabled) { Text(confirmText) } },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Usuń", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Błąd") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
    )
}
