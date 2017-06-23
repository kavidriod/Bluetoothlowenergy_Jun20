package com.apps.bluetoothlowenergy_jun20;

import android.annotation.TargetApi;
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
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

@TargetApi(23)
public class LeService extends Service {
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private final static String TAG = LeService.class.getSimpleName();
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    BluetoothGatt mBluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    public LeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
      return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean connect(String deviceAddress) {
        if (mBluetoothAdapter == null || deviceAddress == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && deviceAddress.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                return true;
            }else {
                return false;
            }
        }

        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (bluetoothDevice == null){
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = bluetoothDevice.connectGatt(this,false,mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress =   deviceAddress;
        mConnectionState = STATE_CONNECTING;
        return true;

    }


public void readCharacteristic(BluetoothGattCharacteristic characteristic){
    Log.i(TAG,"readCharacteristic");

    if (mBluetoothAdapter == null || mBluetoothGatt == null) {
        Log.w(TAG, "BluetoothAdapter not initialized");
        return;
    }
    mBluetoothGatt.readCharacteristic(characteristic);
}


    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic){
        Log.i(TAG,"setCharacteristicNotification");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic,true);

        Log.i(TAG,"characteristic.getUuid() ? "+characteristic.getUuid());
        Log.i(TAG,"NONIN_MEASUREMENT ? "+SampleGattAttributes.NONIN_MEASUREMENT);
        Log.i(TAG,"BLOODPRESSURE_MEASUREMENT ? "+SampleGattAttributes.BLOODPRESSURE_MEASUREMENT);

