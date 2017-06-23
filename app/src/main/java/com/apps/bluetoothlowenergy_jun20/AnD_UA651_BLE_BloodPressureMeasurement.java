package com.apps.bluetoothlowenergy_jun20;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Kavitha on 6/21/2017.
 */

@TargetApi(23)
public class AnD_UA651_BLE_BloodPressureMeasurement {
    public static final String KEY_UNIT = "unit";
    private static final String TAG = "BloodPressureMe";
    public static final String KEY_SYSTOLIC = "systolic";
    public static final String KEY_DIASTOLIC = "diastolic";
    public static final String KEY_MEAS_ARTERIAL_PRESSURE = "measArterialPressure";
    public static final String KEY_PULSE_RATE = "pulseRate";

    public static final String KEY_WEIGHT = "weight";

    public static final String KEY_TIME = "time";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MONTH = "month";
    public static final String KEY_DAY = "day";
    public static final String KEY_HOURS = "hours";
    public static final String KEY_MINUTES = "minutes";
    public static final String KEY_SECONDS = "seconds";

    public static final String KEY_BODY_MOVEMENT_DETECTION = "bodyMovementDetection" ;
    public static final String KEY_CUFF_FIT_DETECTION = "cuffFitDetection" ;
    public static final String KEY_IRREGULAR_PULSE_DETECTION = "irregularPulseDetection";
    public static final String KEY_PULSE_RATE_RANGE_DETECTION = "pulseRateRangeDetection";
    public static final String KEY_MEASUREMENT_POSITION_DETECTION = "measurementPositionDetection";

    public static Bundle parseBloodPressureResult(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG,"readCharacterist");
        Bundle bundle = new Bundle();

        int flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        String flagString = Integer.toBinaryString(flag);
        int offset=0;
        for(int index = flagString.length(); 0 < index ; index--) {
            String key = flagString.substring(index-1 , index);

            if(index == flagString.length()) {
                if(key.equals("0")) {
                    // mmHg
                    Log.d("SN", "mmHg");
                    bundle.putString(KEY_UNIT, "mmHg");
                }
                else {
                    // kPa
                    Log.d("SN", "kPa");
                    bundle.putString(KEY_UNIT, "kPa");
                }
                // Unit
                offset+=1;
                Log.d("SN", "Systolic :"+String.format("%f", characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset)));
                bundle.putFloat(KEY_SYSTOLIC, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                offset+=2;
                Log.d("SN", "Diastolic :"+String.format("%f", characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset)));
                bundle.putFloat(KEY_DIASTOLIC, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                offset+=2;
                Log.d("SN", "Mean Arterial Pressure :"+String.format("%f", characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset)));
                bundle.putFloat(KEY_MEAS_ARTERIAL_PRESSURE, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                offset+=2;
            }
            else if(index == flagString.length()-1) {
                if(key.equals("1")) {
                    // Time Stamp
                    Log.d("SN", "Y :"+String.format("%04d", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)));
                    bundle.putInt(KEY_YEAR, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset));
                    offset+=2;
                    Log.d("SN", "M :"+String.format("%02d", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset)));
                    bundle.putInt(KEY_MONTH, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                    Log.d("SN", "D :"+String.format("%02d", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset)));
                    bundle.putInt(KEY_DAY, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;

                    Log.d("SN", "H :"+String.format("%02d", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset)));
                    bundle.putInt(KEY_HOURS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                    Log.d("SN", "M :"+String.format("%02d", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset)));
                    bundle.putInt(KEY_MINUTES, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                    Log.d("SN", "S :"+String.format("%02d", characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset)));
                    bundle.putInt(KEY_SECONDS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                }
                else {

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    bundle.putInt(KEY_YEAR, calendar.get(Calendar.YEAR));
                    bundle.putInt(KEY_MONTH, calendar.get(Calendar.MONTH)+1);
                    bundle.putInt(KEY_DAY, calendar.get(Calendar.DAY_OF_MONTH));
                    bundle.putInt(KEY_HOURS, calendar.get(Calendar.HOUR));
                    bundle.putInt(KEY_MINUTES, calendar.get(Calendar.MINUTE));
                    bundle.putInt(KEY_SECONDS, calendar.get(Calendar.SECOND));
                }
            }
            else if(index == flagString.length()-2) {
                if(key.equals("1")) {
                    // Pulse Rate
                    Log.d("SN", "Pulse Rate :"+String.format("%f", characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset)));
                    bundle.putFloat(KEY_PULSE_RATE, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                    offset+=2;
                }
            }
            else if(index == flagString.length()-3) {
                // UserID
            }
            else if(index == flagString.length()-4) {
                // Measurement Status Flag
                int statusFalg = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset);
                String statusFlagString = Integer.toBinaryString(statusFalg);
                for(int i = statusFlagString.length(); 0 < i ; i--) {
                    String status = statusFlagString.substring(i-1 , i);
                    if(i == statusFlagString.length()) {
                        bundle.putInt(KEY_BODY_MOVEMENT_DETECTION, (status.endsWith("1"))? 1 : 0 );
                    }
                    else if(i == statusFlagString.length() - 1) {
                        bundle.putInt(KEY_CUFF_FIT_DETECTION, (status.endsWith("1"))? 1 : 0 );
                    }
                    else if(i == statusFlagString.length() - 2) {
                        bundle.putInt(KEY_IRREGULAR_PULSE_DETECTION, (status.endsWith("1"))? 1 : 0 );
                    }
                    else if(i == statusFlagString.length() - 3) {
                        i--;
                        String secondStatus = statusFlagString.substring(i-1 , i);
                        if(status.endsWith("1") && secondStatus.endsWith("0")) {
                            bundle.putInt(KEY_PULSE_RATE_RANGE_DETECTION, 1);
                        }
                        else if(status.endsWith("0") && secondStatus.endsWith("1")) {
                            bundle.putInt(KEY_PULSE_RATE_RANGE_DETECTION, 2);
                        }
                        else if(status.endsWith("1") && secondStatus.endsWith("1")) {
                            bundle.putInt(KEY_PULSE_RATE_RANGE_DETECTION, 3);
                        }
                        else {
                            bundle.putInt(KEY_PULSE_RATE_RANGE_DETECTION, 0);
                        }
                    }
                    else if(i == statusFlagString.length() - 5) {
                        bundle.putInt(KEY_MEASUREMENT_POSITION_DETECTION,(status.endsWith("1"))? 1 : 0);
                    }
                }
            }
        }
        return bundle;
    }
}
