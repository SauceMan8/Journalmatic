//package com.sawyergehring.journalmatic;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.sawyergehring.journalmatic.Common.Common;
//
//import net.rdrei.android.dirchooser.DirectoryChooserConfig;
//import net.rdrei.android.dirchooser.DirectoryChooserFragment;
//
//public class DirectoryChooserActivity extends AppCompatActivity implements
//        DirectoryChooserFragment.OnFragmentInteractionListener {
//
//private TextView mDirectoryTextView;
//private DirectoryChooserFragment mDialog;
//
//@Override
//protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog);
//final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
//        .newDirectoryName("DialogSample")
//        .build();
//        mDialog = DirectoryChooserFragment.newInstance(config);
//
//        mDirectoryTextView = (TextView) findViewById(R.id.textDirectory);
//
//        findViewById(R.id.btnChoose)
//        .setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        mDialog.show(getFragmentManager(), null);
//        }
//        });
//        }
//
//@Override
//public void onSelectDirectory(@NonNull String path) {
//        mDirectoryTextView.setText(path);
//        mDialog.dismiss();
//        }
//
//@Override
//public void onCancelChooser() {
//        mDialog.dismiss();
//        }
//
//
//    public void getPermissionToWriteStorage() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (shouldShowRequestPermissionRationale(
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    Toast.makeText(this," Permission to write to external storage is used to save your journal", Toast.LENGTH_LONG);
//
//                }
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Common.GET_LOCATION_PERMISSIONS_REQUEST);
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        // Make sure it's our original READ_CONTACTS request
//        if (requestCode == Common.GET_LOCATION_PERMISSIONS_REQUEST) {
//            if (grantResults.length == 1 &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "External Storage Write permission granted", Toast.LENGTH_SHORT).show();
//                Common.defaultPreferences.edit().putBoolean("externalStorage", true).apply();
//            } else {
//                // showRationale = false if user clicks Never Ask Again, otherwise true
//                boolean showRationale = false;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                }
//
//                if (showRationale) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//                        Common.defaultPreferences.edit().putBoolean("externalStorage", false).apply();
//                    }
//                } else {
//                    Toast.makeText(this, "External Storage Write permission denied", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }
//}
