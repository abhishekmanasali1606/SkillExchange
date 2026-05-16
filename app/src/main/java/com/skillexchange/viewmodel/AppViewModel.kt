package com.skillexchange.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.skillexchange.data.model.*
import com.skillexchange.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo  = FirestoreRepository()
    private val auth  = FirebaseAuth.getInstance()
    private val prefs = application.getSharedPreferences("se_prefs", Context.MODE_PRIVATE)

    // ─── Phone Auth internal state ────────────────────────────────────────────
    private var verificationId: String? = null

    // ─── Current user ─────────────────────────────────────────────────────────
    var currentUser: User? by mutableStateOf(null)
        private set

    init {
        // First check Firebase Auth, then fall back to local prefs
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                currentUser = repo.getUser(firebaseUser.uid)
                // If Firebase says logged in but no Firestore profile → treat as no user
                if (currentUser == null) auth.signOut()
            }
        } else {
            // Legacy fallback (in case app was updated from old version)
            val savedId = prefs.getString("userId", null)
            if (savedId != null) {
                viewModelScope.launch {
                    currentUser = repo.getUser(savedId)
                    if (currentUser == null) prefs.edit().remove("userId").apply()
                }
            }
        }
        observePosts()
        observeAllSwaps()
        observeUsers()
    }

    // ─── Posts ────────────────────────────────────────────────────────────────
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private fun observePosts() {
        viewModelScope.launch { repo.getPosts().collect { _posts.value = it } }
    }

    // ─── Swaps ────────────────────────────────────────────────────────────────
    private val _allSwaps = MutableStateFlow<List<Swap>>(emptyList())
    val allSwaps: StateFlow<List<Swap>> = _allSwaps

    private fun observeAllSwaps() {
        viewModelScope.launch { repo.getAllSwaps().collect { _allSwaps.value = it } }
    }

    fun mySwaps(): List<Swap> {
        val uid = currentUser?.id ?: return emptyList()
        return _allSwaps.value.filter { it.offeredBy == uid || it.postOwner == uid }
    }

    // ─── Users ────────────────────────────────────────────────────────────────
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private fun observeUsers() {
        viewModelScope.launch { repo.getAllUsers().collect { _users.value = it } }
    }

    fun getUserById(id: String): User? = _users.value.find { it.id == id }

    // ─── Messages ─────────────────────────────────────────────────────────────
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // ─── Theme ───────────────────────────────────────────────────────────────
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // ─── Notifications ────────────────────────────────────────────────────────
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun showSuccess(msg: String) {
        _successMessage.value = msg
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    fun observeMessages(swapId: String) {
        viewModelScope.launch { repo.getMessages(swapId).collect { _messages.value = it } }
    }

    // ─── Email Auth ──────────────────────────────────────────────────────────

    fun registerWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Registration failed")
            }
        }
    }

    fun loginWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: ""
                val user = repo.getUser(uid)
                if (user != null) {
                    currentUser = user
                    showSuccess("Login Successful!")
                    onResult(true, null)
                } else {
                    onResult(true, "SETUP_REQUIRED")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Login failed")
            }
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error sending reset email")
            }
        }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            try {
                // In a real app, you might want to delete user data from Firestore too
                user.delete().await()
                currentUser = null
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updatePassword(newPass: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run { onResult(false, "No user signed in"); return }
        viewModelScope.launch {
            try {
                user.updatePassword(newPass).await()
                showSuccess("Password Updated Successfully!")
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    suspend fun findEmailByPhone(phone: String): String? {
        return repo.getUserByPhone(phone)?.id // In this app, UID is stored in User.id
        // Actually we need the email. Let's assume the user model or auth can provide it.
        // If we can't find email, we can't easily sign in to "update" it unless they are the same UID.
    }

    // ─── Phone Auth ───────────────────────────────────────────────────────────

    /**
     * Step 1: Send OTP to the given phone number.
     * [onCodeSent]     — called when OTP SMS is sent; navigate to OTP entry.
     * [onAutoVerified] — called when Firebase auto-detects the OTP (rare).
     * [onError]        — called with a human-readable error message.
     */
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onAutoVerified: (isNewUser: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // SMS auto-retrieved or instant verification on test devices
                viewModelScope.launch {
                    handleCredential(credential, onAutoVerified, onError)
                }
            }
            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.message ?: "Verification failed. Check the phone number.")
            }
            override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = vId
                onCodeSent()
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
        )
    }

    /**
     * Step 2: Verify the OTP code entered by the user.
     * [onNewUser]      — Firebase user exists but no Firestore profile; show profile setup.
     * [onExistingUser] — User has a Firestore profile; go to Feed.
     * [onError]        — Wrong OTP or other error.
     */
    fun verifyOtp(
        code: String,
        onNewUser: () -> Unit,
        onExistingUser: () -> Unit,
        onError: (String) -> Unit
    ) {
        val vId = verificationId ?: run {
            onError("Session expired. Please request a new OTP.")
            return
        }
        val credential = PhoneAuthProvider.getCredential(vId, code)
        viewModelScope.launch {
            handleCredential(
                credential,
                onResult = { isNew -> if (isNew) onNewUser() else onExistingUser() },
                onError = onError
            )
        }
    }

    /**
     * Step 3 (new users only): Save name and skills to Firestore.
     */
    fun setupProfile(name: String, skillOffered: String, skillWanted: String, phoneNumber: String = "", village: String = "", onDone: () -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onDone(); return }
        viewModelScope.launch {
            val user = User(
                id = uid,
                name = name,
                skillOffered = skillOffered,
                skillWanted = skillWanted,
                phoneNumber = phoneNumber,
                village = village,
                trustScore = 0,
                points = 0
            )
            repo.saveUser(user)
            currentUser = user
            showSuccess("Registration Successful!")
            onDone()
        }
    }

    private suspend fun handleCredential(
        credential: PhoneAuthCredential,
        onResult: (isNewUser: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: run { onError("Sign-in failed. Try again."); return }
            val uid = firebaseUser.uid
            val existingUser = repo.getUser(uid)
            if (existingUser != null) {
                currentUser = existingUser
                onResult(false)
            } else {
                // New Firebase user — profile setup needed
                onResult(true)
            }
        } catch (e: Exception) {
            onError(e.message ?: "Incorrect OTP. Please try again.")
        }
    }

    fun logout(onDone: () -> Unit) {
        auth.signOut()
        prefs.edit().remove("userId").apply()
        currentUser = null
        onDone()
    }

    // ─── Profile ──────────────────────────────────────────────────────────────
    fun updateUserProfile(skillOffered: String, skillWanted: String, phoneNumber: String, village: String) {
        val uid = currentUser?.id ?: return
        viewModelScope.launch {
            repo.updateUserProfile(uid, skillOffered, skillWanted, phoneNumber, village)
            currentUser = currentUser?.copy(
                skillOffered = skillOffered,
                skillWanted = skillWanted,
                phoneNumber = phoneNumber,
                village = village
            )
        }
    }

    // ─── Posts ────────────────────────────────────────────────────────────────
    fun createPost(title: String, description: String, skillRequired: String, hours: Int, village: String, onDone: () -> Unit) {
        val uid = currentUser?.id ?: return
        viewModelScope.launch {
            repo.createPost(Post(userId = uid, title = title, description = description,
                skillRequired = skillRequired, hoursRequired = hours, village = village, timestamp = System.currentTimeMillis()))
            showSuccess("Post Created Successfully!")
            onDone()
        }
    }

    fun getPostById(postId: String): Post? = _posts.value.find { it.id == postId }

    fun deleteAllPosts(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteAllPosts()
            onDone()
        }
    }

    // ─── Swaps ────────────────────────────────────────────────────────────────
    fun createSwap(postId: String, postOwner: String, hours: Int, message: String, onDone: () -> Unit) {
        val uid = currentUser?.id ?: return
        viewModelScope.launch {
            repo.createSwap(Swap(postId = postId, offeredBy = uid, postOwner = postOwner,
                hoursOffered = hours, message = message))
            onDone()
        }
    }

    fun acceptSwap(swapId: String) {
        viewModelScope.launch {
            repo.updateSwapStatus(swapId, "accepted")
            showSuccess("Swap Accepted!")
        }
    }

    fun confirmSwap(swapId: String) {
        val uid = currentUser?.id ?: return
        val swap = _allSwaps.value.find { it.id == swapId } ?: return
        val isOwner = swap.postOwner == uid
        viewModelScope.launch { repo.confirmSwap(swapId, isOwner) }
    }

    // ─── Messages ─────────────────────────────────────────────────────────────
    fun sendMessage(swapId: String, text: String) {
        val uid = currentUser?.id ?: return
        viewModelScope.launch {
            repo.sendMessage(Message(
                swapId = swapId,
                senderId = uid,
                text = text,
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    // ─── AI Suggestion ────────────────────────────────────────────────────────
    fun getAiSuggestion(): String {
        val map = mapOf(
            "Plumber"     to "Learn basic Electrical wiring — many rural homes need combined plumbing + electrical fixes.",
            "Electrician" to "Add Solar panel installation — high demand in rural areas.",
            "Carpenter"   to "Learn Furniture restoration — upcycling is growing in villages.",
            "Mason"       to "Learn Waterproofing techniques — very useful for monsoon season repairs.",
            "Painter"     to "Try Wall texturing and design — premium skill that earns more points.",
            "Welder"      to "Add Metal gate and railing work — constant demand in rural homes.",
            "Mechanic"    to "Learn Water pump repair — huge need in farming communities.",
            "Roofer"      to "Learn Thatching techniques — traditional but highly valued skill.",
            "Farmer"      to "Learn Drip irrigation setup — saves water and increases yield.",
            "Cook"        to "Learn Pickle and preserve making — great for bartering.",
            "Tailor"      to "Learn Embroidery — adds value to your tailoring service."
        )
        return map[currentUser?.skillOffered ?: ""]
            ?: "Consider specializing in one skill — focused expertise earns more trust."
    }
}
