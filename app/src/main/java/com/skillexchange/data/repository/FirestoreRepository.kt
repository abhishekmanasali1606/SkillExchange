package com.skillexchange.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.skillexchange.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCol = db.collection("users")
    private val postsCol = db.collection("posts")
    private val swapsCol = db.collection("swaps")
    private val messagesCol = db.collection("messages")

    // ─── Users ───────────────────────────────────────────────────────────────

    suspend fun saveUser(user: User) {
        usersCol.document(user.id).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersCol.document(userId).get().await().toObject<User>()
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCol.addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull { it.toObject<User>() } ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUserByPhone(phone: String): User? {
        return usersCol.whereEqualTo("phoneNumber", phone).get().await()
            .documents.firstOrNull()?.toObject<User>()
    }

    suspend fun updateUserProfile(userId: String, skillOffered: String, skillWanted: String, phoneNumber: String, village: String) {
        usersCol.document(userId).update(
            mapOf(
                "skillOffered" to skillOffered,
                "skillWanted" to skillWanted,
                "phoneNumber" to phoneNumber,
                "village" to village
            )
        ).await()
    }

    suspend fun incrementTrustAndPoints(userId: String, pointsToAdd: Int) {
        val user = getUser(userId) ?: return
        usersCol.document(userId).update(
            mapOf(
                "trustScore" to minOf(5, user.trustScore + 1),
                "points" to user.points + pointsToAdd
            )
        ).await()
    }

    // ─── Posts ────────────────────────────────────────────────────────────────

    suspend fun createPost(post: Post) {
        val ref = postsCol.document()
        postsCol.document(ref.id).set(post.copy(id = ref.id)).await()
    }

    suspend fun deleteAllPosts() {
        val batch = db.batch()
        val snapshots = postsCol.get().await()
        for (doc in snapshots.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    fun getPosts(): Flow<List<Post>> = callbackFlow {
        val listener = postsCol
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject<Post>() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    // ─── Swaps ────────────────────────────────────────────────────────────────

    suspend fun createSwap(swap: Swap) {
        val ref = swapsCol.document()
        swapsCol.document(ref.id).set(swap.copy(id = ref.id)).await()
    }

    fun getSwapsForUser(userId: String): Flow<List<Swap>> = callbackFlow {
        val listener = swapsCol
            .whereIn("offeredBy", listOf(userId))
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject<Swap>() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun getAllSwaps(): Flow<List<Swap>> = callbackFlow {
        val listener = swapsCol.addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull { it.toObject<Swap>() } ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateSwapStatus(swapId: String, status: String) {
        swapsCol.document(swapId).update("status", status).await()
    }

    suspend fun confirmSwap(swapId: String, isOwner: Boolean) {
        val field = if (isOwner) "confirmedByOwner" else "confirmedByOfferer"
        swapsCol.document(swapId).update(field, true).await()
        val snap = swapsCol.document(swapId).get().await()
        val swap = snap.toObject<Swap>() ?: return
        if (swap.confirmedByOfferer && swap.confirmedByOwner) {
            swapsCol.document(swapId).update("status", "completed").await()
            incrementTrustAndPoints(swap.offeredBy, swap.hoursOffered)
            incrementTrustAndPoints(swap.postOwner, swap.hoursOffered)
        }
    }

    // ─── Messages ─────────────────────────────────────────────────────────────

    suspend fun sendMessage(message: Message) {
        val ref = messagesCol.document()
        messagesCol.document(ref.id).set(message.copy(id = ref.id)).await()
    }

    fun getMessages(swapId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesCol
            .whereEqualTo("swapId", swapId)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { it.toObject<Message>() } ?: emptyList()
                trySend(list.sortedBy { it.timestamp })
            }
        awaitClose { listener.remove() }
    }
}
