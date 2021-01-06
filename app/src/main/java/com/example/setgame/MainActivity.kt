package com.example.setgame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity : Activity(), View.OnClickListener {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.setContentView(R.layout.activity_main)

// Create and launch sign-in intent
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), 1);
        }

        this.findViewById<TextView>(R.id.textView).setText(FirebaseAuth.getInstance().currentUser?.displayName);
        this.findViewById<Button>(R.id.button2).setOnClickListener(this)
        this.findViewById<Button>(R.id.button3).setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        val intent = Intent(this@MainActivity, GameActivity::class.java)
        if (v != null) {
            if (v.tag == "join"){
                intent.putExtra("join", 1)
            }
        }
        startActivity(intent)
    }


}


