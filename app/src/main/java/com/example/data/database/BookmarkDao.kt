package com.example.data.database

import androidx.room.*
import com.example.data.entities.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT isBookmarked FROM bookmarks WHERE lessonId = :lessonId")
    fun isBookmarked(lessonId: Int): Flow<Boolean?>

    @Query("SELECT isBookmarked FROM bookmarks WHERE lessonId = :lessonId")
    suspend fun isBookmarkedDirect(lessonId: Int): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks WHERE isBookmarked = 1")
    fun getAllBookmarks(): Flow<List<Bookmark>>
}
