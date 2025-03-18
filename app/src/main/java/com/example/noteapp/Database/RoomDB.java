package com.example.noteapp.Database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.noteapp.Models.Category;
import com.example.noteapp.Models.Notes;
import com.example.noteapp.Models.Reminder;

@Database(entities = {Notes.class, Category.class, Reminder.class}, version = 6, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;
    private static final String DATABASE_NAME = "NotesApp";

    public synchronized static RoomDB getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(),
                            RoomDB.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration() // xử lý migration
                    .build();
        }
        return database;
    }

    public abstract MainDAO mainDAO();
    public abstract CategoryDAO categoryDAO();
    public abstract ReminderDAO reminderDAO();
}


