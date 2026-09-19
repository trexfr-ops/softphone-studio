package com.softphone.studio.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.softphone.studio.MainActivity
import com.softphone.studio.R

/**
 * Manages native notification dispatching for incoming Polish telecom events.
 * Encapsulates Android 13+ permission validation to prevent SecurityExceptions.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "phantomline_telecom"
    private const val CHANNEL_NAME = "PhantomLine Telecom Alerts"

    /**
     * Create the high-priority notification channel on Android Oreo (API 26) and above.
     *
     * @param context Application context.
     */
    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming SMS and line validity expiration alerts"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Dispatch an incoming SMS notification to the system tray.
     *
     * @param context Application context.
     * @param sender Sender address or name (e.g. DISCORD or phone number).
     * @param message Text body of the SMS.
     * @param lineName Name or number of the recipient virtual line.
     * @param notificationId Unique notification integer ID.
     */
    fun showIncomingSms(
        context: Context,
        sender: String,
        message: String,
        lineName: String,
        notificationId: Int = 1001
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtext = if (lineName.isNotBlank()) "To: $lineName" else "PhantomLine Virtual SIM"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(sender)
            .setContentText(message)
            .setSubText(subtext)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    /**
     * Dispatch an urgent lease expiration alert for an active virtual number.
     *
     * @param context Application context.
     * @param number Formatted phone number.
     * @param daysLeft Remaining days before the line is reclaimed by the carrier.
     */
    fun showLeaseExpiryAlert(context: Context, number: String, daysLeft: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Line Expiration Warning")
            .setContentText("Number $number expires in $daysLeft day(s). Tap to extend validity.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(2001, builder.build())
    }
}
