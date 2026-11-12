package pl.kupimyto.app.data.repository

import kotlinx.coroutines.delay
import pl.kupimyto.app.data.model.*
import java.time.LocalDateTime

/** Dane w pamięci - do rozwoju UI bez bazy. Zniknie po restarcie aplikacji. */
class FakeKupimytoRepository : KupimytoRepository {

    private var seq = 100
    private fun nextId() = seq++
    private fun now(): LocalDateTime = LocalDateTime.now().withSecond(0).withNano(0)
    private suspend fun <T> io(block: () -> T): T {
        delay(120) // symulacja sieci
        return block()
    }
    private fun <T> MutableList<T>.upsert(item: T, id: (T) -> Int) {
        val i = indexOfFirst { id(it) == id(item) }
        if (i >= 0) this[i] = item else add(item)
    }

    private val users = mutableListOf(
        User(1, "Anna", "Kowalska", "1"),
        User(2, "Piotr", "Kowalski", "piotr@example.com"),
        User(3, "Zuzia", "Kowalska", "zuzia@example.com"),
    )
    private val families = mutableListOf(Family(1, "Rodzina Kowalskich", createdBy = 1))
    private val members = mutableListOf(
        FamilyMember(1, familyId = 1, userId = 1, role = FamilyRole.OWNER),
        FamilyMember(2, familyId = 1, userId = 2, role = FamilyRole.ADULT),
        FamilyMember(3, familyId = 1, userId = 3, role = FamilyRole.CHILD),
    )
    private val tasks = mutableListOf(
        // dziś
        Task(1, familyId = 1, title = "Odrobić lekcje", description = "Matematyka i polski", assignedTo = 3,
            dueDate = now().withHour(16).withMinute(0), priority = TaskPriority.HIGH, createdBy = 1,
            icon = EntryIcon.TASK, points = 10),
        // za 2 dni, powtarza się co tydzień
        Task(2, familyId = 1, title = "Zakupy na weekend", assignedTo = 1,
            dueDate = now().plusDays(2).withHour(18).withMinute(0), createdBy = 1,
            icon = EntryIcon.SHOPPING, points = 5, recurrence = Recurrence.WEEKLY),
    )
    private val events = mutableListOf(
        // za 3 dni
        Event(1, familyId = 1, title = "Urodziny babci", startDate = now().plusDays(3).withHour(16).withMinute(0),
            location = "Poznań, ul. Lipowa 5", createdBy = 1, icon = EntryIcon.FAMILY),
    )
    private val lists = mutableListOf(
        ShoppingList(1, familyId = 1, name = "Zakupy na weekend", createdBy = 1),
        ShoppingList(2, familyId = 1, name = "Apteczka", createdBy = 2),
    )
    private val items = mutableListOf(
        ShoppingListItem(1, shoppingListId = 1, name = "Mleko", quantity = 2.0, unit = "l", description = "Łaciate 3,2%", icon = ProductIcon.FOOD),
        ShoppingListItem(2, shoppingListId = 1, name = "Chleb", quantity = 1.0, unit = "szt.", isPurchased = true, icon = ProductIcon.FOOD),
        ShoppingListItem(3, shoppingListId = 1, name = "Jabłka", quantity = 1.5, unit = "kg", description = "Szara reneta", icon = ProductIcon.FOOD),
        ShoppingListItem(4, shoppingListId = 1, name = "Płyn do naczyń", quantity = 1.0, unit = "szt.", icon = ProductIcon.HOME),
        ShoppingListItem(5, shoppingListId = 2, name = "Plastry", quantity = 1.0, unit = "op."),
    )
    private val notifications = mutableListOf(
        Notification(1, userId = 1, title = "Nowe zadanie", message = "Piotr dodał zadanie: Wynieść śmieci", createdAt = now().minusHours(2)),
        Notification(2, userId = 1, title = "Przypomnienie", message = "Jutro urodziny babci!", scheduledAt = now().plusDays(2), createdAt = now().minusDays(1), isRead = true),
    )

    // ---- auth ----
    override suspend fun login(email: String, password: String): User = io {
        users.firstOrNull { it.email.equals(email, ignoreCase = true) }
            ?: throw IllegalArgumentException("Nieprawidłowy e-mail lub hasło")
    }

    override suspend fun register(firstName: String, lastName: String, email: String, password: String): User = io {
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            throw IllegalArgumentException("Konto z tym adresem e-mail już istnieje")
        }
        User(nextId(), firstName, lastName, email, now()).also { users.add(it) }
    }

    override fun logout() = Unit

    // ---- rodziny ----
    override suspend fun getFamilies(userId: Int): List<Family> = io {
        val ids = members.filter { it.userId == userId }.map { it.familyId }
        families.filter { it.id in ids }
    }

    override suspend fun createFamily(name: String, userId: Int): Family = io {
        val f = Family(nextId(), name, userId, now())
        families.add(f)
        members.add(FamilyMember(nextId(), f.id, userId, FamilyRole.OWNER, now()))
        f
    }

    override suspend fun getFamilyMembers(familyId: Int): List<FamilyMember> = io {
        members.filter { it.familyId == familyId }.map { m -> m.copy(user = users.firstOrNull { it.id == m.userId }) }
    }

    // ---- zadania ----
    override suspend fun getTasks(familyId: Int): List<Task> = io { tasks.filter { it.familyId == familyId } }
    override suspend fun saveTask(task: Task): Task = io {
        val t = if (task.id == 0) task.copy(id = nextId(), createdAt = now()) else task
        tasks.upsert(t) { it.id }
        t
    }
    override suspend fun deleteTask(taskId: Int) = io { tasks.removeAll { it.id == taskId }; Unit }

    // ---- zakupy ----
    override suspend fun getShoppingLists(familyId: Int): List<ShoppingList> = io { lists.filter { it.familyId == familyId } }
    override suspend fun saveShoppingList(list: ShoppingList): ShoppingList = io {
        val l = if (list.id == 0) list.copy(id = nextId(), createdAt = now()) else list
        lists.upsert(l) { it.id }
        l
    }
    override suspend fun deleteShoppingList(listId: Int) = io {
        lists.removeAll { it.id == listId }
        items.removeAll { it.shoppingListId == listId } // ON DELETE CASCADE
        Unit
    }
    override suspend fun getShoppingListItems(listId: Int): List<ShoppingListItem> = io { items.filter { it.shoppingListId == listId } }
    override suspend fun saveShoppingListItem(item: ShoppingListItem): ShoppingListItem = io {
        val i = if (item.id == 0) item.copy(id = nextId()) else item
        items.upsert(i) { it.id }
        i
    }
    override suspend fun deleteShoppingListItem(itemId: Int) = io { items.removeAll { it.id == itemId }; Unit }

    // ---- wydarzenia ----
    override suspend fun getEvents(familyId: Int): List<Event> = io { events.filter { it.familyId == familyId } }
    override suspend fun saveEvent(event: Event): Event = io {
        val e = if (event.id == 0) event.copy(id = nextId(), createdAt = now()) else event
        events.upsert(e) { it.id }
        e
    }
    override suspend fun deleteEvent(eventId: Int) = io { events.removeAll { it.id == eventId }; Unit }

    // ---- powiadomienia ----
    override suspend fun getNotifications(userId: Int): List<Notification> = io { notifications.filter { it.userId == userId } }
    override suspend fun markNotificationRead(notificationId: Int) = io {
        val i = notifications.indexOfFirst { it.id == notificationId }
        if (i >= 0) notifications[i] = notifications[i].copy(isRead = true)
        Unit
    }
}
