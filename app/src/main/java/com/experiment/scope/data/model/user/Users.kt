package com.experiment.scope.data.model.user

import com.google.gson.annotations.SerializedName

data class Users (
    @SerializedName("data") var data: List<User>? = null
)
