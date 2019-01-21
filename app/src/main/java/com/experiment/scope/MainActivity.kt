package com.experiment.scope

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import com.experiment.scope.data.SharedPreferencesManager
import com.experiment.scope.data.VehicleLocationsExpirationRunnable
import com.experiment.scope.data.repository.UsersRepository
import com.experiment.scope.map.MapFragment
import com.experiment.scope.users.UsersFragment
import io.realm.Realm
import io.realm.RealmConfiguration

class MainActivity : AppCompatActivity() {
    var tabLayout: TabLayout? = null
    private var expirationRunnable: VehicleLocationsExpirationRunnable? = null
    private var runnableHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = this.findViewById(R.id.tabsRoot)

        initializeRealm()
        initRunnable()
        openUsersView()
    }

    override fun onResume() {
        super.onResume()
        runnableHandler?.post(expirationRunnable)
    }

    override fun onPause() {
        super.onPause()
        runnableHandler?.removeCallbacks(expirationRunnable)
    }

    override fun onStop() {
        super.onStop()
        runnableHandler?.removeCallbacks(expirationRunnable)
    }

    override fun onBackPressed() {
        val mapFragment = supportFragmentManager
            .findFragmentByTag(MapFragment.MAP_FRAGMENT_TAG) as MapFragment

        if (mapFragment.isVisible) {
            openUsersView()
        } else {
            moveTaskToBack(true)
        }
    }

    private fun initRunnable() {
        if (this.mainLooper != null) {
            runnableHandler = Handler(this.mainLooper)
            val sharedPreferencesManager = SharedPreferencesManager(this)

            expirationRunnable = VehicleLocationsExpirationRunnable(
                UsersRepository(
                    this,
                    sharedPreferencesManager
                ),
                runnableHandler!!,
                SYNCHRONIZATION_THRESHOLD_FOR_VEHICLES_LOCATION
            )
        }
    }

    private fun initializeRealm() {
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .name("scope.realm")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.getInstance(realmConfiguration)
        Realm.setDefaultConfiguration(realmConfiguration)
    }

    private fun openUsersView() {
        createFragment(
            supportFragmentManager,
            UsersFragment.createInstance(),
            UsersFragment.USERS_FRAGMENT_TAG)
    }
}
