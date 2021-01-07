package com.example.setgame

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth


class MainActivity : Activity(), View.OnClickListener {

    private lateinit var gameId : Integer

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

    @SuppressLint("ResourceType")
    fun joinGame(){
        this.setContentView(R.layout.join_layout)
        this.findViewById<Button>(R.id.joinButton).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.tag == "join"){
                joinGame()
            }
            else if (v.tag == "joinGame"){
                gameId = Integer(findViewById<EditText>(R.id.gameId).text.toString())
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                intent.putExtra("gameId", gameId)
                startActivity(intent)
            }
            else{
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                startActivity(intent)
            }
        }


    }


}