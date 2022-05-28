package com.example.mylibrary

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mylibrary.MyNotification.Companion.TAG
import com.example.mylibrary.MyNotification.Companion.userToken
import com.example.mylibrary.adapter.AdapterBook
import com.example.mylibrary.model.Book
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.obtions_more_to_book.*
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity(), AdapterBook.onClick, AdapterBook.onClickOnMore {
    var data = ArrayList<Book>()
    val db = Firebase.database
    val mRef = db.reference
    var firestore: FirebaseFirestore? = null
    lateinit var myNotification: MyNotification
     var isFavorite = false
    private val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
    var bookAdapter = AdapterBook(this,data, this, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = Firebase.firestore
        myNotification = MyNotification(this)

        rv_books.layoutManager = LinearLayoutManager(this)
        rv_books.adapter = bookAdapter
        rv_books.layoutManager = LinearLayoutManager(this)
        rv_books.adapter = bookAdapter

       // getBooks()

        val readDate = Read()
        val myRef = mRef.child("Books")
        myRef.addValueEventListener(readDate)

        add_book.setOnClickListener {
var i = Intent(this, AddBookActivity::class.java)
            startActivity(i)
        }



    }

    inner class Read : ValueEventListener {
        @SuppressLint("NotifyDataSetChanged")

        override fun onDataChange(snapshot: DataSnapshot) {
            //data.clear()

            var data2 = ArrayList<Book>()

            for (book in snapshot.children) {
                val myBook = Book(
                    book.key.toString(),
                    book.child("name").value.toString().trim(),
                    book.child("desciption").value.toString().trim(),
                    book.child("nameCategory").value.toString(),
                    book.child("rate").value.toString().toFloat(),
                    book.child("author").value.toString(),
                    book.child("yearRealese").value.toString(),
                    book.child("isFavorite").value as Boolean,
                    book.child("image").value.toString()

                )
                data2.add(myBook)
            }


            bookAdapter.book = data2
            bookAdapter.notifyDataSetChanged()
         //   binding.rcyBooks.adapter!!.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "error with Exception : ${error.message}")
        }

    }



    override fun onclickItem(position: Int, id: String, name: String, isFavorite: Boolean) {


    }

    override fun onclickItemOnClickMore(position: Int, id: String, book:Book ,name: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.obtions_more_to_book)

        bottomSheetDialog.tv_name_book.text = name
        bottomSheetDialog.cancble.setOnClickListener {
            bottomSheetDialog.cancel()
        }

        bottomSheetDialog.btn_edit.setOnClickListener {
            var i = Intent(this, EditBookActivity::class.java)
            i.putExtra("book", book)
             startActivity(i)
            bottomSheetDialog.dismiss()
        }


        bottomSheetDialog.btn_delete.setOnClickListener {
            mRef.child("Books").child(id).removeValue()
                .addOnSuccessListener {
                    myNotification.sendNotification("Delete Book", "Book deleted Successfully", userToken)
                    Toast.makeText(this, "Book deleted Successfully", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message.toString(), Toast.LENGTH_SHORT).show()
                }


            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }



}