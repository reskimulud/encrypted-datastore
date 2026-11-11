/**
 * Copyright (c) 2022
 * Project  : Encrypted DataStore
 * Created by Reski Mulud Muchamad on 28-11-2022
 * GitHub   : https://github.com/reskimulud
 * LinkedIn : https://linkedin.com/in/reskimulud
 */

package io.reskimulud.encrypteddatastore.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.reskimulud.encrypteddatastore.R
import io.github.reskimulud.encrypteddatastore.databinding.DialogEncryptionDecryptionProcessBinding
import io.reskimulud.encrypteddatastore.data.UserProfile
import kotlinx.serialization.json.Json

class EncryptDecryptProcessDialogFragment: BottomSheetDialogFragment() {
    private var _binding: DialogEncryptionDecryptionProcessBinding? = null
    private val binding get() = _binding!!

    private lateinit var factory: ViewModelFactory
    private val viewModel: UserViewModel by activityViewModels { factory }

    private val json = Json { encodeDefaults = true }
    private var isDecrypted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEncryptionDecryptionProcessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        factory = ViewModelFactory.getInstance(requireActivity())
        viewModel.userProfile.observe(viewLifecycleOwner) {
            if (isDecrypted) showProfile(it)
        }
        viewModel.encryptedData.observe(viewLifecycleOwner) {
            if (!isDecrypted) binding.etDialogData.setText(it)
        }

        showEncrypted()

        binding.btnEncryptDecrypt.setOnClickListener {
            if (isDecrypted) {
                showEncrypted()
            } else {
                showDecrypted()
            }

            isDecrypted = !isDecrypted
        }
    }

    private fun showDecrypted() {
        viewModel.userProfile.value?.let { showProfile(it) }
        binding.btnEncryptDecrypt.text = resources.getString(R.string.encrypt)
    }

    private fun showProfile(profile: UserProfile) {
        binding.etDialogData.setText(json.encodeToString(UserProfile.serializer(), profile))
    }

    private fun showEncrypted() {
        viewModel.refreshEncryptedData()
        binding.btnEncryptDecrypt.text = resources.getString(R.string.decrypt)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
