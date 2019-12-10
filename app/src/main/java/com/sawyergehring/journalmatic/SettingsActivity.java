package com.sawyergehring.journalmatic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        context = getApplicationContext();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference numberPreference = findPreference("reminder_time");
            final SwitchPreferenceCompat switchPreference = findPreference("notify");

            if (switchPreference != null) {
                switchPreference.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if(switchPreference.isChecked()) {

                            Intent intent = new Intent(context, NotificationReceiver.class);
                            final PendingIntent pIntent = PendingIntent.getBroadcast(context, NotificationReceiver.REQUEST_CODE,
                                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            alarm.cancel(pIntent);
                        } else {

                        }

                        return true;
                    }
                });
            }

            if (numberPreference != null) {
                numberPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }
                        });
                numberPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (Integer.parseInt(newValue.toString()) > 23) {
                            return false; //change to 23
                        }
                        else if (Integer.parseInt(newValue.toString()) < 0) {
                            return false; //change to 0
                        }
                        else return true;
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}