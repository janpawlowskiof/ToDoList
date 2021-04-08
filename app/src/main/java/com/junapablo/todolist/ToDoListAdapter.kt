package com.junapablo.todolist

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ToDoListAdapter(val context: MainActivity) : RecyclerView.Adapter<ToDoListAdapter.ViewHolder>(){
    val entries = mutableListOf<ToDoEntry>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewNote: TextView = view.findViewById(R.id.textViewNote)
        val textViewDue: TextView = view.findViewById(R.id.textViewDue)
        val textViewType: TextView = view.findViewById(R.id.textViewType)
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
        holder.textViewNote.text = e.note
        holder.textViewDue.text = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm").format(e.time)
        holder.textViewType.text = e.type.toString()
        holder.textViewPriority.text = e.priority.toString()

        if(!Calendar.getInstance().before(Calendar.getInstance().also{it.time = e.time})){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#9c3d30"))
        }
        else if(!Calendar.getInstance().also{it.add(Calendar.DAY_OF_MONTH, 1)}.before(Calendar.getInstance().also{
                it.time = e.time
            })){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#719c30"))
        }
        else{
            holder.cardView.setCardBackgroundColor(Color.DKGRAY)
        }

        holder.itemView.setOnLongClickListener{
            Toast.makeText(context, "Editing your ToDo", Toast.LENGTH_SHORT).show()
            context.editToDoEntryResult.launch(Intent(context, EditToDoEntry::class.java).also{
                it.putExtra("ToDoEntry", e)
            })
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    class SwipeToDeleteCallback(val adapter: ToDoListAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        private val background: ColorDrawable = ColorDrawable(Color.RED)
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
            adapter.entries.removeAt(viewHolder.adapterPosition)
            adapter.notifyDataSetChanged()
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