package com.rashed.CafeAdab

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val splashScreenDuration: Long = 3000 // 3 seconds
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_FIRST_NAME = "firstName"
        private const val KEY_USER_LAST_NAME = "lastName"
        private const val KEY_USER_PHONE = "phoneNumber"
        private const val KEY_USER_CLASS = "userClass"
        private const val KEY_USER_MAJOR = "userMajor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, splashScreenDuration)
    }

    private fun checkLoginStatus() {
        // Check if the user is logged in
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

        if (isLoggedIn) {
            // If the user is logged in, check if user data is stored locally
            val firstName = sharedPreferences.getString(KEY_USER_FIRST_NAME, null)
            if (firstName == null) {
                // If user data is not found, fetch from Firebase
                fetchUserDataFromFirebase()
            } else {
                // If user data is found, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            // If not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchUserDataFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val phoneNumber = document.getString("phoneNumber")
                        val userClass = document.getString("userClass")
                        val userMajor = document.getString("userMajor")

                        // Save the data locally
                        val editor = sharedPreferences.edit()
                        editor.putString(KEY_USER_FIRST_NAME, firstName)
                        editor.putString(KEY_USER_LAST_NAME, lastName)
                        editor.putString(KEY_USER_PHONE, phoneNumber)
                        editor.putString(KEY_USER_CLASS, userClass)
                        editor.putString(KEY_USER_MAJOR, userMajor)
                        editor.apply()

                        // Go to MainActivity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Handle the case where the user data is not found in Firestore
                        startActivity(Intent(this, UserInfoActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener {
                    // Handle the error
                    startActivity(Intent(this, UserInfoActivity::class.java))
                    finish()
                }
        }
    }
}
