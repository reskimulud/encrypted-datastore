package io.reskimulud.encrypteddatastore.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import io.github.reskimulud.encrypteddatastore.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var factory: ViewModelFactory
    private val viewModel: UserViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ViewModelFactory.getInstance(this)

        setupObserver()

        binding.btnSubmit.setOnClickListener {
            onSubmitHandler()
        }

        supportActionBar?.title = "Setting"
    }

    private fun setupObserver() {
        viewModel.userName.observe(this) {
            if (it.isNotEmpty()) binding.etName.setText(it)
        }
        viewModel.userEmail.observe(this) {
            if (it.isNotEmpty()) binding.etEmail.setText(it)
        }
        viewModel.userApiKey.observe(this) {
            if (it.isNotEmpty()) binding.etApiKey.setText(it)
        }
    }

    private fun onSubmitHandler() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val apiKey = binding.etApiKey.text.toString().trim()

        viewModel.apply {
            setUserName(name)
            setUserEmail(email)
            setUserApiKey(apiKey)
        }

        startActivity(
            Intent(
                this@SettingActivity,
                MainActivity::class.java
            )
        )
        Toast.makeText(this@SettingActivity, "Field Updated!", Toast.LENGTH_SHORT).show()
    }
}