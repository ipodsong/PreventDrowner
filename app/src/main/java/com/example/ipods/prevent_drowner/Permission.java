package com.example.ipods.prevent_drowner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import static com.example.ipods.prevent_drowner.Common.PERMISSION_CAMERA;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_READ_STORAGE;
import static com.example.ipods.prevent_drowner.Common.PERMISSION_WRITE_STORAGE;

public class Permission {
    private Context mContext;
    private LinearLayout mView;
    private FrameLayout fView;
    private Activity mActivity;

    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public Permission(Activity activity, Context mContext, LinearLayout view){
        this.mContext = mContext;
        this.mActivity = activity;
        this.mView= view;
    }

    public Permission(Activity activity, Context mContext, FrameLayout view){
        this.mContext = mContext;
        this.mActivity = activity;
        this.fView= view;
    }

    public boolean requestCameraRequest(){
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            int cameraPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);

            if(cameraPermission == PackageManager.PERMISSION_GRANTED){
                return true;
            }else{
                Snackbar.make(mView, "이 앱을 실행하려면 카메라 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions( mActivity, REQUIRED_PERMISSIONS,
                                PERMISSION_CAMERA);
                    }
                }).show();
            }
        }

        return false;
    }

    public boolean requestWriteExternalStorageRequest(){
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else{
            Snackbar.make(mView, "이 앱을 실행하려면 외부 저장소 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions( mActivity, REQUIRED_PERMISSIONS,
                            PERMISSION_WRITE_STORAGE);
                }
            }).show();
        }
        return false;
    }

    public boolean requestReadExternalStorageRequest(){
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else{
            Snackbar.make(mView, "이 앱을 실행하려면 외부 저장소 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions( mActivity, REQUIRED_PERMISSIONS,
                            PERMISSION_READ_STORAGE);
                }
            }).show();
        }
        return false;
    }
}
