package com.experiment.scope.data

import android.os.Handler
import com.experiment.scope.data.repository.UsersRepository

class VehicleLocationsExpirationRunnable(
    private val repository: UsersRepository,
    private val handler: Handler,
    private var refreshInterval: Long
) : Runnable {

    override fun run() {
        handler.postDelayed(this, refreshInterval)
        repository.clearVehiclesLocationCache()
    }

}