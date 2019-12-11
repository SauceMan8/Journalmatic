package com.sawyergehring.journalmatic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sawyergehring.journalmatic.Common.Common;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

public class SaveJournalInRange extends AppCompatActivity {

    private EditText name;
    private EditText date1_year;
    private EditText date1_month;
    private EditText date1_day;
    private EditText date2_year;
    private EditText date2_month;
    private EditText date2_day;

    private Button button;

    private String start_date;
    private String end_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_journal_in_range);
        getPermissionToWriteStorage();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button = findViewById(R.id.button2);
        name = findViewById(R.id.name);
        date1_year = findViewById(R.id.year1);
        date1_month = findViewById(R.id.month1);
        date1_day = findViewById(R.id.day1);
        date2_year = findViewById(R.id.year2);
        date2_month = findViewById(R.id.month2);
        date2_day = findViewById(R.id.day2);

        button.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Cursor c;
                        if (date1_day.getText().toString().matches("")||
                                date1_month.getText().toString().matches("")||
                                date1_year.getText().toString().matches("")||
                                date2_day.getText().toString().matches("")||
                                date2_month.getText().toString().matches("")||
                                date2_year.getText().toString().matches("")){
                            c = getAllItems();
                        }
                        else {
                            String month1 = String.format(Locale.US, "%02d", Integer.valueOf(date1_month.getText().toString()));
                            start_date = date1_year.getText().toString() + "/" + String.format(Locale.US, "%02d", Integer.valueOf(date1_month.getText().toString())) + "/" + String.format(Locale.US, "%02d", Integer.valueOf(date1_day.getText().toString()));
                            end_date = date2_year.getText().toString() + "/" + String.format(Locale.US, "%02d", Integer.valueOf(date2_month.getText().toString())) + "/" + String.format(Locale.US, "%02d", Integer.valueOf(date2_day.getText().toString()));
                            c = getItemsByDates(start_date,end_date);
                        }
                        StringBuilder write = new StringBuilder();

                        while (c.moveToNext()) {

                            String content = c.getString(c.getColumnIndex(JournalContract.JournalEntry.COLUMN_TEXT));
                            String dateString = c.getString(c.getColumnIndex(JournalContract.JournalEntry.COLUMN_DATE));
                            String date2 = c.getString(c.getColumnIndex(JournalContract.JournalEntry.COLUMN_TIMESTAMP));

                            write.append(dateString).append("\n").append(content.replaceAll("(?m)^", "\t")).append("\n\n\n");
                        }

                        writeFileOnInternalStorage(SaveJournalInRange.this, name.getText().toString()+".txt", write.toString());
                        Toast.makeText(SaveJournalInRange.this, "Saved to Downloads folder", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    private void writeDateToStorage() {
        writeFileOnInternalStorage(this, "new", "blank");
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(!file.exists()){
            file.mkdir();
        }

        try{
            File gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();

        }
    }

    private Cursor getItemsByDates(String start, String end) {
        return  Common.mDatabase.query(
                JournalContract.JournalEntry.TABLE_NAME,
                null,
                JournalContract.JournalEntry.COLUMN_TIMESTAMP + " >= \"" + start + "\"" + " AND "
                        + JournalContract.JournalEntry.COLUMN_TIMESTAMP + " <= \"" + end + "\"",
                null,
                null,
                null,
                JournalContract.JournalEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    private Cursor getAllItems() {

        return  Common.mDatabase.query(
                JournalContract.JournalEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                JournalContract.JournalEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    public void getPermissionToWriteStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this," Permission to write to external storage is used to save your journal", Toast.LENGTH_LONG);

            }

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Common.GET_STORAGE_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == Common.GET_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External Storage Write permission granted", Toast.LENGTH_SHORT).show();
                Common.defaultPreferences.edit().putBoolean("externalStorage", true).apply();
            } else {
                // showRationale = false if user clicks Never Ask Again, otherwise true
                boolean showRationale = false;
                showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (showRationale) {
                    Common.defaultPreferences.edit().putBoolean("externalStorage", false).apply();
                } else {
                    Toast.makeText(this, "External Storage Write permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
