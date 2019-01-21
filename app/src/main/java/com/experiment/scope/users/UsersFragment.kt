package com.experiment.scope.users

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.experiment.scope.LoadingProgressDialog
import com.experiment.scope.MainActivity
import com.experiment.scope.R
import com.experiment.scope.createFragment
import com.experiment.scope.data.SharedPreferencesManager
import com.experiment.scope.data.model.user.User
import com.experiment.scope.data.repository.UsersRepository
import com.experiment.scope.listeners.UserClickListener
import com.experiment.scope.map.MapFragment
import com.experiment.scope.data.model.VehicleInfo
import kotlinx.android.synthetic.main.fragment_users.*
import java.util.*
import kotlin.collections.ArrayList


class UsersFragment : Fragment(), UserClickListener {
    private var loadingDialog: LoadingProgressDialog? = null
    private var adapter: UsersAdapter? = null
    private var viewModel: UsersViewModel? = null
    private var userId: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingProgressDialog(requireContext())
        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        val usersRepository = UsersRepository(
            requireActivity(),
            sharedPreferencesManager
        )

        viewModel = ViewModelProviders
            .of(this)
            .get(UsersViewModel::class.java)
        viewModel?.init(usersRepository)

        setupObservables()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
    }

    override fun onClick(position: Int) {
         viewModel?.onUserClicked(position)
    }

    private fun setupObservables() {
        viewModel?.users?.observe(this, Observer { users ->
            if (users != null) {
                showUsers(users)
            } else {
                showGenericErrorMessage()
            }
        })
        viewModel?.vehicleLocations?.observe(this, Observer { vehicleMarkers ->
            if (vehicleMarkers != null) {
                val markers =
                    transformVehicleMarkerCurrentAddress(vehicleMarkers)

                openMapFragment(markers)
            } else {
                showGenericErrorMessage()
            }
        })
        viewModel?.clickedUser?.observe(this, Observer { clickedUserId ->
            if (clickedUserId != null) {
                userId = clickedUserId
            }
        })
        viewModel?.showGenericErrorMessage?.observe(this, Observer {
            showGenericErrorMessage()
        })
        viewModel?.showProgressBarLoader?.observe(this, Observer {
            onShowProgressBarLoader()
        })
        viewModel?.hideProgressBarLoader?.observe(this, Observer {
            onHideProgressBarLoader()
        })
        viewModel?.showLoadingDialog?.observe(this, Observer {
            onShowLoadingDialog()
        })
        viewModel?.hideLoadingDialog?.observe(this, Observer {
            onHideLoadingDialog()
        })
    }

    private fun transformVehicleMarkerCurrentAddress(
        vehicleInfos: ArrayList<VehicleInfo>
    ): ArrayList<VehicleInfo> {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val markers = ArrayList<VehicleInfo>()

        vehicleInfos.forEach {
            val currentAddress = viewModel?.getVehicleCurrentAddress(
                geoCoder,
                it.location.lat,
                it.location.lon,
                getString(R.string.vehicle_information_container_no_address)
            )

            if (currentAddress != null) {
                it.currentAddress = currentAddress
            }

            markers.add(it)
        }

        return markers
    }

    private fun initializeViews() {
        usersList.visibility = View.VISIBLE
        usersList.layoutManager = GridLayoutManager(requireContext(), 2)
        (requireActivity() as MainActivity).tabLayout?.getTabAt(0)?.text =
                getString(R.string.tab_title_users)
    }

    private fun initializeAdapter(users: ArrayList<User>) {
        adapter = UsersAdapter(users, this)
        usersList.adapter = adapter
        usersList.adapter.notifyDataSetChanged()
    }

    private fun showUsers(users: ArrayList<User>) {
        initializeAdapter(users)
    }

    private fun openMapFragment(
        vehicleInfos: ArrayList<VehicleInfo>
    ) {
        createFragment(
            requireActivity().supportFragmentManager,
            MapFragment.createInstance(userId, vehicleInfos),
            MapFragment.MAP_FRAGMENT_TAG)
    }

    private fun showGenericErrorMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.message_generic_error),
            Toast.LENGTH_SHORT).show()
    }

    private fun onShowProgressBarLoader() {
        loader.visibility = View.VISIBLE
    }

    private fun onHideProgressBarLoader() {
        loader.visibility = View.GONE
    }

    private fun onShowLoadingDialog() {
        loadingDialog?.show()
    }

    private fun onHideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    companion object {
        const val USERS_FRAGMENT_TAG = "USERS_FRAGMENT_TAG"

        @JvmStatic
        fun createInstance(): UsersFragment {
            return UsersFragment()
        }
    }
}