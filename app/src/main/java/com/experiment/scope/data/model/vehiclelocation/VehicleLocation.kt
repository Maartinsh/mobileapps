package com.experiment.scope.data.model.vehiclelocation

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class VehicleLocation (
    @SerializedName("vehicleid") var vehicleid: Int = 0,
    @SerializedName("lat") var lat: Float = 0.toFloat(),
    @SerializedName("lon") var lon: Float = 0.toFloat()
) : RealmObject()