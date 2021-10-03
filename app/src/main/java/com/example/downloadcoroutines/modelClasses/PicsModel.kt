package com.example.downloadcoroutines.modelClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pics")
data class PicsModel(
    val id: String? = "",

    val author: String? = null,

    @PrimaryKey(autoGenerate = false)
    val download_url: String,

    val url: String? = null,

    var placeHolderUrl: String? = null,

    var isVisible: Boolean? = false,

    var author_url: String? = "",

    var post_url: String? = "",
) {
}