package io.reskimulud.encrypteddatastore.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
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
        viewModel.userPhoneNumber.observe(this) {
            if (it.isNotEmpty()) binding.etPhoneNumber.setText(it)
        }
        viewModel.userApiKey.observe(this) {
            if (it.isNotEmpty()) binding.etApiKey.setText(it)
        }
    }

    private fun onSubmitHandler() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val apiKey = binding.etApiKey.text.toString().trim()

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            viewModel.apply {
                setUserName(name)
                setUserEmail(email)
                setUserPhoneNumber(phoneNumber)
                setUserApiKey(apiKey)
            }

            finish()
            Toast.makeText(this@SettingActivity, "Field Updated!", Toast.LENGTH_SHORT).show()
        } else {
            binding.etEmail.error = "Email not Valid!"
        }
    }
}