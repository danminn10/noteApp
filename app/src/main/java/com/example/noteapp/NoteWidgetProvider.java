package com.example.noteapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.noteapp.Database.RoomDB;
import com.example.noteapp.Models.Notes;

public class NoteWidgetProvider extends AppWidgetProvider {

    // Called when the first widget instance is added to the home screen
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // Perform setup when the widget is created for the first time
        Log.d("NoteWidgetProvider", "Widget enabled for the first time.");
    }

    // Called when the widget is updated (e.g., content changes or widget is refreshed)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each widget individually
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // Update the widget with the selected note's data
    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        new Thread(() -> {
            try {
                // Get the note ID from SharedPreferences
                int selectedNoteId = getNoteIdFromPreferences(context);

                if (selectedNoteId == -1) {
                    return;  // No valid note ID, don't update
                }

                // Fetch the note from the database
                RoomDB database = RoomDB.getInstance(context);
                Notes selectedNote = database.mainDAO().getNoteById(selectedNoteId);

                // If no note found, set default data
                String noteTitle = selectedNote != null ? selectedNote.getTitle() : "No note found";
                String noteContent = selectedNote != null ? selectedNote.getNotes() : "Please add a note.";

                // Create a RemoteViews object to update the widget UI
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note);
                views.setTextViewText(R.id.widget_title, noteTitle);
                views.setTextViewText(R.id.widget_note_content, noteContent);

                // Handle click event: Open MainActivity when clicked
                Intent widgetIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, widgetIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("NoteWidgetProvider", "Error in updating widget", e);
            }
        }).start();
    }

    // Method to get the note ID from SharedPreferences
    private int getNoteIdFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NoteWidgetPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("note_id", -1);  // Default value is -1 if not found
    }
}
