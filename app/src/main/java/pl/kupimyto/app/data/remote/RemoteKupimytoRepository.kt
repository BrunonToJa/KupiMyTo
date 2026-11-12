package pl.kupimyto.app.data.remote

import pl.kupimyto.app.data.model.*
import pl.kupimyto.app.data.repository.KupimytoRepository

class RemoteKupimytoRepository(
    private val api: KupimytoApi,
    private val tokens: TokenHolder,
) : KupimytoRepository {

    override suspend fun login(email: String, password: String): User {
        val r = api.login(LoginRequest(email, password))
        tokens.token = r.token
        return r.user
    }

    override suspend fun register(firstName: String, lastName: String, email: String, password: String): User {
        val r = api.register(RegisterRequest(firstName, lastName, email, password))
        tokens.token = r.token
        return r.user
    }

    override fun logout() { tokens.token = null }

    override suspend fun getFamilies(userId: Int) = api.families(userId)
    override suspend fun createFamily(name: String, userId: Int) = api.createFamily(Family(name = name, createdBy = userId))
    override suspend fun getFamilyMembers(familyId: Int) = api.members(familyId)

    override suspend fun getTasks(familyId: Int) = api.tasks(familyId)
    override suspend fun saveTask(task: Task) =
        if (task.id == 0) api.createTask(task.familyId, task) else api.updateTask(task.id, task)
    override suspend fun deleteTask(taskId: Int) = api.deleteTask(taskId)

    override suspend fun getShoppingLists(familyId: Int) = api.shoppingLists(familyId)
    override suspend fun saveShoppingList(list: ShoppingList) =
        if (list.id == 0) api.createShoppingList(list.familyId, list) else api.updateShoppingList(list.id, list)
    override suspend fun deleteShoppingList(listId: Int) = api.deleteShoppingList(listId)

    override suspend fun getShoppingListItems(listId: Int) = api.items(listId)
    override suspend fun saveShoppingListItem(item: ShoppingListItem) =
        if (item.id == 0) api.createItem(item.shoppingListId, item) else api.updateItem(item.id, item)
    override suspend fun deleteShoppingListItem(itemId: Int) = api.deleteItem(itemId)

    override suspend fun getEvents(familyId: Int) = api.events(familyId)
    override suspend fun saveEvent(event: Event) =
        if (event.id == 0) api.createEvent(event.familyId, event) else api.updateEvent(event.id, event)
    override suspend fun deleteEvent(eventId: Int) = api.deleteEvent(eventId)

    override suspend fun getNotifications(userId: Int) = api.notifications(userId)
    override suspend fun markNotificationRead(notificationId: Int) = api.markRead(notificationId)
}
