package com.example.mylibrary

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mylibrary.model.Book
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.activity_edit_book.*
import kotlinx.android.synthetic.main.add_rate.*
import kotlinx.android.synthetic.main.choose_put_image.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class EditBookActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var isFavorite: Boolean = false
    var categoryName: String = ""
    val db = Firebase.database
    val mRef = db.reference

    var storage: FirebaseStorage? = null
    var reference: StorageReference? = null
    var downloadUrl: String = ""
    val myNotification = MyNotification(this)

    var list_name_categories = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null
    var rate = 3F
    lateinit var id:String
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)
        firestore = Firebase.firestore
        storage = Firebase.storage
        reference = storage!!.reference
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
       id = intent.getStringExtra("id").toString()


        var book = intent.getParcelableExtra<Book>("book")


        if (book != null) {

            et_name_book_in_edit.setText(book.name)
            et_name_author_in_edit.setText(book.authorName.toString())
            etdescription_in_edit.setText(book.desciption)
            tv_rating_edit.setText(book.rating.toString())
            rate = book.rating

            if (!book.image.isNullOrEmpty())
                image_book_in_edit.visibility = View.VISIBLE
            downloadUrl = book.image.toString()
            Glide.with(this).load(book.image.toString()).into(image_book_in_edit)
            categoryName = book.categoryName.toString()
            list_name_categories.add("${book.categoryName}")
            getCategories()
        }

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.add_rate)
        bottomSheetDialog.rating_bar.rating = rate

        add_rating_in_edit.setOnClickListener {


            bottomSheetDialog.show()

        }

        btn_change_image.setOnClickListener {
            pickImage()

        }


        btn_back_in_edit.setOnClickListener {
            onBackPressed()
        }
        btn_cancle_in_edit.setOnClickListener {

            onBackPressed()

        }


        bottomSheetDialog.rating_bar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            rate = rating
            tv_rating.text = "$rating"

        }


        btn_seve_and_edit.setOnClickListener {
            editBook(
                Book(
                    book!!.id.toString(), et_name_book_in_edit.text.toString(),
                    etdescription_in_edit.text.toString(),sp_categories_in_edit.selectedItem.toString() ,
                    rate.toFloat(),  et_name_author_in_edit.text.toString(), Timestamp(Date()).toString(), book!!.isFavorite ,downloadUrl
                ),
            )
        }


    }


    fun getCategories() {
        firestore!!.collection("categories").get()
            .addOnSuccessListener { querySnapshot ->

                // list_name_categories[0] = categoryName
                for (i in 0 until querySnapshot.documents.size) {
                    if (querySnapshot.documents.get(i).get("name_category")
                            .toString() != categoryName
                    ) {
                        list_name_categories.add(
                            querySnapshot.documents.get(i).get("name_category").toString()
                        )

                    }
                }
                adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    list_name_categories
                )
                sp_categories_in_edit.adapter = adapter
            }
    }

    private fun editBook(book: Book) { //


        val newBook = book.toMap()

        val childrenUpdates = hashMapOf<String, Any?>(
           book.id.toString() to newBook
        )

        mRef.child("Books").updateChildren(childrenUpdates)
            .addOnSuccessListener {
                //  myNotification.sendNotification("Update Book", "book updated Successfully",
                myNotification.sendNotification(
                    "Add Book", "Book added Successfully",
                    MyNotification.userToken
                )

                Toast.makeText(this, "Book updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.message.toString(), Toast.LENGTH_SHORT).show()
            }


    }


    private fun pickImage() {
        // bottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.choose_put_image)

        bottomSheetDialog.image_from_camera.setOnClickListener {
            //camera
            val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
            startActivityForResult(camera, 203)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.image_from_gallery.setOnClickListener {
            //gallery
            val gallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 103)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.cancble.setOnClickListener {
            bottomSheetDialog.cancel()
        }
        bottomSheetDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 103) {
            image_book_in_edit.visibility = View.VISIBLE
            image_book_in_edit.setImageURI(data!!.data)
            //uploading image after picking it
            uploadImage(data.data)
        } else if (resultCode == Activity.RESULT_OK && requestCode == 203) {
            val bitmap = data!!.extras!!.get("data")
            image_book_in_edit.visibility = View.VISIBLE
            image_book_in_edit.setImageBitmap(bitmap as Bitmap)
            //uploading image after picking it
            uploadImageAndSaveUri(bitmap as Bitmap)
        }
    }

    private fun uploadImageAndSaveUri(bitmap: Bitmap) {
        progressDialog.setMessage("Upload Image...")
        progressDialog.show()
        val outputStream = ByteArrayOutputStream()
        val storageRef =
            reference!!.child("books/${UUID.randomUUID()}")

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val image = outputStream.toByteArray()

        storageRef.putBytes(image)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnCompleteListener { it ->
                        if (it.isComplete) {
                            progressDialog.dismiss()
                            //getting download url, we will stored it in firestore with course info
                            downloadUrl = it.result.toString()

                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadImage(data: Uri?) {
        progressDialog.setMessage("Upload Image...")
        progressDialog.show()
        reference!!.child("books/${UUID.randomUUID()}")
            .putFile(data!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    //getting download url, we will stored it in firestore with course info
                    downloadUrl = it.toString()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

}