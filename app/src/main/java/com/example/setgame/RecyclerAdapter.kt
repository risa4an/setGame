package com.example.setgame

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class RecyclerAdapter(cards: ArrayList<Int>, context: Context, listener: OnCardClickListener) : RecyclerView.Adapter<RecyclerAdapter.CardsHolder>(), Adapter {

    val inflater = LayoutInflater.from(context)
    val cardsArray = cards
    val onCardClickListener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsHolder {
        val view = inflater.inflate(R.layout.card_item, parent, false)
        return CardsHolder(view, onCardClickListener)
    }

    override fun getItemCount(): Int {
        return cardsArray.size
    }

    override fun onBindViewHolder(holder: CardsHolder, position: Int) {
        val cardId = cardsArray.get(position)

        holder.img?.tag = cardId!!;
        holder.img?.setBackgroundColor(Color.WHITE);
        holder.img?.setImageURI(Uri.parse("android.resource://com.example.setgame/drawable/c$cardId"))
        //holder.img?.setPadding(2, 2, 2, 2)
        holder.layout?.setPadding(2,2,2,2)
        holder.layout?.id = cardId
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        TODO("Not yet implemented")
    }

    override fun getItem(position: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getViewTypeCount(): Int {
        TODO("Not yet implemented")
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    class CardsHolder(v: View, l: OnCardClickListener) : RecyclerView.ViewHolder(v), View.OnClickListener {
        //2
        var img : ImageView? = null
        var layout  : View? = null
        var OnCardClickListener = l

        //3
        init {
            img = v.findViewById(R.id.imageView)
            layout = v
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            OnCardClickListener.onCardClick(v?.id!!)
        }
    }

    public interface OnCardClickListener{
        fun onCardClick(cardId : Int)
    }


}




