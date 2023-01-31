package com.example.gmapmarker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gmapmarker.MainFragment.Companion.lat
import com.example.gmapmarker.MainFragment.Companion.lng
import com.example.gmapmarker.databinding.FragmentMapsBinding
import com.example.gmapmarker.dto.MarkerDataEntity
import com.example.gmapmarker.extentions.icon
import com.example.gmapmarker.objects.CustomDeleteMode
import com.example.gmapmarker.viewmodel.MarkerViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.cameraPosition
import com.google.maps.android.ktx.utils.collection.addMarker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : Fragment() {
    private lateinit var binding: FragmentMapsBinding
    private var target = LatLng(55.621074, 37.744693)
    private lateinit var googleMap: GoogleMap
    private lateinit var manager: MarkerManager
    private val listMarkerCollection = mutableListOf<MarkerManager.Collection>()
    private var markerPosition: LatLng? = null
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                googleMap.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        lastLocation = location
                        putMarker(currentLatLng, manager, "My Location")
                    }
                }
            }
        }


    @Inject
    lateinit var customDeleteMode: CustomDeleteMode
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return binding.root
    }

    private val viewModel: MarkerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycleScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()
            googleMap.apply {
                isTrafficEnabled = true
                isBuildingsEnabled = true
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    setAllGesturesEnabled(true)
                }

            }
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    googleMap.apply {
                        isMyLocationEnabled = true
                        uiSettings.isMyLocationButtonEnabled = true
                        uiSettings.isRotateGesturesEnabled = true
                        uiSettings.isMapToolbarEnabled = true
                    }
                    LocationServices.getFusedLocationProviderClient(requireContext())
                        .lastLocation.addOnSuccessListener {
                            locationZoom(it)
                        }
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(
                        requireContext(),
                        "shouldShowRequestPermissionRationale",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }

            }
            manager = MarkerManager(googleMap)
            googleMap.setOnMapClickListener { latLng ->
                dialogPutMarker(latLng, manager)
            }
            markerControl()
            customDeleteMode()
            dialogClearMarkers(googleMap)
            showListMarkers()

        }

    }

    private fun locationZoom(lastLocation: Location) {
        val latLng =
            LatLng(
                arguments?.lat?.toDouble() ?: lastLocation.latitude,
                arguments?.lng?.toDouble() ?: lastLocation.longitude
            )
        target = latLng
        lifecycleScope.launchWhenCreated {
            googleMap.awaitAnimateCamera(CameraUpdateFactory.newCameraPosition(
                cameraPosition {
                    target(target)
                    zoom(20F)
                }
            ))
        }
    }

    private fun showListMarkers() {
        binding.showListMarkers.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_mainFragment)
        }
    }

    private fun markerControl() {
        lifecycleScope.launchWhenCreated {
            viewModel.markers.collectLatest { list ->
                googleMap.clear()
                if (list.isNotEmpty()) {
                    list.map { marker ->
                        putMarkerOnCreate(marker, manager)
                        customMarkerDelete(marker)
                    }
                }
            }
        }
    }

    private fun customDeleteMode() {
        if (customDeleteMode.deleteModeStateFlow.value == true) {
            binding.deleteModeTint.setColorFilter(Color.argb(255, 235, 76, 89))
            binding.deleteModeIcon.isChecked = false
        }else{
            binding.deleteModeIcon.isChecked = true
        }
        binding.deleteMode.setOnClickListener {
            customDeleteMode.setDeleteMode(binding.deleteModeIcon.isChecked)
            if (customDeleteMode.deleteModeStateFlow.value == true) {
                binding.deleteModeTint.setColorFilter(Color.argb(255, 235, 76, 89))
                binding.deleteModeIcon.isChecked = false
            } else {
                binding.deleteModeTint.setColorFilter(Color.argb(255, 131, 137, 150))
                binding.deleteModeIcon.isChecked = true
            }
        }
    }


    private fun putMarker(latLng: LatLng, manager: MarkerManager, desc: String) {
        lifecycleScope.launchWhenCreated {
            try {
                val geo = Geocoder(requireContext())
                val address = geo.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val collection = manager.newCollection().apply {
                    addMarker {
                        position(latLng)
                        title("${address?.component1()?.thoroughfare} ${address?.component1()?.featureName}")
                        snippet(desc)
                        icon(
                            requireNotNull(
                                getDrawable(
                                    requireContext(),
                                    R.drawable.netology_marker
                                )
                            )
                        )
                    }.apply {
                        viewModel.insert(
                            MarkerDataEntity(
                                id = 0,
                                title = "${address?.component1()?.thoroughfare}/${address?.component1()?.featureName}",
                                lat = latLng.latitude,
                                lng = latLng.longitude,
                                description = desc
                            )
                        )
                    }
                }
                listMarkerCollection.add(collection)
            }catch (e: java.lang.Exception){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dialogClearMarkers(googleMap: GoogleMap) {
        val dialogBinding =
            layoutInflater.inflate(R.layout.dialog_for_clear_markers, null)
        val dialog = Dialog(requireContext())
        lifecycleScope.launchWhenCreated {
            viewModel.markers.collectLatest { list ->
                binding.clearAll.setOnClickListener {
                    if (list.isNotEmpty()) {
                        dialog.apply {
                            setContentView(dialogBinding)
                            setTitle(R.string.description)
                            setCancelable(true)
                            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            val text = dialogBinding.findViewById<TextView>(R.id.attentionText)
                            text.text = getString(R.string.are_you_wanted_remove_this_marker)
                            val yes = dialogBinding.findViewById<Button>(R.id.yes)
                            val no = dialogBinding.findViewById<Button>(R.id.no)
                            yes.setOnClickListener {
                                clearAllMarkers(googleMap)
                                this.cancel()
                            }
                            no.setOnClickListener {
                                this.cancel()
                            }
                        }.also {
                            it.show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "You are have not markers",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }

    private fun dialogPutMarker(latLng: LatLng, manager: MarkerManager) {
        val dialogBinding =
            layoutInflater.inflate(R.layout.dialog_screen_for_input_description, null)
        val dialog = Dialog(requireContext())
        dialog.apply {
            setContentView(dialogBinding)
            setTitle(R.string.description)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val desc = dialogBinding.findViewById<EditText>(R.id.desc_in)
            val save = dialogBinding.findViewById<Button>(R.id.save)
            val cancelBtn = dialogBinding.findViewById<Button>(R.id.cancel)
            desc.requestFocus()
            save.setOnClickListener {
                putMarker(latLng, manager, desc.text.toString())
                desc.clearFocus()
                desc.text.clear()
                this.cancel()
            }
            cancelBtn.setOnClickListener {
                desc.clearFocus()
                desc.text.clear()
                this.cancel()
            }
        }.also {
            it.show()
        }
    }

    private fun putMarkerOnCreate(markerDataEntity: MarkerDataEntity, manager: MarkerManager) {
        lifecycleScope.launchWhenCreated {
            val latLng = LatLng(markerDataEntity.lat, markerDataEntity.lng)
            val collection = manager.newCollection().apply {
                addMarker {
                    position(latLng)
                    title(markerDataEntity.title)
                    snippet(markerDataEntity.description)
                    icon(
                        requireNotNull(
                            getDrawable(
                                requireContext(),
                                R.drawable.netology_marker
                            )
                        )
                    )
                }
            }
            listMarkerCollection.clear()
            listMarkerCollection.add(collection)
        }
    }

    private fun customMarkerDelete(
        markerDataEntity: MarkerDataEntity,
    ) {
        listMarkerCollection.map { collection ->
            collection.setOnMarkerClickListener { marker ->
                marker.isDraggable = true
                if (customDeleteMode.deleteModeStateFlow.value == true) {
                    if (markerDataEntity.lat == marker.position.latitude && markerDataEntity.lng == marker.position.longitude) {
                        viewModel.removeById(markerDataEntity.id)
                        listMarkerCollection.clear()
                        marker.remove()
                    }
                } else {
                    collection.setOnMarkerDragListener(object : OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker) {
                            marker.hideInfoWindow()
                            Log.d("onMarkerDrag", marker.position.toString())
                        }

                        override fun onMarkerDragEnd(marker: Marker) {
                            val geo = Geocoder(requireContext())
                            val address = geo.getFromLocation(
                                marker.position.latitude,
                                marker.position.longitude,
                                1
                            )
                            marker.title =
                                "${address?.component1()?.thoroughfare} ${address?.component1()?.featureName}"
                            val thisMarkerPosition = marker.position
                            viewModel.updateById(
                                markerDataEntity.id,
                                marker.title ?: "",
                                thisMarkerPosition.latitude,
                                thisMarkerPosition.longitude,
                                markerDataEntity.description ?: ""
                            )
                            marker.showInfoWindow()
                        }

                        override fun onMarkerDragStart(marker: Marker) {
                            markerPosition = marker.position
                        }
                    })
                    marker.showInfoWindow()
                }
                true
            }
        }
    }

    private fun clearAllMarkers(googleMap: GoogleMap) {
        googleMap.clear()
        viewModel.clearAll()
    }


}