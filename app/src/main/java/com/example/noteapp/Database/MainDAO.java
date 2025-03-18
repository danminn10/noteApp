package com.example.noteapp.Database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.noteapp.Models.Notes;

import java.util.List;

@Dao
public interface MainDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notes note);

    @Query("UPDATE notes SET title = :title, notes = :notes WHERE id = :id")
    void update(int id, String title, String notes);

    @Delete
    void delete(Notes note);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> getAll();

    @Query("UPDATE notes SET pinned = :isPinned WHERE id = :id")
    void pin(int id, boolean isPinned);

    @Query("SELECT * FROM notes WHERE title LIKE :query OR notes LIKE :query")
    List<Notes> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE categoryId = :categoryId ORDER BY id DESC")
    List<Notes> getNotesByCategory(int categoryId);

    @Query("UPDATE notes SET categoryId = NULL WHERE categoryId = :categoryId")
    void updateNotesCategoryToNull(int categoryId);

    // Phương thức để lấy ghi chú theo ID
    @Query("SELECT * FROM notes WHERE ID = :id")
    Notes getNoteById(int id);

}

