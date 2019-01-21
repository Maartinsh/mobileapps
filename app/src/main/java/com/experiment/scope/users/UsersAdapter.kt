package com.experiment.scope.users

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.experiment.scope.R
import com.experiment.scope.data.model.user.User
import com.experiment.scope.listeners.UserClickListener

class UsersAdapter(
    private val list: ArrayList<User>,
    private val listener: UserClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        holder = UsersViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(viewType, parent, false)
        )

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (position >= 0) {
                    listener.onClick(position)
                }
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = list[position]

        when (holder) {
            is UsersViewHolder -> {
                holder.bind(user)
            }
        }
    }

    override fun getItemCount(): Int = list.size
    override fun getItemViewType(position: Int): Int = R.layout.item_user_card
}