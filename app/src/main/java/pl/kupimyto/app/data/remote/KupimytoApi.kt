package pl.kupimyto.app.data.remote

import pl.kupimyto.app.data.model.*
import retrofit2.http.*

/**
 * Kontrakt REST, który musi spełnić backend (PHP/Node/Spring... - dowolny) stojący przed bazą MariaDB.
 * Aplikacja NIGDY nie łączy się z MySQL bezpośrednio.
 * Ścieżki są względne wobec API_BASE_URL z app/build.gradle.kts.
 */
interface KupimytoApi {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): AuthResponse
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("users/{id}/families") suspend fun families(@Path("id") userId: Int): List<Family>
    @POST("families") suspend fun createFamily(@Body family: Family): Family
    @GET("families/{id}/members") suspend fun members(@Path("id") familyId: Int): List<FamilyMember>

    @GET("families/{id}/tasks") suspend fun tasks(@Path("id") familyId: Int): List<Task>
    @POST("families/{id}/tasks") suspend fun createTask(@Path("id") familyId: Int, @Body task: Task): Task
    @PUT("tasks/{id}") suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Task
    @DELETE("tasks/{id}") suspend fun deleteTask(@Path("id") id: Int)

    @GET("families/{id}/shopping-lists") suspend fun shoppingLists(@Path("id") familyId: Int): List<ShoppingList>
    @POST("families/{id}/shopping-lists") suspend fun createShoppingList(@Path("id") familyId: Int, @Body list: ShoppingList): ShoppingList
    @PUT("shopping-lists/{id}") suspend fun updateShoppingList(@Path("id") id: Int, @Body list: ShoppingList): ShoppingList
    @DELETE("shopping-lists/{id}") suspend fun deleteShoppingList(@Path("id") id: Int)

    @GET("shopping-lists/{id}/items") suspend fun items(@Path("id") listId: Int): List<ShoppingListItem>
    @POST("shopping-lists/{id}/items") suspend fun createItem(@Path("id") listId: Int, @Body item: ShoppingListItem): ShoppingListItem
    @PUT("shopping-list-items/{id}") suspend fun updateItem(@Path("id") id: Int, @Body item: ShoppingListItem): ShoppingListItem
    @DELETE("shopping-list-items/{id}") suspend fun deleteItem(@Path("id") id: Int)

    @GET("families/{id}/events") suspend fun events(@Path("id") familyId: Int): List<Event>
    @POST("families/{id}/events") suspend fun createEvent(@Path("id") familyId: Int, @Body event: Event): Event
    @PUT("events/{id}") suspend fun updateEvent(@Path("id") id: Int, @Body event: Event): Event
    @DELETE("events/{id}") suspend fun deleteEvent(@Path("id") id: Int)

    @GET("users/{id}/notifications") suspend fun notifications(@Path("id") userId: Int): List<Notification>
    @POST("notifications/{id}/read") suspend fun markRead(@Path("id") id: Int)
}
