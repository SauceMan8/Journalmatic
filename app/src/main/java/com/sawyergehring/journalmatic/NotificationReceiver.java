package com.sawyergehring.journalmatic;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.text.DateFormat;
import java.util.Date;

import static com.sawyergehring.journalmatic.Journalmatic.Reminder_Channel;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


//        Toast.makeText(context, "Receive Broadcast", Toast.LENGTH_SHORT).show();


        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, EditorActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("selectedDate", getTodayDateAsString());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, Reminder_Channel)
                .setSmallIcon(R.drawable.ic_main_icon_foreground)
                .setContentTitle("Journal Reminder")
                .setContentText("Reminder to write in your journal today!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).setWhen(when);

        notificationManager.notify(2, notification.build());


        //notification_ID++;

    }

    private String getTodayDateAsString() {
        DateFormat dateOutputFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateOutputFormat.format(new Date(System.currentTimeMillis()));
    }

}
