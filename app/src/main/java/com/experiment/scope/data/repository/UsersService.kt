package com.experiment.scope.data.repository

import com.experiment.scope.data.model.user.Users
import com.experiment.scope.data.model.vehiclelocation.VehicleLocations
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface UsersService {
    companion object {
        const val GET_USERS = "?op=list"
        const val GET_VEHICLE_LOCATIONS = "?op=getlocations"
    }

    @GET(GET_USERS)
    fun getUsers(): Single<Users>

    @GET(GET_VEHICLE_LOCATIONS)
    fun getVehicleLocation(
        @Query("userid") userId: Int
    ): Single<VehicleLocations>

}