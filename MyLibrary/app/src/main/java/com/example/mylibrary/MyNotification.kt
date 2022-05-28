package com.example.mylibrary

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject


class MyNotification(var context: Context) {

    private val channel_id = "channel id"
    private val notification_ID = 300
    private var queue: RequestQueue? = null
    private val TAG = "MyNotification"
    private val FIREBASE_CLOUD_MESSAGEING_URL = "https://fcm.googleapis.com/fcm/send"

    companion object {
        var userToken: String = ""

        val TAG = "MyNotification"

        fun getUserToken(context: Context) {
            if (checkGooglePlayServices(context)) {
                //GET Token
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                            return@addOnCompleteListener
                        }

                        userToken = task.result
                        Log.d(TAG, "Token $userToken")

                    }
            } else {
                Toast.makeText(context, "can't used notification from firebase", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        fun checkGooglePlayServices(context: Context): Boolean {
            val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            return if (status != ConnectionResult.SUCCESS) {
                Log.e(TAG, "Error")
                false
            } else {
                Log.d(TAG, "Google play services updated")
                true
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(id: Int, title: String, message: String, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channel_id,
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(true)
            channel.description = message
            channel.enableLights(true)

            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channel_id)

        val notification: Notification = builder.setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setContentText(message)
            .setContentTitle(title)
            .build()

        notificationManager.notify(id, notification)
    }

    fun sendNotification(title: String, message: String, topic: String) {
        try {
            //queue of requests
            if (queue != null) {
                queue?.cancelAll { true }
                Log.d(TAG, "cancel all the requests, with sequenceNumber ${queue?.sequenceNumber}")
            } else {
                queue = Volley.newRequestQueue(context)
            }
            // data format
            val data: JSONObject = JSONObject()
                .put("title", title)
                .put("body", message)
            val notificationData = JSONObject()
                .put("notification", data)
                .put("to", topic)

            // request format
            val request =
                object : JsonObjectRequest(Request.Method.POST,
                    FIREBASE_CLOUD_MESSAGEING_URL,
                    notificationData,
                    Response.Listener {

                    },
                    {
                        Log.e(TAG, "request fail due to ${it.message}")
                    }) {
                    // add headers
                    override fun getHeaders(): MutableMap<String, String> {
                        val api_key = "AAAAyA1I9Tk:APA91bFzj93vZMw_mbEmIzM65GVOWo2u2AOxFkl64mD2g8KzzYCoESGfbA583nY7ogKN2iCM8ftaPnNO06aByuHBJaGQzFzxZLs6tmhq7khbV_Eg60NO4JlE3DcLNxDge1C3zRVqj6mQ"
                        val headers = HashMap<String, String>()
                        headers["Content-Type"] = "application/json"
                        headers["Authorization"] = "key=$api_key"
                        return headers
                    }
                }

            // add request to the volley request queue
            queue?.add(request)
        } catch (e: Exception) {
            Log.e(TAG, "send request fail due to ${e.message}")
        }
    }


}
