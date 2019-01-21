package com.experiment.scope.data

import android.os.Handler
import com.experiment.scope.data.repository.UsersRepository
import com.experiment.scope.listeners.UpdateVehicleLocationListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class VehicleLocationUpdateRunnable(
    private val userId: Int,
    private val repository: UsersRepository,
    private val handler: Handler,
    private var refreshInterval: Long,
    private val listener: UpdateVehicleLocationListener
) : Runnable {
    private val compositeDisposable = CompositeDisposable()

    override fun run() {
        handler.postDelayed(this, refreshInterval)

        val disposable = repository.getUserVehiclesLocation(userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ vehiclesLocation ->
                listener.onVehicleLocationUpdate(vehiclesLocation)
            }, { })

        compositeDisposable.add(disposable)
    }

    fun clear() {
        compositeDisposable.clear()
    }

}