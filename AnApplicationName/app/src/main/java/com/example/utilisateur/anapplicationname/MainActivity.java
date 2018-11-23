package com.example.utilisateur.anapplicationname;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class MainActivity extends Activity {
    UUID service_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    UUID RX_char_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    UUID TX_char_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    UUID UUID_notif = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    ListView lv;
    Button but;
    private static final int REQUEST_ENABLE_BT = 5;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice myDevice;
    BluetoothGatt gatt;
    MyAdapter adapter;
    BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        but = findViewById(R.id.sendData);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gatt == null) {
                    Log.e("error", "lost connection");
                }
                BluetoothGattService Service = gatt.getService(service_UUID);
                if (Service == null) {
                    Log.e("error", "service not found!");
                }
                BluetoothGattCharacteristic charac = Service
                        .getCharacteristic(TX_char_UUID);
                if (charac == null) {
                    Log.e("", "char not found!");
                }

                byte[] value = new byte[1];
                value[0] = (byte) (21 & 0xFF);
                charac.setValue("Yolo");
                gatt.writeCharacteristic(charac);
            }
        });

        lv = findViewById(R.id.lvDevice);
        ArrayList<mBluetoothDevice> lbd = new ArrayList<>();
        adapter = new MyAdapter(this, lbd);
        lv.setAdapter(adapter);


        final BluetoothAdapter.LeScanCallback scanCallback =
                new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                        mBluetoothDevice scannedbd = new mBluetoothDevice(bluetoothDevice.toString(), bluetoothDevice.getAddress() != null ? bluetoothDevice.getAddress() : "", bluetoothDevice.getName() != null ? bluetoothDevice.getName() : "", bluetoothDevice);
                        if (!mBluetoothDevice.getBtd().contains(scannedbd)) {
                            mBluetoothDevice.addDevice(scannedbd);
                            adapter.add(scannedbd);
                        }
                    }};

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                bluetoothAdapter.stopLeScan(scanCallback);
                myDevice = ((mBluetoothDevice)a.getItemAtPosition(position)).getBd();
                communicate();
            }
        });

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        bluetoothAdapter.startLeScan(scanCallback);
    }

    public void communicate() {
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == STATE_CONNECTED){
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BluetoothGattCharacteristic characteristic = gatt.getService(service_UUID).getCharacteristic(RX_char_UUID);
                gatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_notif);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                BluetoothGattCharacteristic characteristic = gatt.getService(service_UUID).getCharacteristic(RX_char_UUID);
                gatt.setCharacteristicNotification(characteristic, true);
                characteristic.setValue(new byte[]{1, 1});
                gatt.writeCharacteristic(characteristic);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.d("CharChanged", String.valueOf(characteristic.getValue()[0] & 0xFF));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.d("CharWrite", String.valueOf((short)characteristic.getValue()[0]));

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d("CharRead", String.valueOf((short)characteristic.getValue()[0]));

            }
        };

        gatt = myDevice.connectGatt(this, false, gattCallback);
        gatt.connect();
    }
}
