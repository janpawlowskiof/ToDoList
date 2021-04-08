package com.junapablo.todolist

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.junapablo.todolist.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ToDoListAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    lateinit var entriesDao: ToDoEntryDao


    private val newToDoEntryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val toDoEntry = result.data?.extras?.get("ToDoEntry")
            if (toDoEntry !is ToDoEntry)
                return@registerForActivityResult

            adapter.addEntry(toDoEntry)
            Log.d("mDebug", "Note: ${toDoEntry.note}")
        }
    }

    val editToDoEntryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val toDoEntry = result.data?.extras?.get("ToDoEntry")
            val originalToDoEntry = result.data?.extras?.get("OriginalToDoEntry")
            if (toDoEntry !is ToDoEntry || originalToDoEntry !is ToDoEntry)
                return@registerForActivityResult

            adapter.replaceEntry(toDoEntry, originalToDoEntry)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        entriesDao = Room.databaseBuilder(
            applicationContext,
            ToDoEntriesDatabase::class.java, "entries"
        ).build().toDoEntryDao()

        binding.buttonAddNew.setOnClickListener {
            newToDoEntryResult.launch(Intent(this, AddNewToDoEntry::class.java))
        }

        adapter = ToDoListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemTouchHelper = ItemTouchHelper(ToDoListAdapter.SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // spinner and sorting
        binding.spinnerSortType.adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            ToDoEntry.SortTypes.values()
        )
        binding.spinnerSortType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("mDebug", "Nothing selected")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.d("mDebug", "Item selected")
                val sortType = ToDoEntry.SortTypes.values()[position]
                adapter.getEntries().sortWith(sortType.comparator)
                adapter.notifyDataSetChanged()
            }
        }

        adapter.loadAllEntries()
    }
}