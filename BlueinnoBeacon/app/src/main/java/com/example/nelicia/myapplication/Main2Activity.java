package com.example.nelicia.myapplication;

import android.content.Intent;
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
import android.os.Handler;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener,BeaconConsumer{

    private Button emoticon1,emoticon2,emoticon3;
    private TextView meter;
    private Intent emoticonIntent,graphIntent;
    private BeaconManager beaconManager;
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
            meter.setText(String.valueOf( String.format("%.2f",collection.iterator().next().getDistance())+"m"));
        }
    };
}
