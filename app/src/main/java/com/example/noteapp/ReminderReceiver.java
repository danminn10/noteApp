package com.example.noteapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("noteId", -1);
        if (noteId != -1) {
            showNotification(context, noteId);
        }
    }

    @SuppressLint("NotificationPermission")
    private void showNotification(Context context, int noteId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra("noteId", noteId);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, noteId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(context, "reminder_channel")
                    .setSmallIcon(R.drawable.ic_reminder)
                    .setContentTitle("Reminder")
                    .setContentText("You have a reminder for your note!" + noteId)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();

            notificationManager.notify(noteId, notification);
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reminder_channel",
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
