package pl.kupimyto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.kupimyto.app.BuildConfig

// Konto bazowe do szybkiego logowania (tryb demo, USE_FAKE_DATA = true)
private const val DEMO_EMAIL = "1"
private const val DEMO_PASSWORD = "1"

@Composable
fun AuthScreen(vm: AppViewModel) {
    val demo = BuildConfig.USE_FAKE_DATA
    var register by remember { mutableStateOf(false) }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(vm.lastEmail.ifBlank { if (demo) DEMO_EMAIL else "" }) }
    var password by remember { mutableStateOf(if (demo) DEMO_PASSWORD else "") }

    val valid = email.isNotBlank() && password.isNotBlank() &&
            (!register || (first.isNotBlank() && last.isNotBlank()))

    Column(
        Modifier.fillMaxSize().systemBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Domownik", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        if (register) {
            AppTextField(first, { first = it }, "Imię")
            AppTextField(last, { last = it }, "Nazwisko")
        }
        AppTextField(email, { email = it }, "E-mail", keyboardType = KeyboardType.Email)
        AppTextField(password, { password = it }, "Hasło", keyboardType = KeyboardType.Password, password = true)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { if (register) vm.register(first, last, email, password) else vm.login(email, password) },
            enabled = valid && !vm.loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(if (register) "Załóż konto" else "Zaloguj się") }

        if (demo && !register) {
            OutlinedButton(
                onClick = { vm.login(DEMO_EMAIL, DEMO_PASSWORD) },
                enabled = !vm.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Szybkie logowanie: Anna (demo)") }
        }

        TextButton(onClick = { register = !register }) {
            Text(if (register) "Mam już konto" else "Nie mam konta - zarejestruj się")
        }
        if (vm.loading) CircularProgressIndicator()
    }
}