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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.ProductIcon
import pl.kupimyto.app.data.model.ShoppingListItem

fun ProductIcon.vector(): ImageVector = when (this) {
    ProductIcon.CART -> Icons.Outlined.ShoppingCart
    ProductIcon.FOOD -> Icons.Outlined.Restaurant // TODO: podmienić na jabłko z Figmy
    ProductIcon.HOME -> Icons.Outlined.Home
    ProductIcon.BOX -> Icons.Outlined.Inventory2
}

private val BASE_UNITS = listOf("szt.", "kg", "g", "l", "ml", "op.")
private const val NEW_PRODUCT = "Nowy produkt"

/**
 * Formularz dodawania/edycji produktu.
 * [history] - produkty z listy; wybór jednego z nich uzupełnia formularz zapisanymi danymi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSheet(
    initial: ShoppingListItem?,
    history: List<ShoppingListItem>,
    onDismiss: () -> Unit,
    onSave: (ShoppingListItem) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val fieldShape = RoundedCornerShape(14.dp)
    val editing = initial != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var picked by remember { mutableStateOf(NEW_PRODUCT) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var qty by remember { mutableStateOf((initial?.quantity ?: 1.0).pretty()) }
    var unit by remember { mutableStateOf(initial?.unit ?: "szt.") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: ProductIcon.CART) }
    var confirmDelete by remember { mutableStateOf(false) }

    val unitOptions = if (unit in BASE_UNITS) BASE_UNITS else BASE_UNITS + unit
    val previous = remember(history) {
        history.distinctBy { it.name.trim().lowercase() }.sortedBy { it.name.lowercase() }
    }
    val colors = formFieldColors()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // --- nagłówek ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (editing) "Edytuj produkt" else "Dodaj produkt",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(cs.primaryContainer).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Close, "Zamknij", Modifier.size(20.dp), tint = cs.onPrimaryContainer) }
            }

            // --- wybór z wcześniejszych produktów (tylko przy dodawaniu) ---
            if (!editing && previous.isNotEmpty()) {
                FormLabel("Wybierz z wcześniejszych produktów", hint = "Wybór automatycznie uzupełni zapisane dane.") {
                    SelectField(
                        value = picked,
                        options = listOf(NEW_PRODUCT) + previous.map { it.name },
                        onSelect = { choice ->
                            picked = choice
                            if (choice == NEW_PRODUCT) {
                                name = ""; qty = "1"; unit = "szt."; desc = ""; icon = ProductIcon.CART
                            } else {
                                previous.firstOrNull { it.name == choice }?.let { p ->
                                    name = p.name
                                    qty = p.quantity.pretty()
                                    unit = p.unit ?: "szt."
                                    desc = p.description ?: ""
                                    icon = p.icon ?: ProductIcon.CART
                                }
                            }
                        },
                    )
                }
            }

            FormLabel("Nazwa produktu") {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    placeholder = { Text("Np. mleko") },
                    singleLine = true, shape = fieldShape, colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormLabel("Ilość", Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(7) },
                        singleLine = true, shape = fieldShape, colors = colors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                FormLabel("Jednostka", Modifier.weight(1f)) {
                    SelectField(value = unit, options = unitOptions, onSelect = { unit = it })
                }
            }

            FormLabel("Dodatkowy opis", hint = "Opis pojawi się pod nazwą produktu na liście.") {
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    placeholder = { Text("Marka, wariant, smak lub inne szczegóły") },
                    minLines = 4, shape = fieldShape, colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FormLabel("Wybierz ikonę produktu") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProductIcon.entries.forEach { ic ->
                        val sel = ic == icon
                        Box(
                            Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(fieldShape)
                                .background(if (sel) cs.primary else cs.surfaceContainerLowest)
                                .border(1.dp, if (sel) cs.primary else cs.outlineVariant, fieldShape)
                                .clickable { icon = ic },
                            contentAlignment = Alignment.Center,
                        ) { Icon(ic.vector(), null, tint = if (sel) cs.onPrimary else cs.onSurface) }
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
                    onClick = {
                        val base = initial ?: ShoppingListItem(shoppingListId = 0, name = "")
                        onSave(
                            base.copy(
                                name = name.trim(),
                                quantity = qty.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0,
                                unit = unit,
                                description = desc.ifBlank { null },
                                icon = icon,
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1.2f).height(52.dp),
                ) { Text(if (editing) "Zapisz zmiany" else "Dodaj produkt") }
            }
            if (editing && onDelete != null) {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Usuń produkt", color = cs.error)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Usunąć produkt?") },
            text = { Text("„${name}” zostanie usunięty z listy.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete?.invoke() }) {
                    Text("Usuń", color = cs.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Anuluj") } },
        )
    }
}