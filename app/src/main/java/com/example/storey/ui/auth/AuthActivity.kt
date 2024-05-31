package com.example.storey.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.storey.R
import com.example.storey.databinding.ActivityAuthBinding
import com.example.storey.ui.auth.loginregister.LoginFragment

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.auth, LoginFragment())
                .commit()
        }
    }
}