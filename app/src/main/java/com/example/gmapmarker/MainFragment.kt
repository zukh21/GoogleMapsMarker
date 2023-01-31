package com.example.gmapmarker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gmapmarker.adapter.CustomMarkerAdapter
import com.example.gmapmarker.databinding.FragmentMainBinding
import com.example.gmapmarker.dto.MarkerDataEntity
import com.example.gmapmarker.interfaces.ItemListener
import com.example.gmapmarker.objects.DataTransferArg
import com.example.gmapmarker.viewmodel.MarkerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainFragment : Fragment(), ItemListener {
    private lateinit var binding: FragmentMainBinding
    private lateinit var markerAdapter: CustomMarkerAdapter
    private val vModel: MarkerViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        markerAdapter = CustomMarkerAdapter(this)
        binding.rcView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launchWhenCreated {
            binding.rcView.apply {
                adapter = markerAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        binding.rcView.context,
                        DividerItemDecoration.VERTICAL
                    )
                )
            }
            vModel.markers.collectLatest {
                markerAdapter.submitList(it)
            }
        }
    }

    override fun markerItemOnClick(markerDataEntity: MarkerDataEntity) {
        findNavController().navigate(R.id.action_mainFragment_to_mapsFragment, Bundle().apply {
            lat = markerDataEntity.lat.toString()
            lng = markerDataEntity.lng.toString()
        })
    }

    override fun markerUpdate(markerDataEntity: MarkerDataEntity, description: String) {
        vModel.updateById(markerDataEntity.id, markerDataEntity.title ?: "Your marker", markerDataEntity.lat, markerDataEntity.lng, description)
    }

    companion object {
        var Bundle.lat: String? by DataTransferArg
        var Bundle.lng: String? by DataTransferArg
    }
}

