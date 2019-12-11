package com.sawyergehring.journalmatic;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.sawyergehring.journalmatic.Common.Common;

import java.util.Calendar;

public class Journalmatic extends Application {

    public static final String Reminder_Channel = "reminder";
    public static final String ERRORS_CHANNEL = "Errors";
    public static final int INTERVAL_DAY = (1000 * 60 * 60 * 24);
    private NotificationManagerCompat notificationManager;
    private AlarmManager am;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();


        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannels();
        Common.defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //startFastTestNotification();
//        createAlarm();
        startElapsedDayTestNotification();
    }

    private void startNotificationService() {

    }


    private void startFastTestNotification() {


            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 20);
            Toast.makeText(this, "Alarm created1" , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, NotificationReceiver.class);

            am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            pendingIntent = PendingIntent.getBroadcast(this, NotificationReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            am.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + (10*1000),
                    (10*1000),
                    pendingIntent);

    }

    private void startElapsedDayTestNotification() {

        Integer time = Integer.parseInt(Common.defaultPreferences.getString("reminder_time", "20"));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time);
        Toast.makeText(this, "Alarm created1" , Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NotificationReceiver.class);

        String length = Common.defaultPreferences.getString("notification_feq", "Daily");
        Integer multiple = 1;
        switch (length) {
            case "daily":
                break;
            case "weekly":
                multiple = 7;
                break;
            case "monthly":
                multiple = 30;
                break;
        }


        am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, NotificationReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * multiple,
                pendingIntent);

    }



    public void createAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Common.defaultPreferences.getString("reminder_time", "10")));
        //Toast.makeText(this, "Alarm created2" , Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NotificationReceiver.class);

        am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);

    }

    public void cancelRemider() {
        am.cancel(pendingIntent);
    }

    public void sendReminderNotification() {

        Intent notificationIntent = new Intent(this, EditorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, Reminder_Channel)
                .setSmallIcon(R.drawable.ic_main_icon_foreground)
                .setContentTitle("Journalmatic Start")
                .setContentText("Notification set")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(3, notification);
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminder = new NotificationChannel(
                    Reminder_Channel,  "Reminders", NotificationManager.IMPORTANCE_LOW
            );
            reminder.setDescription("This is the reminder channel");

            NotificationChannel errors = new NotificationChannel(
                    ERRORS_CHANNEL,  "Errors", NotificationManager.IMPORTANCE_HIGH
            );
            errors.setDescription("This is the errors channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(reminder);
            manager.createNotificationChannel(errors);

        }
    }
}
