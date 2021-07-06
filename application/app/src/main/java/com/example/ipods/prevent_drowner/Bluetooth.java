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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.util.Hex;

import static com.example.ipods.prevent_drowner.Common.CCCD;
import static com.example.ipods.prevent_drowner.Common.RX_CHAR_UUID;
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


    // ble adapter
    public BluetoothAdapter ble_adapter_;
    // flag for scanning
    private boolean is_scanning_ = false;
    // flag for connection
    public boolean connected_ = false;
    // flag for device availablity
    private boolean device_found_ = false;

    // scan results
    private Map<String, BluetoothDevice> scan_results_;
    // scan callback
    private ScanCallback scan_cb_;
    // ble scanner
    private BluetoothLeScanner ble_scanner_;
    // ble gatt
    private BluetoothGatt ble_gatt_;

    /*** scan results ***/
    private int empty_results_count = 0;

    boolean bCheckBT = false;

    public static String DEVICE_NAME = "AMMONATE_0000";
    //public static String DEVICE_NAME = "Big9";
    //public static String DEVICE_NAME = "GWBMD01_UART";

    // ble manager
    private BluetoothManager ble_manager;

    //RSSI delay
    private int rssiDelay = 5000;
    private int sleepModeOnCount = 0;
    private int sleepModeOffCount = 0;
    private boolean isSleepModeOn = false;

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        /*** bluetooth ***/
        this.setBluetoothSetting();
        //startScan();

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
        ble_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        /*** ble manager ***/
        if(ble_manager == null) {
            ble_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }

        /*** set ble adapter ***/
        ble_adapter_ = ble_manager.getAdapter();

        if(ble_scanner_ == null){
            ble_scanner_ = ble_adapter_.getBluetoothLeScanner();
        }

        scan_results_ = new HashMap<>();
        scan_cb_ = new BLEScanCallback(scan_results_);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "service unbound");

        /*** BT status ***/
        unregisterReceiver(bluetoothStateReceiver);

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (ble_manager == null) {
            ble_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (ble_manager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                Toast.makeText(this, "블루투스를 활성화 시켜야합니다", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        ble_adapter_ = ble_manager.getAdapter();

        if (ble_adapter_ == null) {
            Toast.makeText(this, "블루투스를 활성화 시켜야합니다", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    //-- Scan Result -- Getting feedback from Scan results/
    private class BLEScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> cb_scan_results_;

        /* Constructor */
        BLEScanCallback(Map<String, BluetoothDevice> _scan_results) {
            cb_scan_results_ = _scan_results;
        }

        @Override
        public void onScanResult(int _callback_type, ScanResult _result) {
            Log.d(TAG, "onScanResult");
            addScanResult(_result);

            if (_result.getDevice().getName().equals(DEVICE_NAME)) {
                stopScan();
                return;
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> _results) {
            for (ScanResult result : _results) {
                addScanResult(result);

                if (result.getDevice().getName().equals(DEVICE_NAME)) {
                    stopScan();
                    return;
                }
            }
        }

        @Override
        public void onScanFailed(int _error) {
            Log.e(TAG, "BLE scan failed with code " + _error);
        }

        /* Add scan result */
        private void addScanResult(ScanResult _result) {
            // get scanned device
            BluetoothDevice device = _result.getDevice();
            // get scanned device MAC address
            String device_address = device.getAddress();
            // add the device to the result list
            cb_scan_results_.put(device_address, device);
            // log
            Log.d(TAG, "scan results device: " + device);
        }
    }

    /*** Start BLE scan ***/
    public void startScan() {
        Log.d(TAG, "Starting Scan");

        // check ble adapter and ble enabled
        if (ble_adapter_ == null || !ble_adapter_.isEnabled()) {
            //requestEnableBLE();
            Log.d(TAG, "Scanning Failed: ble not enabled");
            return;
        }
        // disconnect gatt server
        disconnectGattServer();

        List<ScanFilter> filters = new ArrayList<>();
        //ScanFilter scan_filter= new ScanFilter.Builder().build();
        ScanFilter scan_filter = new ScanFilter.Builder()
                .setDeviceName(DEVICE_NAME)
                .build();
        filters.add(scan_filter);

        //// scan settings
        // set low power scan mode
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        scan_results_ = new HashMap<>();
        scan_cb_ = new BLEScanCallback(scan_results_);

        //// now ready to scan
        // start scan
        scanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, 5000);

        ble_scanner_.startScan(filters, settings, scan_cb_);
        // set scanning flag
        is_scanning_ = true;
    }

    private void stopScan() {
        // check pre-conditions
        if (is_scanning_ && ble_adapter_ != null && ble_adapter_.isEnabled() && ble_scanner_ != null) {
            // stop scanning
            ble_scanner_.stopScan(scan_cb_);
            scanComplete();
        }
        // reset flags
        scan_cb_ = null;
        is_scanning_ = false;
    }


    /*** Handle scan results after scan stopped ***/
    private void scanComplete() {

        boolean anyOtherAmmoniteDevice = false;

        empty_results_count = 0;

        // loop over the scan results and connect to them
        for (String device_addr : scan_results_.keySet()) {
            // get device instance using its MAC address
            BluetoothDevice device = scan_results_.get(device_addr);
            if(device.getName().equals(DEVICE_NAME)){
                connectDevice(device);
            }
        }

        //No wanted results
        Log.d(TAG, "no wanted device found");
    }

    /*** Connect to the ble device ***/
    private void connectDevice(BluetoothDevice _device) {
        device_found_ = true ;

        // update the status
        GattClientCallback gatt_client_cb = new GattClientCallback();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ble_gatt_ = _device.connectGatt(this, false, gatt_client_cb, 2);
        }
    }

    boolean SleepModeOn = false;
    int SleepModeRssi = 0;

    /* Gatt Client Callback class */
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt _gatt, int _status, int _new_state) {
            super.onConnectionStateChange(_gatt, _status, _new_state);

            if (_new_state == BluetoothProfile.STATE_CONNECTED) {
                bCheckBT = true;
                connected_ = true;
                device_found_= true;

                Log.d(TAG, "Connected to the GATT server");
                _gatt.discoverServices();



            } else if (_new_state == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from the GATT server");

                bCheckBT = false;
                connected_ = false;
                device_found_ = false;
                disconnectGattServer();
            }
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
                sendData(0);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

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

                try {
                    String decodedMsg = new String(characteristic.getValue(), "UTF-8");
                    Log.d(TAG, "Characteristic : " + decodedMsg);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Characteristic read unsuccessful, status: " + status);
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }
    }

    private final Runnable sleepModeRunnable = new Runnable() {
        @Override
        public void run() {
            SleepModeOn = false;
        }
    };

    //Enables receive of MSG from LED
    public void enableTXNotification() {
        BluetoothGattService RxService = ble_gatt_.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            Log.e(TAG, "TXNotification : No RxService");
            //showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);

        if (TxChar == null) {
            Log.e(TAG, "TXNotification : No TxService");
            //showMessage("Tx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        ble_gatt_.setCharacteristicNotification(TxChar, true);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        ble_gatt_.writeDescriptor(descriptor);
    }

    /*** Disconnect Gatt Server ***/
    public void disconnectGattServer() {

        // reset the connection flag
        connected_ = false;
        // disconnect and close the gatt
        if (ble_gatt_ != null) {
            ble_gatt_.disconnect();
            ble_gatt_.close();
            ble_gatt_= null;
        }
    }

    /*** send data to BT***/
    public void sendData(int iint1) {
        // check connection
        try {
            if (!connected_) {
                Log.e(TAG, "Failed to sendData due to no connection");
                return;
            }

            BluetoothGattService RxService = ble_gatt_.getService(RX_SERVICE_UUID);

            // find command characteristics from the GATT server
            BluetoothGattCharacteristic cmd_characteristic = RxService.getCharacteristic(RX_CHAR_UUID);

            // disconnect if the characteristic is not found
            if (cmd_characteristic == null) {
                Log.e(TAG, "Unable to find cmd characteristic");
                disconnectGattServer();
                //mcourseStartbtn.setClickable(true);
                return;
            }

            startLED(cmd_characteristic, 1);

        } catch (Exception e) {
            //
        }
    }


    /*
   Start stimulation
   @param cmd_characteristic command characteristic instance
   @param program_id stimulation program id
    */
    private void startLED(BluetoothGattCharacteristic _cmd_characteristic, final int _program_id) {
        Log.d(TAG, "Request starting LED");

        // set values to the characteristicBluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        _cmd_characteristic.setValue(protocol_CONNECT_BT);
        // write the characteristic
        boolean success = ble_gatt_.writeCharacteristic(_cmd_characteristic);
        // check the result
        if (success) {
            Log.d(TAG, Hex.bytesToStringUppercase(protocol_CONNECT_BT));

        } else {
            //og.e(TAG, "Failed to write command");
        }
    }

    public static final byte[] protocol_CONNECT_BT   = {0x42, 0x31, 0x30, 0x30, 0x73};

}
