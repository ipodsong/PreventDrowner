package com.example.ipods.prevent_drowner;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.ipods.prevent_drowner.Common.RX_CHAR_UUID;
import static com.example.ipods.prevent_drowner.Common.RX_SERVICE_UUID;
import static com.example.ipods.prevent_drowner.Common.TX_CHAR_UUID;


/**
 * Created by KimCheolWoo on 2018-10-27.
 */

public class BluetoothUtils {
    /*
    Find characteristics of BLE
    @param gatt gatt instance
    @return list of found gatt characteristics
     */
    public static List<BluetoothGattCharacteristic> findBLECharacteristics(BluetoothGatt _gatt ) {
        List<BluetoothGattCharacteristic> matching_characteristics = new ArrayList<>();
        List<BluetoothGattService> service_list = _gatt.getServices();
        BluetoothGattService service = findGattService(service_list);
        if (service == null) {
            return matching_characteristics;
        }

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic : characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matching_characteristics.add(characteristic);
            }
        }

        return matching_characteristics;
    }

    /*
    Find command characteristic of the peripheral device
    @param gatt gatt instance
    @return found characteristic
     */
    @Nullable
    public static BluetoothGattCharacteristic findCommandCharacteristic( BluetoothGatt _gatt ) {
        return findCharacteristic( _gatt, TX_CHAR_UUID );
    }

    /*
    Find response characteristic of the peripheral device
    @param gatt gatt instance
    @return found characteristic
     */
    @Nullable
    public static BluetoothGattCharacteristic findResponseCharacteristic( BluetoothGatt _gatt ) {
        return findCharacteristic( _gatt, RX_CHAR_UUID );
    }

    /*
    Find the given uuid characteristic
    @param gatt gatt instance
    @param uuid_string uuid to query as string
     */
    @Nullable
    private static BluetoothGattCharacteristic findCharacteristic(BluetoothGatt _gatt, UUID _uuid_string) {
        List<BluetoothGattService> service_list= _gatt.getServices();
        BluetoothGattService service= BluetoothUtils.findGattService( service_list );
        if( service == null ) {
            return null;
        }

        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
        for( BluetoothGattCharacteristic characteristic : characteristicList) {
            if( matchCharacteristic( characteristic, _uuid_string ) ) {
                return characteristic;
            }
        }

        return null;
    }

    /*
    Match the given characteristic and a uuid string
    @param characteristic one of found characteristic provided by the server
    @param uuid_string uuid as string to match
    @return true if matched
     */
    private static boolean matchCharacteristic( BluetoothGattCharacteristic _characteristic, UUID _uuid ) {
        if( _characteristic == null ) {
            return false;
        }
        UUID uuid = _characteristic.getUuid();
        return matchUUIDs( uuid, _uuid );
    }

    /*
    Find Gatt service that matches with the server's service
    @param service_list list of services
    @return matched service if found
     */
    @Nullable
    private static BluetoothGattService findGattService(List<BluetoothGattService> _service_list) {
        for (BluetoothGattService service : _service_list) {
            UUID service_uuid = service.getUuid();
            if (matchServiceUUIDString(service_uuid)) {
                return service;
            }
        }
        return null;
    }

    /*
    Try to match the given uuid with the service uuid
    @param service_uuid_string service UUID as string
    @return true if service uuid is matched
     */
    private static boolean matchServiceUUIDString(UUID _service_uuid_string) {
        return matchUUIDs( _service_uuid_string, RX_SERVICE_UUID );
    }

    /*
    Check if there is any matching characteristic
    @param characteristic query characteristic
     */
    private static boolean isMatchingCharacteristic( BluetoothGattCharacteristic _characteristic ) {
        if( _characteristic == null ) {
            return false;
        }
        UUID uuid = _characteristic.getUuid();
        return matchCharacteristicUUID(uuid);
    }

    /*
    Query the given uuid as string to the provided characteristics by the server
    @param characteristic_uuid_string query uuid as string
    @return true if the matched is found
     */
    private static boolean matchCharacteristicUUID( UUID _characteristic_uuid ) {
        return matchUUIDs( _characteristic_uuid, TX_CHAR_UUID ,RX_CHAR_UUID);
    }

    /*
    Try to match a uuid with the given set of uuid
    @param uuid_string uuid to query
    @param matches a set of uuid
    @return true if matched
     */
    private static boolean matchUUIDs( UUID _uuid_string, UUID... _matches ) {
        for( UUID match : _matches ) {
            if( _uuid_string.equals(match) ) {
                return true;
            }
        }

        return false;
    }
}