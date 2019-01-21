package com.experiment.scope.data.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class Owner(
    @SerializedName("name") var name: String? = null,
    @SerializedName("surname") var surname: String? = null,
    @SerializedName("foto") var foto: String? = null
) : RealmObject()




