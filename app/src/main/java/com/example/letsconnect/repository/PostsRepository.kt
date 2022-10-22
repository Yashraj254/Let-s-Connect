package com.example.letsconnect.repository

import android.content.Context
import android.provider.SyncStateContract.Helpers.update
import com.example.letsconnect.*
import com.example.letsconnect.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

class PostsRepository @Inject constructor(
    private val database: FirebaseFirestore,
    private var storageRef: StorageReference,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {
    private val databaseRef = database.collection("chats")
    private val allPostRef = database.collection(KEY_ALL_POSTS)
    private val currentUser = auth.currentUser!!.uid
    suspend fun showCurrentPost(postId: String): Resource<DocumentSnapshot> = safeApiCall {
        database.collection(KEY_ALL_POSTS).document(postId).get()
    }


    suspend fun getAllPosts(): Resource<QuerySnapshot> = safeApiCall {
        allPostRef.orderBy("uploadTime", Query.Direction.DESCENDING).get()
    }

    suspend fun getLikedPosts(): Resource<QuerySnapshot> = safeApiCall {
        database.collection("users").document(auth.currentUser!!.uid)
            .collection("likedPosts").orderBy("uploadTime", Query.Direction.DESCENDING).get()
    }

    suspend fun showCurrentUserPosts(userId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(userId)
            .collection("myPosts").get()
    }

    suspend fun addNewPost(postMessage: String) = safeApiCall {
        database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .onSuccessTask {
                val date = Date()
                val list = ArrayList<String>()
                val username = it.getString(KEY_USER_NAME)
                val userId = auth.currentUser!!.uid
                val profileImage = it.getString(KEY_PROFILE_IMAGE)
                val post = HashMap<String, Any>()
                post[KEY_USER_ID] = userId
                post[KEY_USER_NAME] = username!!
                if (profileImage != null) {
                    post[KEY_PROFILE_IMAGE] = it.getString(KEY_PROFILE_IMAGE)!!
                }
                post[KEY_EMAIL] = auth.currentUser!!.email!!
                post[KEY_TOTAL_VIEWS] = 0
                post[KEY_TOTAL_COMMENTS] = 0
                post[KEY_TOTAL_LIKES] = 0
                post[KEY_UPLOAD_TIME] = date.time
                post[KEY_POST_MESSAGE] = postMessage
                post["likedBy"] = list
                val postId = database.collection(KEY_ALL_POSTS).document()
                post["postId"] = postId.id
                postId.set(post).addOnSuccessListener {
                    database.collection("users").document(userId).collection("myPosts")
                        .document(postId.id).set(post)

                }
            }
    }

    suspend fun postComment(
        postId: String,
        message: String,
    ) = safeApiCall {
        val userId = auth.currentUser!!.uid
        database.collection(KEY_COLLECTION_USERS).document(userId).get().onSuccessTask {
            val comment = HashMap<String, Any>()
            comment[KEY_USER_ID] = userId
            comment[KEY_USER_NAME] = it.getString(KEY_USER_NAME)!!
            comment[KEY_EMAIL] = it.getString(KEY_EMAIL)!!
            comment["commentMessage"] = message
            if (it.getString(KEY_PROFILE_IMAGE) != null)
                comment[KEY_PROFILE_IMAGE] = it.getString(KEY_PROFILE_IMAGE).toString()
            val date = Date()
            comment[KEY_UPLOAD_TIME] = date.time
            database.collection(KEY_ALL_POSTS).document(postId).collection("comments")
                .add(comment).onSuccessTask {
                    database.collection(KEY_ALL_POSTS).document(postId).collection("comments").get()
                        .onSuccessTask {
                            val totalComments = it.size()
                            val obj = mutableMapOf<String, Int>()
                            obj[KEY_TOTAL_COMMENTS] = totalComments
                            database.collection(KEY_ALL_POSTS).document(postId)
                                .update(obj as Map<String, Any>)
                            database.collection("users").document(userId).collection("myPosts")
                                .get().addOnSuccessListener {
                                for (i in it) {
                                    if (i.getString("postId") == postId) {
                                        database.collection("users").document(userId)
                                            .collection("myPosts")
                                            .document(postId)
                                            .update(obj as Map<String, Any>)
                                    }
                                }
                            }
                            database.collection("users").document(userId).collection("likedPosts")
                                .get().addOnSuccessListener {
                                for (i in it) {
                                    if (i.getString("postId") == postId) {
                                        database.collection("users").document(userId)
                                            .collection("likedPosts")
                                            .document(postId)
                                            .update(obj as Map<String, Any>)
                                    }
                                }
                            }

                        }
                }
        }
    }

    suspend fun getAllComments(postId: String): Resource<QuerySnapshot> = safeApiCall {
        database.collection(KEY_ALL_POSTS).document(postId).collection("comments")
            .orderBy("uploadTime").get()
    }

    suspend fun likePost(post: Post) = safeApiCall {
        val list = post.likedBy
        val isLiked = list.contains(currentUser)
        if (isLiked) {
            post.likedBy.remove(currentUser)
            database.collection("users").document(currentUser)
                .collection("likedPosts").document(post.postId).delete()
        } else {
            post.likedBy.add(currentUser)
            database.collection("users").document(currentUser)
                .collection("likedPosts").document(post.postId).set(post)

        }
        val obj = mutableMapOf<String, Any>()
        obj["likedBy"] = post.likedBy
        obj["totalLikes"] = post.likedBy.size
        database.collection("users").document(currentUser)
            .collection("myPosts").document(post.postId).update(obj as Map<String, Any>)

        database.collection(KEY_ALL_POSTS).document(post.postId)
            .update(obj as Map<String, Any>)

    }

}