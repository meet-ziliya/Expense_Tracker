package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class MonthData(val monthName: String, val amount: Double)

class MonthWiseAdapter(private var monthList: List<MonthData>) :
    RecyclerView.Adapter<MonthWiseAdapter.MonthViewHolder>() {

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMonthName: TextView = itemView.findViewById(R.id.tvMonthName)
        val tvMonthAmount: TextView = itemView.findViewById(R.id.tvMonthAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_wise, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val item = monthList[position]
        holder.tvMonthName.text = item.monthName
        holder.tvMonthAmount.text = "₹ %.2f".format(item.amount)
    }

    override fun getItemCount(): Int = monthList.size
}
