package com.example.nelicia.bluetoothtest;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import java.util.List;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity{

    private int REQUEST_ENABLE_BT=101;
    private Handler mHandler;
    private boolean mScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothDevice mBluetoothDevice;

    private BluetoothLeScanner mBLEScanner;
    private BluetoothGatt mBluetoothGatt;

    private int count=0;

    List<BluetoothGattService> services;

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.disable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_ENABLE_BT)
        {
            if(resultCode==RESULT_OK) {
                Log.i("result_ok",String.valueOf(mBluetoothAdapter.isEnabled()));
                scanLeDevice(mBluetoothAdapter.isEnabled());



            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i("BLE","not support");
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if(mBLEScanner==null)
            {
                Log.i("ble not init","ble not init");
            }
        }

        mHandler=new Handler();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            scanLeDevice(mBluetoothAdapter.isEnabled());
        }

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.i("scanning", String.valueOf(enable));
            // Stops scanning after a pre-defined scan period.

            /*
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("scanning","start");
                    mScanning = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBLEScanner.stopScan(mLeScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            */


            mScanning = true;
            Log.i("scanning", "startLeScan");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBLEScanner.startScan(mLeScanCallback);
            }
        } else {
            mScanning = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBLEScanner.stopScan(mLeScanCallback);
            }
        }
    }


    // Device scan callback.

    private  ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.i("scanning:", String.valueOf(result.getDevice().getName()));

                    if(result.getDevice().getName().equals("HELLIO"))
                    {
                        Log.i("scanning","scanstop");
                        mBluetoothDevice=result.getDevice();
                        scanLeDevice(false);
                        mBluetoothGatt=mBluetoothDevice.connectGatt(MainActivity.this,false,mGattCallback);
                        //BluetoothGattCharacteristic characteristic=new BluetoothGattCharacteristic(mBluetoothDevice.getUuids());
                        //mBluetoothGatt.writeCharacteristic()
                    }

                }
            };


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String recvData=new String(characteristic.getValue());
            Log.i("onCharacteristicChanged", recvData);

            for(BluetoothGattService service:services) {
                service.getCharacteristics().get(0).setValue("me too");
                mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());



                for (BluetoothGattService service : services) {
                    //gatt.readCharacteristic(service.getCharacteristics().get(0));
                    mBluetoothGatt.setCharacteristicNotification(service.getCharacteristics().get(0),true);
                }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i("onDescriptorRead", String.valueOf(descriptor.getValue()));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead" + String.valueOf(count), new String(characteristic.getValue()) + " " + gatt.getDevice().getName());

            count++;
            //gatt.disconnect();
        }
    };

}
