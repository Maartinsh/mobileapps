package com.experiment.scope.users

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.experiment.scope.R
import com.experiment.scope.data.model.user.User
import com.squareup.picasso.Picasso


class UsersViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val ownerNameSurnameTextView = itemView.findViewById<TextView>(R.id.ownerNameSurname)
    private val carModelTextView = itemView.findViewById<TextView>(R.id.carInfo)
    private val ownerImage = itemView.findViewById<ImageView>(R.id.ownerImage)

    fun bind(user: User) {
        val ownerImageUrl = user.owner?.foto
        val nameSurname = "${user.owner?.name} ${user.owner?.surname}"

        if (user.vehicles != null && user.vehicles!!.isNotEmpty()) {
            val carModel = "${user.vehicles?.first()?.make} ${user.vehicles?.first()?.model}"
            setCarModelInfo(carModel)
        }

        setUserNameSurname(nameSurname)
        setImage(ownerImageUrl)
    }

    private fun setUserNameSurname(nameSurname: String) {
        ownerNameSurnameTextView.text = nameSurname
    }

    private fun setCarModelInfo(carModel: String) {
        carModelTextView.text = carModel
    }

    private fun setImage(imageUrl: String?) {
        Picasso.get()
            .load(imageUrl)
            .fit()
            .centerCrop()
            .placeholder(R.drawable.default_placeholder)
            .error(R.drawable.default_placeholder)
            .into(ownerImage)
    }

}