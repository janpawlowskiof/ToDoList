package com.junapablo.todolist

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ToDoListAdapter(val context: MainActivity) : RecyclerView.Adapter<ToDoListAdapter.ViewHolder>(){
    private val entries = mutableListOf<ToDoEntry>()
    var sortingType = ToDoEntry.SortTypes.FromNearest.comparator

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewNote: TextView = view.findViewById(R.id.textViewNote)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        val textViewDue: TextView = view.findViewById(R.id.textViewDue)
        val imageViewType: ImageView = view.findViewById(R.id.imageViewType)
        val textViewPriority: TextView = view.findViewById(R.id.textViewPriority)
        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.to_do_entry, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val e = entries[position]
        holder.textViewTitle.text = e.title
        holder.textViewNote.text = e.note
        holder.textViewDue.text = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm", Locale.US).format(e.time)
        holder.imageViewType.setImageBitmap(Utils.getBitmapFromAsset(context, e.type))
        holder.textViewPriority.text = e.priority.toString()

        if(!Calendar.getInstance().before(Calendar.getInstance().also { it.timeInMillis = e.time })){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#9c3d30"))
        }
        else if(!Calendar.getInstance().also{it.add(Calendar.DAY_OF_MONTH, 1)}.before(Calendar.getInstance().also {
                    it.timeInMillis = e.time
                })){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#719c30"))
        }
        else{
            holder.cardView.setCardBackgroundColor(Color.GRAY)
        }

        holder.itemView.setOnLongClickListener{
            Toast.makeText(context, "Editing your ToDo", Toast.LENGTH_SHORT).show()
            context.editToDoEntryResult.launch(Intent(context, EditToDoEntry::class.java).also {
                it.putExtra("ToDoEntry", e)
            })
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener {
            var diff = e.time - System.currentTimeMillis()
            if (diff > 0){
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedDays: Long = diff / daysInMilli
                diff %= daysInMilli

                val elapsedHours: Long = diff / hoursInMilli
                diff %= hoursInMilli

                val elapsedMinutes: Long = diff / minutesInMilli

                if(elapsedDays > 0){
                    Toast.makeText(context, "$elapsedDays days, $elapsedHours hours until due date", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(context, "$elapsedHours hours and $elapsedMinutes minutes until due date", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(context, "Task is overdue!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    fun getEntries(): MutableList<ToDoEntry>{
        return entries
    }

    fun sort(){
        entries.sortWith(sortingType)
        notifyDataSetChanged()
    }

    fun loadAllEntries(){
        entries.clear()
        context.lifecycleScope.launch {
            context.entriesDao.getAll().forEach {
                it.setNotification(context)
                entries.add(it)
            }
            notifyDataSetChanged()
        }
    }

    fun removeEntry(index: Int){
        val deleted = entries.removeAt(index)
        deleted.cancelNotification(context)
        notifyDataSetChanged()
        context.lifecycleScope.launch {
            context.entriesDao.delete(deleted)
        }
    }

    fun addEntry(entry: ToDoEntry){
        entries.add(entry)
        sort()
        entry.setNotification(context)
        context.lifecycleScope.launch {
            context.entriesDao.insertAll(entry)
        }
    }

    fun replaceEntry(newEntry: ToDoEntry, oldEntry: ToDoEntry){
        newEntry.setNotification(context)
        oldEntry.cancelNotification(context)
        entries[entries.indexOf(oldEntry)] = newEntry
        sort()
        notifyDataSetChanged()

        context.lifecycleScope.launch {
            context.entriesDao.insertAll(newEntry)
            context.entriesDao.delete(oldEntry)
        }
    }

    class SwipeToDeleteCallback(val adapter: ToDoListAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        private val background: ColorDrawable = ColorDrawable(Color.GREEN)
        private val icon =
            ContextCompat.getDrawable(adapter.context, android.R.drawable.ic_menu_delete)!!

        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapter.removeEntry(viewHolder.adapterPosition)
        }

        override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
        ) {

            super.onChildDraw(
                    c, recyclerView, viewHolder, dX,
                    dY, actionState, isCurrentlyActive
            )
            val itemView = viewHolder.itemView
            val backgroundCornerOffset = 20
            val backgroundHeightOffset = 20

            val cardHeight = itemView.bottom - itemView.top
            val iconHeightPercent = 0.5
            val iconHeight = (cardHeight * iconHeightPercent).toInt()
            val iconBottom: Int = itemView.top + cardHeight / 2 + iconHeight / 2

            val iconTop: Int = itemView.top + cardHeight / 2 - iconHeight / 2
            val iconScaleRatio = iconHeight.toFloat() / icon.intrinsicHeight.toFloat()
            val iconWidth = (icon.intrinsicWidth * iconScaleRatio * 0.5).toInt()

            val iconLeft: Int = itemView.right + dX.toInt() / 2 - iconWidth
            val iconRight = itemView.right + dX.toInt() / 2 + iconWidth

            if (dX < 0) { // Swiping to the left
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top + backgroundHeightOffset, itemView.right, itemView.bottom - backgroundHeightOffset
                )
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0)
                icon.setBounds(0, 0, 0, 0)
            }

            background.draw(c)
            icon.draw(c)
        }
    }
}