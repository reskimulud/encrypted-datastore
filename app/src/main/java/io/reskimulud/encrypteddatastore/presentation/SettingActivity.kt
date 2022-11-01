package io.reskimulud.encrypteddatastore.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.reskimulud.encrypteddatastore.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Setting"
    }
}