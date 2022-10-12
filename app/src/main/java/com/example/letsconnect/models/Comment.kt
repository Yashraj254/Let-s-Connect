package com.example.letsconnect.models

class Comment {
    constructor() {}
    constructor(
        userId: String,
        profileImage:String?,
        username: String?,
        email: String?,
        uploadTime: Long,
        commentMessage: String?,

    ) {
        this.userId = userId
        this.username = username
        this.email = email
        this.profileImage = profileImage
        this.uploadTime = uploadTime
        this.commentMessage = commentMessage
    }

    lateinit var userId: String
    var username: String? = null
    var email: String? = null
    var profileImage:String? = null
    var uploadTime: Long = 0
    var commentMessage: String? = null

}
