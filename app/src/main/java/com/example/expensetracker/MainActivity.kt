package com.example.expensetracker

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvToday: TextView
    private lateinit var tvMonth: TextView
    private lateinit var etDate: EditText
    private lateinit var etCategory: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnSubmit: Button
    private var selectedDate: String = ""
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Expense Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for daily expense reminders"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // ✅ Setup Toolbar for 3-dot menu
        setSupportActionBar(findViewById(R.id.toolbar))

        dbHelper = DatabaseHelper(this)

        // Initialize Views
        tvToday = findViewById(R.id.tvToday)
        tvMonth = findViewById(R.id.tvMonth)
        etDate = findViewById(R.id.etDate)
        etCategory = findViewById(R.id.etCategory)
        etAmount = findViewById(R.id.etAmount)
        etDesc = findViewById(R.id.etDesc)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Set today’s date automatically
        val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etDate.setText(todayDate)
        selectedDate = todayDate

        // Date Picker
        etDate.setOnClickListener { showDatePicker() }

        // Category Dialog
        etCategory.setOnClickListener { showCategoryDialog() }

        // Save Expense
        btnSubmit.setOnClickListener { saveExpense() }

        // Update totals
        updateTotals()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                selectedDate = sdf.format(calendar.time)
                etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showCategoryDialog() {
        val categories = arrayOf("Food", "Travel", "Shopping", "Bills", "Entertainment", "Other")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Category")
        builder.setItems(categories) { _, which ->
            etCategory.setText(categories[which])
        }
        builder.show()
    }

    private fun saveExpense() {
        val date = etDate.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val desc = etDesc.text.toString().trim()

        if (date.isEmpty() || category.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.insertExpense(date, category, amount, desc)

        if (success) {
            Toast.makeText(this, "Expense Saved!", Toast.LENGTH_SHORT).show()
            etCategory.text.clear()
            etAmount.text.clear()
            etDesc.text.clear()
            etDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
            updateTotals()
        } else {
            Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotals() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = sdf.format(calendar.time)

        val todayTotal = dbHelper.getTotalForDate(todayDate)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val monthTotal = dbHelper.getTotalForMonth(month, year)

        tvToday.text = "Today\n₹ %.2f".format(todayTotal)
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        tvMonth.text = "$monthName Total\n₹ %.2f".format(monthTotal)
    }

    // ✅ Show menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dayWiseReports -> startActivity(Intent(this, DayWiseReportActivity::class.java))
            R.id.monthWiseReports -> startActivity(Intent(this, MonthWiseReportsActivity::class.java))
}
        return super.onOptionsItemSelected(item)
    }
}
