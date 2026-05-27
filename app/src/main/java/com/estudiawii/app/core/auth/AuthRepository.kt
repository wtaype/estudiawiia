@file:Suppress("DEPRECATION")

package com.estudiawii.app.core.auth

import android.content.Context
import android.content.Intent
import com.estudiawii.app.R
import com.estudiawii.app.core.model.Smile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.text.Normalizer

/**
 * Repositorio de autenticación — instancias de Firebase son lazy (0ms al construir).
 * Firebase se inicializa la primera vez que se accede, siempre desde Dispatchers.IO.
 */
class AuthRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser get() = auth.currentUser
    val currentEmail: String? get() = auth.currentUser?.email
    val isLoggedIn: Boolean get() = auth.currentUser != null

    /**
     * Devuelve el perfil Firestore del usuario Firebase Auth actualmente logueado.
     * Retorna null si no hay sesion activa o si el perfil NO existe en Firestore
     * (caso: usuario Google que abrio sesion pero nunca completo su registro).
     */
    suspend fun getSessionProfile(): Smile? {
        val user = auth.currentUser ?: return null
        return getProfileByUid(user.uid)
    }

    suspend fun login(emailOrUser: String, password: String): Smile {
        val clean = emailOrUser.trim().lowercase()
        val email = if ("@" in clean) clean else getEmailByUser(clean) ?: error("Usuario no encontrado")
        val user = auth.signInWithEmailAndPassword(email, password).await().user ?: error("No se pudo iniciar sesion")
        return getProfileByUid(user.uid) ?: getProfileByEmail(user.email.orEmpty()) ?: error("Perfil no encontrado")
    }

    suspend fun register(usuario: String, nombre: String, apellidos: String, email: String, password: String): Smile {
        val cleanUser = usuario.usuarioKey()
        val cleanEmail = email.trim().lowercase()
        require(cleanUser.length >= 3) { "El usuario necesita al menos 3 caracteres" }
        require(cleanEmail.contains("@")) { "Ingresa un email valido" }
        require(password.length >= 6) { "La contrasena necesita al menos 6 caracteres" }
        require(!userExists(cleanUser)) { "Ese usuario ya existe" }
        require(!emailExists(cleanEmail)) { "Ese email ya existe" }
        val user = auth.createUserWithEmailAndPassword(cleanEmail, password).await().user ?: error("No se pudo crear la cuenta")
        val profile = Smile(uid = user.uid, usuario = cleanUser, nombre = nombre, apellidos = apellidos, email = cleanEmail)
        firestore.collection("smiles").document(cleanUser).set(profile.toFirestore(newDocument = true)).await()
        return profile
    }

    suspend fun recover(email: String) {
        auth.sendPasswordResetEmail(email.trim().lowercase()).await()
    }

    fun googleIntent(context: Context): Intent {
        val webClientId = context.webClientId()
        require(webClientId.isNotBlank()) { "Falta OAuth web client en google-services.json" }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options).signInIntent
    }

    suspend fun loginWithGoogle(data: Intent?): Smile? {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
        val token = account.idToken ?: error("Google no devolvio idToken")
        val user = auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await().user ?: error("No se pudo iniciar con Google")
        return getProfileByUid(user.uid) ?: getProfileByEmail(user.email.orEmpty())
    }

    suspend fun completeGoogleRegistration(usuario: String): Smile {
        val user = auth.currentUser ?: error("Vuelve a iniciar con Google")
        val cleanUser = usuario.usuarioKey()
        val email = user.email?.trim()?.lowercase().orEmpty()
        require(cleanUser.matches(Regex("[a-z0-9_-]{4,}"))) { "Usuario minimo 4 caracteres" }
        require(email.contains("@")) { "Google no devolvio email valido" }
        require(!userExists(cleanUser)) { "Ese usuario ya existe" }
        require(!emailExists(email)) { "Ese email ya esta registrado" }
        val parts = user.displayName.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val profile = Smile(
            uid = user.uid,
            usuario = cleanUser,
            nombre = parts.firstOrNull() ?: cleanUser,
            apellidos = parts.drop(1).joinToString(" "),
            email = email,
            avatar = user.photoUrl?.toString(),
            registradoCon = "google",
        )
        firestore.collection("smiles").document(cleanUser).set(profile.toFirestore(newDocument = true)).await()
        return profile
    }

    fun logout(context: Context) {
        auth.signOut()
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }

    suspend fun updateProfilePhoto(usuario: String, avatar: String?) {
        firestore.collection("smiles").document(usuario.lowercase().trim()).update(
            mapOf("avatar" to avatar)
        ).await()
    }

    suspend fun userExists(usuario: String): Boolean =
        firestore.collection("smiles").document(usuario.trim().lowercase()).get().await().exists()

    suspend fun emailExists(email: String): Boolean =
        firestore.collection("smiles").whereEqualTo("email", email.trim().lowercase()).limit(1).get().await().documents.isNotEmpty()

    private suspend fun getEmailByUser(usuario: String): String? =
        firestore.collection("smiles").document(usuario.trim().lowercase()).get().await().getString("email")

    private suspend fun getProfileByUid(uid: String): Smile? =
        firestore.collection("smiles").whereEqualTo("uid", uid).limit(1).fastGet().documents.firstOrNull()?.toSmile()

    private suspend fun getProfileByEmail(email: String): Smile? =
        firestore.collection("smiles").whereEqualTo("email", email.trim().lowercase()).limit(1).fastGet().documents.firstOrNull()?.toSmile()
}

private fun com.google.firebase.firestore.DocumentSnapshot.toSmile(): Smile =
    Smile(
        uid = getString("uid").orEmpty(),
        usuario = getString("usuario").orEmpty(),
        nombre = getString("nombre").orEmpty(),
        apellidos = getString("apellidos").orEmpty(),
        email = getString("email").orEmpty(),
        avatar = getString("avatar") ?: getString("foto"),
        plan = getString("plan") ?: "free",
        rol = getString("rol") ?: "usuario",
        estado = getString("estado") ?: "activo",
        activo = getBoolean("activo") ?: true,
        registradoCon = getString("registradoCon") ?: getString("registradoPor") ?: "correo",
        terminos = getBoolean("terminos") ?: true,
        tema = getString("tema") ?: "Paz",
        verificado = getBoolean("verificado") ?: false,
        segmento = getString("segmento") ?: "publico",
    )

private fun Context.webClientId(): String {
    return runCatching { getString(R.string.default_web_client_id) }.getOrDefault("")
}

private fun String.usuarioKey(): String =
    Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace(Regex("[^a-z0-9_-]+"), "")
        .take(32)

private suspend fun Query.fastGet() =
    runCatching { get(Source.CACHE).await() }
        .getOrElse { get(Source.DEFAULT).await() }
        .let { if (it.isEmpty) get(Source.DEFAULT).await() else it }
