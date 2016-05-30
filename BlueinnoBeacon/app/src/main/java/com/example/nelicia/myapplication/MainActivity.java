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


    private int REQUEST_ENABLE_BT=101;
    private Intent intent;                                  //메인2액티비티로 넘어가는 인텐트
    private BluetoothAdapter mBluetoothAdapter;             //블루투스 어뎁터 이거 있어야 ble탐색할수 있음
    private BluetoothLeScanner mBLEScanner;                  //ble스캐너 실제로 스캔동작을 수행하는 클래스 이거 있어야 ble탐색할수 있음
    private Handler mHandler;                                 //핸들러
    private static final long SCAN_PERIOD = 60000;            //스캔시간
    private boolean mScanning;
    private Runnable runnable;


    private ArrayList<String> mBluetoothDeviceNameList;                  //ble디바이스 이름 저장 리스트
    private ArrayList<BluetoothDevice> mBluetoothDeviceList;             //ble 디바이스 인스턴스들 저장하는 리스트
    private ArrayAdapter<String> mListViewAdapter;                       //리스트형식으로 보여줄것이기 때문에 필요

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent=new Intent(MainActivity.this,Main2Activity.class);

        Button button=(Button)findViewById(R.id.startbutton);                 //버튼 연결
        mBluetoothDeviceNameList =new ArrayList<String>();                           //탐색해서 찾은 디바이스들 이름을 담을 리스트
        mBluetoothDeviceList=new ArrayList<BluetoothDevice>();                        //탐색해서 찾은 디바이스들을 담을 리스트
        mListViewAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mBluetoothDeviceNameList);   //리스트뷸 어뎁터 초기화


        button.setOnClickListener(this);              //버튼 이벤트 연결

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {               //ble을 지원하는지 확인
            Log.i("BLE","not support");
            finish();                          //지원안하면 앱 종료
        }

        final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {                    //안드로이드 젤리빈보다 높은 버전인지 체크
            mBluetoothAdapter = bluetoothManager.getAdapter();                               //젤리빈 이상이면 블루투스 어뎁터 가져옴
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {                          //ble는 롤리팝 이후 버전부터 지원한다. 롤리팝 이상 인지 체크
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();                         //ble 스캐너를 생성한다.
            if(mBLEScanner==null)
            {
                Log.i("ble not init", "ble not init");
            }
        }

        mHandler=new Handler();                                                             //핸들러 초기화

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_ENABLE_BT)                                                          //블루투스를 켤것인지 확인하는 팝업창이 꺼지고
        {
            if(resultCode==RESULT_OK) {                                                        //확인버튼을 눌렀다면
                Log.i("result_ok", String.valueOf(mBluetoothAdapter.isEnabled()));
                scanLeDevice(mBluetoothAdapter.isEnabled());                                    //스캔을 시작한다.



            }
        }
    }



    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.startbutton:

                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);                    //스타트 버튼을 눌렀다면 블루투스를 켤것인지 확인하는 팝업을 띄운다.
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                else
                {
                    scanLeDevice(mBluetoothAdapter.isEnabled());                          //이미 블루투스가 켜져있다면 바로 스캔을 시작한다.
                }

                AlertDialog.Builder dialog= new AlertDialog.Builder(this);                      //팝업창을 띄울 준비를 한다.

                LinearLayout linearLayout= new LinearLayout(this);
                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                linearLayout.setLayoutParams(layoutParams);
                linearLayout.setOrientation(LinearLayout.VERTICAL);


                ListView listview=new ListView(this);                    //팝업에 들어갈 리스트뷰를 세팅한다.
                listview.setId(0);
                listview.setOnItemClickListener(mItemClickListener);
                listview.setAdapter(mListViewAdapter);

                linearLayout.addView(listview);


                Button searchButton= new Button(this);             //팝업에 들어갈 버튼을 세팅한다.
                searchButton.setId(0);                           
                searchButton.setText("Search");
                searchButton.setOnClickListener(this);

                linearLayout.addView(searchButton);

                dialog.setView(linearLayout);
                dialog.show();                                     //다이얼로그를 보여준다.


                intent=new Intent(MainActivity.this,Main2Activity.class);             //메인2액티비트로 넘어갈 준비를 하고

                startActivity(intent);       //액티비티 시작시킨다.
                break;

            case 0:
                if(!mScanning)
                {
                    scanLeDevice(mBluetoothAdapter.isEnabled());               //만약 팝업창의 스캔버튼을 눌렀다면 이미 스캔중인지 체크한 뒤에 스캔중 아니면 스캔 시작
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


    private void scanLeDevice(final boolean enable) {            //스캔동작시에 반응하는 함수
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
            mHandler.postDelayed(runnable, SCAN_PERIOD);                   //60초 뒤에 동작하는 스레드   스캔동작을 멈춘다.




            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("scanning", "scan start");
                mScanning=true;                                                     //mScanning이 true면
                mBLEScanner.startScan(mLeScanCallback);                           //스캔 시작
            }
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("scanning", "scan end");
                mScanning=false;                                               //mScanning이 false이면
                mBLEScanner.stopScan(mLeScanCallback);                         //스캔 멈춤
            }
        }
    }

    private ScanCallback mLeScanCallback =                                   //스캔도중에 발생하는 이벤트에 맞춰 반응하는 곳
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
                public void onScanResult(int callbackType, final ScanResult result) {                //새로운 장치를 하나씩 찾을때 마다
                    super.onScanResult(callbackType, result);
                    Log.i("scanning:", String.valueOf(result.getDevice().getName()));

                    if(result.getDevice().getName()!=null) {                                            //이름이 null이 아니고
                        if (mBluetoothDeviceNameList.indexOf(result.getDevice().getName() + "\n" + result.getDevice().getAddress()) == -1) {       //찾은 장치들이 저장된 리스트뷰에 있는 값이 아니라면
                            Log.i("scanning name", result.getDevice().getName());
                            mBluetoothDeviceNameList.add(result.getDevice().getName() + "\n" + result.getDevice().getAddress());
                            mBluetoothDeviceList.add(result.getDevice());                                                                         //리스트뷰에 저장하고
                            mListViewAdapter.notifyDataSetChanged();                                             //팝업창의 블루투스 장치들을 새로 고침한다.
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

    OnItemClickListener mItemClickListener=new OnItemClickListener() {                            //블루투스 스캔중에 리스트뷰에 나오는 리스트를 터치하면 반응하는곳
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch ((int) id)                                                                      //몇번째 리스트를 가져왔는지 확인해서
            {
                case 0:
                    Log.i("touch listview ", String.valueOf(position));
                    mHandler.removeCallbacks(runnable);
                    scanLeDevice(false);                                                    //스캔을 멈추고
                    BLEDataClass.mBluetoothDevice=mBluetoothDeviceList.get(position);                   //ble클래스를 터치한 리스트가 가지고 있는 블루투스 정보로 세팅한다.

                    intent=new Intent(MainActivity.this,Main2Activity.class);             //메인2액티비트로 넘어갈 준비를 하고

                    startActivity(intent);       //액티비티 시작시킨다.
                    break;
            }
        }
    };

}
