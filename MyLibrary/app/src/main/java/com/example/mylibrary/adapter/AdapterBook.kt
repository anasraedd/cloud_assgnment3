package com.example.mylibrary.adapter

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mylibrary.EditBookActivity
import com.example.mylibrary.R
import com.example.mylibrary.model.Book
import kotlinx.android.synthetic.main.item_book.view.*
import java.util.*
import kotlin.collections.ArrayList

class AdapterBook(var activity: Activity, var book: ArrayList<Book>, var click: onClick, var clickOnMore:onClickOnMore): RecyclerView.Adapter<AdapterBook.MyViewHolder>() {


    interface onClick{
        fun onclickItem(position: Int, id: String, name:String,  isFavorite:Boolean)
    }

    interface onClickOnMore{
        fun onclickItemOnClickMore(position: Int, id: String, book: Book, name:String)
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
var imageBook = itemView.image_book_item
        var nameBook = itemView.name_book
        var nameAuthor = itemView.name_author
//        var yearRelease = itemView.year_release
        var rating = itemView.rating2
        var category = itemView.category
        var btnFavorite = itemView.btn_favorite
//        var btnDownload = itemView.btn_download
        var btnMore = itemView.btn_more

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val root = LayoutInflater.from(activity).inflate(R.layout.item_book, parent, false)
        return MyViewHolder(root)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(activity).load(book[position].image).into(holder.imageBook)
        holder.nameBook.text = book[position].name
        holder.nameAuthor.text = book[position].authorName


        var date2 = ""
        var indexstart = 0

        holder.rating.text = book[position].rating.toString()
        holder.category.text = book[position].categoryName
        if (book[position].isFavorite != null){
            if (book[position].isFavorite){

                holder.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)

            }else{
                holder.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }


        holder.btnFavorite.setOnClickListener {
            if (book[position].isFavorite){
//            holder.btnFavorite.setImageIcon(R.drawable.ic_baseline_favorite_24)
                holder.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                book[position].isFavorite = false
                click.onclickItem(
                    position,
                    book[position].id.toString(),
                    book[position].name.toString(),
                    false)
            }else{
                holder.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                book[position].isFavorite = true
                click.onclickItem(
                    position,
                    book[position].id.toString(),
                    book[position].name.toString(),
                    true)
            }

        }

        holder.btnMore.setOnClickListener {

            clickOnMore.onclickItemOnClickMore(
                position,
                book[position].id.toString(),
                book[position],
                book[position].name.toString(),)


        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDateString(seconds: Long, outputPattern: String): String {
        try {
            val dateFormat = SimpleDateFormat(outputPattern, Locale.ENGLISH)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = seconds * 1000
            val date = calendar.time
            return dateFormat.format(date)
        } catch (e: Exception) {
          //  Log.e("utils", "Date format", e)
            return ""
        }
    }
    override fun getItemCount(): Int {
return book.size
    }
}