package com.apps.bluetoothlowenergy_jun20;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kavitha on 6/20/2017.
 */

public class LeDeviceListAdapter extends BaseAdapter {



    private ArrayList<BluetoothDevice> bluetoothDevices;
    LayoutInflater layoutInflater;

    public LeDeviceListAdapter(Context context ){
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bluetoothDevices = new ArrayList<>();
    }


    public void addDevice(BluetoothDevice bluetoothDevice){
        if (!bluetoothDevices.contains(bluetoothDevice)){
            bluetoothDevices.add(bluetoothDevice);
        }
    }

    public void clear(){
    bluetoothDevices.clear();
    }

    @Override
    public int getCount() {
        return bluetoothDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return bluetoothDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public BluetoothDevice getDevice(int position){
        return bluetoothDevices.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null){
            convertView = layoutInflater.inflate(R.layout.listof_ble_devices,null);
            viewHolder = new ViewHolder();
            viewHolder.devicenametextView = (TextView) convertView.findViewById(R.id.devicenametextView);
            viewHolder.devicemactextView = (TextView) convertView.findViewById(R.id.devicemactextView);
            convertView.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        BluetoothDevice eachBluetoothDevice = bluetoothDevices.get(position);
        final String deviceName = eachBluetoothDevice.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.devicenametextView.setText(deviceName);
            else
            viewHolder.devicenametextView.setText("Unknown Device");

        viewHolder.devicemactextView.setText(eachBluetoothDevice.getAddress());


        return convertView;
    }


    class ViewHolder{
        TextView devicenametextView,devicemactextView;
    }
}
