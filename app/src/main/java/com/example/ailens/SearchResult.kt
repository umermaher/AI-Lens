package com.example.ailens

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchResult(
    val title:String?=null,
    val link:String?=null,
    val displayedLink:String?=null,
    val snippet:String?=null
):Parcelable

@Parcelize
class SearchResultList: ArrayList<SearchResult>(),Parcelable