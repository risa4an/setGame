package com.example.setgame

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class GameActivity : Activity(), RecyclerAdapter.OnCardClickListener {


    public lateinit var game : Game
    var extraRows = 0
    private lateinit var databaseGame: DatabaseReference
    private lateinit var scoresLayout: LinearLayout
    private lateinit var scoresText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var adapter : com.example.setgame.RecyclerAdapter
    private lateinit var cardsOnField : ArrayList<Int>


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.game_activity)

        databaseGame = Firebase.database.reference
        val arguments = intent.extras
        val gameId : Integer
        if (arguments != null){
            gameId = arguments["gameId"] as Integer
        }
        else {
            game = Game()
            game.id =
                Integer.parseInt(SimpleDateFormat("yyyyMMdd").format(Date())) * 100 + Random.nextInt(0, 100);
            gameId = Integer(game.id!!)

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


    private val listener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val gameTemp = dataSnapshot.getValue<Game>()!!
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
                val chosenCardsTemp = game.chosenCards
                game = gameTemp
                game.chosenCards = chosenCardsTemp
                addExtraRow()
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }


    fun createField(){
        this.setContentView(R.layout.game_activity)

        //RecyclerView + GridLayoutManager
        gridLayoutManager = GridLayoutManager(this, 3)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = gridLayoutManager

        //SCORES
        val textView = findViewById<TextView>(R.id.textView2)
        textView.setText(game.id.toString())
        textView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?){
                val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Copied", game.id.toString())
                clipboard.setPrimaryClip(clip)
            }
        })

        scoresLayout = findViewById<LinearLayout>(R.id.scoresTable)
        scoresText = TextView(this)
        var temp = ""
        for (v in game.scores.values.toList()){
            for (key in v.keys.toList()){
                temp = temp + key.toString() + " -> "+ v[key].toString() + "\n"
            }
        }
        scoresText.setText(temp)
        scoresLayout.addView(scoresText)
        cardsOnField = ArrayList()

        for (i in 0..11)
            cardsOnField.add(game.popCard()!!)

        adapter = RecyclerAdapter(cardsOnField, this, this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.addCards).setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                game.extraRows = game.extraRows + 1
                databaseGame.setValue(game)
            }})
    }


    fun changeCards(){
        for (card in game.previousChosenCards){
            cardsOnField.remove(card)
            cardsOnField.add(game.popCard()!!)
        }
        var temp = ""
        for (v in game.scores.values.toList()){
            for (key in v.keys.toList()){
                temp = temp + key.toString() + " -> "+ v[key].toString() + "\n"
            }
        }
        scoresText.setText(temp)
        adapter.notifyDataSetChanged()
    }


    fun getScores(){
        val scoresLayout = findViewById<LinearLayout>(R.id.scoresLayout)
        val tv = TextView(this)
        scoresLayout.addView(tv)
        var temp = ""
        for (v in game.scores.values.toList()){
            for (key in v.keys.toList()){
                temp = temp + key.toString() + " -> "+ v[key].toString() + "\n"
            }
        }
        tv.setText(temp)
        findViewById<ConstraintLayout>(R.id.relativeLayout).addView(scoresLayout)
    }


    fun addExtraRow(){
        extraRows = game.extraRows
        for(i in 0..2){
            cardsOnField.add(game.popCard()!!)
        }
        adapter.notifyDataSetChanged()
    }


    override fun onCardClick(cardId: Int) {
        val cardLayout = this.findViewById<ConstraintLayout>(cardId)
        val card = cardLayout.findViewById<ImageView>(R.id.imageView)
        val flag =  game.chooseCard(cardId);
        if (flag)
            card.setBackgroundColor(Color.RED);
        else
            card.setBackgroundColor(Color.WHITE);
        if (game.chosenCards.size == 3){
            if (FirebaseAuth.getInstance().currentUser?.let { game.checkSet(it) }!!){
                databaseGame.setValue(game)
                changeCards()
            }
        }
    }
}




