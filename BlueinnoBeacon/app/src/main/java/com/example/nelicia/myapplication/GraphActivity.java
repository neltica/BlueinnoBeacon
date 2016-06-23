package com.example.nelicia.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.graphview.LineGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GraphActivity extends AppCompatActivity implements BeaconConsumer {

    private ViewGroup viewGroup;
    String[] legendArr;
    float[] graph1;
    LineGraphVO vo;
    List<LineGraph> arrGraph;
    LineGraphView lineGraphView;
    private BeaconManager beaconManager;
    private ArrayList<Float> dataLinkedList;

    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> services;
    private boolean startFlag;
    private int postOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setTitle("GraphActivity");

        postOutput=-1;
        dataLinkedList =new ArrayList<Float>();
        viewGroup=(ViewGroup)findViewById(R.id.groupview);

        legendArr 	= new String[]{"1", "2", "3", "4", "5","6","7","8","9","10"};
        graph1= new float[10];

        arrGraph=new ArrayList<LineGraph>();

        arrGraph.add(new LineGraph("android", 0xaa66ff33, graph1));


        //vo = makeLineGraphAllSetting();
        int paddingBottom 	= LineGraphVO.DEFAULT_PADDING;
        int paddingTop 		= LineGraphVO.DEFAULT_PADDING;
        int paddingLeft 	= LineGraphVO.DEFAULT_PADDING;
        int paddingRight 	= LineGraphVO.DEFAULT_PADDING;

        //graph margin
        int marginTop 		= LineGraphVO.DEFAULT_MARGIN_TOP;
        int marginRight 	= LineGraphVO.DEFAULT_MARGIN_RIGHT;

        //max value
        int maxValue 		= 5;//LineGraphVO.DEFAULT_MAX_VALUE;

        //increment
        int increment 		= 1;//LineGraphVO.DEFAULT_INCREMENT;

        vo = new LineGraphVO(
                paddingBottom, paddingTop, paddingLeft, paddingRight,
                marginTop, marginRight, maxValue, increment, legendArr, arrGraph);
        vo.setGraphNameBox(new GraphNameBox());
        //vo.setDrawRegion(true);

        lineGraphView=new LineGraphView(this, vo);
        viewGroup.addView(lineGraphView);

        mBluetoothGatt=BLEDataClass.mBluetoothDevice.connectGatt(this,false,mGattCallback);
        startFlag=false;

        while(true)
        {
            if(services!=null)
            {
                startFlag=true;
                break;
            }
        }

        beaconManager= BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);


    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                if(collection.size()>0)
                {
                    //Log.i("range", "first beacon i see is about" + collection.iterator().next().getDistance() + "meters away.");

                    if(dataLinkedList.size()==5)
                    {
                        dataLinkedList.remove(0);
                        dataLinkedList.add((float) collection.iterator().next().getDistance());
                    }
                    else
                    {
                        dataLinkedList.add((float) collection.iterator().next().getDistance());
                    }
                    for(int i=0;i< dataLinkedList.size();i++)
                    {
                        graph1[i]= dataLinkedList.get(i);
                    }
                    arrGraph 		= new ArrayList<LineGraph>();
                    arrGraph.add(new LineGraph("android", 0xaa66ff33, graph1));


                    vo.setArrGraph(arrGraph);
                    MotionEvent down_event=MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN,0,0,0);
                    //MotionEvent up_event=MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,0,0,0);
                    lineGraphView.dispatchTouchEvent(down_event);


                    if(startFlag) {

                        if(collection.iterator().next().getDistance()>=1.0)
                        {

                            if(postOutput!=2) {
                                Log.i("sendData", String.valueOf(2)+" "+collection.iterator().next().getDistance());
                                    for (BluetoothGattService service : services) {
                                        service.getCharacteristics().get(0).setValue(2, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                                        mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                                    }

                                postOutput=2;
                            }
                        }
                        else if(collection.iterator().next().getDistance()>=0.6)
                        {

                            if(postOutput!=1)
                            {
                                Log.i("sendData", String.valueOf(1)+" "+collection.iterator().next().getDistance());
                                    for (BluetoothGattService service : services) {
                                        service.getCharacteristics().get(0).setValue(1,BluetoothGattCharacteristic.FORMAT_SINT8,0);
                                        mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                                    }

                                postOutput=1;
                            }

                        }
                        else if(collection.iterator().next().getDistance()>=0.3)
                        {

                            if(postOutput!=0)
                            {
                                Log.i("sendData", String.valueOf(0)+" "+collection.iterator().next().getDistance());

                                    for (BluetoothGattService service : services) {
                                        service.getCharacteristics().get(0).setValue(0,BluetoothGattCharacteristic.FORMAT_SINT8,0);
                                        mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                                    }


                                postOutput=0;
                            }

                        }
                    }

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
