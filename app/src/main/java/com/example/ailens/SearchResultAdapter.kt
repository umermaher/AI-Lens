package com.example.ailens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.search_item.view.*

class SearchResultAdapter(
    private val context: Context,
    private val searchList:List<SearchResult>,
    private val listener: OnSearchItemClickListener
    ) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model=searchList[position]
        holder.itemView.searchTitle.text=model.title
        holder.itemView.searchSnippet.text=model.link
        holder.itemView.searchDescription.text=model.snippet

        holder.itemView.setOnClickListener {
            listener.onClickListener(position)
        }
    }

    interface OnSearchItemClickListener{
        fun onClickListener(position: Int)
    }

    override fun getItemCount(): Int = searchList.size

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        init {

        }
    }
}