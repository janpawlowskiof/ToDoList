package com.junapablo.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.Comparator


@Entity
@Parcelize
data class ToDoEntry(
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "note") val note: String,
        @ColumnInfo(name = "time") var time: Long,
        @ColumnInfo(name = "priority") var priority: Int = 0,
        @ColumnInfo(name = "type") var type: String = EntryType.Shopping.toString(),
        @ColumnInfo(name = "creationTime") var creationTime: Long = System.currentTimeMillis(),
        @PrimaryKey val id: String = UUID.randomUUID().toString()
) : Parcelable {

    enum class EntryType(val value: String, val displayString: String) {
        Important("important.png", "❗"),
        Shopping("shopping.png", "\uD83D\uDED2"),
        Call("call.png", "☎️");

        override fun toString(): String {
            return displayString
        }
    }

    enum class SortTypes(val value: String, val comparator: Comparator<ToDoEntry>) {
        FromNearest("From Nearest", Comparator { o1, o2 -> o1.time.compareTo(o2.time) }),
        FromFurther("From Further", Comparator { o1, o2 -> o2.time.compareTo(o1.time) }),
        ByPriorityAscending("By Priority Ascending", Comparator { o1, o2 -> o1.priority.compareTo(o2.priority) }),
        ByPriorityDescending("By Priority Descending", Comparator { o1, o2 -> o2.priority.compareTo(o1.priority) }),
        ByType("By Type", Comparator { o1, o2 -> o1.type.compareTo(o2.type) });

        override fun toString(): String {
            return value
        }
    }

    fun setNotification(context: Context) {
        cancelNotification(context)
        Log.d("mDebug", "Setting reminder")
        val notificationIntent = Intent(context, ToDoReminderBroadcast::class.java)
        notificationIntent.putExtra("ToDoEntryId", this.id)
        notificationIntent.putExtra("notificationId", creationTime.toInt())
        val pendingIntent = PendingIntent.getBroadcast(context, (System.currentTimeMillis() - creationTime).toInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.add(Calendar.MINUTE, -5)

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
        )
    }

    fun cancelNotification(context: Context) {
        Log.d("mDebug", "Canceling reminder")
        val notificationIntent = Intent(context, ToDoReminderBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, creationTime.toInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
        NotificationManagerCompat.from(context).cancel(creationTime.toInt())
    }
}

@Dao
interface ToDoEntryDao {
    @Query("SELECT * FROM todoentry")
    suspend fun getAll(): List<ToDoEntry>

    @Query("SELECT * FROM todoentry WHERE id=:id")
    suspend fun getById(id: String): ToDoEntry

    @Insert
    suspend fun insertAll(vararg users: ToDoEntry)

    @Delete
    suspend fun delete(user: ToDoEntry)
}

@Database(entities = arrayOf(ToDoEntry::class), version = 1)
abstract class ToDoEntriesDatabase : RoomDatabase() {
    abstract fun toDoEntryDao(): ToDoEntryDao
}
