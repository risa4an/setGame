package com.example.setgame

import android.app.Activity
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
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
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import com.google.firebase.database.DatabaseReference;


class GameActivity : Activity(), View.OnClickListener {


    lateinit var game : Game
    lateinit var gameTemp : Game
    lateinit var gameId : Integer
    lateinit var cardsArray : List<Int>
    var extraRows = 0
    private lateinit var databaseGame: DatabaseReference
    private lateinit var scoresLayout: LinearLayout
    private lateinit var scoresText: TextView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.game_activity)

        databaseGame = Firebase.database.reference
        val arguments = intent.extras

        if (arguments != null){
            gameId = arguments["gameId"] as Integer
        }
        else {
            game = Game()
            game.id =
                Integer.parseInt(SimpleDateFormat("yyyyMMdd").format(Date())) * 100 + Random.nextInt(0, 100);
            gameId = Integer(game.id!!)

            //FirebaseAuth.getInstance().currentUser?.let { game.joinGame(it) }
            //databaseGame = Firebase.database.getReference("games/$gameId")
           // databaseGame.setValue(game)

            //createField()
        }
        databaseGame = databaseGame.child("games").child(gameId.toString())
        databaseGame.addValueEventListener(listener)
        if (arguments == null){
            FirebaseAuth.getInstance().currentUser?.let { game.joinGame(it) }
            databaseGame.setValue(game)
            createField()
        }
        else {
            val name = FirebaseAuth.getInstance().currentUser?.displayName
            if (name != null) {
                databaseGame.child("scores").child(name).setValue(0)
            }
        }


    }




//    fun createListener(){
//        databaseGame = Firebase.database.getReference("games/$gameId")
//        if (game.id != null) {
//            databaseGame.setValue(game)
//        }
//        databaseGame.addValueEventListener(listener)
//
//        val name = FirebaseAuth.getInstance().currentUser?.displayName
//        if (name != null) {
//            databaseGame.child("scores").child(name).setValue(0)
//        }
//        databaseGame.child("test").setValue(0)
//        createField()
//    }

    private val listener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            gameTemp = dataSnapshot.getValue<Game>()!!
            if(!(::game.isInitialized)){
                game = gameTemp
                createField()
            }
            else if (gameTemp.previousChosenCards.isNotEmpty() && game.previousChosenCards.isNotEmpty()) {
                if (gameTemp.previousChosenCards[0] != game.previousChosenCards[0]) {
                    game = gameTemp
                    changeCards()
                }
            }
            else if (gameTemp.previousChosenCards.isNotEmpty() && game.previousChosenCards.isEmpty()) {

                    game = gameTemp
                    changeCards()
            }

            else if(gameTemp.cards.size == 0){
                getScores()
            }
            if (gameTemp.extraRows != game.extraRows || game.extraRows != extraRows){
                game = gameTemp
                addExtraRow()
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    fun createField(){
        this.setContentView(R.layout.game_activity)
        findViewById<TextView>(R.id.textView2).setText(gameId.toString())
        scoresLayout = findViewById<LinearLayout>(R.id.scoresTable)
        scoresText = TextView(this)
        var temp = ""
        for(v in game.scores.keys.toList()){
            temp = temp + v.toString() + " -> "+ game.scores[v].toString() + "\n"
        }
        scoresText.setText(temp)
        scoresLayout.addView(scoresText)
        val layout = findViewById<TableLayout>(R.id.tableLayout)
//        layout.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT)
//        layout.setPadding(3, 3, 3, 3)
        cardsArray = List(0, {0})
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

            val width = (metrics.widthPixels - 30) / 3
            val height = (width / 1.81).toInt();

            for (j in 0..2) {
                val temp = ImageButton(this)
                val cardCode = game.popCard()
                var bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("android.resource://com.example.setgame/drawable/c$cardCode"));
                bmp = Bitmap.createScaledBitmap(bmp, width, height.toInt(), true);
                temp.id = cardCode!!;
                temp.setBackgroundColor(Color.WHITE);
                temp.setImageBitmap(bmp)
                temp.setPadding(2, 2, 2, 2)
                temp.setOnClickListener(this)
                tr.addView(temp, j)
            }
            layout.addView(tr, i)
        }
        findViewById<Button>(R.id.addCards).setOnClickListener(this)
        //this.findViewById<ConstraintLayout>(R.id.relativeLayout).addView(layout)
    }

    fun changeCards(){
        var temp = ""
        for(v in game.scores.keys.toList()){
            temp = temp + v.toString() + " -> "+ game.scores[v].toString() + "\n"
        }
        scoresText.setText(temp)
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

    fun addExtraRow(){
        extraRows = game.extraRows
        val layout = findViewById<TableLayout>(R.id.tableLayout)
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

        val width = (metrics.widthPixels - 30) / 3
        val height = (width / 1.81).toInt();
        for (j in 0..2) {
            val temp = ImageButton(this)
            val cardCode = game.popCard()
            var bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse("android.resource://com.example.setgame/drawable/c$cardCode"));
            bmp = Bitmap.createScaledBitmap(bmp, width, height.toInt(), true);
            temp.id = cardCode!!;
            temp.setBackgroundColor(Color.WHITE);
            temp.setImageBitmap(bmp)
            temp.setPadding(2, 2, 2, 2)
            temp.setOnClickListener(this)
            tr.addView(temp, j)
        }
        layout.addView(tr, extraRows + 3)
    }



    override fun onClick(v: View) {
        if (v.tag == "addCards"){
            game.extraRows = game.extraRows + 1
            databaseGame.setValue(game)
            return
        }
        val flag =  game.chooseCard(v.id as Int?);
        if (flag)
            v.setBackgroundColor(Color.RED);
        else
            v.setBackgroundColor(Color.WHITE);
        if (game.chosenCards.size == 3){
            if (FirebaseAuth.getInstance().currentUser?.let { game.checkSet(it) }!!){
                databaseGame.setValue(game)
                changeCards()
            }

        }
    }
}


