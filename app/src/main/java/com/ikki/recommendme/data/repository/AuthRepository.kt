package com.ikki.recommendme.data.repository

import com.ikki.recommendme.data.local.AppDataStore
import com.ikki.recommendme.domain.model.AuthResult
import com.ikki.recommendme.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest

/**
 * Authentication abstraction.
 *
 * Current implementation: [LocalAuthRepository] (local accounts in DataStore).
 * When the Firebase project is ready, add FirebaseAuthRepository implementing
 * this same interface - no UI/ViewModel changes needed.
 */
interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(name: String, email: String, password: String): AuthResult
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signOut()
}

class LocalAuthRepository(private val store: AppDataStore) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = store.currentProfile

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("recommendme::$password".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val e = email.trim()
        if (e.isEmpty() || password.isEmpty()) {
            return AuthResult.Error("Email and password are required.")
        }
        if (!emailRegex.matches(e)) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        val user = store.users.first().find { it.email.equals(e, ignoreCase = true) }
            ?: return AuthResult.Error("No account found for that email.")
        if (user.provider == "local" && user.passwordHash != hash(password)) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }
        store.setSession(user.email)
        return AuthResult.Success(user)
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        val n = name.trim()
        val e = email.trim()
        if (n.isEmpty()) return AuthResult.Error("Name is required.")
        if (e.isEmpty()) return AuthResult.Error("Email is required.")
        if (!emailRegex.matches(e)) return AuthResult.Error("Please enter a valid email address.")
        if (password.length < 6) return AuthResult.Error("Password must be at least 6 characters.")
        val exists = store.users.first().any { it.email.equals(e, ignoreCase = true) }
        if (exists) return AuthResult.Error("An account with this email already exists.")

        val user = UserProfile(
            email = e,
            name = n,
            passwordHash = hash(password),
            provider = "local",
            onboarded = false
        )
        store.upsertUser(user)
        store.setSession(user.email)
        return AuthResult.Success(user)
    }

    override suspend fun signInWithGoogle(): AuthResult {
        // Placeholder until Firebase Auth is connected:
        // a single shared "Google" demo account is created on first use.
        val email = "google.user@recommendme.app"
        val existing = store.users.first().find { it.email.equals(email, ignoreCase = true) }
        return if (existing != null) {
            store.setSession(existing.email)
            AuthResult.Success(existing)
        } else {
            val user = UserProfile(
                email = email,
                name = "Google User",
                passwordHash = null,
                provider = "google",
                onboarded = false
            )
            store.upsertUser(user)
            store.setSession(user.email)
            AuthResult.Success(user)
        }
    }

    override suspend fun signOut() {
        store.setSession(null)
    }
}
