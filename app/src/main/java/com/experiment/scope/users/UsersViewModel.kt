package com.experiment.scope.users

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.location.Geocoder
import com.experiment.scope.SingleLiveEvent
import com.experiment.scope.data.model.user.User
import com.experiment.scope.data.model.vehiclelocation.VehicleLocation
import com.experiment.scope.data.repository.UsersRepository
import com.experiment.scope.data.model.VehicleInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class UsersViewModel(application: Application) : AndroidViewModel(application) {
    private var _users = MutableLiveData<ArrayList<User>>()
    val users: LiveData<ArrayList<User>>
        get() = _users
    private var _clickedUser = MutableLiveData<Int>()
    val clickedUser: LiveData<Int>
        get() = _clickedUser
    private var _vehicleLocations = MutableLiveData<ArrayList<VehicleInfo>>()
    val vehicleLocations: LiveData<ArrayList<VehicleInfo>>
        get() = _vehicleLocations
    private val _showGenericErrorMessage = SingleLiveEvent<Void>()
    val showGenericErrorMessage: LiveData<Void>
        get() = _showGenericErrorMessage
    private val _showProgressBarLoader = SingleLiveEvent<Void>()
    val showProgressBarLoader: LiveData<Void>
        get() = _showProgressBarLoader
    private val _hideProgressBarLoader = SingleLiveEvent<Void>()
    val hideProgressBarLoader: LiveData<Void>
        get() = _hideProgressBarLoader
    private val _showLoadingDialog = SingleLiveEvent<Void>()
    val showLoadingDialog: LiveData<Void>
        get() = _showLoadingDialog
    private val _hideLoadingDialog = SingleLiveEvent<Void>()
    val hideLoadingDialog: LiveData<Void>
        get() = _hideLoadingDialog
    private val userList = ArrayList<User>()
    private val disposables = CompositeDisposable()

    private lateinit var usersRepository: UsersRepository

    fun init(
        usersRepository: UsersRepository
    ) {
        this.usersRepository = usersRepository
        getUsers()
    }

    fun onUserClicked(position: Int) {
        val clickedUser = userList[position]
        _clickedUser.value = clickedUser.userid

        _showLoadingDialog.call()
        val disposable = usersRepository.getUserVehiclesLocation(clickedUser.userid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ locations ->
                if (locations.isNullOrEmpty()) {
                    onGenericError()
                } else {
                    val vehicles = transformVehiclesInfo(locations)
                    onOpenMapFragment(vehicles)
                }
            }, {
                onGenericError()
            })

        disposables.add(disposable)
    }

    private fun transformVehiclesInfo(
        vehicleLocations: ArrayList<VehicleLocation>
    ): ArrayList<VehicleInfo> {
        val vehicles = ArrayList<VehicleInfo>()

        vehicleLocations.forEach {
            val vehicle = usersRepository.getVehicleInfo(it.vehicleid)
            val currentAddress = ""

            if (vehicle != null) {
                val vehicleMarker = VehicleInfo(
                    it.vehicleid,
                    vehicle.foto!!,
                    "${vehicle.make!!} ${vehicle.model!!}",
                    currentAddress,
                    vehicle.color!!,
                    it
                )

                vehicles.add(vehicleMarker)
            }
        }

        return vehicles
    }

    private fun getUsers() {
        _showProgressBarLoader.call()
        val disposable = usersRepository.getUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ users ->
                userList.clear()

                if (users.isNullOrEmpty()) {
                    onGenericError()
                } else {
                    onShowUsers(users)
                }
            }, {
                onGenericError()
            })

        disposables.add(disposable)
    }

    fun getVehicleCurrentAddress(
        geoCoder: Geocoder,
        latitude: Float,
        longitude: Float,
        fallbackInfo: String
    ): String {
        val addresses = geoCoder.getFromLocation(
            latitude.toDouble(),
            longitude.toDouble(),
            1
        )

        return if (addresses != null && addresses.isNotEmpty()) {
            val addressThoroughfare = addresses[0].thoroughfare
            val addressSubThoroughfare = addresses[0].subThoroughfare

            if (addressThoroughfare == null || addressSubThoroughfare == null) {
                fallbackInfo
            } else {
                "$addressThoroughfare, $addressSubThoroughfare"
            }
        } else {
            fallbackInfo
        }
    }

    private fun onShowUsers(users: ArrayList<User>) {
        _hideProgressBarLoader.call()
        userList.addAll(users)
        _users.value = users
    }

    private fun onOpenMapFragment(vehicles: ArrayList<VehicleInfo>) {
        _hideLoadingDialog.call()
        _vehicleLocations.value = vehicles
    }

    private fun onGenericError() {
        _hideLoadingDialog.call()
        _hideProgressBarLoader.call()
        _showGenericErrorMessage.call()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}