@file:UseSerializers(DbDateTimeSerializer::class)

package pl.kupimyto.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

// Każda klasa odpowiada jednej tabeli z database_kupimyto.sql.
// @SerialName = dokładna nazwa kolumny, więc JSON z backendu może być 1:1 z wierszem z bazy.
// UWAGA: password_hash celowo NIE występuje w modelu - nigdy nie powinien trafić do aplikacji.

@Serializable
enum class FamilyRole {
    @SerialName("owner") OWNER,
    @SerialName("adult") ADULT,
    @SerialName("child") CHILD,
    @SerialName("member") MEMBER,
}

@Serializable
enum class TaskStatus {
    @SerialName("todo") TODO,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("done") DONE,
}

@Serializable
enum class TaskPriority {
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH,
}

@Serializable
enum class Recurrence {
    @SerialName("daily") DAILY,
    @SerialName("weekly") WEEKLY,
    @SerialName("monthly") MONTHLY,
}

@Serializable
enum class EntryIcon {
    @SerialName("calendar") CALENDAR,
    @SerialName("task") TASK,
    @SerialName("family") FAMILY,
    @SerialName("shopping") SHOPPING,
}

/** tabela `users` */
@Serializable
data class User(
    val id: Int = 0,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    @SerialName("created_at") val createdAt: LocalDateTime? = null,
) {
    val fullName: String get() = "$firstName $lastName"
}

/** tabela `families` */
@Serializable
data class Family(
    val id: Int = 0,
    val name: String,
    @SerialName("created_by") val createdBy: Int,
    @SerialName("created_at") val createdAt: LocalDateTime? = null,
)

/** tabela `familymembers` (+ opcjonalnie dołączony użytkownik, żeby wyświetlić imię) */
@Serializable
data class FamilyMember(
    val id: Int = 0,
    @SerialName("family_id") val familyId: Int,
    @SerialName("user_id") val userId: Int,
    val role: FamilyRole = FamilyRole.MEMBER,
    @SerialName("joined_at") val joinedAt: LocalDateTime? = null,
    val user: User? = null,
)

/** tabela `events` */
@Serializable
data class Event(
    val id: Int = 0,
    @SerialName("family_id") val familyId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("start_date") val startDate: LocalDateTime,
    @SerialName("end_date") val endDate: LocalDateTime? = null,
    val location: String? = null,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_at") val createdAt: LocalDateTime? = null,
    val icon: EntryIcon? = null,
    val recurrence: Recurrence? = null,
)

/** tabela `tasks` */
@Serializable
data class Task(
    val id: Int = 0,
    @SerialName("family_id") val familyId: Int,
    val title: String,
    val description: String? = null,
    @SerialName("assigned_to") val assignedTo: Int? = null,
    @SerialName("due_date") val dueDate: LocalDateTime? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_at") val createdAt: LocalDateTime? = null,
    val icon: EntryIcon? = null,
    val points: Int = 0,
    val recurrence: Recurrence? = null,
)

/** tabela `shoppinglists` */
@Serializable
data class ShoppingList(
    val id: Int = 0,
    @SerialName("family_id") val familyId: Int,
    val name: String,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_at") val createdAt: LocalDateTime? = null,
)

/** tabela `shoppinglistitems` (is_purchased: backend powinien zwracać boolean true/false) */
@Serializable
data class ShoppingListItem(
    val id: Int = 0,
    @SerialName("shopping_list_id") val shoppingListId: Int,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String? = "szt.",
    @SerialName("is_purchased") val isPurchased: Boolean = false,
    val description: String? = null,
    val icon: ProductIcon? = null,
)

/** tabela `notifications` */
@Serializable
data class Notification(
    val id: Int = 0,
    @SerialName("user_id") val userId: Int,
    val title: String,
    val message: String? = null,
    @SerialName("scheduled_at") val scheduledAt: LocalDateTime? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: LocalDateTime? = null,
)

@Serializable
enum class ProductIcon {
    @SerialName("cart") CART,
    @SerialName("food") FOOD,
    @SerialName("home") HOME,
    @SerialName("box") BOX,
}