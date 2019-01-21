package com.experiment.scope.data.model.vehiclelocation

import com.google.gson.annotations.SerializedName

open class VehicleLocations(
    @SerializedName("data") var data: List<VehicleLocation>? = null
)