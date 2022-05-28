package com.example.mylibrary.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Book(
    var id: String? = "",
    var name: String? = "",
    var desciption: String? ="",
    var categoryName: String? ="",
    var rating: Float = 0.0f,
    var authorName: String? ="",
    var yearRelease: String?,
    var isFavorite: Boolean =false,
    var image: String? =""
):Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(desciption)
        parcel.writeString(categoryName)
        parcel.writeFloat(rating)
        parcel.writeString(authorName)
        parcel.writeString(yearRelease)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Book> {
        override fun createFromParcel(parcel: Parcel): Book {
            return Book(parcel)
        }

        override fun newArray(size: Int): Array<Book?> {
            return arrayOfNulls(size)
        }
    }

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to this.id,
            "name" to this.name,
            "desciption" to this.desciption,
            "nameCategory" to this.categoryName,
            "rate" to this.rating,
            "author" to this.authorName,
            "yearRelease" to yearRelease,
            "isFavorite" to isFavorite,
            "image" to image

        )
    }

}