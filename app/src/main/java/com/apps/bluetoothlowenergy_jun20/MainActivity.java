package com.apps.bluetoothlowenergy_jun20;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 2;
    BluetoothAdapter bluetoothAdapter;
    int  REQUEST_ENABLE_BT = 1;
    Handler handler;
    private static final long SCAN_PERIOD = 10000;
    BluetoothLeScanner bluetoothLeScanner;
    ScanSettings settings;
    List<ScanFilter> filters;
    BluetoothGatt mBluetoothGatt;
    private  String TAG = MainActivity.class.getSimpleName();
    LeDeviceListAdapter leDeviceListAdapter;


    Button scanButton;
    ListView bleDevicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = (Button) findViewById(R.id.scanButton);
        bleDevicesListView = (ListView) findViewById(R.id.bleDevicesListView);

        handler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!bluetoothAdapter.isEnabled()){
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
                }else{


                    if (Build.VERSION.SDK_INT >= 23){
                        //Grant Permission in runtime
                        if (requestPermissionAtRuntime()){
                            if (Build.VERSION.SDK_INT >= 21){
                                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                                filters = new ArrayList<>();
                            }
                            //  scanLeDevice(true);
                            leDeviceListAdapter = new LeDeviceListAdapter(getApplicationContext());

                           scanLeDevice(true);


                        }

                    }else{
                        //User already granted permission before Installation
                    }


                }

            }
        });

        bleDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 BluetoothDevice bluetoothDevice = leDeviceListAdapter.getDevice(position);
                if (bluetoothDevice == null){

                    return;
                }

                Intent intent = new Intent(getApplicationContext(),ConnectActivity.class);
                intent.putExtra(ConnectActivity.EXTRAS_DEVICE_NAME,bluetoothDevice.getName());
                intent.putExtra(ConnectActivity.EXTRAS_DEVICE_ADDRESS,bluetoothDevice.getAddress());

                scanLeDevice(false);

                startActivity(intent);

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();



    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothAdapter != null || bluetoothAdapter.isEnabled()){
            scanLeDevice(false);
        }
    }

    private void scanLeDevice(boolean isEnabled) {
        if (isEnabled){
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        if (Build.VERSION.SDK_INT < 21){
            bluetoothAdapter.stopLeScan(leScanCallback);
        }else{
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }
},SCAN_PERIOD);

            if (Build.VERSION.SDK_INT < 21){
                bluetoothAdapter.startLeScan(leScanCallback);
            }else{
                bluetoothLeScanner.startScan(filters,settings,scanCallback);
            }
        }else {
if (Build.VERSION.SDK_INT < 21){
bluetoothAdapter.stopLeScan(leScanCallback);
}else{
    bluetoothLeScanner.stopScan(scanCallback);
}
        }
    }


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG ,"callbackType "+ String.valueOf(callbackType));
            Log.i(TAG ,"result "+ result.toString());

           // connectToDevice(result.getDevice());
            addToAdapter(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult scanResult :results){
                Log.i(TAG ,"scanResult "+ scanResult.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i(TAG ,"onScanFailed "+ errorCode);
        }
    };

   /* private void connectToDevice(BluetoothDevice device) {
        if (mBluetoothGatt == null){
            mBluetoothGatt = device.connectGatt(this,false,gattCallback);
            scanLeDevice(false);
        }
    }*/


    private void  addToAdapter(BluetoothDevice device){
         leDeviceListAdapter.addDevice(device);
        bleDevicesListView.setAdapter(leDeviceListAdapter);
        leDeviceListAdapter.notifyDataSetChanged();
    }


    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Log.i(TAG ,"onLeScan"+ device.toString());
                             //connectToDevice(device);
                             addToAdapter(device);
                         }
                     });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == Activity.RESULT_CANCELED){
                //Bluetooth is not Enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean requestPermissionAtRuntime(){
        String[] permissionsToRequest = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };


        List<String> listPermissionsNeeded = new ArrayList<String>();
        int result;
        for (String s:permissionsToRequest){
        result = ContextCompat.checkSelfPermission(this,s);
            if (result != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(s);
            }
        }


        if (!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
     switch (requestCode){
         case  PERMISSION_REQUEST_CODE:

             if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                 scanLeDevice(true);

             }else {
                 requestPermissionAtRuntime();
             }
             break;
         default:
             break;
     }
    }
}
