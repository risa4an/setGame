package com.example.setgame

import com.google.firebase.auth.FirebaseUser
import java.util.*

class Game {
    var scores: HashMap<String?, Int>
    var id: Int? = null
    var cards: MutableList<Int?>
    var chosenCards: MutableList<Int>
    var previousChosenCards: List<Int>
    fun popCard(): Int? {
        return if (cards.size > 0) {
            val card = cards[0]
            cards.removeAt(0)
            card
        } else null
    }

    val isEnd: Boolean
        get() = if (cards.isEmpty()) true else false

    fun checkSet(user: FirebaseUser): Boolean {
        val result = chosenCards[0] + chosenCards[1] + chosenCards[2]
        return if (result % 3 == 0) {
            previousChosenCards = ArrayList(chosenCards)
            chosenCards.clear()
            scores[user.displayName] = scores[user.displayName]!! + 1
            true
        } else false
    }

    fun chooseCard(card: Int?): Boolean {
        return if (chosenCards.contains(card)) {
            card?.let { chosenCards.remove(it) }
            false
        } else {
            card?.let { chosenCards.add(it) }
            true
        }
    }

    fun joinGame(user: FirebaseUser) {
        scores[user.displayName] = 0
    }

    init {
        scores = HashMap()
        cards = ArrayList()
        chosenCards = ArrayList()
        previousChosenCards = ArrayList()
        for (x1 in 1..3) {
            for (x2 in 1..3) {
                for (x3 in 1..3) {
                    for (x4 in 1..3) {
                        val temp = x1 * 1000 + x2 * 100 + x3 * 10 + x4
                        cards.add(temp)
                    }
                }
            }
        }
        Collections.shuffle(cards)
    }
}