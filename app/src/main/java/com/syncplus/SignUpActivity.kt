package com.syncplus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var checkBoxPrivacy: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize EditText fields
        editTextName = findViewById(R.id.editTextName)
        editTextUsername = findViewById(R.id.editTextUser)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        checkBoxPrivacy = findViewById(R.id.checkBox)

        // Set onClickListener for Sign Up button
        val buttonSignUp: Button = findViewById(R.id.buttonSignUp)
        buttonSignUp.setOnClickListener {
            if (validateForm()) {
                signUpWithEmailPassword()
            }
        }

        // Set onCheckedChangeListener for Privacy Policy CheckBox
        checkBoxPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.privacypolicies.com/live/ad2b95d7-cba1-4eb9-a6eb-c053d780de9a")))
            }
        }
    }

    private fun validateForm(): Boolean {
        val name: String = editTextName.text.toString().trim()
        val username: String = editTextUsername.text.toString().trim()
        val email: String = editTextEmail.text.toString().trim()
        val password: String = editTextPassword.text.toString().trim()

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun signUpWithEmailPassword() {
        val username: String = editTextUsername.text.toString().trim()
        val email: String = editTextEmail.text.toString().trim()
        val password: String = editTextPassword.text.toString().trim()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save the username and email mapping in Firestore
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email
                    )
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            Log.d("SignUpActivity", "User data saved successfully")
                            // Show a success message
                            Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show()
                            // Navigate to SignInActivity after a short delay
                            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                            finish() // Finish SignUpActivity to prevent returning to it
                        }
                        .addOnFailureListener { e ->
                            Log.e("SignUpActivity", "Error saving user data: ${e.message}")
                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val errorMessage = task.exception?.message ?: "Authentication failed."
                    if (errorMessage.contains("email address is already in use")) {
                        Toast.makeText(this, "Email already in use. Please sign in.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                    } else {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
