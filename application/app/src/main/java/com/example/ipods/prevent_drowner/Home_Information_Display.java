package com.example.ipods.prevent_drowner;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

public class Home_Information_Display extends Fragment implements View.OnClickListener{

    /*** widget ***/
    private TextView displayChildName;
    private TextView displayChildAge;
    private TextView displayChildWeight;

    /*** alert level ***/
    private int alertLevel = 0;
    private Animation alertAnimation;
    public Vibrator mVibrator;

    /*** alert sound ***/
    private AudioManager audioManager;
    private MediaPlayer mp;


    /*** floating action button ***/
    private FloatingActionButton mainFloatingActionButton;
    private FloatingActionButton bluetoothConnectFloatingActionButton, alarmFloatingActionButton, cautionFloatingActionButton;

    private boolean isFabOpen = false;

    private Animation fab_open;
    private Animation fab_close;

    public static Home_Information_Display newInstance(String name, int age, float weight) {
        Home_Information_Display fragment = new Home_Information_Display();
        Bundle args = new Bundle();
        args.putString(Common.CHILD_NAME, name);
        args.putInt(Common.CHILD_AGE, age);
        args.putFloat(Common.CHILD_WEIGHT, weight);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        alertAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alert);

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamVolume(AudioManager.STREAM_RING),
                AudioManager.FLAG_ALLOW_RINGER_MODES);

        mp = MediaPlayer.create(getContext(), R.raw.alert);
        mp.setVolume(1.0f, 1.0f);

        return inflater.inflate(R.layout.activity_home_information_display, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.actionButton);
        mainFloatingActionButton.setOnClickListener(fabOnClickListener);

        alarmFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.noticeLifeGuard);
        cautionFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.alertButton);
        bluetoothConnectFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.connectBluetoothButton);

        alarmFloatingActionButton.setOnClickListener(this);
        cautionFloatingActionButton.setOnClickListener(this);
        bluetoothConnectFloatingActionButton.setOnClickListener(this);

        displayChildName = (TextView)view.findViewById(R.id.displayChildName);
        displayChildAge = (TextView)view.findViewById(R.id.displayChildAge);
        displayChildWeight = (TextView)view.findViewById(R.id.displayChildWeight);

        displayChildName.setText(getArguments().getString(Common.CHILD_NAME));
        displayChildAge.setText(String.valueOf(getArguments().getInt(Common.CHILD_AGE)));
        displayChildWeight.setText(String.valueOf(getArguments().getFloat(Common.CHILD_WEIGHT)));
    }

    private FloatingActionButton.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setFloatingActionButtonAnimation();
        }
    };

    public void setFloatingActionButtonAnimation() {
        if (isFabOpen) {
            alarmFloatingActionButton.startAnimation(fab_close);
            cautionFloatingActionButton.startAnimation(fab_close);
            bluetoothConnectFloatingActionButton.startAnimation(fab_close);
            alarmFloatingActionButton.setClickable(false);
            cautionFloatingActionButton.setClickable(false);
            isFabOpen = false;
        } else {
            alarmFloatingActionButton.startAnimation(fab_open);
            cautionFloatingActionButton.startAnimation(fab_open);
            bluetoothConnectFloatingActionButton.startAnimation(fab_open);
            alarmFloatingActionButton.setClickable(true);
            cautionFloatingActionButton.setClickable(true);

            isFabOpen = true;
        }
    }




    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.noticeLifeGuard:
                break;
            case R.id.alertButton:
                alertLevel++;

                Toast.makeText(getContext(), "alertButton", Toast.LENGTH_LONG).show();
                switch(alertLevel){
                    case 0:
                        cautionFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.caution_level_0));
                        break;
                    case 1:
                        cautionFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.caution_level_1));
                        break;
                    case 2:
                        cautionFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.caution_level_2));
                        mVibrator.vibrate(1000);
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.release();
                            }
                        });
                        break;

                    default:
                        cautionFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.caution_level_2));
                        mVibrator.vibrate(1000);
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.release();
                            }
                        });
                        break;

                }

                break;
            case R.id.connectBluetoothButton:
                ((Bluetooth)((MainActivity)getActivity()).bluetoothService).startScan();
                break;
        }
    }
}
