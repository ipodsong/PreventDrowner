package com.example.ipods.prevent_drowner;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class Common {
    /*** fragment index ***/
    public final static int homeFragmentIndex   = 0;
    public final static int cameraFragmentIndex = 1;

    /*** Fragment Movement Req ***/
    public final static String TO_HOME_FRAG = "MOVE_TO_HOME";
    public final static String TO_CAMERA_FRAG = "MOVE_TO_CAMERA";
    public final static String CAPTURED_IMG_URI = "IMAGE_URI";

    /*** Activity Result ***/
    public final static int CAMERA_REUQEST = 0;

    /*** Image ***/
    public final static String CAMERA_IMAGE_URI = "PICTURE_TAKEN_CAMERA_URI";

    /*** Permission ****/
    public static final int PERMISSION_CAMERA = 1;
    public static final int PERMISSION_WRITE_STORAGE = 2;
    public static final int PERMISSION_READ_STORAGE = 3;

    public void BroadCastUpdate(Context context, final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void BroadCastUpdate(Context context, final String action, String dataName, String data) {
        final Intent intent = new Intent(action);
        intent.putExtra(dataName, data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
