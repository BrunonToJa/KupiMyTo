package pl.kupimyto.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import pl.kupimyto.app.data.model.*
import pl.kupimyto.app.data.repository.KupimytoRepository
import pl.kupimyto.app.data.SettingsStore
import pl.kupimyto.app.data.ThemeMode

/** Jeden ViewModel na całą aplikację (na start). Później można rozbić na osobne per ekran. */
class AppViewModel(
    private val repo: KupimytoRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    var user by mutableStateOf<User?>(null); private set
    var families by mutableStateOf<List<Family>>(emptyList()); private set
    var family by mutableStateOf<Family?>(null); private set
    var members by mutableStateOf<List<FamilyMember>>(emptyList()); private set
    var tasks by mutableStateOf<List<Task>>(emptyList()); private set
    var shoppingLists by mutableStateOf<List<ShoppingList>>(emptyList()); private set
    var openedList by mutableStateOf<ShoppingList?>(null); private set
    var items by mutableStateOf<List<ShoppingListItem>>(emptyList()); private set
    var events by mutableStateOf<List<Event>>(emptyList()); private set
    var notifications by mutableStateOf<List<Notification>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    var themeMode by mutableStateOf(settings.themeMode); private set
    var checkedItemIds by mutableStateOf<Set<Int>>(emptySet()); private set
    val lastEmail: String get() = settings.lastEmail

    fun changeTheme(mode: ThemeMode) {
        themeMode = mode
        settings.themeMode = mode
    }

    val unreadCount: Int get() = notifications.count { !it.isRead }

    fun clearError() { error = null }

    private fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch {
            loading = true
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: "Coś poszło nie tak"
            } finally {
                loading = false
            }
        }
    }

    // ---------- sesja ----------
    fun login(email: String, password: String) = launchSafe {
        val u = repo.login(email.trim(), password)
        settings.lastEmail = email.trim()
        startSession(u)
    }

    fun register(firstName: String, lastName: String, email: String, password: String) = launchSafe {
        val u = repo.register(firstName.trim(), lastName.trim(), email.trim(), password)
        settings.lastEmail = email.trim()
        startSession(u)
    }

    private suspend fun startSession(u: User) {
        user = u
        families = repo.getFamilies(u.id)
        if (families.size == 1) openFamily(families.first())
    }

    fun logout() {
        repo.logout()
        user = null
        checkedItemIds = emptySet()
        families = emptyList()
    }

    // ---------- rodzina ----------
    fun selectFamily(f: Family) = launchSafe { openFamily(f) }

    fun createFamily(name: String) = launchSafe {
        val u = user ?: return@launchSafe
        val f = repo.createFamily(name.trim(), u.id)
        families = repo.getFamilies(u.id)
        openFamily(f)
    }

    fun leaveFamily() {
        resetFamilyState()
    }

    fun refresh() = launchSafe { family?.let { loadFamilyData(it.id) } }

    private suspend fun openFamily(f: Family) {
        family = f
        loadFamilyData(f.id)
    }

    private suspend fun loadFamilyData(familyId: Int) {
        val u = user ?: return
        members = repo.getFamilyMembers(familyId)
        tasks = repo.getTasks(familyId)
        shoppingLists = repo.getShoppingLists(familyId)
        events = repo.getEvents(familyId)
        notifications = repo.getNotifications(u.id)
        openedList?.let { items = repo.getShoppingListItems(it.id) }
    }

    private fun resetFamilyState() {
        family = null; members = emptyList(); tasks = emptyList(); shoppingLists = emptyList()
        openedList = null; items = emptyList(); events = emptyList(); notifications = emptyList()
    }

    private val familyId: Int get() = family!!.id

    // ---------- zadania ----------
    fun saveTask(task: Task) = launchSafe {
        repo.saveTask(task.copy(familyId = familyId, createdBy = task.createdBy ?: user?.id))
        tasks = repo.getTasks(familyId)
    }

    fun toggleTaskDone(task: Task) = saveTask(
        task.copy(status = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE)
    )

    fun deleteTask(task: Task) = launchSafe {
        repo.deleteTask(task.id)
        tasks = repo.getTasks(familyId)
    }

    // ---------- zakupy ----------

    /** Otwiera główną listę zakupów rodziny (tworzy ją, jeśli jeszcze nie istnieje). */
    fun openShopping() = launchSafe {
        val fid = familyId
        var lists = repo.getShoppingLists(fid)
        if (lists.isEmpty()) {
            repo.saveShoppingList(ShoppingList(familyId = fid, name = "Lista zakupów", createdBy = user?.id))
            lists = repo.getShoppingLists(fid)
        }
        shoppingLists = lists
        val main = lists.first()
        openedList = main
        items = repo.getShoppingListItems(main.id)
    }

    fun saveItem(item: ShoppingListItem) = launchSafe {
        val list = openedList ?: return@launchSafe
        repo.saveShoppingListItem(item.copy(shoppingListId = list.id))
        items = repo.getShoppingListItems(list.id)
    }
    fun createShoppingList(name: String) = launchSafe {
        repo.saveShoppingList(ShoppingList(familyId = familyId, name = name.trim(), createdBy = user?.id))
        shoppingLists = repo.getShoppingLists(familyId)
    }

    fun deleteShoppingList(list: ShoppingList) = launchSafe {
        repo.deleteShoppingList(list.id)
        shoppingLists = repo.getShoppingLists(familyId)
    }

    fun openList(list: ShoppingList) = launchSafe {
        items = repo.getShoppingListItems(list.id)
        openedList = list
    }

    fun closeList() {
        openedList = null
        items = emptyList()
    }

    fun addItem(name: String, quantity: Double, unit: String) = launchSafe {
        val list = openedList ?: return@launchSafe
        repo.saveShoppingListItem(ShoppingListItem(shoppingListId = list.id, name = name, quantity = quantity, unit = unit))
        items = repo.getShoppingListItems(list.id)
    }

    fun toggleItem(item: ShoppingListItem) = launchSafe {
        repo.saveShoppingListItem(item.copy(isPurchased = !item.isPurchased))
        items = repo.getShoppingListItems(item.shoppingListId)
    }

    fun deleteItem(item: ShoppingListItem) = launchSafe {
        repo.deleteShoppingListItem(item.id)
        checkedItemIds = checkedItemIds - item.id
        items = repo.getShoppingListItems(item.shoppingListId)
    }

    /** Zaznaczenie produktu na liście (jeszcze nie "kupione"). */
    fun toggleChecked(item: ShoppingListItem) {
        checkedItemIds = if (item.id in checkedItemIds) checkedItemIds - item.id else checkedItemIds + item.id
    }

    /** "Zapisz zakupy": wszystkie zaznaczone produkty trafiają do kupionych. */
    fun saveShopping() = launchSafe {
        val list = openedList ?: return@launchSafe
        items.filter { it.id in checkedItemIds && !it.isPurchased }.forEach {
            repo.saveShoppingListItem(it.copy(isPurchased = true))
        }
        checkedItemIds = emptySet()
        items = repo.getShoppingListItems(list.id)
    }

    // ---------- wydarzenia ----------
    fun saveEvent(event: Event) = launchSafe {
        repo.saveEvent(event.copy(familyId = familyId, createdBy = event.createdBy ?: user?.id))
        events = repo.getEvents(familyId)
    }

    fun deleteEvent(event: Event) = launchSafe {
        repo.deleteEvent(event.id)
        events = repo.getEvents(familyId)
    }

    // ---------- powiadomienia ----------
    fun markRead(n: Notification) = launchSafe {
        val u = user ?: return@launchSafe
        repo.markNotificationRead(n.id)
        notifications = repo.getNotifications(u.id)
    }
}
