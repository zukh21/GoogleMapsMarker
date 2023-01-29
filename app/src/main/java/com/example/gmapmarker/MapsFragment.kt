package com.example.gmapmarker

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gmapmarker.databinding.FragmentMapsBinding
import com.example.gmapmarker.dto.MarkerDataEntity
import com.example.gmapmarker.extentions.icon
import com.example.gmapmarker.viewmodel.MarkerViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
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

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMarkerClickListener {
    private lateinit var binding: FragmentMapsBinding
    private val target = LatLng(55.600249, 37.720411)
    private lateinit var googleMap: GoogleMap
    private val listMarkerCollection = mutableListOf<MarkerManager.Collection>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val viewModel: MarkerViewModel by viewModels()
    private val markersList = mutableListOf<Marker>()

    @SuppressLint("PotentialBehaviorOverride")
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

            val manager = MarkerManager(googleMap)
            lifecycleScope.launchWhenCreated {
                viewModel.markers.collectLatest { list ->
                    if (list.isNotEmpty()) {
                        list.map { marker ->
                            putMarkerOnCreate(marker, manager)
                                listMarkerCollection.map { collection ->
                                    customMarkerDelete(collection, marker)
                                }

                        }
                    }
                }
            }
            lifecycleScope.launchWhenCreated {
                viewModel.markers.collectLatest { list ->
                    binding.clearAll.setOnClickListener {
                        if (list.isNotEmpty()) {
                            dialogClearMarkers(googleMap)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "You are have not any markers",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }



            googleMap.setOnMapClickListener { latLng ->
                dialogPutMarker(latLng, manager)
            }
            googleMap.awaitAnimateCamera(CameraUpdateFactory.newCameraPosition(
                cameraPosition {
                    target(target)
                    zoom(15F)
                }
            ))
        }
    }

    private fun putMarker(latLng: LatLng, manager: MarkerManager, desc: String) {
        lifecycleScope.launchWhenCreated {
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
                    markersList.add(this)
                    tag = listOf(latLng, "position")
                }
            }
            listMarkerCollection.add(collection)
        }
    }

    private fun dialogClearMarkers(googleMap: GoogleMap) {
        val dialogBinding =
            layoutInflater.inflate(R.layout.dialog_for_clear_markers, null)
        val dialog = Dialog(requireContext())
        dialog.apply {
            setContentView(dialogBinding)
            setTitle(R.string.description)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
            customMarkerDelete(collection, markerDataEntity)

        }
    }

    private fun customMarkerDelete(
        collection: MarkerManager.Collection,
        markerDataEntity: MarkerDataEntity,
    ) {
        collection.setOnMarkerClickListener {
            viewModel.removeById(markerDataEntity.id)
            it.remove()

            true
        }
    }

    private fun clearAllMarkers(googleMap: GoogleMap) {
        googleMap.clear()
        viewModel.clearAll()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.remove()
        return true
    }
}