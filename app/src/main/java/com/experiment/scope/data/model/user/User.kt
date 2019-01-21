package com.experiment.scope.data.model.user

import com.experiment.scope.data.model.Owner
import com.experiment.scope.data.model.Vehicle
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject

open class User (
    @SerializedName("userid") var userid: Int = 0,
    @SerializedName("owner") var owner: Owner? = null,
    @SerializedName("vehicles") var vehicles: RealmList<Vehicle>? = null
) : RealmObject()
