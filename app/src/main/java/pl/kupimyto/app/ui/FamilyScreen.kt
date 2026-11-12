package pl.kupimyto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FamilyScreen(vm: AppViewModel) {
    var newName by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Cześć, ${vm.user?.firstName}!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = vm::logout) { Text("Wyloguj") }
        }
        Text("Wybierz rodzinę", style = MaterialTheme.typography.titleMedium)

        if (vm.families.isEmpty()) {
            Text("Nie należysz jeszcze do żadnej rodziny. Utwórz nową poniżej.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.families, key = { it.id }) { f ->
                Card(onClick = { vm.selectFamily(f) }, modifier = Modifier.fillMaxWidth()) {
                    Text(f.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }
            }
        }
        if (vm.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        HorizontalDivider()
        Text("Nowa rodzina", style = MaterialTheme.typography.titleMedium)
        AppTextField(newName, { newName = it }, "Nazwa rodziny")
        Button(
            onClick = { vm.createFamily(newName); newName = "" },
            enabled = newName.isNotBlank() && !vm.loading,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Utwórz rodzinę") }
    }
}
