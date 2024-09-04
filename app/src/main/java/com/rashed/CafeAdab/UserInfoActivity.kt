package com.rashed.CafeAdab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserInfoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://cafeadab-6b721-default-rtdb.europe-west1.firebasedatabase.app")

        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val submitButton = findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && phone.isNotEmpty()) {
                saveUserInfo(firstName, lastName, phone)
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserInfo(firstName: String, lastName: String, phone: String) {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            Log.d("UserInfoActivity", "UID found: $uid")
            val userRef = database.getReference("users/$uid")
            val userInfo = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phone" to phone
            )

            userRef.setValue(userInfo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("UserInfoActivity", "Information saved successfully!")
                    Toast.makeText(this, "Information saved!", Toast.LENGTH_SHORT).show()
                    // Move to the next activity
                    val intent = Intent(this, SpinnerActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("UserInfoActivity", "Failed to save information: ${task.exception?.message}")
                    Toast.makeText(this, "Failed to save information: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("UserInfoActivity", "Error: ${exception.message}")
            }
        } else {
            Log.e("UserInfoActivity", "User not logged in!")
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

}
