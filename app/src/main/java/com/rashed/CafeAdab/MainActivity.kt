package com.rashed.CafeAdab

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Setup toolbar and drawer

        // Set window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://cafeadab-6b721-default-rtdb.europe-west1.firebasedatabase.app")

        // Get the TextView from the layout
        val userInfoTextView = findViewById<TextView>(R.id.userInfoTextView)


    fun fetchAndDisplayUserInfo(userInfoTextView: TextView) {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val userRef = database.getReference("users/$uid")
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java)
                    val lastName = snapshot.child("lastName").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val paye = snapshot.child("paye").getValue(String::class.java)
                    val reshte = snapshot.child("reshte").getValue(String::class.java)

                    // Display user info in the TextView
                    val userInfo = "First Name: $firstName\n" +
                            "Last Name: $lastName\n" +
                            "Phone: $phone\n" +
                            "Paye: $paye\n" +
                            "Reshte: $reshte"

                    userInfoTextView.text = userInfo
                } else {
                    userInfoTextView.text = "User data not found."
                }
            }.addOnFailureListener { exception ->
                userInfoTextView.text = "Failed to load user data: ${exception.message}"
            }
        } else {
            userInfoTextView.text = "User not logged in!"
        }
    }
}}
