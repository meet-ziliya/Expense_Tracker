package com.example.expensetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*

class DayWiseReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView
    private lateinit var tvMonth: TextView
    private lateinit var dbHelper: DatabaseHelper

    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_wise_report)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        recyclerView = findViewById(R.id.recyclerDayWise)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        tvMonth = findViewById(R.id.tvMonth)

        dbHelper = DatabaseHelper(this)

        val calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH) + 1
        currentYear = calendar.get(Calendar.YEAR)

        updateMonthDisplay()
        loadExpensesForMonth(currentMonth, currentYear)

        btnPrevMonth.setOnClickListener {
            currentMonth--
            if (currentMonth < 1) {
                currentMonth = 12
                currentYear--
            }
            updateMonthDisplay()
            loadExpensesForMonth(currentMonth, currentYear)
        }

        btnNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 12) {
                currentMonth = 1
                currentYear++
            }
            updateMonthDisplay()
            loadExpensesForMonth(currentMonth, currentYear)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun updateMonthDisplay() {
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
            .format(SimpleDateFormat("MM", Locale.getDefault()).parse(currentMonth.toString())!!)
        tvMonth.text = "$monthName $currentYear"
    }

    private fun loadExpensesForMonth(month: Int, year: Int) {
        val allExpenses = dbHelper.getAllExpenses()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val filteredExpenses = mutableListOf<Expense>()

        if (allExpenses.moveToFirst()) {
            do {
                val date = allExpenses.getString(allExpenses.getColumnIndexOrThrow("date"))
                val category = allExpenses.getString(allExpenses.getColumnIndexOrThrow("category"))
                val amount = allExpenses.getDouble(allExpenses.getColumnIndexOrThrow("amount"))
                val desc = allExpenses.getString(allExpenses.getColumnIndexOrThrow("description"))

                try {
                    val parsedDate = sdf.parse(date)
                    val cal = Calendar.getInstance()
                    cal.time = parsedDate!!
                    val expenseMonth = cal.get(Calendar.MONTH) + 1
                    val expenseYear = cal.get(Calendar.YEAR)

                    if (expenseMonth == month && expenseYear == year) {
                        filteredExpenses.add(Expense(date, category, amount, desc))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (allExpenses.moveToNext())
        }
        allExpenses.close()

        val expenseItems = filteredExpenses.map {
            ExpenseItem(it.category, it.amount, it.date)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DayWiseAdapter(expenseItems)

        val total = filteredExpenses.sumOf { it.amount }
        tvTotalAmount.text = "₹%.2f".format(total)
    }
}
