package zephyr.android.HxMBT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

public class BTClient extends Thread {
    UUID MY_UUID;
    private BluetoothAdapter _adapter;
    private BluetoothSocket _btStream;
    private BTComms _comms;
    private String _connectionString;
    private BluetoothDevice _device;
    private boolean _isConnected;
    public boolean _isValidBlueToothAddress;
    private Vector<ConnectedListener<BTClient>> eventSubscribers;

    public BluetoothDevice getDevice() {
        return this._device;
    }

    public BTComms getComms() {
        return this._comms;
    }

    public boolean IsConnected() {
        return this._isConnected;
    }

    public void IsValidBlueToothAddress() {
        this._isValidBlueToothAddress = BluetoothAdapter.checkBluetoothAddress(this._connectionString);
    }

    private void OnConnected() {
        Iterator<ConnectedListener<BTClient>> iter = ((Vector) this.eventSubscribers.clone()).iterator();
        while (iter.hasNext()) {
            ((ConnectedListener) iter.next()).Connected(new ConnectedEvent(this));
        }
    }

    public void addConnectedEventListener(ConnectedListener<BTClient> listener) {
        this.eventSubscribers.add(listener);
    }

    public void removeConnectedEventListener(ConnectedListener<BTClient> listener) {
        this.eventSubscribers.remove(listener);
    }

    public BTClient(BluetoothAdapter adapter, String connectionString) {
        this.MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        this._isConnected = false;
        this._isValidBlueToothAddress = false;
        this.eventSubscribers = new Vector();
        this._adapter = adapter;
        this._isConnected = false;
        this._comms = null;
        this._btStream = null;
        this._device = null;
        this._connectionString = connectionString;
        IsValidBlueToothAddress();
        this._device = this._adapter.getRemoteDevice(this._connectionString);
        try {
            this._btStream = this._device.createRfcommSocketToServiceRecord(this.MY_UUID);
            if (this._btStream != null) {
                this._btStream.connect();
                this._isConnected = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this._adapter.cancelDiscovery();
        if (this._isConnected) {
            StartCommunication();
        } else {
            System.out.println("Can't connect to the BioHarness.");
        }
    }

    private void StartCommunication() {
        this._comms = new BTComms(this._btStream);
        this._comms.start();
        OnConnected();
    }

    public void Close() {
        if (this._btStream != null) {
            try {
                this._comms.Close();
                this._btStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this._isConnected = false;
    }
}
