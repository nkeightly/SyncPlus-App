package com.syncplus



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var networkReceiver: NetworkReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        networkReceiver = NetworkReceiver()

        val textView4: TextView = findViewById(R.id.textView4)
        val signInButton: Button = findViewById(R.id.button)

        // Create a SpannableString for the clickable text
        val spannableString = SpannableString(textView4.text)

        // Set the clickable span for "Create new Account"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle click action, navigate to SignUpActivity
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            }
        }

        // Apply the clickable span to the specified range of characters
        spannableString.setSpan(clickableSpan, 14, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the color for the clickable text
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.blueClickable)),
            14,
            30,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the SpannableString to the TextView
        textView4.text = spannableString
        textView4.movementMethod = LinkMovementMethod.getInstance()

        // Set onClickListener for the "Sign in" button
        signInButton.setOnClickListener {
            // Handle sign-in action, navigate to SignInActivity
            startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    override fun onStop() {
        super.onStop()
        // Sign out the user when the app is closed or put into the background
        FirebaseAuth.getInstance().signOut()
    }

    inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                Toast.makeText(context, "Back online! Syncing data...", Toast.LENGTH_SHORT).show()
                // Firestore will automatically sync data, you can also add your sync logic here if needed
            }
        }
    }



}
