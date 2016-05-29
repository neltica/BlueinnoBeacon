package com.example.nelicia.myapplication;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private int REQUEST_ENABLE_BT=101;    // 블루투스ON/OFF 팝업 리턴 코드
    private Intent intent;                //다음엑티비티 인텐트
    private BluetoothAdapter mBluetoothAdapter;  //블루투스 어뎁터
    private BluetoothLeScanner mBLEScanner;   //BLE스캐너
    private Handler mHandler;  //핸들러
    private static final long SCAN_PERIOD = 60000; //postDelay 정지 시간
    private boolean mScanning;  //스캐너 플래그
    private Runnable runnable;      //러너블


    private ArrayList<String> mBluetoothDeviceNameList;          //ble 장치 탐색중에 검색되는 이름들을 저장(중복없음)
    private ArrayList<BluetoothDevice> mBluetoothDeviceList;    //ble 장치 탐색중에 검색되는 장치들의 인스턴스를 저장
    private ArrayAdapter<String> mListViewAdapter;          //리스트뷰 어뎁터  (어뎁터가 리스트뷰를 동작시킴)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent=new Intent(MainActivity.this,Main2Activity.class);

        Button button=(Button)findViewById(R.id.startbutton);
        mBluetoothDeviceNameList =new ArrayList<String>();                    //리스트 초기화
        mBluetoothDeviceList=new ArrayList<BluetoothDevice>();                //리스트  초기화
        mListViewAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mBluetoothDeviceNameList);          //어뎁터 초기화


        button.setOnClickListener(this);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {   //ble기능 있는지 체크
            Log.i("BLE","not support");
            finish();  //없음 종료
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter = bluetoothManager.getAdapter();   //블루투스 어뎁터 생성
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();   //ble 스캐너 생성
            if(mBLEScanner==null)
            {
                Log.i("ble not init", "ble not init");
            }
        }

        mHandler=new Handler();  //핸들러 초기화

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_ENABLE_BT)    //  만약 블루투스 ON/OFF 팝업창이고
        {
            if(resultCode==RESULT_OK) {    //OK를 리턴했으면
                Log.i("result_ok", String.valueOf(mBluetoothAdapter.isEnabled()));
                scanLeDevice(mBluetoothAdapter.isEnabled());   //스캐너에 사용가능을 날림.



            }
        }
    }



    @Override
    public void onClick(View v) {    //클릭 이벤트 받는곳
        switch(v.getId())
        {
            case R.id.startbutton:   //start버튼이면
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                else
                {
                    scanLeDevice(mBluetoothAdapter.isEnabled());
                }

                AlertDialog.Builder dialog= new AlertDialog.Builder(this);

                LinearLayout linearLayout= new LinearLayout(this);
                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                linearLayout.setLayoutParams(layoutParams);
                linearLayout.setOrientation(LinearLayout.VERTICAL);


                ListView listview=new ListView(this);
                listview.setId(0);
                listview.setOnItemClickListener(mItemClickListener);
                listview.setAdapter(mListViewAdapter);

                linearLayout.addView(listview);


                Button searchButton= new Button(this);
                searchButton.setId(0);
                searchButton.setText("Search");
                searchButton.setOnClickListener(this);

                linearLayout.addView(searchButton);

                dialog.setView(linearLayout);
                dialog.show();
                break;

            case 0:
                if(!mScanning)
                {
                    scanLeDevice(mBluetoothAdapter.isEnabled());
                }
                else
                {
                    Log.i("scanning","already run");
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.i("scanning", String.valueOf(enable));
            // Stops scanning after a pre-defined scan period.


            runnable=new Runnable() {
                @Override
                public void run() {
                    Log.i("scanning", "postDelay end");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning=false;
                        mBLEScanner.stopScan(mLeScanCallback);
                    }
                }
            };
            mHandler.postDelayed(runnable, SCAN_PERIOD);




            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("scanning", "scan start");
                mScanning=true;
                mBLEScanner.startScan(mLeScanCallback);
            }
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("scanning", "scan end");
                mScanning=false;
                mBLEScanner.stopScan(mLeScanCallback);
            }
        }
    }

    private ScanCallback mLeScanCallback =
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

                    if(result.getDevice().getName()!=null) {
                        if (mBluetoothDeviceNameList.indexOf(result.getDevice().getName() + "\n" + result.getDevice().getAddress()) == -1) {
                            Log.i("scanning name", result.getDevice().getName());
                            mBluetoothDeviceNameList.add(result.getDevice().getName() + "\n" + result.getDevice().getAddress());
                            mBluetoothDeviceList.add(result.getDevice());
                            mListViewAdapter.notifyDataSetChanged();
                        }

                    /*
                    if (result.getDevice().getName().equals("HELLIO"))
                    {
                        Log.i("scanning", "scanstop");
                        BLEDataClass.mBluetoothDevice=result.getDevice();
                        scanLeDevice(false);
                    }
                    */
                    }
                }
            };

    OnItemClickListener mItemClickListener=new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch ((int) id)
            {
                case 0:
                    Log.i("touch listview ", String.valueOf(position));
                    mHandler.removeCallbacks(runnable);
                    scanLeDevice(false);
                    BLEDataClass.mBluetoothDevice=mBluetoothDeviceList.get(position);

                    intent=new Intent(MainActivity.this,Main2Activity.class);

                    startActivity(intent);
                    break;
            }
        }
    };


}
