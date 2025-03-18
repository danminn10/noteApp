package com.example.noteapp.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders",
        foreignKeys = @ForeignKey(entity = Notes.class,
                parentColumns = "ID",
                childColumns = "noteId",
                onDelete = ForeignKey.CASCADE))
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "noteId")
    private int noteId;

    @ColumnInfo(name = "reminderTime")
    private long reminderTime; // Thời gian nhắc nhở dưới dạng timestamp

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }
}

