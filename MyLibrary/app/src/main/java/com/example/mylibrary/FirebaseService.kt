package com.example.mylibrary

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


var NOTIFICATION_ID = 1

class FirebaseService : FirebaseMessagingService() {

    private val TAG = "FirebaseService"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(message: RemoteMessage) {

        if (message.notification != null) {
            Log.d(TAG, "Message Notification Body: ${message.notification?.body}")
            Log.d(TAG, "Message Notification Body: ${message.notification?.title}")
            Log.d(TAG, "Message Notification Body: ${message.data}")

            MyNotification(this)
                .showNotification(
                    NOTIFICATION_ID,
                    message.notification?.title!!,
                    message.notification?.body!!,
                    Intent(this, FirebaseService::class.java)
                )
            NOTIFICATION_ID++
        }

        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

}