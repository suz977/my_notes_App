package com.example.mynotes.repository

import androidx.room.Query
import com.example.mynotes.databse.NoteDatabase
import com.example.mynotes.model.Note

class NoteRepository(private val db : NoteDatabase) {

    //suspend because functions are pre - defined
    suspend fun insertNote(note : Note) = db.getNoteDao().insertNote(note)
    suspend fun updateNote(note : Note) = db.getNoteDao().updateNote(note)
    suspend fun delNote(note: Note) = db.getNoteDao().delNote(note)

    fun getAllNotes() = db.getNoteDao().getAllNotes()

    fun searchNote(query: String?) = db.getNoteDao().searchNote(query)
}