package com.example.jetpackcomposeapp.Topic.vc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.jetpackcomposeapp.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val tag = "VideoCallFCM"
    private val channelId = "urgent_channel"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"] ?: "Notification Title"
        val message = data["body"] ?: "Notification Message"
        val senderMail = data["senderMail"] ?: "Sender"
        val messageId = data["messageId"] ?: "jetpack_compose_room"
        val type = data["type"] ?: "0"
        val name = data["name"] ?: "User"

        Log.d(tag, "Data received: $data")
        Log.d(tag, "Type of notification: $type")

        if (type == "1") {
            val serviceIntent = Intent(this, FrontService::class.java).apply {
                putExtra(FrontService.EXTRA_TITLE, title)
                putExtra(FrontService.EXTRA_BODY, message)
                putExtra(FrontService.EXTRA_SENDER_ID, senderMail)
                putExtra(FrontService.EXTRA_MESSAGE_ID, messageId)
                putExtra(FrontService.EXTRA_NAME, name)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent.apply {
                    putExtra("title", title)
                    putExtra("message", message)
                    putExtra("senderId", senderMail)
                    putExtra("messageId", messageId)
                    putExtra("name", name)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            showNotification(
                channelId = channelId,
                channelName = "Notification Channel",
                senderMail = senderMail,
                message = message,
                pendingIntent = pendingIntent,
                name = name
            )
        }
    }

    private fun showNotification(
        channelId: String,
        channelName: String,
        senderMail: String,
        message: String,
        pendingIntent: PendingIntent,
        name: String
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for $channelName"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("$name ($senderMail)")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
