package com.sawyergehring.journalmatic;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class Journalmatic extends Application {

    public static final String Reminder_Channel = "reminder";
    public static final String CHANNEL_2_ID = "Errors";
    private NotificationManagerCompat notificationManager;
    private AlarmManager am;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = NotificationManagerCompat.from(this);

        createNotificationChannels();
        createAlarm();
    }

    private void startNotification() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(this, BroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if (am != null) {
            //am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            am.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);
            sendReminderNotification();
        }

    }



    public void createAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        Toast.makeText(this, "Alarm created" , Toast.LENGTH_SHORT).show();
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
                .setContentTitle("Test")
                .setContentText("Notification set")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminder = new NotificationChannel(
                    Reminder_Channel,  "Reminders", NotificationManager.IMPORTANCE_LOW
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
