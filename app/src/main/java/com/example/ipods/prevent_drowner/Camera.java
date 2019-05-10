package com.example.ipods.prevent_drowner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.ipods.prevent_drowner.Common.CAMERA_IMAGE_URI;
import static com.example.ipods.prevent_drowner.Common.CAPTURED_IMG_URI;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_CAMERA;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_WRITE_STORAGE;
import static com.example.ipods.prevent_drowner.Common.TO_HOME_FRAG;
import static com.example.ipods.prevent_drowner.Common.cameraFragmentIndex;
import static com.example.ipods.prevent_drowner.Common.homeFragmentIndex;

public class Camera extends Fragment implements View.OnClickListener{
    private final String TAG = "CameraActivity";
    private LinearLayout mainLayout;

    private Common common;

    /*** database ***/
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;

    /*** permission ***/
    private Permission permission;
    private boolean isCameraEnabled = false;
    private boolean isWriteStorageEnabled = false;

    /*** camera ***/
    private CameraView mCameraView;

    /*** camera functions ***/
    private FloatingActionButton mCameraSwapBtn;
    private ImageButton mCameraCaptureBtn;

    /*** background Handler ***/
    private Handler mBackgroundHandler;

    /*** image uri ***/
    private String imageUri;

    /*** camera facing ***/
    private static final int CAMERA_FACING_FRONT = CameraView.FACING_FRONT;
    private static final int CAMERA_FACING_BACK =CameraView.FACING_BACK;
    private int CURRENT_FACING = CAMERA_FACING_BACK;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        common = new Common();
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefEditor = mPref.edit();
        ((MainActivity)getActivity()).currentFragment = cameraFragmentIndex;
        return inflater.inflate(R.layout.activity_camera, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainLayout = (LinearLayout)view.findViewById(R.id.main_layout);

        mCameraView =(CameraView)view.findViewById(R.id.preview_camera);
        mCameraView.addCallback(mCallback);
        checkPermission();

        mCameraSwapBtn = (FloatingActionButton)view.findViewById(R.id.camera_flip_camera_btn);
        mCameraSwapBtn.setOnClickListener(this);

        mCameraCaptureBtn = (ImageButton)view.findViewById(R.id.cameraCapture);
        mCameraCaptureBtn.setOnClickListener(this);

        ((MainActivity)getActivity()).setNavigationVisibility(false);
    }




    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.camera_flip_camera_btn:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                break;
            case R.id.cameraCapture:
                if(mCameraView != null){
                    mCameraView.takePicture();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPrefEditor.putString("CAPTURED_IMG_URI", imageUri);
                            mPrefEditor.commit();

                            //common.BroadCastUpdate(getContext(), CAPTURED_IMG_URI, "imageUri",imageUri);
                            common.BroadCastUpdate(getContext(), TO_HOME_FRAG);
                        }

                    }, 2000);
                }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mCameraView.stop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case PERMISSION_CAMERA:
                if(resultCode == PackageManager.PERMISSION_GRANTED){
                    if(isCameraEnabled && isWriteStorageEnabled){
                        mCameraView.start();
                    }
                }
                break;
            case PERMISSION_WRITE_STORAGE:
                if(resultCode == PackageManager.PERMISSION_GRANTED){
                    if(isCameraEnabled && isWriteStorageEnabled){
                        mCameraView.start();
                    }
                }
                break;
        }
    }

    /*** camera callback ***/
    private CameraView.Callback mCallback = new CameraView.Callback(){

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            super.onPictureTaken(cameraView, data);

            Log.d(TAG, "onPictureTaken " + data.length);

            Snackbar.make(mainLayout, "사진을 촬영하셨습니다",
                    Snackbar.LENGTH_SHORT).show();

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
            final String fileName =  "prevent_drowner_"+sdf.format(cal.getTime());

            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            fileName + ".jpg");
                    Log.d(TAG, "fileDir : " + file.getAbsoluteFile() );
                    imageUri = file.getAbsoluteFile().toString();

                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(file);
                        os.write(data);
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Cannot write to " + file, e);
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                    }
                }
            });


        }
    };

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }


    /*** request permission ***/
    private void checkPermission(){
        this.permission = new Permission(getActivity(), getContext(), mainLayout);
        this.permission.requestWriteExternalStorageRequest();

        if(this.permission.requestCameraRequest() && this.permission.requestReadExternalStorageRequest()){
            mCameraView.start();
        }
    }
}
