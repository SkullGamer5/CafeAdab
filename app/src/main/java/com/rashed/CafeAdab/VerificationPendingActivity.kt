package com.rashed.CafeAdab

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class VerificationPendingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var countdownTextView: TextView
    private lateinit var resendButton: Button
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_pending)

        auth = FirebaseAuth.getInstance()

        countdownTextView = findViewById(R.id.countdownTextView)
        resendButton = findViewById(R.id.resendVerificationButton)

        startCountdown()

        resendButton.setOnClickListener {
            sendVerificationEmail()
            startCountdown()  // Restart the countdown after resending
        }

        startVerificationCheckLoop()
    }

    private fun startCountdown() {
        resendButton.isEnabled = false  // Disable resend button during countdown
        object : CountDownTimer(60000, 1000) {  // 60 seconds timer

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                countdownTextView.text = "Resend available in ${millisUntilFinished / 1000}s"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                countdownTextView.text = "You can resend the email now."
                resendButton.isEnabled = true  // Enable resend button
            }
        }.start()
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser
        user?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startVerificationCheckLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                auth.currentUser?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            Toast.makeText(this@VerificationPendingActivity, "Email verified!", Toast.LENGTH_SHORT).show()
                            // Move to UserInfoActivity
                            val intent = Intent(this@VerificationPendingActivity, UserInfoActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            handler.postDelayed(this, 3000) // Check again after 3 seconds
                        }
                    } else {
                        handler.postDelayed(this, 3000) // Check again after 3 seconds
                    }
                }
            }
        }, 3000) // Initial delay before first check
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the handler when the activity is destroyed
    }
}