        //Check For Nonin
        if (UUID.fromString(SampleGattAttributes.NONIN_MEASUREMENT).equals(characteristic.getUuid())){
            Log.i(TAG,"1");

            BluetoothGattDescriptor bluetoothGattDescriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            Log.i(TAG,"2");
            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Log.i(TAG,"3");
            mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
            Log.i(TAG,"4");
        }else if (UUID.fromString(SampleGattAttributes.BLOODPRESSURE_MEASUREMENT).equals(characteristic.getUuid())){

            BluetoothGattDescriptor bluetoothGattDescriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
        }
    }


    public class  LocalBinder extends Binder{
        LeService getService(){
            return LeService.this;
        }
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public void disconnect() {
        Log.i(TAG, "disconnect()");

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mConnectionState = STATE_DISCONNECTED;
        mBluetoothGatt.disconnect();

        close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Log.i(TAG, "close()");

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String  intentAction;

            Log.i(TAG, "status ? "+status);
            Log.i(TAG, "newState ? "+newState);

            if (newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                updateListView(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                updateListView(intentAction);
                disconnect();
            }else  if(status != BluetoothGatt.GATT_SUCCESS){
                Log.i(TAG, "GATT Failure");
                if (mConnectionState != STATE_DISCONNECTED)
                disconnect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS){
              //  BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService( UUID.fromString(SampleGattAttributes.NONIN_SERVICE))
                  //      .getCharacteristic( UUID.fromString(SampleGattAttributes.NONIN_MEASUREMENT));
               // readCharacteristic(characteristic);
               // setCharacteristicNotification(characteristic);

                updateListView(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

       /* @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead !");
            if (status == BluetoothGatt.GATT_SUCCESS){
                updateListView(ACTION_DATA_AVAILABLE,characteristic);
            }
        }*/

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged !");
            updateListView(ACTION_DATA_AVAILABLE,characteristic);
        }
    };

    private void updateListView(String actionGattServicesDiscovered,BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(actionGattServicesDiscovered);

        Log.i(TAG,"updateListView characteristic.getUuid() ? "+characteristic.getUuid());
        Log.i(TAG,"updateListView NONIN_MEASUREMENT ? "+SampleGattAttributes.NONIN_MEASUREMENT);

        //Check For Nonin
       if (UUID.fromString(SampleGattAttributes.NONIN_MEASUREMENT).equals(characteristic.getUuid())){

           String heartRate =   parserNoninResult(characteristic);

            intent.putExtra(EXTRA_DATA, heartRate);
            sendBroadcast(intent);
       }else if (UUID.fromString(SampleGattAttributes.BLOODPRESSURE_MEASUREMENT).equals(characteristic.getUuid())){
           Bundle bundle = AnD_UA651_BLE_BloodPressureMeasurement.parseBloodPressureResult(characteristic);
           String sys="",dia="",pulse="";
           sys = String.valueOf(Math.round(bundle.getFloat(AnD_UA651_BLE_BloodPressureMeasurement.KEY_SYSTOLIC)));
           dia = String.valueOf(Math.round(bundle.getFloat(AnD_UA651_BLE_BloodPressureMeasurement.KEY_DIASTOLIC)));
           pulse = String.valueOf(Math.round(bundle.getFloat(AnD_UA651_BLE_BloodPressureMeasurement.KEY_PULSE_RATE)));
           String unit = bundle.getString(AnD_UA651_BLE_BloodPressureMeasurement.KEY_UNIT);

           String yr = String.valueOf(bundle.getInt(AnD_UA651_BLE_BloodPressureMeasurement.KEY_YEAR));
           String mon =  String.valueOf(bundle.getInt(AnD_UA651_BLE_BloodPressureMeasurement.KEY_MONTH));
           String day =  String.valueOf(bundle.getInt(AnD_UA651_BLE_BloodPressureMeasurement.KEY_DAY));
           String hr =  String.valueOf(bundle.getInt(AnD_UA651_BLE_BloodPressureMeasurement.KEY_HOURS));
           String min =  String.valueOf(bundle.getInt(AnD_UA651_BLE_BloodPressureMeasurement.KEY_MINUTES));
           String sec =  String.valueOf(bundle.getInt(AnD_UA651_BLE_BloodPressureMeasurement.KEY_SECONDS));


           String time = yr+"-"+mon+"-"+day+" "+hr+":"+min+":"+sec;

           String bp_Result = "BP is "+sys+"/"+dia+"Pulse is "+pulse+"/nUnit is "+unit+"/nDate & Time is "+time;
           intent.putExtra(EXTRA_DATA,bp_Result);
           sendBroadcast(intent);

       }


    }

    private String parserNoninResult(BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "Measurements recieved!");

        String  kSpo2 = "--";
        String   kPulseRate = "--";
        int  decivoltage = 0;

        String result = "Spo2 is "+kSpo2+"Pulse is "+kPulseRate+"\n"+"Voltage is "+decivoltage;

        // Indicates the current device status
        final int status = characteristic.getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        // Indicates that the display is synchronized to the SpO2 and pulse
        // rate values contained in this packet
        final int kSyncIndication = (status & 0x1);
        // Average amplitude indicates low or marginal signal quality
        final int kWeakSignal = (status & 0x2) >> 1;
        // Used to indicate that the data successfully passed the SmartPoint
        // Algorithm
        final int kSmartPoint = (status & 0x4) >> 2;
        // An absence of consecutive good pulse signal
        final int kSearching = (status & 0x8) >> 3;
        // CorrectCheck technology indicates that the finger is placed
        // correctly in the oximeter
        final int kCorrectCheck = (status & 0x10) >> 4;
        // Low or critical battery is indicated on the device
        final int kLowBattery = (status & 0x20) >> 5;
        // indicates whether Bluetooth connection is encrypted
        final int kEncryption = (status & 0x40) >> 6;

        // Voltage level of the batteries in use in .1 volt increments
        // [decivolts]
         decivoltage = characteristic.getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8, 2);
        // Value that indicates the relative strength of the pulsatile
        // signal. Units 0.01% (hundreds of a precent)
        final int paiValue = (characteristic.getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8, 3) * 256 + characteristic
                .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4));
        // Value that indicates that number of seconds since the device went
        // into run mod (between 0-65535)
        final int secondCnt = (characteristic.getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8, 5) * 256 + characteristic
                .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 6));
        // SpO2 percentage 0-100 (127 indicates missing)
        final int spo2 = characteristic.getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8, 7);
        // Pulse Rate in beats per minute, 0-325. (511 indicates missing)
        final int pulseRate = (characteristic.getIntValue(
                BluetoothGattCharacteristic.FORMAT_UINT8, 8) * 256 + characteristic
                .getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 9));

        // A value of 127 indicates no data for SpO2
        if (spo2 == 127) {
            kSpo2 = "--";
        } else {
            kSpo2 = String.format(" %d", spo2);
        }
        // A value of 511 indicates no data for pulse
        if (pulseRate == 511) {
            kPulseRate = "--";
        } else {
            kPulseRate = String.format(" %d", pulseRate);
        }

        result = "Spo2 is "+kSpo2+"Pulse is "+kPulseRate+"\n"+"Voltage is "+decivoltage;

        Log.i(TAG, "result recieved! "+result);

        return result;
    }

    private void updateListView(String actionGattServicesDiscovered) {
        Intent intent = new Intent(actionGattServicesDiscovered);
        sendBroadcast(intent);
    }

    public List<BluetoothGattService> getSupportedGattServices(){
        if (mBluetoothGatt == null)
            return null;

        return  mBluetoothGatt.getServices();
    }
}
