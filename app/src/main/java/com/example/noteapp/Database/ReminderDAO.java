package com.example.noteapp.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.noteapp.Models.Reminder;

import java.util.List;

@Dao
public interface ReminderDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE noteId = :noteId")
    List<Reminder> getRemindersForNote(int noteId);

    @Query("SELECT * FROM reminders WHERE reminderTime <= :currentTime")
    List<Reminder> getDueReminders(long currentTime);

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(int id);
}
