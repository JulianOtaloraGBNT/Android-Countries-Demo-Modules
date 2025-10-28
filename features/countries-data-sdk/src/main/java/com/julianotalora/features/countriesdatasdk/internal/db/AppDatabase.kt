package com.julianotalora.features.countriesdatasdk.internal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.julianotalora.features.countriesdatasdk.internal.db.converters.Converters

@Database(
    entities = [CountryEntity::class, RefreshStateEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun countryDao(): CountryDao
    abstract fun refreshStateDao(): RefreshStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun create(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "countries_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
