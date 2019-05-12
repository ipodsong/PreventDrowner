package com.example.ipods.prevent_drowner;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import static com.example.ipods.prevent_drowner.Common.CAMERA_IMAGE_URI;
import static com.example.ipods.prevent_drowner.Common.CAMERA_REUQEST;
import static com.example.ipods.prevent_drowner.Common.CAPTURED_IMG_URI;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_CAMERA;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_READ_STORAGE;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_WRITE_STORAGE;
import static com.example.ipods.prevent_drowner.Common.TO_CAMERA_FRAG;
import static com.example.ipods.prevent_drowner.Common.TO_HOME_FRAG;
import static com.example.ipods.prevent_drowner.Common.homeFragmentIndex;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private final String TAG = "MainActivity";

    private FrameLayout mainLayout;

    ImageButton mAlertButton;
    boolean isWarning = false;

    /*** Fragment ***/
    public int currentFragment = homeFragmentIndex;

    private boolean doubleBackToHome = false;

    /*** common ***/
    private Common common;

    /*** permission ***/
    private Permission permission;

    /*** warning : sound + animation ***/
    private Animation alertAnimation;
    private MediaPlayer mp;

    /*** vibrator ***/
    private Vibrator mVibrator;

    /*** bottom navigation view ***/
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new Home());
        initializeReceiver();

        common = new Common();
        mainLayout = (FrameLayout) findViewById(R.id.fragment_container);

        /*** request permission ***/
        checkPermission();

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*** animation ***/
        alertAnimation = AnimationUtils.loadAnimation(this, R.anim.alert);

        /*** vibrator ***/
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        /*** media player **/
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamVolume(AudioManager.STREAM_RING),
                AudioManager.FLAG_ALLOW_RINGER_MODES);

        mp = MediaPlayer.create(MainActivity.this, R.raw.alert);
        mp.setVolume(1.0f, 1.0f);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToHome) {
            loadFragment(new Home());
            return;
        }

        if(currentFragment != homeFragmentIndex){
            Toast.makeText(this, "뒤로 가기 버튼을 한번 더 누르면 홈 화면으로 전환됩니다", Toast.LENGTH_SHORT).show();
            this.doubleBackToHome = true;
            new Handler().postDelayed(backPressedRunnable, 2000);
        }else{
            super.onBackPressed();
        }
    }

    private final Runnable backPressedRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToHome = false;
        }
    };

    /*** broadcast receiver ***/
    public void initializeReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(TO_HOME_FRAG);
        filter.addAction(TO_CAMERA_FRAG);

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mReceiver, filter);
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch(action){
                case TO_HOME_FRAG:


                    loadFragment(new Home());
                    break;
                case TO_CAMERA_FRAG:
                    loadFragment(new Camera());
                    break;
                case CAPTURED_IMG_URI:
                    break;

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case PERMISSION_CAMERA:
                break;
            case PERMISSION_WRITE_STORAGE:
                break;
            case PERMISSION_READ_STORAGE:
                break;
        }
    }

    ImageButton.OnClickListener alertButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isWarning) {
                isWarning = false;
                makeMeShake(view, 20, 5);
                mVibrator.vibrate(1000);
                mp.start();
            } else {
                isWarning = true;
                view.clearAnimation();
//                mp.stop();
            }
        }
    };


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new Home();
                    break;
                case R.id.navigation_dashboard:
                    fragment = new Camera();
                    break;
                case R.id.navigation_notifications:
                    break;
            }
            return loadFragment(fragment);
        }
    };

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mp.release();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mReceiver);

    }

    public static View makeMeShake(View view, int duration, int offset) {
        Animation anim = new TranslateAnimation(-offset,offset,0,0);
        anim.setDuration(duration);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(10);
        view.startAnimation(anim);
        return view;
    }

    /*** menu ***/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*** request permission ***/
    private void checkPermission(){
        this.permission = new Permission(this, getApplicationContext(), mainLayout);
        this.permission.requestCameraRequest();
        this.permission.requestReadExternalStorageRequest();
        this.permission.requestWriteExternalStorageRequest();
    }

    public void setNavigationVisibility(boolean visible) {
        if (navView.isShown() && !visible) {
            navView.setVisibility(View.GONE);
        }
        else if (!navView.isShown() && visible){
            navView.setVisibility(View.VISIBLE);
        }
    }

}
