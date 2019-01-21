package com.experiment.scope.listeners

import com.experiment.scope.data.model.vehiclelocation.VehicleLocation

interface UpdateVehicleLocationListener {
    fun onVehicleLocationUpdate(vehicleLocations: ArrayList<VehicleLocation>)
}