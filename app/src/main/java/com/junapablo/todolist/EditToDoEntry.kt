package com.junapablo.todolist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.junapablo.todolist.databinding.ActivityAddNewToDoEntryBinding
import com.junapablo.todolist.databinding.ActivityEditToDoEntryBinding
import java.util.*

class EditToDoEntry : AppCompatActivity() {
    private lateinit var binding: ActivityEditToDoEntryBinding
    lateinit var originalEntry: ToDoEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditToDoEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        originalEntry = intent.extras?.get("ToDoEntry") as ToDoEntry

        binding.spinnerType.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, ToDoEntry.EntryType.values())
        binding.spinnerType.setSelection(ToDoEntry.EntryType.values().indexOf(originalEntry.type))
        binding.spinnerPriority.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, (0..6).toList())
        binding.spinnerPriority.setSelection(originalEntry.priority)

        binding.textInputNote.setText(originalEntry.note)

        binding.buttonSave.setOnClickListener {
            val entry = ToDoEntry(binding.textInputNote.text.toString(), Date())
            entry.type = binding.spinnerType.selectedItem as ToDoEntry.EntryType
            entry.priority = binding.spinnerPriority.selectedItem as Int

            val c = Calendar.getInstance()
            c.time = originalEntry.time

            val timePickerListener = TimePickerDialog.OnTimeSetListener{ _, hourOfDay, minute ->

                entry.time = Calendar.getInstance().also {
                    it.time = entry.time
                    it.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    it.set(Calendar.MINUTE, minute)
                }.time

                Intent().putExtra("ToDoEntry", entry).putExtra("OriginalToDoEntry", originalEntry).also{ intent ->
                    setResult(RESULT_OK, intent)
                }
                finish()
            }
            val timePickerDialog = TimePickerDialog(this, timePickerListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
            val datePickerListener = DatePickerDialog.OnDateSetListener{ _, year, month, dayOfMonth ->
                entry.time = Calendar.getInstance().also {
                    it.set(year, month, dayOfMonth)
                }.time
                timePickerDialog.show()
            }
            DatePickerDialog(this, datePickerListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(
                Calendar.DAY_OF_MONTH)).show()
        }
        binding.buttonDiscard.setOnClickListener {
            setResult(RESULT_CANCELED, Intent())
            finish()
        }
    }
}