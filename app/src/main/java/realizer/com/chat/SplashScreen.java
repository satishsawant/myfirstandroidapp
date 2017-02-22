package realizer.com.chat;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import realizer.com.chat.chat.ChatThreadListActivity;

public class SplashScreen extends AppCompatActivity {
    int sleeptime=4000;
    String currentVersion="",newversion="";
    String packagename="realizer.com.chat";
    int curV,newV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                sleeptime=4000;
                requestForSpecificPermission();
            }
        }
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        ab.hide();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

            Thread timerThread = new Thread() {
                public void run() {
                    try {
                        sleep(sleeptime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {

                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
                        String LogCheck=sharedpreferences.getString("Login", "");
                        if (LogCheck.equals("true"))
                        {
                            Intent intent = new Intent(SplashScreen.this, ChatThreadListActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        finish();
                    }
                }
            };
            timerThread.start();
    }
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {
                    /*android.Manifest.permission.INTERNET,
                        android.Manifest.permission.READ_PHONE_STATE,*/
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.INTERNET,
                }, 101);
    }

}
