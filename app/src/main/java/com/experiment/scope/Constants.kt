package com.experiment.scope

import java.util.concurrent.TimeUnit

val SYNCHRONIZATION_THRESHOLD_FOR_USERS = TimeUnit.DAYS.toMillis(1)
val SYNCHRONIZATION_THRESHOLD_FOR_VEHICLES_LOCATION = TimeUnit.SECONDS.toMillis(30)
val UPDATE_THRESHOLD_FOR_VEHICLES_LOCATION = TimeUnit.MINUTES.toMillis(1)

const val GOOGLE_API_KEY = "AIzaSyAmPYjV9G22vzj0lGOQf395_45neT2zu2A"
