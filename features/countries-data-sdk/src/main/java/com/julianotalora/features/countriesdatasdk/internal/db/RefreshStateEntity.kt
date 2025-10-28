package com.julianotalora.features.countriesdatasdk.internal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refresh_states")
data class RefreshStateEntity(
    @PrimaryKey val key: String,
    val lastUpdatedMillis: Long
)
