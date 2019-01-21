package com.experiment.scope.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class Vehicle (
    @SerializedName("vehicleid") var vehicleid: Int = 0,
    @SerializedName("make") var make: String? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("year") var year: String? = null,
    @SerializedName("color") var color: String? = null,
    @SerializedName("vin") var vin: String? = null,
    @SerializedName("foto") var foto: String? = null
) : RealmObject()