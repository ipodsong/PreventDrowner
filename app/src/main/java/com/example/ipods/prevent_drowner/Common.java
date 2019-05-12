package com.example.ipods.prevent_drowner;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.UUID;

public class Common {
    /*** Fragment index ***/
    public final static int homeFragmentIndex   = 0;
    public final static int cameraFragmentIndex = 1;

    /*** Fragment Movement Req ***/
    public final static String TO_HOME_FRAG = "MOVE_TO_HOME";
    public final static String TO_CAMERA_FRAG = "MOVE_TO_CAMERA";
    public final static String CAPTURED_IMG_URI = "IMAGE_URI";

    /*** Activity Result ***/
    public final static int CAMERA_REUQEST      = 0;

    /*** Image ***/
    public final static String CAMERA_IMAGE_URI = "PICTURE_TAKEN_CAMERA_URI";


    /*** Child Information ***/
    public final static String CHILD_NAME       = "CHILD_INFORMATION_NAME";
    public final static String CHILD_AGE        = "CHILD_INFORMATION_AGE";
    public final static String CHILD_WEIGHT     = "CHILD_INFORMATION_WEIGHT";

    /*** Bluetooth ***/
    public final static String BluetoothDeviceName = "";

    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");




    /*** Permission ****/
    public static final int PERMISSION_CAMERA = 1;
    public static final int PERMISSION_WRITE_STORAGE = 2;
    public static final int PERMISSION_READ_STORAGE = 3;


    /*** Broadcast ***/

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
