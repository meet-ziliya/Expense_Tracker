package com.example.expensetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

class MonthWiseReportsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var btnPrevYear: ImageView
    private lateinit var btnNextYear: ImageView
    private lateinit var tvYear: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var recyclerMonthWise: RecyclerView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: MonthWiseAdapter

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var monthList = mutableListOf<MonthData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_month_wise_reports)

        dbHelper = DatabaseHelper(this)

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Views
        btnPrevYear = findViewById(R.id.btnPrevYear)
        btnNextYear = findViewById(R.id.btnNextYear)
        tvYear = findViewById(R.id.tvYear)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        recyclerMonthWise = findViewById(R.id.recyclerMonthWise)

        recyclerMonthWise.layoutManager = LinearLayoutManager(this)

        tvYear.text = currentYear.toString()

        // Load data
        loadMonthData(currentYear)

        // Click listeners for year navigation
        btnPrevYear.setOnClickListener {
            currentYear--
            tvYear.text = currentYear.toString()
            loadMonthData(currentYear)
        }

        btnNextYear.setOnClickListener {
            currentYear++
            tvYear.text = currentYear.toString()
            loadMonthData(currentYear)
        }
    }

    private fun loadMonthData(year: Int) {
        monthList.clear()

        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        var totalForYear = 0.0

        for (i in 1..12) {
            val totalForMonth = dbHelper.getTotalForMonth(i, year)
            if (totalForMonth > 0) {
                monthList.add(MonthData(months[i - 1], totalForMonth))
                totalForYear += totalForMonth
            }
        }

        adapter = MonthWiseAdapter(monthList)
        recyclerMonthWise.adapter = adapter
        tvTotalAmount.text = " ₹%.2f".format(totalForYear)
    }
}
