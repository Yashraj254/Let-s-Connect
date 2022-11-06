package com.example.letsconnect.repository

import android.content.Context
import android.widget.Toast
import com.example.letsconnect.*
import com.example.letsconnect.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class UserRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private var storageRef: StorageReference,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {
    @Inject
    lateinit var currentUser: String
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun getUser(userId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection("users").document(userId).get()
    }

    suspend fun getFollowing() = flow {
        try {
            emit(Response.Loading)
            database.collection("users").document(currentUser).get().await()
                .toObject(Users::class.java)
                .also {
                    emit(Response.Success(it?.following))
                }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun showCurrentUserDetails(userId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(userId).get()
    }

    suspend fun checkUsername(username: String) = flow {
        var usernameFound = false
        try {
            emit(Response.Loading)
            database.collection("users").get().await().toObjects(Users::class.java)
                .also {
                    for (i in it) {
                        if (username == i.username) {
                            usernameFound = true
                            break
                        }
                    }
                    emit(Response.Success(usernameFound))
                }

        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }

    }

    suspend fun updateUserProfile(name: String, username: String) = safeApiCall {
        val obj = mutableMapOf<String, String>()
        obj["name"] = name
        obj[KEY_USER_NAME] = username
        database.collection("users").document(currentUser)
            .update(obj as Map<String, Any>)
        database.collection("users").document(currentUser).collection("myPosts").get()
            .addOnSuccessListener {
                for (i in it) {
                    database.collection("allPosts").document(i.getString("postId").toString())
                        .update(obj as Map<String, Any>)
                    database.collection("users").document(currentUser).collection("myPosts")
                        .document(i.getString("postId").toString()).update(obj as Map<String, Any>)

                }
            }
    }

    suspend fun unfollowUser(userId: String) = safeApiCall {
        database.collection("users").document(currentUser).get().addOnSuccessListener {
            val following = it.get(KEY_FOLLOWING) as ArrayList<String>
            following.remove(userId)
            val followingObj = HashMap<String, Any>()
            followingObj[KEY_FOLLOWING] = following
            database.collection(KEY_COLLECTION_USERS).document(currentUser)
                .update(followingObj)
        }

         database.collection("users").document(userId).get().addOnSuccessListener {
            val followers = it.get(KEY_FOLLOWERS) as ArrayList<String>
             followers.remove(userId)
            val followerObj = HashMap<String, Any>()
             followerObj[KEY_FOLLOWERS] = followers
            database.collection(KEY_COLLECTION_USERS).document(userId)
                .update(followerObj)
        }
}

suspend fun removeFollower(userId: String) = safeApiCall {
    database.collection(KEY_COLLECTION_USERS).document(currentUser).get().onSuccessTask {
        val followers = it.get(KEY_FOLLOWERS) as ArrayList<String>
        followers.remove(userId)
        val followersObj = HashMap<String, Any>()
        followersObj[KEY_FOLLOWERS] = followers
        database.collection("users").document(currentUser).update(followersObj)
    }
    database.collection(KEY_COLLECTION_USERS).document(userId).get().onSuccessTask {
        val following = it.get(KEY_FOLLOWING) as ArrayList<String>
        following.remove(currentUser)
        val followingObj = HashMap<String, Any>()
        followingObj[KEY_FOLLOWERS] = following
        database.collection("users").document(userId).update(followingObj)
    }
}

suspend fun followUser(userId: String) = safeApiCall {
    database.collection(KEY_COLLECTION_USERS).document(currentUser).get().onSuccessTask {
        val following = it.get(KEY_FOLLOWING) as ArrayList<String>
        following.add(userId)
        val followingObj = HashMap<String, Any>()
        followingObj[KEY_FOLLOWING] = following
        database.collection(KEY_COLLECTION_USERS).document(currentUser).update(followingObj)
    }
    database.collection(KEY_COLLECTION_USERS).document(userId).get()
        .onSuccessTask { follow ->
            val followers = follow.get(KEY_FOLLOWERS) as ArrayList<String>
            followers.add(currentUser)
            val followersObj = HashMap<String, Any>()
            followersObj[KEY_FOLLOWERS] = followers
            database.collection(KEY_COLLECTION_USERS).document(userId).update(followersObj)
        }
}

suspend fun getAllUsers(): Resource<QuerySnapshot> = safeApiCall {
    database.collection("users").get()
}

suspend fun sendAndReceiveMessage(textMessage: String, receiverUid: String) = safeApiCall {
    val senderUid = auth.uid.toString()
    val randomKey = database.collection("chats").document().id
    val message = HashMap<String, Any>()
    val senderRoom = senderUid + receiverUid
    val receiverRoom = receiverUid + senderUid

    message["message"] = textMessage
    message["senderId"] = senderUid
    message["timeStamp"] = Date().time
    message["sentTime"] = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    database.collection("chats").document(senderRoom).collection("message")
        .document(randomKey).set(message)
    database.collection("chats").document(receiverRoom).collection("message")
        .document(randomKey).set(message)
}

suspend fun getAllChats(senderRoom: String): Resource<QuerySnapshot> = safeApiCall {
    database.collection("chats").document(senderRoom).collection("message").orderBy("timeStamp")
        .get()
}

suspend fun uploadProfilePic(encodedImage: ByteArray) = safeApiCall {
    storageRef.putBytes(encodedImage).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener {
            val obj = mutableMapOf<String, String>()
            obj[KEY_PROFILE_IMAGE] = it.toString()
            database.collection("users").document(currentUser)
                .update(obj as Map<String, Any>).addOnSuccessListener {
                    Toast.makeText(context, "Profile Picture Uploaded", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }.addOnFailureListener {

        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
    }
}
}