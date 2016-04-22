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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import android.os.Handler;
import android.widget.ThemedSpinnerAdapter;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Main2Activity extends AppCompatActivity implements View.OnClickListener,BeaconConsumer{

    private Button emoticon1,emoticon2,emoticon3;
    private TextView meter;
    private Intent emoticonIntent,graphIntent;
    private BeaconManager beaconManager;

    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> services;

    private static final int EMOTIONRESULTCODE=1;

    private int[][] emoticonArray;

    private String fileName;
    private File file;

    private boolean startFlag=false;

    private int recvData,recvCount,sendCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        setTitle("Main2Activity");
        recvData=-129;
        recvCount=0;
        sendCount=0;
        emoticonArray=new int[3][8];
        fileName="setting.txt";
        if(new File(getFilesDir(),fileName).exists()==true)
        {
            FileRead();
        }

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

        while(true) {
            if(services!=null) {
                for(int i=0;i<3;i++)
                {
                    for(int j=0;j<8;j++)
                    {
                        Log.i("sendData", String.valueOf(emoticonArray[i][j]));
                        for (BluetoothGattService service : services) {
                            service.getCharacteristics().get(0).setValue(emoticonArray[i][j],BluetoothGattCharacteristic.FORMAT_SINT8,0);
                            mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
        }

        startFlag=true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    void FileSave()
    {
        file=new File(getFilesDir(),fileName);

        try {
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            for(int i=0;i<3;i++)
            {
                for(int j=0;j<8;j++)
                {
                    fileOutputStream.write(String.valueOf(emoticonArray[i][j]).getBytes());
                    if(j==7)
                    {
                        fileOutputStream.write("\n".getBytes());
                    }
                    else
                    {
                        fileOutputStream.write(" ".getBytes());
                    }
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void FileRead()
    {
        String fileName="setting.txt";
        File file=new File(getFilesDir(),fileName);
        byte[] buffer= new byte[1024];
        try {
            FileInputStream fileInputStream=new FileInputStream(file);
            fileInputStream.read(buffer);
            Log.i("buffer", new String(buffer));
            String[] str=new String(buffer).split("\n");
            for(int i=0;i<3;i++)
            {
                int j=0;
                for(String temp:str[i].split(" "))
                {
                    emoticonArray[i][j]=Integer.parseInt(temp);
                    j++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("onActivityResult","insert"+" "+requestCode+" "+resultCode);
        int[] resultArray;
        switch (requestCode)
        {
            case EMOTIONRESULTCODE:
                if(resultCode==RESULT_OK)
                {
                    switch(data.getExtras().getInt("EMOTICONARRAYNUM"))
                    {
                        case 0:
                            resultArray=data.getExtras().getIntArray("EMOTICONRESULT");
                            emoticonArray[0]=resultArray;
                            break;
                        case 1:
                            resultArray=data.getExtras().getIntArray("EMOTICONRESULT");
                            emoticonArray[1]=resultArray;
                            break;
                        case 2:
                            resultArray=data.getExtras().getIntArray("EMOTICONRESULT");
                            emoticonArray[2]=resultArray;
                            break;
                    }
                }
                FileSave();
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.emoticon1:
                //startActivity(emoticonIntent);
                emoticonIntent.putExtra("EMOTICONARRAYNUM",0);
                emoticonIntent.putExtra("EMOTICONARRAY",emoticonArray[0]);
                startActivityForResult(emoticonIntent, EMOTIONRESULTCODE);
                break;
            case R.id.emoticon2:
                //startActivity(emoticonIntent);
                emoticonIntent.putExtra("EMOTICONARRAYNUM",1);
                emoticonIntent.putExtra("EMOTICONARRAY",emoticonArray[1]);
                startActivityForResult(emoticonIntent, EMOTIONRESULTCODE);
                break;
            case R.id.emoticon3:
                //startActivity(emoticonIntent);
                emoticonIntent.putExtra("EMOTICONARRAYNUM",2);
                emoticonIntent.putExtra("EMOTICONARRAY",emoticonArray[2]);
                startActivityForResult(emoticonIntent, EMOTIONRESULTCODE);
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
                    //Log.i("range", "first beacon i see is about" + collection.iterator().next().getDistance() + "meters away.");


                    if(startFlag) {
                        if(collection.iterator().next().getDistance()>=1.0)
                        {
                            Log.i("sendData", String.valueOf(2));
                            for (BluetoothGattService service : services) {
                                service.getCharacteristics().get(0).setValue(2,BluetoothGattCharacteristic.FORMAT_SINT8,0);
                                mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                            }
                        }
                        else if(collection.iterator().next().getDistance()>=0.6)
                        {
                            Log.i("sendData", String.valueOf(1));
                            for (BluetoothGattService service : services) {
                                service.getCharacteristics().get(0).setValue(1,BluetoothGattCharacteristic.FORMAT_SINT8,0);
                                mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                            }
                        }
                        else if(collection.iterator().next().getDistance()>=0.3)
                        {
                            Log.i("sendData", String.valueOf(0));
                            for (BluetoothGattService service : services) {
                                service.getCharacteristics().get(0).setValue(0,BluetoothGattCharacteristic.FORMAT_SINT8,0);
                                mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                            }
                        }
                        else
                        {
                            Log.i("sendData", String.valueOf(0));
                            for (BluetoothGattService service : services) {
                                service.getCharacteristics().get(0).setValue(-1,BluetoothGattCharacteristic.FORMAT_SINT8,0);
                                mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                            }
                        }
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

            Log.i("onCharacteristicChanged", new String(characteristic.getValue()));


            String[] recvData=new String(characteristic.getValue()).split(" ");


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
