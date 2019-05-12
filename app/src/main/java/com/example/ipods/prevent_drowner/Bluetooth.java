package com.example.ipods.prevent_drowner;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.ipods.prevent_drowner.Common.CCCD;
import static com.example.ipods.prevent_drowner.Common.RX_SERVICE_UUID;
import static com.example.ipods.prevent_drowner.Common.TX_CHAR_UUID;

public class Bluetooth extends Service {
    private final String TAG = "_service";

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private ScanCallback bluetoothScanCallback;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;

    private Handler scanHandler = new Handler();

    private final IBinder mBinder = new PreventDrowner();

    public class PreventDrowner extends Binder {
        Bluetooth getService() {
            return Bluetooth.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        this.setBluetoothSetting();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);


        return START_NOT_STICKY; // run until explicitly stopped.
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        setBluetoothSetting();
                        break;
                }
            }
        }
    };

    public void setBluetoothSetting(){
        /*** ble manager ***/
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

        /*** ble manager ***/
        if(bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }

        /*** set ble adapter ***/
        if(bluetoothAdapter == null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        if(bluetoothLeScanner == null){
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        bluetoothScanCallback = new BluetoothScanCallback();
    }

    public void startScan() {
        Log.d(TAG, "start scan");

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Scanning Failed: ble not enabled");
            return;
        }

        // check if location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //requestLocationPermission();
                Log.d(TAG, "Scanning Failed: no fine location permission");
                return;
            }
        }
        // disconnect gatt server
        disconnectGattServer();

        List<ScanFilter> filters = new ArrayList<>();
        //ScanFilter scan_filter= new ScanFilter.Builder().build();
        ScanFilter scan_filter = new ScanFilter.Builder()
                .setDeviceName(Common.BluetoothDeviceName)
                .build();
        filters.add(scan_filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        bluetoothScanCallback = new BluetoothScanCallback();
        bluetoothLeScanner.startScan(filters, settings, bluetoothScanCallback);

        scanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(bluetoothScanCallback);
            }
        }, 5000);
    }


    private class BluetoothScanCallback extends ScanCallback {
        private String TAG = "BluetoothScanCallback";

        @Override
        public void onScanResult(int _callback_type, ScanResult _result) {
            if(_result.getDevice().getName().equals(Common.BluetoothDeviceName)){
                connectDevice(_result.getDevice());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> _results) {
            for(ScanResult result : _results){
                if(result.getDevice().getAddress().equals(Common.BluetoothDeviceName)){
                    connectDevice(result.getDevice());
                    return;
                }
            }
        }

        @Override
        public void onScanFailed(int _error) {
            Log.e(TAG, "BLE scan failed with code " + _error);
        }
    }

    /*** Connect to the ble device ***/
    private void connectDevice(BluetoothDevice _device) {
        // update the status
        bluetoothGattCallBack bluetoothGattCallBack = new bluetoothGattCallBack();
        bluetoothLeScanner.stopScan(bluetoothScanCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = _device.connectGatt(this, false, bluetoothGattCallBack, 2);
        }
    }

    private class bluetoothGattCallBack extends BluetoothGattCallback {
        private final String TAG = "bluetoothGattCallBack";

        @Override
        public void onConnectionStateChange(BluetoothGatt _gatt, int _status, int _new_state) {
            super.onConnectionStateChange(_gatt, _status, _new_state);

            if (_new_state == BluetoothProfile.STATE_CONNECTED) {


            } else if (_new_state == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        public void enableTXNotification() {
            BluetoothGattService RxService = bluetoothGatt.getService(RX_SERVICE_UUID);
            if (RxService == null) {
                Log.e(TAG, "TXNotification : No RxService");
                return;
            }
            BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);

            if (TxChar == null) {
                Log.e(TAG, "TXNotification : No TxService");
                return;
            }

            bluetoothGatt.setCharacteristicNotification(TxChar, true);
            BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt _gatt, int _status) {
            super.onServicesDiscovered(_gatt, _status);

            if (_status == BluetoothGatt.GATT_SUCCESS) {
                enableTXNotification();
            }

            // check if the discovery failed
            if (_status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Device service discovery failed, status: " + _status);
                return;
            }

            // find discovered characteristics
            List<BluetoothGattCharacteristic> matching_characteristics = BluetoothUtils.findBLECharacteristics(_gatt);

            if (matching_characteristics.isEmpty()) {
                Log.e(TAG, "Unable to find characteristics");
                disconnectGattServer();
                return;
            }

            Log.d(TAG, "Services discovery is successful");
            // log for successful discovery
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            if (descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE && status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            try {
                byte[] msg = characteristic.getValue();
                String decodedMsg = new String(msg, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully");
            } else {
                Log.e(TAG, "Characteristic write unsuccessful, status: " + status);
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully");

                try {
                    byte[] msg = characteristic.getValue();
                    String decodedMsg = new String(msg, "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e(TAG, "Characteristic read unsuccessful, status: " + status);
            }
        }
    }

    public void disconnectGattServer() {
        // disconnect and close the gatt
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt= null;
        }
    }


}
