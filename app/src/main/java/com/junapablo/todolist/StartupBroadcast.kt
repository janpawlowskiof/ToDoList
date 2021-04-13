package com.junapablo.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class StartupBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("mDebug", "Startup broadcast, yay")
        GlobalScope.launch(Dispatchers.IO){
            Room.databaseBuilder(
                    context!!,
                    ToDoEntriesDatabase::class.java, "entries"
            ).build().toDoEntryDao().getAll().forEach { it.setNotification(context) }

        }
    }
}
