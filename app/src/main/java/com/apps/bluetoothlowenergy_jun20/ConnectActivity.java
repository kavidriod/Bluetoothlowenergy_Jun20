package com.apps.bluetoothlowenergy_jun20;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TargetApi(23)
public class ConnectActivity extends AppCompatActivity {

    private final static String TAG = ConnectActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    TextView connection_state,data_value,device_address;
    String deviceName,deviceAddress;
    LeService leService;
    private boolean mConnected = false;

    ExpandableListView gatt_services_list;
    ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;


    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        Intent intent = getIntent();

        deviceName = intent.getStringExtra("EXTRAS_DEVICE_NAME");
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS");

        toolbar.setTitle(deviceName);


        connection_state = (TextView) findViewById(R.id.connection_state);
        data_value = (TextView) findViewById(R.id.data_value);
        device_address = (TextView) findViewById(R.id.device_address);
        gatt_services_list = (ExpandableListView) findViewById(R.id.gatt_services_list);
        gatt_services_list.setOnChildClickListener(expandableOnChildClickListener);

        device_address.setText(deviceAddress);


        Intent gattServiceIntent = new Intent(this,LeService.class);
        bindService(gattServiceIntent,mServiceConnection,BIND_AUTO_CREATE);

    }


    private final ExpandableListView.OnChildClickListener expandableOnChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if (mGattCharacteristics != null){
                BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);

                leService.readCharacteristic(characteristic);

                leService.setCharacteristicNotification(characteristic);

                  return  true;
            }
            return false;
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            leService = ((LeService.LocalBinder) service).getService();
            if (!leService.initialize()){
                finish();
                Log.e(TAG, "Unable to initialize Bluetooth");
            }

            leService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            leService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver,makeGattUpdateIntentFilter());
        if (leService != null){
            final  boolean result = leService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private  static IntentFilter makeGattUpdateIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(LeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(LeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(LeService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    private final BroadcastReceiver
            mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final  String action = intent.getAction();
            if (LeService.ACTION_GATT_CONNECTED.equals(action)){
                mConnected = true;
                updateConnectionState("Connected");
            }else if (LeService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                updateConnectionState("Disconnected");
                clearUI();
            }if (LeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                displayGattServices(leService.getSupportedGattServices());
            }if (LeService.ACTION_DATA_AVAILABLE.equals(action)){
                displayData(intent.getStringExtra(LeService.EXTRA_DATA));
            }

        }
    };

    private void displayGattServices(List<BluetoothGattService> supportedGattServices) {
        if (supportedGattServices == null)
            return;

        String uuid = null;
        String unknownServiceString = "unknown service";
        String unknownCharaString = "unknown characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics  = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.

        for (BluetoothGattService bluetoothGattService:supportedGattServices){
            HashMap<String,String> currentServiceDate = new HashMap<>();
            uuid = bluetoothGattService.getUuid().toString();
            currentServiceDate.put(LIST_NAME,SampleGattAttributes.lookup(uuid,unknownServiceString));
            currentServiceDate.put(LIST_UUID,uuid);
            gattServiceData.add(currentServiceDate);

            // Loops through available Characteristics.
            List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charsList = new ArrayList<>();
            ArrayList<HashMap<String,String>> gattCharacteristicGroupData = new ArrayList<>();

            for (BluetoothGattCharacteristic gattCharacteristic:gattCharacteristics){
                charsList.add(gattCharacteristic);
                HashMap<String,String> currentCharacteristicData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharacteristicData.put(LIST_NAME,SampleGattAttributes.lookup(uuid,unknownCharaString));
                currentCharacteristicData.put(LIST_UUID,uuid);
                gattCharacteristicGroupData.add(currentCharacteristicData);
            }

            mGattCharacteristics.add(charsList);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }


        SimpleExpandableListAdapter simpleExpandableListAdapter = new SimpleExpandableListAdapter(
                this,

                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME,LIST_UUID},
                new int[]{android.R.id.text1,android.R.id.text2},

                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME,LIST_UUID},
                new int[]{android.R.id.text1,android.R.id.text2}
        );
        gatt_services_list.setAdapter(simpleExpandableListAdapter);
    }


    private void clearUI() {
        gatt_services_list.setAdapter((SimpleExpandableListAdapter) null);
        data_value.setText("No data");
    }


    private void updateConnectionState(final String resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connection_state.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            data_value.setText(data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        leService = null;
    }
}
