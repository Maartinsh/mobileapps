package com.experiment.scope.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.experiment.scope.*
import com.experiment.scope.R
import com.experiment.scope.data.SharedPreferencesManager
import com.experiment.scope.data.VehicleLocationUpdateRunnable
import com.experiment.scope.data.model.VehicleInfo
import com.experiment.scope.data.model.vehiclelocation.VehicleLocation
import com.experiment.scope.data.repository.UsersRepository
import com.experiment.scope.listeners.UpdateVehicleLocationListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_map.*
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class MapFragment : Fragment(), OnMapReadyCallback, UpdateVehicleLocationListener {
    private var vehicles = ArrayList<VehicleInfo>()
    private var markersOnMap = HashMap<Int, MarkerOptions>()

    private lateinit var googleMap: GoogleMap
    private var showVehicleInformation = false
    private var drawRoute = false
    private var userId = 0

    private var origin: LatLng? = null
    private var destination: LatLng? = null
    private var runnableHandler: Handler? = null
    private var locationRequest: LocationRequest? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationUpdateRunnable: VehicleLocationUpdateRunnable? = null
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if (locationResult == null) {
                return
            }
            for (location in locationResult.locations) {
                if (location != null) {
                    origin = LatLng(location.latitude, location.longitude)
                    drawPath()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkUserPermissions()
        setLocationClient()
        setLocationUpdateRunnable()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        runnableHandler?.post(locationUpdateRunnable)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        runnableHandler?.removeCallbacks(locationUpdateRunnable)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        runnableHandler?.removeCallbacks(locationUpdateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUpdateRunnable?.clear()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        setMapStyle()
        setMarkers()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCurrentLocation()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.message_permission_denied,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onVehicleLocationUpdate(vehicleLocations: ArrayList<VehicleLocation>) {
        markersOnMap.forEach { marker ->
            val vehicleId = marker.key

            vehicleLocations.forEach { singleVehicleLocation ->
                if (vehicleId == singleVehicleLocation.vehicleid) {
                    val newLocation = com.google.android.gms.maps.model.LatLng(
                        singleVehicleLocation.lat.toDouble(),
                        singleVehicleLocation.lon.toDouble()
                    )

                    marker.value.position(newLocation)
                }
            }
        }
    }

    private fun drawPath() {
        if (drawRoute && origin != null) {
            val now = DateTime()

            val results = DirectionsApi.newRequest(getGeoContext())
                .mode(TravelMode.DRIVING)
                .origin(origin)
                .destination(destination)
                .departureTime(now)
                .await()

            addPolyline(results)
        }
    }

    private fun addPolyline(results: DirectionsResult) {
        val decodedPath = PolyUtil.decode(
            results.routes[0].overviewPolyline.encodedPath
        )

        googleMap.addPolyline(
            PolylineOptions()
                .addAll(decodedPath)
                .width(2F)
                .color(requireContext().resources.getColor(R.color.colorPrimary))
        )
    }

    private fun getGeoContext(): GeoApiContext {
        return GeoApiContext()
            .setQueryRateLimit(3)
            .setApiKey(GOOGLE_API_KEY)
            .setConnectTimeout(1, TimeUnit.SECONDS)
            .setReadTimeout(1, TimeUnit.SECONDS)
            .setWriteTimeout(1, TimeUnit.SECONDS)
    }

    private fun setLocationClient() {
        fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = TimeUnit.MINUTES.toMillis(5)
    }

    private fun setLocationUpdateRunnable() {
        if (requireActivity().mainLooper != null) {
            runnableHandler = Handler(requireActivity().mainLooper)
            val sharedPreferencesManager = SharedPreferencesManager(requireContext())

            locationUpdateRunnable = VehicleLocationUpdateRunnable(
                userId,
                UsersRepository(
                    requireActivity(),
                    sharedPreferencesManager
                ),
                runnableHandler!!,
                UPDATE_THRESHOLD_FOR_VEHICLES_LOCATION,
                this
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun checkUserPermissions() {
        if (ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
                requestUserPermission()
        } else {
            requestCurrentLocation()
        }
    }

    private fun requestUserPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            LOCATION_REQUEST_CODE
        )
    }

    private fun setMarkers() {
        if (vehicles.isNotEmpty()) {
            googleMap.setOnMarkerClickListener(markerClickListener)

            vehicles.forEachIndexed { position, vehicleMarker ->
                val location = com.google.android.gms.maps.model.LatLng(
                    vehicleMarker.location.lat.toDouble(),
                    vehicleMarker.location.lon.toDouble()
                )

                val markerColor = markerColors[position]
                val markerOption = MarkerOptions()
                    .position(location)
                    .alpha(0.7f)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))

                markersOnMap[vehicleMarker.vehicleId] = markerOption

                val marker = googleMap.addMarker(markerOption)
                marker.tag = position

                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(marker.position, MAP_ZOOM_LEVEL)
                )
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.style_json
                )
            )

            if (!success) {
                Log.e(MAP_FRAGMENT_TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(MAP_FRAGMENT_TAG, "Can't find style. Error: ", e)
        }
    }

    private fun initializeViews(savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).tabLayout?.getTabAt(0)?.text =
                getString(R.string.tab_title_map)

        findRouteButton.setOnClickListener(findRouteClickListener)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun showVehicleInfo(vehicleInfo: VehicleInfo) {
        vehicleInfoContainer.visibility = View.VISIBLE

        vehicleModel.text = vehicleInfo.name
        currentAddress.text = vehicleInfo.currentAddress
        vehicleColor.text = String.format(
            getString(R.string.vehicle_information_container_color, vehicleInfo.color)
        )

        Picasso.get()
            .load(vehicleInfo.image)
            .fit()
            .centerCrop()
            .placeholder(R.drawable.default_placeholder)
            .error(R.drawable.default_placeholder)
            .into(vehicleImage)
    }

    private fun hideVehicleInfo() {
        vehicleInfoContainer.visibility = View.GONE
    }

    @SuppressLint("MissingPermission")
    private val findRouteClickListener = View.OnClickListener {
        val position = it.tag as Int
        val vehicleMarker = vehicles[position]

        destination = LatLng(
            vehicleMarker.location.lat.toDouble(),
            vehicleMarker.location.lon.toDouble()
        )

        if (isLocationEnabled(requireContext())) {
            drawRoute = true
            requestCurrentLocation()
        } else {
            drawRoute = false
            Toast.makeText(
                requireContext(),
                R.string.message_enable_location,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val markerClickListener = GoogleMap.OnMarkerClickListener { marker ->
        val position = marker.tag as Int
        val vehicleMarker = vehicles[position]
        showVehicleInformation = !showVehicleInformation

        if (showVehicleInformation) {
            showVehicleInfo(vehicleMarker)
        } else {
            hideVehicleInfo()
        }

        findRouteButton.tag = position
        return@OnMarkerClickListener false
    }

    companion object {
        const val MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG"
        const val MAP_ZOOM_LEVEL = 14F
        const val LOCATION_REQUEST_CODE = 1000

        @JvmStatic
        fun createInstance(
            userId: Int,
            vehicleLocations: ArrayList<VehicleInfo>
        ): MapFragment {
            val fragment = MapFragment()
            fragment.userId = userId
            fragment.vehicles.addAll(vehicleLocations)
            return fragment
        }
    }

    private val markerColors = arrayListOf(
        BitmapDescriptorFactory.HUE_AZURE,
        BitmapDescriptorFactory.HUE_BLUE,
        BitmapDescriptorFactory.HUE_CYAN,
        BitmapDescriptorFactory.HUE_GREEN,
        BitmapDescriptorFactory.HUE_MAGENTA,
        BitmapDescriptorFactory.HUE_ORANGE,
        BitmapDescriptorFactory.HUE_RED,
        BitmapDescriptorFactory.HUE_ROSE,
        BitmapDescriptorFactory.HUE_VIOLET
    )
}