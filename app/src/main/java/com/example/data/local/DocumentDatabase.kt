package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "documents")
data class PdfDocumentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val filePath: String,
  val pageCount: Int,
  val fileSize: Long,
  val createdAt: Long
)

@Dao
interface PdfDocumentDao {
  @Query("SELECT * FROM documents ORDER BY createdAt DESC")
  fun getAllDocuments(): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
  fun searchDocuments(query: String): Flow<List<PdfDocumentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: PdfDocumentEntity): Long

  @Query("DELETE FROM documents WHERE id = :id")
  suspend fun deleteDocumentById(id: Long)

  @Query("UPDATE documents SET title = :newTitle WHERE id = :id")
  suspend fun updateTitle(id: Long, newTitle: String)
}

@Database(entities = [PdfDocumentEntity::class], version = 1, exportSchema = false)
abstract class DocumentDatabase : RoomDatabase() {
  abstract fun pdfDocumentDao(): PdfDocumentDao

  companion object {
    @Volatile private var INSTANCE: DocumentDatabase? = null

    fun getDatabase(context: Context): DocumentDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                DocumentDatabase::class.java,
                "pictopdf_database"
              )
              .fallbackToDestructiveMigration()
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
