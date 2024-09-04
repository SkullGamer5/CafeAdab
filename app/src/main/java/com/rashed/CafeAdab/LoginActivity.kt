package com.rashed.CafeAdab

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

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
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<ImageButton>(R.id.loginButton)
        val signupTextView = findViewById<TextView>(R.id.signupTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null && user.isEmailVerified) {
                                fetchUserDataFromFirebase(user.uid)
                                val editor = sharedPreferences.edit()
                                editor.putBoolean(KEY_IS_LOGGED_IN, true)
                                editor.apply()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                auth.signOut()
                                Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        signupTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun fetchUserDataFromFirebase(userId: String) {
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
                    editor.putBoolean(KEY_IS_LOGGED_IN, true)
                    editor.putString(KEY_USER_FIRST_NAME, firstName)
                    editor.putString(KEY_USER_LAST_NAME, lastName)
                    editor.putString(KEY_USER_PHONE, phoneNumber)
                    editor.putString(KEY_USER_CLASS, userClass)
                    editor.putString(KEY_USER_MAJOR, userMajor)
                    editor.apply()

                    // Navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Handle the case where user data is not found
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
            }
    }
}
