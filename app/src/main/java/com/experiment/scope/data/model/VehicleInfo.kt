package com.experiment.scope.data.model

import com.experiment.scope.data.model.vehiclelocation.VehicleLocation

data class VehicleInfo (
    var vehicleId: Int,
    var image: String,
    var name: String,
    var currentAddress: String,
    var color: String,
    val location: VehicleLocation
)