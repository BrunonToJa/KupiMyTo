package pl.kupimyto.app.ui

import androidx.compose.runtime.Composable

@Composable
fun AppRoot(vm: AppViewModel) {
    when {
        vm.user == null -> AuthScreen(vm)
        vm.family == null -> FamilyScreen(vm)
        else -> MainScreen(vm)
    }
    vm.error?.let { ErrorDialog(it, vm::clearError) }
}
