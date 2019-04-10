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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    int amount = 1;

    private static final String DOSIEROMAT_BT_NAME = "Dosieromat Proto";

    private static final int RQS_ENABLE_BLUETOOTH = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 20;

    private BtHandlerDosieromat mBtHandler;
    private List<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            RecipeXmlParser xmlParser = new RecipeXmlParser(getResources().getXml(R.xml.recipes));
            recipes = xmlParser.parseRecipeXml();
            for (Recipe r : recipes) {
                Log.d("RecipeList", r.toString());
            }
        }
        catch (XmlPullParserException | IOException e) {
            Log.d("RecipeList", Log.getStackTraceString(e));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.decreaseAmountBtn).setOnClickListener(onClickListener);
        findViewById(R.id.increaseAmountBtn).setOnClickListener(onClickListener);
        findViewById(R.id.sendBtn).setOnClickListener(onClickListener);
//        findViewById(R.id.scanBtn).setOnClickListener(onClickListener);
//        findViewById(R.id.connectBtn).setOnClickListener(onClickListener);


        updateAmountTxt();
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtHandler = new BtHandlerDosieromat(btManager.getAdapter(), this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        mBtHandler.startScan();
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

                } else {
                    Toast.makeText(this,
                            "no permission granted",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

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
                    // Hardcoded recipe number for coffee
                    List<String> commands = recipes.get(0).toCommandList(amount);

                    for (String c : commands) {
                        mBtHandler.sendMessage(c);
                    }
                    break;
            }

        }
    };

    private void updateAmountTxt() {
        TextView amountTxt = (TextView) findViewById(R.id.amountTxt);

        if (amount == 1) {
            amountTxt.setText(amount + " Tasse");
        }
        else if (amount > 0 && amount <= 8) {
            amountTxt.setText(amount + " Tassen");
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
            Toast.makeText(getApplicationContext(), "Minimale Anzahl an Tassen erreicht!", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseAmount() {
        if(amount < 8) {
            amount++;
            updateAmountTxt();
        }
        else {
            Toast.makeText(getApplicationContext(), "Maximale Anzahl an Tassen erreicht!", Toast.LENGTH_SHORT).show();
        }
    }
}

//                case R.id.scanBtn:
//                        mBtHandler.startScan();
//                        findViewById(R.id.connectBtn).setEnabled(true);
//                        break;
//                        case R.id.connectBtn:
//                        mBtHandler.connectToDosieromat();
//                        break;