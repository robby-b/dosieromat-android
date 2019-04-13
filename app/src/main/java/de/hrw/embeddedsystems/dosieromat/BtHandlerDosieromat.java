package de.hrw.embeddedsystems.dosieromat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
Diese Klasse ist für alles bzgl. der BLE-Kommunikation zuständig
 */
public class BtHandlerDosieromat {
    private static final UUID SERVICE_UUID = UUID.fromString("a964d61b-a5b3-4b5c-9e1e-65029b3d936d");
    private static final UUID CHARACTERISTIC_UUID_RX =  UUID.fromString("9c731b8a-9088-48fe-8f8a-9bc071a3784b");
    private static final UUID CHARACTERISTIC_UUID_TX =  UUID.fromString("8d0c925d-0f4a-4c4b-bfc3-758c89282220");

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    private static final int SCAN_PERIOD = 10000;

    private static final String DEBUG_TAG = "BT_DEBUG";

    private Context mContext;
    private boolean mScanning;
    private Map<String, BluetoothDevice> mScanResults;
    private boolean mConnected;
    private BluetoothAdapter mBlueToothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private ScanCallback mScanCallback;
    private Handler mHandler;

    private boolean mWriteInitalized;
    private boolean writeFinished;
    private BluetoothDevice mDosieromat;

    private BluetoothGatt mGatt;

    public BtHandlerDosieromat(BluetoothAdapter bluetoothAdapter, Context context) {
        mBlueToothAdapter = bluetoothAdapter;
        mBluetoothScanner = mBlueToothAdapter.getBluetoothLeScanner();
        mContext = context;
    }

    // Startet den Scan nach dem Mikrocontroller
    public void startScan() {
        if(mScanning) {
            return;
        }

        List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_UUID)).build()); // Filter sodass ausschließlich nach dem Dosieromaten gescannt wird
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

        mScanResults = new HashMap<>();
        mScanCallback = new BtLeScanCallback(mScanResults);

        mBluetoothScanner.startScan(filters, settings, mScanCallback);

        mHandler = new Handler();
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);


        mScanning = true;
        Log.d(DEBUG_TAG, "Started scanning");
    }

    private void stopScan() {
        if(!mScanning) {
            return;
        }

        mBluetoothScanner.stopScan(mScanCallback);
        scanComplete();

        mScanCallback = null;
        mScanning = false;
        mHandler  = null;
        Log.d(DEBUG_TAG, "Stopped scanning");
    }

    // sendet eine Nachricht an den Mikrocontroller, genutzt zur Übertragung der Befehle
    public void sendMessage(String message) {
        mWriteInitalized = true;

        if (mConnected && mWriteInitalized) {
            writeFinished = false;
            BluetoothGattService service = mGatt.getService(SERVICE_UUID);
            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(CHARACTERISTIC_UUID_RX);

            byte[] messageBytes;
            messageBytes = message.getBytes(StandardCharsets.UTF_8);
            characteristicWrite.setValue(messageBytes);

            boolean success = mGatt.writeCharacteristic(characteristicWrite);
        }
    }

    // Wird ausgeführt wenn der Scan abgeschlossen ist
    private void scanComplete() {
        if(!mScanResults.isEmpty()) {
            // Da nur nach dem Mikrocontroller gesucht wird, wurde er hier gefunden
            for (String deviceAddress : mScanResults.keySet()) {
                Log.d(DEBUG_TAG, "Found device: " + deviceAddress + mScanResults.get(deviceAddress).getName());
                mDosieromat = mScanResults.get(deviceAddress);
            }

            connectToDosieromat();
        } else {
            // Wenn der Mikrocontroller nicht gefunden wird, wird automatisch ein neuer scan gestartet
            startScan();
        }
    }

    public BluetoothDevice getDosieromat() {
        return mDosieromat;
    }

    public void connectToDosieromat() {
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = mDosieromat.connectGatt(mContext, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE);
    }

    public void disconnectGattServer() {
        Log.d(DEBUG_TAG, "Closing Gatt connection");

        mConnected = false;
        mWriteInitalized = false;

        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }

        connectToDosieromat();
    }

    // Innere Klasse um die ScanCallbacks angepasst zu überschreiben
    private class BtLeScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> mScanResults;

        private BtLeScanCallback(Map<String, BluetoothDevice> scanResults) {
            mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
            stopScan();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(DEBUG_TAG, "BATH SCAN RESULT");

            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(DEBUG_TAG, "BT Scan failed with error code: " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            mScanResults.put(result.getDevice().getAddress(), result.getDevice());
        }
    }

    // Innere Klasse um die allgemeine BT Callbacks angepasst zu überschreiben
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(DEBUG_TAG, "onConnectionStateChange newState: " + newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.d(DEBUG_TAG, "Connection Gatt failure status " + status);
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                Log.d(DEBUG_TAG, "Connection not GATT sucess status " + status);
                disconnectGattServer();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(DEBUG_TAG, "Connected to device " + gatt.getDevice().getAddress());

                mConnected = true;

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(DEBUG_TAG, "Disconnected from device");
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if(status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            BluetoothGattCharacteristic characteristicNotify = service.getCharacteristic(CHARACTERISTIC_UUID_TX);

            gatt.setCharacteristicNotification(characteristicNotify, true);

            BluetoothGattDescriptor desc = characteristicNotify.getDescriptors().get(0);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);

            BluetoothGattCharacteristic characteristicWrite = service.getCharacteristic(CHARACTERISTIC_UUID_RX);
            characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mWriteInitalized = gatt.setCharacteristicNotification(characteristicWrite, true);
        }

        private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
            if (characteristicWriteSuccess) {
                Log.d(DEBUG_TAG, "Characteristic notification set successfully for " + characteristic.getUuid().toString());
            } else {
                Log.d(DEBUG_TAG, "Characteristic notification set failure for " + characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            byte[] messageBytes = characteristic.getValue();
            Log.d(DEBUG_TAG, "Received:");
            String messageString;
            messageString = new String(messageBytes, StandardCharsets.UTF_8);
            if(messageString.startsWith("PROGRESS;")) {
                int progressPercent = Integer.parseInt(messageString.split(";")[1]);
                Log.d(DEBUG_TAG, "Progress: " + progressPercent);
            }

            Log.d(DEBUG_TAG, "Received message: " + messageString);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if(status == BluetoothGatt.GATT_SUCCESS) {
                writeFinished = true;
                Log.d(DEBUG_TAG, "sent message: " + characteristic.getStringValue(0));
            }
        }
    }
}
