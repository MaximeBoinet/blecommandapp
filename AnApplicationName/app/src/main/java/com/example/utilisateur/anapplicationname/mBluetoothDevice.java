package com.example.utilisateur.anapplicationname;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public class mBluetoothDevice {

    private static ArrayList<mBluetoothDevice> btd = new ArrayList<>();

    private String title;
    private String name;
    private String mac;
    private BluetoothDevice bd;

    public mBluetoothDevice(String title, String name, String mac, BluetoothDevice bd) {

        this.title = title;
        this.name = name;
        this.mac = mac;
        this.bd = bd;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public static ArrayList<mBluetoothDevice> getBtd() {
        return btd;
    }

    public static void addDevice (mBluetoothDevice bdv) {
        btd.add(bdv);
    }

    public BluetoothDevice getBd() {
        return bd;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof mBluetoothDevice) {
            return ((mBluetoothDevice) obj).getTitle().equals(this.title) && ((mBluetoothDevice) obj).name.equals(this.name) && ((mBluetoothDevice) obj).mac.equals(this.mac);
        }
        return super.equals(obj);
    }
}
