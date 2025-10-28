package com.julianotalora.features.countriesdatasdk.internal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RefreshStateDao {

    @Query("SELECT * FROM refresh_states WHERE 'key' = :key")
    suspend fun getByKey(key: String): RefreshStateEntity?

    @Upsert
    suspend fun upsert(refreshState: RefreshStateEntity)
}
