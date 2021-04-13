package com.junapablo.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class ToDoReminderBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("mDebug", "im alive")
        if(context == null) return
        if(intent == null) return

        val entryId = intent.extras?.getString("ToDoEntryId") as String
        val notificationId = intent.extras?.getInt("notificationId")!!

        GlobalScope.launch(Dispatchers.IO){
            val entry = Room.databaseBuilder(
                    context,
                    ToDoEntriesDatabase::class.java, "entries"
            ).build().toDoEntryDao().getById(entryId)

            val title = if (entry.time - System.currentTimeMillis() > 0) "Task ${entry.title} is due in less than 5 minutes" else "Task ${entry.title} is overdue"

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setLargeIcon(Utils.getBitmapFromAsset(context, entry.type))
                    .setContentTitle(title)
                    .setContentText("${SimpleDateFormat("HH:mm", Locale.US).format(entry.time)} ${entry.note}")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true)

            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        }
    }
}