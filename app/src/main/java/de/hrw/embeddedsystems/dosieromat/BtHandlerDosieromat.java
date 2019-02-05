package de.hrw.embeddedsystems.dosieromat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BtHandlerDosieromat {
    private static final String SERVICE_UUID = "a964d61b-a5b3-4b5c-9e1e-65029b3d936d";
    private static final String CHARACTERISTIC_UUID_RX = "9c731b8a-9088-48fe-8f8a-9bc071a3784b";
    private static final String CHARACTERISTIC_UUID_TX = "8d0c925d-0f4a-4c4b-bfc3-758c89282220";

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

    private BluetoothDevice mDosieromat;

    private BluetoothGatt mGatt;

    public BtHandlerDosieromat(BluetoothAdapter bluetoothAdapter, Context context) {
        mBlueToothAdapter = bluetoothAdapter;
        mBluetoothScanner = mBlueToothAdapter.getBluetoothLeScanner();
        mContext = context;
    }

    public void startScan() {
        if(mScanning) {
            return;
        }

        List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SERVICE_UUID)).build());
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

    private void scanComplete() {
        if(!mScanResults.isEmpty()) {
            for (String deviceAddress : mScanResults.keySet()) {
                Log.d(DEBUG_TAG, "Found device: " + deviceAddress + mScanResults.get(deviceAddress).getName());
                mDosieromat = mScanResults.get(deviceAddress);
            }
        }
    }

    public BluetoothDevice getDosieromat() {
        return mDosieromat;
    }

    public void connectToDosieromat() {
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = mDosieromat.connectGatt(mContext, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE);
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void disconnectGattServer() {
        Log.d(DEBUG_TAG, "Closing Gatt connection");

        mConnected = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

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
                setConnected(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(DEBUG_TAG, "Disconnected from device");
                disconnectGattServer();
            }
        }


    }
}
