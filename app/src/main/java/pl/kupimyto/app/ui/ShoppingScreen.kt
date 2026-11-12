package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.data.model.ProductIcon
import pl.kupimyto.app.data.model.ShoppingListItem

@Composable
fun ShoppingScreen(vm: AppViewModel, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var tab by rememberSaveable { mutableStateOf(0) } // 0 = do kupienia, 1 = kupione
    var editing by remember { mutableStateOf<ShoppingListItem?>(null) }
    var creating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.openShopping() }

    val toBuy = vm.items.filter { !it.isPurchased }.sortedBy { it.id }
    val bought = vm.items.filter { it.isPurchased }.sortedBy { it.id }
    val checkedCount = toBuy.count { it.id in vm.checkedItemIds }
    val visible = if (tab == 0) toBuy else bought

    Column(Modifier.fillMaxSize()) {
        // --- nagłówek: wstecz, tytuł, okrągły przycisk "+" ---
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć") }
            Text("Lista zakupów", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(cs.primary).clickable { creating = true },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Add, "Dodaj produkt", tint = cs.onPrimary) }
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            SegmentedTabs(listOf("Do kupienia ${toBuy.size}", "Kupione ${bought.size}"), tab) { tab = it }
        }
        Spacer(Modifier.height(12.dp))

        // --- lista ---
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (visible.isEmpty()) {
                Text(
                    if (tab == 0) "Lista jest pusta.\nDodaj produkt przyciskiem +" else "Nic jeszcze nie kupiono.",
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(visible, key = { it.id }) { item ->
                        if (tab == 0) {
                            ProductCard(
                                item = item,
                                checked = item.id in vm.checkedItemIds,
                                onToggle = { vm.toggleChecked(item) },
                                onClick = { editing = item },
                            )
                        } else {
                            // w "Kupione" dotknięcie kółka zwraca produkt na listę do kupienia
                            ProductCard(
                                item = item,
                                checked = true,
                                onToggle = { vm.toggleItem(item) },
                                onClick = { editing = item },
                            )
                        }
                    }
                }
            }
        }

        // --- Zapisz zakupy ---
        if (tab == 0 && toBuy.isNotEmpty()) {
            Button(
                onClick = vm::saveShopping,
                enabled = checkedCount > 0 && !vm.loading,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
            ) {
                Text(
                    if (checkedCount > 0) "Zapisz zakupy ($checkedCount)" else "Zapisz zakupy",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    if (creating || editing != null) {
        val initial = editing
        ProductSheet(
            initial = initial,
            history = vm.items,
            onDismiss = { creating = false; editing = null },
            onSave = { vm.saveItem(it); creating = false; editing = null },
            onDelete = if (initial != null) ({ vm.deleteItem(initial); creating = false; editing = null }) else null,
        )
    }
}

@Composable
private fun SegmentedTabs(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surfaceVariant)
            .padding(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (sel) cs.primary else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (sel) cs.onPrimary else cs.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProductCard(item: ShoppingListItem, checked: Boolean, onToggle: () -> Unit, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BuyToggle(checked, onToggle)
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon((item.icon ?: ProductIcon.CART).vector(), null, Modifier.size(22.dp), tint = cs.onPrimaryContainer)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (checked) cs.onSurfaceVariant else cs.onSurface,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            item.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${item.quantity.pretty()} ${item.unit.orEmpty()}".trim(),
            style = MaterialTheme.typography.labelLarge,
            color = cs.onPrimaryContainer,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(cs.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun BuyToggle(checked: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (checked) cs.primary else Color.Transparent)
                .border(2.dp, if (checked) cs.primary else cs.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Filled.Check, "Zaznaczone", Modifier.size(16.dp), tint = cs.onPrimary)
        }
    }
}