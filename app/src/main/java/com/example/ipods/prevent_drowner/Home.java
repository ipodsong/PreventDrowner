package com.example.ipods.prevent_drowner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.IOException;

import static com.example.ipods.prevent_drowner.Common.CAMERA_IMAGE_URI;
import static com.example.ipods.prevent_drowner.Common.CAPTURED_IMG_URI;
import static com.example.ipods.prevent_drowner.Common.TO_CAMERA_FRAG;
import static com.example.ipods.prevent_drowner.Common.TO_HOME_FRAG;
import static com.example.ipods.prevent_drowner.Common.homeFragmentIndex;

public class Home extends Fragment implements View.OnClickListener{

    private Common common;

    /*** database ***/
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;

    private final String TAG = "HOME_FRAG";
    private LinearLayout mainLayout;

    /*** ImageView ***/
    private Bitmap mImageBitmap;
    private ImageView childImage;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        common = new Common();
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefEditor = mPref.edit();
        ((MainActivity)getActivity()).currentFragment = homeFragmentIndex;
        return inflater.inflate(R.layout.activity_home, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "view created");

        ((MainActivity)getActivity()).setNavigationVisibility(true);

        mainLayout = (LinearLayout)view.findViewById(R.id.main_layout);
        childImage = (ImageView)view.findViewById(R.id.main_take_picture_btn);
        childImage.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TO_HOME_FRAG);
        filter.addAction(TO_CAMERA_FRAG);
        filter.addAction(CAPTURED_IMG_URI);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        if(mPref.getString("CAPTURED_IMG_URI", null)!= null){
            childImage.setPadding(0, 0, 0, 0);
            childImage.setScaleType(ImageView.ScaleType.FIT_XY);

            Glide.with(getContext()).load(mPref.getString("CAPTURED_IMG_URI", null))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(childImage);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }




    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.main_take_picture_btn:
                common.BroadCastUpdate(getContext(), TO_CAMERA_FRAG);
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case CAPTURED_IMG_URI:
                    if (intent.hasExtra("imageUri")) {
                        File file = new File(intent.getStringExtra("imageUri"));
                        //imageUri = Uri.fromFile(file);

                        Log.d(TAG, "mReceiver");
                    }else {
                        Snackbar.make(mainLayout, "사진 파일을 찾지 못했습니다", Snackbar.LENGTH_LONG).show();
                    }


                    break;
            }
        }
    };
}
