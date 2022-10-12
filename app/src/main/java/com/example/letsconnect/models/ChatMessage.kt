package com.example.letsconnect.models


class ChatMessage {
    constructor() {}
    constructor(
         message:String?,
         senderId:String?,
         timeStamp:Long?,
         sentTime:String
    ) {
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
        this.sentTime = sentTime
    }
    var message:String? = null
    var senderId:String? = null
    var timeStamp:Long? = 0
    var sentTime:String?=null
}
