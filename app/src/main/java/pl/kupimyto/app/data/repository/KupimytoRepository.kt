package pl.kupimyto.app.data.repository

import pl.kupimyto.app.data.model.*

/**
 * Jedyny punkt styku aplikacji z danymi.
 * Implementacje: FakeKupimytoRepository (pamięć) i RemoteKupimytoRepository (REST -> baza).
 * Konwencja save*: id == 0 oznacza INSERT, id > 0 oznacza UPDATE.
 */
interface KupimytoRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(firstName: String, lastName: String, email: String, password: String): User
    fun logout()

    suspend fun getFamilies(userId: Int): List<Family>
    suspend fun createFamily(name: String, userId: Int): Family
    suspend fun getFamilyMembers(familyId: Int): List<FamilyMember>

    suspend fun getTasks(familyId: Int): List<Task>
    suspend fun saveTask(task: Task): Task
    suspend fun deleteTask(taskId: Int)

    suspend fun getShoppingLists(familyId: Int): List<ShoppingList>
    suspend fun saveShoppingList(list: ShoppingList): ShoppingList
    suspend fun deleteShoppingList(listId: Int)

    suspend fun getShoppingListItems(listId: Int): List<ShoppingListItem>
    suspend fun saveShoppingListItem(item: ShoppingListItem): ShoppingListItem
    suspend fun deleteShoppingListItem(itemId: Int)

    suspend fun getEvents(familyId: Int): List<Event>
    suspend fun saveEvent(event: Event): Event
    suspend fun deleteEvent(eventId: Int)

    suspend fun getNotifications(userId: Int): List<Notification>
    suspend fun markNotificationRead(notificationId: Int)
}
