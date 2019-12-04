package com.sawyergehring.journalmatic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Journalmatic extends Application {

    public static final String CHANNEL_1_ID = "reminder";
    public static final String CHANNEL_2_ID = "Errors";

    @Override
    public void onCreate() {
        super.onCreate();
        
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminder = new NotificationChannel(
                    CHANNEL_1_ID,  "Reminders", NotificationManager.IMPORTANCE_LOW
            );
            reminder.setDescription("This is the reminder channel");

            NotificationChannel errors = new NotificationChannel(
                    CHANNEL_2_ID,  "Errors", NotificationManager.IMPORTANCE_HIGH
            );
            errors.setDescription("This is the errors channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(reminder);
            manager.createNotificationChannel(errors);

        }
    }
}
