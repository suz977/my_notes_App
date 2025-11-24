package com.example.mynotes.databse

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mynotes.model.Note

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote (note : Note)

    @Update
    suspend fun updateNote (note: Note)

    @Delete
    suspend fun delNote (note: Note)

    @Query("SELECT * FROM NOTES ORDER BY id DESC")
    fun getAllNotes () : LiveData<List<Note>>

    @Query("SELECT * FROM NOTES WHERE notetitle LIKE :query OR notebody LIKE :query")
    fun searchNote(query: String?) : LiveData<List<Note>>
}