package com.rashed.CafeAdab

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SpinnerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var payeSpinner: Spinner
    private lateinit var reshteSpinner: Spinner
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spinner)

        auth = FirebaseAuth.getInstance()

        payeSpinner = findViewById(R.id.payeSpinner)
        reshteSpinner = findViewById(R.id.reshteSpinner)
        saveButton = findViewById(R.id.saveButton)

        // Set up the spinners
        val payeOptions = arrayOf("دهم", "یازدهم", "دوازدهم")
        val reshteOptions = arrayOf("ریاضی", "انسانی", "تجربی")

        val payeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, payeOptions)
        payeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        payeSpinner.adapter = payeAdapter

        val reshteAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reshteOptions)
        reshteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reshteSpinner.adapter = reshteAdapter

        // Save the selected data
        saveButton.setOnClickListener {
            val selectedPaye = payeSpinner.selectedItem.toString()
            val selectedReshte = reshteSpinner.selectedItem.toString()

            saveUserData(selectedPaye, selectedReshte)
        }
    }

    private fun saveUserData(paye: String, reshte: String) {
        val userId = auth.currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://cafeadab-6b721-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userId!!)

        // Save to Firebase
        val userData = mapOf(
            "paye" to paye,
            "reshte" to reshte
        )
        database.updateChildren(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Save to SharedPreferences
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("paye", paye)
                    putString("reshte", reshte)
                    apply()
                }

                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()

                // Move to the next activity or finish the setup
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to save data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
