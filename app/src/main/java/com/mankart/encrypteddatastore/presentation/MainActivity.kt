package com.mankart.encrypteddatastore.presentation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mankart.encrypteddatastore.databinding.ActivityMainBinding

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "users_data")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var factory: ViewModelFactory
    private val viewModel: UserViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ViewModelFactory.getInstance(this)

        val etEmail = binding.etEmail

        binding.btnSubmit.setOnClickListener {
            if (etEmail.text?.isBlank() == false) {
                viewModel.setUserEmail(etEmail.text.toString())
            }
        }
        binding.btnLoad.setOnClickListener {
            viewModel.userEmail.observe(this) {
                binding.tvEmail.text = it
            }
        }
    }
}