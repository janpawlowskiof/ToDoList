package com.junapablo.todolist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.DatePicker
import com.junapablo.todolist.databinding.ActivityAddNewToDoEntryBinding
import com.junapablo.todolist.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_edit_to_do_entry.view.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class AddNewToDoEntry : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewToDoEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewToDoEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spinnerType.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, ToDoEntry.EntryType.values())
        binding.spinnerPriority.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, (0..6).toList())

        binding.buttonAdd.setOnClickListener {
            val entry = ToDoEntry(binding.textInputTitle.text.toString(), binding.textInputNote.text.toString(), 0)
            entry.type = binding.spinnerType.selectedItem.toString()
            entry.priority = binding.spinnerPriority.selectedItem as Int

            val c = Calendar.getInstance()

            val timePickerListener = TimePickerDialog.OnTimeSetListener{ _, hourOfDay, minute ->

                entry.time = Calendar.getInstance().also {
                    it.timeInMillis = entry.time
                    it.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    it.set(Calendar.MINUTE, minute)
                }.timeInMillis

                Intent().putExtra("ToDoEntry", entry).also{intent ->
                    setResult(RESULT_OK, intent)
                }
                finish()
            }
            val timePickerDialog = TimePickerDialog(this, timePickerListener, 12, 0, true)

            val datePickerListener = DatePickerDialog.OnDateSetListener{ _, year, month, dayOfMonth ->

                entry.time = Calendar.getInstance().also {
                    it.set(year, month, dayOfMonth)
                }.timeInMillis

                timePickerDialog.show()
            }
            DatePickerDialog(this, datePickerListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}