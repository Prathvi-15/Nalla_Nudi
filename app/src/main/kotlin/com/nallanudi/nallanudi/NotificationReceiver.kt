package com.nallanudi.nallanudi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val words =
            AppDatabase.getInstance(context)
                .wordDao()
                .getAllWords()

        if (words.isEmpty()) return

        val word =
            words[(Math.random() * words.size).toInt()]

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Word",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description = "Daily word of the day"
            }

            manager.createNotificationChannel(channel)
        }

        val openApp =
            Intent(context, MainActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val builder =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)

                .setContentTitle(
                    "📖 Word of the Day — ${word.englishWord}"
                )

                .setContentText(
                    "${word.kannadaWord} : ${word.kannadaExplanation}"
                )

                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "${word.kannadaWord}\n${word.kannadaExplanation}"
                        )
                )

                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )

                .setContentIntent(pendingIntent)

                .setAutoCancel(true)

        manager.notify(
            1001,
            builder.build()
        )
    }

    companion object {

        const val CHANNEL_ID =
            "nallanudi_channel"
    }
}