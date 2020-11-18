package com.example.photomemo

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "photo_table")
data class Photo(
        @PrimaryKey @ColumnInfo(name = "uri") val uri: String,
        @ColumnInfo(name = "memo") val memo: String
) {
}

@Dao
interface PhotoDao {
    @Query("SELECT * from photo_table")
    fun getPhotos(): LiveData<List<Photo>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo)
    abstract fun getPhots(): LiveData<List<Photo>>
}

@Database(entities = arrayOf(Photo::class), version = 1, exportSchema = false)
public abstract class PhotoRoomDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    companion object {
        @Volatile
        private var INSTANCE: PhotoRoomDatabase? = null
        fun getPhotoDatabase(context: Context): PhotoRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PhotoRoomDatabase::class.java,
                        "photo_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

class PhotoRepository(private val photoDao: PhotoDao) {
    val allPhotos: LiveData<List<Photo>> = photoDao.getPhots()
    suspend fun insert(photo: Photo) {
        photoDao.insert(photo)
    }
}

