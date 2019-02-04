// sources:
// http://android-er.blogspot.com/2016/06/scan-specified-ble-devices-with.html
// https://medium.com/@avigezerit/bluetooth-low-energy-on-android-22bc7310387a

package de.hrw.embeddedsystems.dosieromat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    int amount = 1;

    private static final String SERVICE_UUID = "a964d61b-a5b3-4b5c-9e1e-65029b3d936d";
    private static final String RX_UUID = "9c731b8a-9088-48fe-8f8a-9bc071a3784b";
    private static final String TX_UUID = "8d0c925d-0f4a-4c4b-bfc3-758c89282220";

    private static final String DOSIEROMAT_BT_NAME = "Dosieromat Proto";

    private BluetoothDevice dosieromatDevice;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private boolean mScanning;

    private static final int RQS_ENABLE_BLUETOOTH = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 20;

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;

    // copy end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.decreaseAmountBtn).setOnClickListener(onClickListener);
        findViewById(R.id.increaseAmountBtn).setOnClickListener(onClickListener);
        findViewById(R.id.sendBtn).setOnClickListener(onClickListener);

        updateAmountTxt();

        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,
                    "BLUETOOTH_LE not supported in this device!",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();
        mScanning = false;

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "bluetoothManager.getAdapter()==null",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
        else {
            scanLeDevice(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    scanLeDevice(true);
                } else {
                    Toast.makeText(this,
                            "no permission granted",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /*
 to call startScan (ScanCallback callback),
 Requires BLUETOOTH_ADMIN permission.
 Must hold ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get results.
  */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(scanCallback);

                    Toast.makeText(MainActivity.this,
                            "Scan timeout",
                            Toast.LENGTH_LONG).show();

                    mScanning = false;
                }
            }, SCAN_PERIOD);

            //mBluetoothLeScanner.startScan(scanCallback);

            //scan specified devices only with ScanFilter
            // TODO: implement scanfilter instead of if in onScanResult
//            ScanFilter scanFilter = new ScanFilter.Builder()
//                            .setDeviceName("Dosieromat Proto")
//                            .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
//                            .build();
            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
            //scanFilters.add(scanFilter);

            ScanSettings scanSettings =
                    new ScanSettings.Builder().build();

            mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
            startScanTxt();
            mScanning = true;
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);
            mScanning = false;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, RQS_ENABLE_BLUETOOTH);
            }
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if(result.getDevice().getName() != null && result.getDevice().getName().equals(DOSIEROMAT_BT_NAME)) {
                scanLeDevice(false);
                foundDevice(result.getDevice());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Toast.makeText(getApplicationContext(),
                    "found more than one device",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this,
                    "onScanFailed: " + String.valueOf(errorCode),
                    Toast.LENGTH_LONG).show();
        }
    };

    final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.decreaseAmountBtn:
                    decreaseAmount();
                    break;
                case R.id.increaseAmountBtn:
                    increaseAmount();
                    break;
                case R.id.sendBtn:
                    // TODO: Send coffee amount to ESP32
                    break;
            }

        }
    };

    private void startScanTxt() {
        TextView connectedTxt = findViewById(R.id.connectingTxt);
        connectedTxt.setText("Scanning");
    }

    private void foundDevice(BluetoothDevice bd) {
        TextView connectedTxt = findViewById(R.id.connectingTxt);
        dosieromatDevice = bd;

        //bd.createBond();


        connectedTxt.setText("Found Device");

        Toast.makeText(MainActivity.this,
                "bond state " + String.valueOf(bd.getBondState()),
                Toast.LENGTH_LONG).show();

        Log.w("[5466886]", bd.toString());
    }


    private void updateAmountTxt() {
        TextView amountTxt = (TextView) findViewById(R.id.amountTxt);

        if (amount == 1) {
            amountTxt.setText(amount + " Kanne");
        }
        else if (amount > 0 && amount <= 8) {
            amountTxt.setText(amount + " Kannen");
        }
        else {
            amountTxt.setText("Error");
        }
    }

    private void decreaseAmount() {
        if(amount > 1) {
            amount--;
            updateAmountTxt();
        }
        else {
            Toast.makeText(getApplicationContext(), "Minimale Anzahl an Kannen erreicht!", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseAmount() {
        if(amount < 8) {
            amount++;
            updateAmountTxt();
        }
        else {
            Toast.makeText(getApplicationContext(), "Maximale Anzahl an Kannen erreicht!", Toast.LENGTH_SHORT).show();
        }

    }
}
