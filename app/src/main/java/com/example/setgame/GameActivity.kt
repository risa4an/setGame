package com.example.setgame

import android.app.Activity
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaCodec
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class GameActivity : Activity(), View.OnClickListener {
    lateinit var id: Integer
    @RequiresApi(Build.VERSION_CODES.R)
    var game = Game()
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.game_activity)
        val db = FirebaseDatabase.getInstance()
        db.getReference().setValue("games")
        database = db.getReference("games")
        val arguments = intent.extras
        if (arguments != null){
            val et = EditText(this)
            et.tag = "gameId"
            val button = Button(this)
            button.text = "Join"
            button.tag = "join"
            val ll = LinearLayout(this)
            ll.addView(et)
            ll.addView(button)
            this.findViewById<ConstraintLayout>(R.id.relativeLayout).addView(ll)
        }
        else{
            game = Game()
            var Rand = Random(100)
            game.id = Integer.parseInt(SimpleDateFormat("yyyyMMddSS").format(Date())) * 100 + Rand.nextInt();
            id = Integer(game.id!!)
            FirebaseAuth.getInstance().currentUser?.let { game.joinGame(it) }
            database.child(game.id.toString()).setValue(game)
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    game = dataSnapshot.child(game.id.toString()).getValue() as Game
                    if (game.cards.size > 0) {
                        changeCards()
                    }
                    else {
                        getScores()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            database.addValueEventListener(postListener)
            createField()
        }


    }

    fun createField(){
        val layout = TableLayout(this)
        layout.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT)
        layout.setPadding(1, 1, 1, 1)
        var cards: List<ImageButton?>
        for (i in 0..3) {
            val tr = TableRow(this)
            tr.setLayoutParams(
                    TableRow.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT,
                            1.0f
                    )
            )
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)

            val width = (metrics.widthPixels - 5) / 3;
            val height = width / 1.81;

            for (j in 0..2) {
                val temp = ImageButton(this)
                val cardCode = game.popCard()
                var bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("android.resource://com.example.setgame/drawable/c$cardCode"));
                bmp = Bitmap.createScaledBitmap(bmp, width, height.toInt(), true);
                temp.id = cardCode!!;
                temp.setBackgroundColor(Color.WHITE);
                //     temp.setLayoutParams(ViewGroup.LayoutParams (2000, 3000))
                temp.setImageBitmap(bmp)
                temp.setOnClickListener(this);

                tr.addView(temp, j)
            }
            layout.addView(tr, i)
        }
        this.findViewById<ConstraintLayout>(R.id.relativeLayout).addView(layout)
    }

    fun changeCards(){
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = (metrics.widthPixels - 5) / 3;
        val height = width / 1.81;
        for(card in game.previousChosenCards){
            val temp = this.findViewById<ImageButton>(card)
            val newCard = game.popCard();
            var bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("android.resource://com.example.setgame/drawable/c$newCard"));
            bmp = Bitmap.createScaledBitmap(bmp, width, height.toInt(), true);
            temp.setImageBitmap(bmp);
            temp.setBackgroundColor(Color.WHITE)
            temp.id = newCard!!
        }
    }

    fun getScores(){
        val scoresLayout = findViewById<LinearLayout>(R.id.scoresLayout)
        for (scores in game.scores){
            val tv = TextView(this)
            tv.setText(scores.key + " -> " + scores.value)
            scoresLayout.addView(tv)
        }
        findViewById<ConstraintLayout>(R.id.relativeLayout).addView(scoresLayout)
    }


    override fun onClick(v: View) {
        if (v.tag == "gameId"){
            id = Integer(findViewById<EditText>(v.id).text.toString())
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    game = dataSnapshot.child(id.toString()).value as Game
                    if (game.cards.size > 0) {
                        changeCards()
                    }
                    else {
                        getScores()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            database.addValueEventListener(postListener)
            createField()
            return
        }
        val flag =  game.chooseCard(v.id as Int?);
        if (flag)
            v.setBackgroundColor(Color.RED);
        else
            v.setBackgroundColor(Color.WHITE);
        if (game.chosenCards.size == 3){
            if (FirebaseAuth.getInstance().currentUser?.let { game.checkSet(it) }!!){
                database.child(game.id.toString()).setValue(game)
                changeCards()
            }

        }
    }


}


