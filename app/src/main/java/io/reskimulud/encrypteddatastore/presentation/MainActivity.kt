package io.reskimulud.encrypteddatastore.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bumptech.glide.Glide
import io.github.reskimulud.encrypteddatastore.R
import io.github.reskimulud.encrypteddatastore.databinding.ActivityMainBinding
import io.reskimulud.encrypteddatastore.data.datastore.PreferencesDataStore

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

        binding.btnShowDialog.setOnClickListener {
            EncryptDecryptProcessDialogFragment().show(supportFragmentManager, EncryptDecryptProcessDialogFragment::class.java.simpleName)
        }

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
        viewModel.imageUrl.observe(this) {
            Glide.with(this)
                .load(it)
                .into(binding.ivImage)
        }
        viewModel.userApiKey.observe(this) {
            viewModel.getRandomImage(it)
        }

        viewModel.loadingState.observe(this) { isLoading ->
            viewModel.message.observe(this) { message ->
                showMessageState(isLoading, message, binding.tvMessageLoading)
            }
        }
        viewModel.errorState.observe(this) { isError ->
            viewModel.message.observe(this) { message ->
                Log.e("MainActivity", "error: $isError, message: $message")
                showMessageState(isError, message, binding.tvMessageError)
            }
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
            R.id.phone_call -> {
                viewModel.userPhoneNumber.observe(this) {
                    if (it.isNotEmpty() && it != PreferencesDataStore.DEFAULT_VALUE) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Phone Number is empty", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            else -> false
        }
    }

    private fun showMessageState(state: Boolean, message: String, target: TextView) {
        if (state) {
            target.visibility = View.VISIBLE
            target.text = message
        } else {
            target.visibility = View.GONE
        }
    }
}