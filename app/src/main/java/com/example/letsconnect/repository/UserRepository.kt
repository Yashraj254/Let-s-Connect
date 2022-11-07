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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private var storageRef: StorageReference,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {


    suspend fun getUser(userId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection("users").document(userId).get()
    }



    fun userSignOut() {
        auth.signOut()
    }

    suspend fun checkIfFollowing(userId: String) = flow {
    val currentUser = auth.currentUser!!.uid

    try {
            database.collection("users").document(currentUser).get().await().also {
                val followingList = it.get("following") as ArrayList<String>
                if(followingList.contains(userId))
                    emit(Response.Success(true))
                else
                    emit(Response.Success(false))

            }
        }
        catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
}

    suspend fun showCurrentUserDetails(userId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(userId).get()
    }

    suspend fun checkUsername(username: String) = flow {
        val currentUser = auth.currentUser!!.uid
        var usernameFound = false
        try {
            emit(Response.Loading)
            database.collection("users").get().await().toObjects(Users::class.java)
                .also {
                    for (i in it) {
                        if (username == i.username && i.userId != currentUser) {
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

    suspend fun updateUserProfile(name: String, username: String) = flow {
        val currentUser = auth.currentUser!!.uid

        try {
            emit(Response.Loading)
            val obj = mutableMapOf<String, String>()
            obj["name"] = name
            obj[KEY_USER_NAME] = username
            database.collection("users").document(currentUser)
                .update(obj as Map<String, Any>).also {
                    emit(Response.Success(true))
                }
            database.collection("users").document(currentUser).collection("myPosts").get()
                .addOnSuccessListener {
                    for (i in it) {
                        database.collection("allPosts").document(i.getString("postId").toString())
                            .update(obj as Map<String, Any>)
                        database.collection("users").document(currentUser).collection("myPosts")
                            .document(i.getString("postId").toString())
                            .update(obj as Map<String, Any>)
                    }
                }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }


    suspend fun removeFollower(userId: String) = safeApiCall {
        val currentUser = auth.currentUser!!.uid

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

    suspend fun followUser(userId: String) = flow {
        val currentUser = auth.currentUser!!.uid

        try{
            database.collection(KEY_COLLECTION_USERS).document(currentUser).get().onSuccessTask {
                val following = it.get(KEY_FOLLOWING) as ArrayList<String>
                following.add(userId)
                val followingObj = HashMap<String, Any>()
                followingObj[KEY_FOLLOWING] = following
                database.collection(KEY_COLLECTION_USERS).document(currentUser).update(followingObj)
            }
            database.collection(KEY_COLLECTION_USERS).document(userId).get().await().also {
                 follow ->
                    val followers = follow.get(KEY_FOLLOWERS) as ArrayList<String>
                    followers.add(currentUser)
                    val followersObj = HashMap<String, Any>()
                    followersObj[KEY_FOLLOWERS] = followers
                    database.collection(KEY_COLLECTION_USERS).document(userId).update(followersObj)
                    emit(Response.Success(true))
                }
        }
        catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun unfollowUser(userId: String) = flow {
        val currentUser = auth.currentUser!!.uid

        try{
            database.collection("users").document(currentUser).get().addOnSuccessListener {
                val following = it.get(KEY_FOLLOWING) as ArrayList<String>
                following.remove(userId)
                val followingObj = HashMap<String, Any>()
                followingObj[KEY_FOLLOWING] = following
                database.collection(KEY_COLLECTION_USERS).document(currentUser)
                    .update(followingObj)
            }

            database.collection("users").document(userId).get().await().also {
                val followers = it.get(KEY_FOLLOWERS) as ArrayList<String>
                followers.remove(userId)
                val followerObj = HashMap<String, Any>()
                followerObj[KEY_FOLLOWERS] = followers
                database.collection(KEY_COLLECTION_USERS).document(userId)
                    .update(followerObj)
                emit(Response.Success(true))
            }
        }
        catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }

    }

    suspend fun getAllUserProfiles() = flow {

        try {
            emit(Response.Loading)
            database.collection("users").get().await().also {
                val usersList = ArrayList<Users>()
                for (i in it) {
                    val user = i.toObject(Users::class.java)
                    usersList.add(user)
                }
                emit(Response.Success(usersList))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun getAllUsers() = flow {
        val currentUser = auth.currentUser!!.uid
        try {
            emit(Response.Loading)
            database.collection("users").get().await().also {
                val usersList = ArrayList<Users>()
                for (i in it) {
                    val user = i.toObject(Users::class.java)
                    usersList.add(user)
                    if (user.userId == currentUser) {
                        usersList.remove(user)
                    }
                }
                emit(Response.Success(usersList))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun getAllFollowers(userId: String) = flow{
        try {
            emit(Response.Loading)
            database.collection("users").document(userId).get().await().also {
                val followerList = it.get("followers") as ArrayList<String>
                emit(Response.Success(followerList))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun getAllFollowing(userId: String) = flow{
        try {
            emit(Response.Loading)
            database.collection("users").document(userId).get().await().also {
                val followingList = it.get("following") as ArrayList<String>
                emit(Response.Success(followingList))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
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
        val currentUser = auth.currentUser!!.uid

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