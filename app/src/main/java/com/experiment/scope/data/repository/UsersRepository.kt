package com.experiment.scope.data.repository

import android.app.Activity
import android.content.Context
import com.experiment.scope.MainActivity
import com.experiment.scope.SYNCHRONIZATION_THRESHOLD_FOR_USERS
import com.experiment.scope.data.SharedPreferencesManager
import com.experiment.scope.data.model.Vehicle
import com.experiment.scope.data.model.user.User
import com.experiment.scope.data.model.vehiclelocation.VehicleLocation
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmResults
import retrofit2.converter.gson.GsonConverterFactory

class UsersRepository(
    activity: Activity,
    private val sharedPreferencesManager: SharedPreferencesManager
) : Repository(activity) {
    private val retrofit = retrofitBuilder
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val usersService = retrofit.create(UsersService::class.java)

    fun getUsers(): Single<ArrayList<User>> {
        val cachedUsers = getCachedUsers()

        return if (cachedUsers.isEmpty()) {
            getUsersFromApi()
        } else {
            if (shouldSynchronizeUsers()) {
                getUsersFromApi()
            } else {
                Single.just(cachedUsers)
            }
        }
    }

    fun getUserVehiclesLocation(
        userId: Int
    ): Single<ArrayList<VehicleLocation>> {
        val cachedVehiclesLocation = getCachedVehiclesLocation()

        return if (cachedVehiclesLocation.isEmpty()) {
            getVehiclesLocationFromApi(userId)
        } else {
            Single.just(cachedVehiclesLocation)
        }
    }

    fun clearVehiclesLocationCache() {
        Realm.getDefaultInstance().use { realmInstance ->
            realmInstance.executeTransaction { realm ->
                val vehiclesLocation: RealmResults<VehicleLocation> =
                    realm.where(VehicleLocation::class.java)
                        .findAll()
                vehiclesLocation.deleteAllFromRealm()
            }
        }
    }

    fun getVehicleInfo(vehicleId: Int): Vehicle? {
        var vehicle: Vehicle? = null
        Realm.getDefaultInstance().use { realmInstance -> realmInstance.executeTransaction { realm ->
                val realmResult: Vehicle? =
                    realm.copyFromRealm(realm
                        .where(Vehicle::class.java)
                        .equalTo("vehicleid", vehicleId)
                        .findFirst()
                    )
                vehicle = realmResult
            }
        }

        return vehicle
    }

    private fun getVehiclesLocationFromApi(
        userId: Int
    ): Single<ArrayList<VehicleLocation>> {
        return usersService.getVehicleLocation(userId)
            .retry(5)
            .map { json ->
                val locations = ArrayList<VehicleLocation>()

                json.data?.forEach { location ->
                    locations.add(location)
                }

                cacheVehiclesLocations(locations)
                return@map locations
            }
    }

    private fun getUsersFromApi(): Single<ArrayList<User>> {
        return usersService.getUsers()
            .retry(5)
            .map { json ->
                val users = ArrayList<User>()

                json.data?.forEach { user ->
                    users.add(user)
                }

                cacheUsers(users)
                return@map users
            }
    }

    private fun cacheVehiclesLocations(locations: ArrayList<VehicleLocation>) {
        Realm.getDefaultInstance().use { realmInstance ->
            realmInstance.executeTransaction { realm ->
                val oldLocations: RealmResults<VehicleLocation> =
                    realm.where(VehicleLocation::class.java)
                        .findAll()
                oldLocations.deleteAllFromRealm()

                realm.insertOrUpdate(locations)
            }
        }
    }

    private fun cacheUsers(users: ArrayList<User>) {
        Realm.getDefaultInstance().use { realmInstance ->
            realmInstance.executeTransaction { realm ->
                val oldUsers: RealmResults<User> =
                    realm.where(User::class.java)
                        .findAll()
                oldUsers.deleteAllFromRealm()

                realm.insertOrUpdate(users)
                sharedPreferencesManager.setPreviousUsersSyncTime(System.currentTimeMillis())
            }
        }
    }

    private fun getCachedUsers(): ArrayList<User> {
        val users: ArrayList<User> = ArrayList()
        Realm.getDefaultInstance().use { realmInstance -> realmInstance.executeTransaction { realm ->
            val realmResults: List<User> =
                realm.copyFromRealm(realm
                    .where(User::class.java)
                    .findAll())
            users.addAll(realmResults)
            }
        }

        return if (users.isEmpty()) {
            ArrayList()
        } else {
            users
        }
    }

    private fun getCachedVehiclesLocation(): ArrayList<VehicleLocation> {
        val locations: ArrayList<VehicleLocation> = ArrayList()
        Realm.getDefaultInstance().use { realmInstance -> realmInstance.executeTransaction { realm ->
                val realmResults: List<VehicleLocation> =
                    realm.copyFromRealm(realm
                        .where(VehicleLocation::class.java)
                        .findAll())
                locations.addAll(realmResults)
            }
        }

        return if (locations.isEmpty()) {
            ArrayList()
        } else {
            locations
        }
    }

    private fun shouldSynchronizeUsers(): Boolean {
        val previousSyncTime = sharedPreferencesManager.getPreviousUsersSyncTime()
        val difference = System.currentTimeMillis() - previousSyncTime

        return getCachedUsers().isEmpty()
                || difference > SYNCHRONIZATION_THRESHOLD_FOR_USERS
    }
}