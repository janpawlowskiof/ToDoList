package com.junapablo.todolist

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.Comparator


@Parcelize
data class ToDoEntry(
    val note: String,
    var time: Date,
    var priority: Int = 0,
    var type: EntryType = EntryType.Shopping
) : Parcelable {
    enum class EntryType(val value: String) {
        Important("❗"),
        Shopping("\uD83D\uDED2"),
        Call("☎");

        override fun toString(): String {
            return value
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
}