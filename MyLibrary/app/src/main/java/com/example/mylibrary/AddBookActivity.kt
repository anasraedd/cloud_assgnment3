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
import com.example.mylibrary.MyNotification.Companion.userToken
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.firebase.Timestamp
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.add_rate.*
import kotlinx.android.synthetic.main.choose_put_image.*
import kotlinx.android.synthetic.main.item_book.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class AddBookActivity : AppCompatActivity() {
    val db = Firebase.database
    val mRef = db.reference
    var firestore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null
    var reference: StorageReference? = null
    var list_name_categories = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null
    var rate: Float? = null
    val myNotification = MyNotification(this)
    private lateinit var progressDialog: ProgressDialog
    var downloadUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        firestore = Firebase.firestore
        storage = Firebase.storage
        reference = storage!!.reference
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        getCategories()
        list_name_categories.add("اختر التصنيف")

        Firebase.messaging.subscribeToTopic("Books")

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list_name_categories)
        sp_categories.adapter  = adapter

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.add_rate)



        btn_add_image.setOnClickListener {
           pickImage()

        }
        
bottomSheetDialog.rating_bar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
rate =rating
    tv_rating.text = "$rating"
    add_rating.text = "تعديل التقييم"
   // Toast.makeText(this, "rating is : $rating ", Toast.LENGTH_LONG).show()
}



        add_rating.setOnClickListener {
           bottomSheetDialog.show()
        }

        btn_back.setOnClickListener {
            onBackPressed()
        }
        btn_cancle.setOnClickListener {
            onBackPressed()

        }

        btn_seve_and_add.setOnClickListener {
            AddBook(UUID.randomUUID().toString(), et_name_book.text.toString(),
                etdescription.text.toString(), et_name_author.text.toString(),
                rate, sp_categories.selectedItem.toString())
        }

    }

    private fun AddBook(id:String, name:String, desciption:String, author:String, rate: Float?, nameCategory:String ) { //yearRealese:String,
     if (id.isNotEmpty() && name.isNotEmpty() && desciption.isNotEmpty()  && author.isNotEmpty() && rate != null
         && nameCategory != null && nameCategory != "اختر التصنيف"
     ){
             progressDialog.show()

         val book = hashMapOf(
             "id" to id,
             "name" to name,
             "desciption" to desciption,
             "author" to author,
             "yearRealese" to Timestamp(Date()),
             "image" to downloadUrl,
             "rate" to rate,
             "nameCategory" to nameCategory,
             "isFavorite" to false

         )


         mRef.child("Books").child(id).setValue(book)
             .addOnSuccessListener {
                 myNotification.sendNotification("Add Book", "Book added Successfully", userToken)
                 Toast.makeText(this, "Book added Successfully", Toast.LENGTH_SHORT).show()


             }


         onBackPressed()

     }else{
         Toast.makeText(this, "يجب ملئ البيانات المطلوبة", Toast.LENGTH_LONG).show()
     }
     }


    fun getCategories() {
        firestore!!.collection("categories").get()
            .addOnSuccessListener { querySnapshot ->
                for (i in 0 until querySnapshot.documents.size) {
                    list_name_categories.add(querySnapshot.documents.get(i).get("name_category").toString())

                }

            }
    }


    private fun pickImage() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.choose_put_image)

        bottomSheetDialog.image_from_camera.setOnClickListener {

            val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
            startActivityForResult(camera, 203)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.image_from_gallery.setOnClickListener {
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
            image_book.visibility = View.VISIBLE
            image_book.setImageURI(data!!.data)
            btn_add_image.text = "Change Image"
            //uploading image after picking it
            uploadImage(data.data)
        } else if (resultCode == Activity.RESULT_OK && requestCode == 203) {
            val bitmap = data!!.extras!!.get("data")
            image_book.visibility = View.VISIBLE
            image_book.setImageBitmap(bitmap as Bitmap)
            btn_add_image.text = "Change Image"
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