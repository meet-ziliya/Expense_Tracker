package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ExpenseTracker.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "expenses"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_DESC = "description"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_DATE TEXT," +
                "$COLUMN_CATEGORY TEXT," +
                "$COLUMN_AMOUNT REAL," +
                "$COLUMN_DESC TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert expense
    fun insertExpense(date: String, category: String, amount: Double, description: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_DESC, description)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    // Get all expenses
    fun getAllExpenses(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun getTotalForDate(date: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(amount) FROM expenses WHERE date = ?",
            arrayOf(date)
        )
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    fun getTotalForMonth(month: Int, year: Int): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT date, amount FROM expenses", null)
        var total = 0.0

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        while (cursor.moveToNext()) {
            val dateStr = cursor.getString(0)
            val amount = cursor.getDouble(1)

            try {
                val date = sdf.parse(dateStr)
                val cal = java.util.Calendar.getInstance()
                cal.time = date

                val expenseMonth = cal.get(java.util.Calendar.MONTH) + 1
                val expenseYear = cal.get(java.util.Calendar.YEAR)

                if (expenseMonth == month && expenseYear == year) {
                    total += amount
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        cursor.close()
        db.close()
        return total
    }

    fun getExpensesForDate(date: String): List<Expense> {
        val list = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM expenses WHERE date = ?", arrayOf(date))

        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                list.add(Expense(date, category, amount, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }



}
