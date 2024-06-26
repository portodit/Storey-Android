package com.example.storey.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey
    val id: String,

    val nextKey: Int?,

    val previousKey: Int?
)