package io.reskimulud.encrypteddatastore.presentation

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bumptech.glide.Glide
import io.github.reskimulud.encrypteddatastore.R
import io.github.reskimulud.encrypteddatastore.databinding.ActivityMainBinding

val Context.encryptedDataStore: DataStore<Preferences> by preferencesDataStore(name = "encrypted_users_data")
val Context.unencryptedDataStore: DataStore<Preferences> by preferencesDataStore(name = "unencrypted_users_data")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var factory: ViewModelFactory
    private val viewModel: UserViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ViewModelFactory.getInstance(this)

        setupObserver()

        supportActionBar?.title = "Profile"
    }

    private fun setupObserver() {
        viewModel.userName.observe(this) {
            binding.tvName.text = it

            Glide.with(this)
                .load("https://ui-avatars.com/api/?background=random&name=$it")
                .into(binding.ivProfile)
        }
        viewModel.userEmail.observe(this) {
            binding.tvEmail.text = it
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }
}