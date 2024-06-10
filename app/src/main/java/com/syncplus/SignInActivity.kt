package com.syncplus

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var imageViewTogglePassword: ImageView
    private var isPasswordVisible: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Facebook Login
        callbackManager = CallbackManager.Factory.create()

        // Initialize EditText fields
        editTextName = findViewById(R.id.editTextUser)
        editTextPassword = findViewById(R.id.editTextPassword)

        imageViewTogglePassword = findViewById(R.id.imageViewTogglePassword)

        imageViewTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                // Hide Password
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                imageViewTogglePassword.setImageResource(R.drawable.eye) // The eye icon drawable
            } else {
                // Show Password
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imageViewTogglePassword.setImageResource(R.drawable.eye_off) // The eye-off icon drawable
            }
            isPasswordVisible = !isPasswordVisible
            // Move the cursor to the end of the text
            editTextPassword.setSelection(editTextPassword.text.length)
        }

        // Set onClickListener for Sign In button
        val buttonSignIn: Button = findViewById(R.id.buttonSignIn)
        buttonSignIn.setOnClickListener {
            if (validateForm()) {
                signInWithUsernamePassword()
            }
        }

        // Set onClickListener for Google Sign-In button
        val buttonGoogle: Button = findViewById(R.id.buttonGoogle)
        buttonGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Set onClickListener for Facebook Login button
        val buttonFacebook: Button = findViewById(R.id.buttonFacebook)
        buttonFacebook.setOnClickListener {
            signInWithFacebook()
        }

        // Set onClickListener for the Back icon
        val imageViewBack: ImageView = findViewById(R.id.imageView_back)
        imageViewBack.setOnClickListener {
            onBackPressed() // Navigate back to the previous screen
        }
    }

    private fun validateForm(): Boolean {
        val username: String = editTextName.text.toString().trim()
        val password: String = editTextPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun signInWithUsernamePassword() {
        val username: String = editTextName.text.toString().trim()
        val password: String = editTextPassword.text.toString().trim()

        // Retrieve the email associated with the username from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Username not found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val email = documents.first().getString("email") ?: ""
                if (email.isEmpty()) {
                    Toast.makeText(this, "Email not found for this username.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, navigate to MainActivity or appropriate activity
                            startActivity(Intent(this@SignInActivity, LandingActivity::class.java))
                            finish() // Finish SignInActivity to prevent returning to it
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext, "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
    }

    private fun signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // Facebook Login successful, navigate to appropriate activity
                    startActivity(Intent(this@SignInActivity, ProjectActivity::class.java))
                    finish() // Finish current activity to prevent going back
                }

                override fun onCancel() {
                    // Handle canceled Facebook Login
                }

                override fun onError(error: FacebookException?) {
                    // Handle Facebook Login error
                    Toast.makeText(this@SignInActivity, "Facebook Login failed", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            // Google Sign-In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                // Handle the case where Google sign-in was successful but account is null
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }

        } else {
            // Handle Facebook Login result
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI and navigate to appropriate activity
                    startActivity(Intent(this@SignInActivity, LogActivity::class.java))
                    finish() // Finish SignInActivity to prevent returning to it
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN_GOOGLE = 9001
    }
}