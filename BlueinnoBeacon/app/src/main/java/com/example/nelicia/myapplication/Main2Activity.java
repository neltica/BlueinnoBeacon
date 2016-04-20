package com.example.nelicia.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;

import android.os.Handler;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Main2Activity extends AppCompatActivity implements View.OnClickListener,BeaconConsumer{

    private Button emoticon1,emoticon2,emoticon3;
    private TextView meter;
    private Intent emoticonIntent,graphIntent;
    private BeaconManager beaconManager;

    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> services;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        setTitle("Main2Activity");
        emoticonIntent=new Intent(Main2Activity.this,EmoticonActivity.class);
        graphIntent=new Intent(Main2Activity.this,GraphActivity.class);

        emoticon1=(Button)findViewById(R.id.emoticon1);
        emoticon2=(Button)findViewById(R.id.emoticon2);
        emoticon3=(Button)findViewById(R.id.emoticon3);

        meter=(TextView)findViewById(R.id.meter);

        emoticon1.setOnClickListener(this);
        emoticon2.setOnClickListener(this);
        emoticon3.setOnClickListener(this);
        meter.setOnClickListener(this);

        beaconManager= BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        mBluetoothGatt=BLEDataClass.mBluetoothDevice.connectGatt(this,false,mGattCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.emoticon1:
                startActivity(emoticonIntent);
                break;
            case R.id.emoticon2:
                startActivity(emoticonIntent);
                break;
            case R.id.emoticon3:
                startActivity(emoticonIntent);
                break;
            case R.id.meter:
                startActivity(graphIntent);
                break;
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if(collection.size()>0)
                {
                    Log.i("range", "first beacon i see is about" + collection.iterator().next().getDistance() + "meters away.");

                    for(BluetoothGattService service:services) {
                        service.getCharacteristics().get(0).setValue(String.format("%.2f",collection.iterator().next().getDistance())+"\n");
                        mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                    }

                    Message msg=Message.obtain();
                    msg.obj=collection;
                    mHandler.sendMessage(msg);

                }
            }
        });

        try{
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        }
        catch(RemoteException e)
        {
            Log.i("catch","catch");
        }
    }

    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Collection<Beacon> collection= (Collection<Beacon>) msg.obj;
            meter.setText(String.valueOf(String.format("%.2f", collection.iterator().next().getDistance()) + "m"));
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
            Log.i("onCharacteristicRead", new String(characteristic.getValue()) + " " + gatt.getDevice().getName());
            //gatt.disconnect();
        }
    };
}
