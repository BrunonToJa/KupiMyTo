package pl.kupimyto.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.kupimyto.app.data.model.User

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    val password: String, // hashowanie (password_hash) robi backend, nigdy aplikacja
)

@Serializable
data class AuthResponse(val token: String, val user: User)

class TokenHolder {
    @Volatile var token: String? = null
}
