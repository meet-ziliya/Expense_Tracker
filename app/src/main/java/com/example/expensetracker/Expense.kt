package com.example.expensetracker

data class Expense(
    val date: String,
    val category: String,
    val amount: Double,
    val description: String
)
