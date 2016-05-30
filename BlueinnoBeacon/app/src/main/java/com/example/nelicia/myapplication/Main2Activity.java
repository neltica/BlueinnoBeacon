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

    private Button emoticon1,emoticon2,emoticon3;   //이모티콘 버튼 을 담을 변수
    private TextView meter;                       //미터를 표시하는 텍스트 뷰를 담을 변수
    private Intent emoticonIntent,graphIntent;                //이모티콘 설정 액티비티랑 그래프 표현 액티비티로 가는 intent를 담은 변수
    private BeaconManager beaconManager;                    //비콘 메니저  (이게 있어야 비콘을 이용할 수 있음)

    private BluetoothGatt mBluetoothGatt;                  //gatt통신을 위한 변수
    private List<BluetoothGattService> services;            //ble에서 제공하는 service들을 담을 변수

    private static final int EMOTIONRESULTCODE=1;                   //이모티콘액티비티 종료 시그널

    private int[][] emoticonArray;                               //이모티콘배열    (3개의 모든 이모티콘을 담고있음)

    private String fileName;                                   //이모티콘설정을 파일로 저장하기 때문에 파일 이름을 저장하고 있어야함.
    private File file;                                        //파일 클래스

    private boolean startFlag=false;                          //ble쪽으로 전송을 시작할지 말지 선택하는 플래그(ble연결되기 전에 전송을 시작하면 애러가 나므로)

    private int postOutput;                                      //이전에 ble측으로 전송한 값을 저장한 변수로 중복해서 데이터를 전송하는것을 막기 위함, 계속해서 ble측으로 데이터를 전송하면 비콘의 동작속도가 느려지고 이는 거리측정에 정확성을 떨어뜨림.

    private int recvData,recvCount,sendCount;                   //데이터를 받고 보낼때 필요한 변수들


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        setTitle("Main2Activity");                           //액티비티 이름을 정한다.
        recvData=-129;                                      //최초에는 ble로 부터 절대 받을 수 없는수인 -129로 초기화한다.
        recvCount=0;
        sendCount=0;
        postOutput=-1;                                     //이전에 전송한 값을 -1로 초기화한다. 이는 ble측으로 데이터를 전송한적없음을 나타낸다.
        emoticonArray=new int[3][8];                          //이모티콘 배열을 초기화한다.
        fileName="setting.txt";                                 //세팅파일 이름을 지정한다.
        if(new File(getFilesDir(),fileName).exists()==true)   //파일이 있다면
        {
            FileRead();                                    //읽어들인다.
        }

        emoticonIntent=new Intent(Main2Activity.this,EmoticonActivity.class);                //이모티콘 intent를 지정한다.
        graphIntent=new Intent(Main2Activity.this,GraphActivity.class);                      //graph intent를 지정한다.

        emoticon1=(Button)findViewById(R.id.emoticon1);                             //이모티콘 버튼들을 연결한다.
        emoticon2=(Button)findViewById(R.id.emoticon2);
        emoticon3=(Button)findViewById(R.id.emoticon3);

        meter=(TextView)findViewById(R.id.meter);                                //미터 textview를 연결한다.

        emoticon1.setOnClickListener(this);                                     //버튼을 누를떄 이벤트를 연결해준다,
        emoticon2.setOnClickListener(this);
        emoticon3.setOnClickListener(this);
        meter.setOnClickListener(this);                                           //마찬가지

        beaconManager= BeaconManager.getInstanceForApplication(this);                         //비콘 메니저를 가져온다.
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));                //UUID들을 이용해서 세팅을 한다.
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);                                                                                                              //비콘 동작 시작!

        mBluetoothGatt=BLEDataClass.mBluetoothDevice.connectGatt(this,false,mGattCallback);                                                  //gatt통신 동작 시작

        while(true) {
            if(services!=null) {                                                    //서비스가 초기화 됐는지 체크한다.(초기화 되지 않았다는 말은 ble와 연결되지 않았다는 말이다.)
                for(int i=0;i<3;i++)                                               //연결이 됐다면
                {
                    for(int j=0;j<8;j++)
                    {
                        Log.i("sendData", String.valueOf(emoticonArray[i][j]));
                        for (BluetoothGattService service : services) {
                            service.getCharacteristics().get(0).setValue(emoticonArray[i][j],BluetoothGattCharacteristic.FORMAT_SINT8,0);                    //이모티콘정보를 ble측으로 전송한다.
                            mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                        }
                        try {
                            Thread.sleep(100);                                        //너무 빠르게 전송하면 데이터가 깨질 염려가 있으므로 100ms정도의 텀으로 전송한다.
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
        }

        startFlag=true;                                                              //이모티콘을 날리고 나면 그때부터 ble측에 표시할 이모티콘을 알려주는 데이터를 전송하기 시작한다.

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);          //액티비티가 종료되면 비콘 수신도 종료한다.
    }


    void FileSave()                                  //이모티콘 정보를 파일에 저장하는 메소드이다.
    {
        file=new File(getFilesDir(),fileName);

        try {
            FileOutputStream fileOutputStream=new FileOutputStream(file);                               //출력용 스트림을 생성
            for(int i=0;i<3;i++)
            {
                for(int j=0;j<8;j++)
                {
                    fileOutputStream.write(String.valueOf(emoticonArray[i][j]).getBytes());                     //이모티콘에서 1인덱스씩 읽어와서 파일에 쓴다.
                    if(j==7)
                    {
                        fileOutputStream.write("\n".getBytes());                                             //이모티콘 하나를 다 썼으면 다음줄로 옮긴다.
                    }
                    else
                    {
                        fileOutputStream.write(" ".getBytes());                                             //각 인덱스 사이에는 스페이스가 존재.
                    }
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void FileRead()                                             //이모티콘 정보를 설정파일에서 불러오는 메소드.
    {
        String fileName="setting.txt";                         //설정파일 이름은 고정이다.
        File file=new File(getFilesDir(),fileName);                                         //파일을 가져온다.
        byte[] buffer= new byte[1024];
        try {
            FileInputStream fileInputStream=new FileInputStream(file);
            fileInputStream.read(buffer);                                           //읽어와서
            Log.i("buffer", new String(buffer));
            String[] str=new String(buffer).split("\n");
            for(int i=0;i<3;i++)
            {
                int j=0;
                for(String temp:str[i].split(" "))
                {
                    emoticonArray[i][j]=Integer.parseInt(temp);                     //배열에 입력해준다.
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {                //이모티콘 액티비티나 그래프 액티비티가 종료되서 메인 액티비로 돌아올때 반응하는 곳이다.
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("onActivityResult","insert"+" "+requestCode+" "+resultCode);
        int[] resultArray;
        switch (requestCode)
        {
            case EMOTIONRESULTCODE:                                                //만약에 이모티콘 액비티에서 메인액티비티로 돌아왔다면
                if(resultCode==RESULT_OK)
                {
                    switch(data.getExtras().getInt("EMOTICONARRAYNUM"))
                    {
                        case 0:
                            resultArray=data.getExtras().getIntArray("EMOTICONRESULT");                            //이모티콘 액티비티에서 설정한 이모티콘 모양을 emoticonArray[0][0]~emoticonArray[0][7] 까지 저장한다.  (8x8 64개의 정보를 8개 로 압축하는 방법은 이모티콘 액티비티에서 설명)
                            emoticonArray[0]=resultArray;
                            break;
                        case 1:
                            resultArray=data.getExtras().getIntArray("EMOTICONRESULT");                       //이모티콘 액티비티에서 설정한 이모티콘 모양을 emoticonArray[1][0]~emoticonArray[1][7] 까지 저장한다.  (8x8 64개의 정보를 8개 로 압축하는 방법은 이모티콘 액티비티에서 설명)
                            emoticonArray[1]=resultArray;
                            break;
                        case 2:
                            resultArray=data.getExtras().getIntArray("EMOTICONRESULT");  //이모티콘 액티비티에서 설정한 이모티콘 모양을 emoticonArray[2][0]~emoticonArray[2][7] 까지 저장한다.  (8x8 64개의 정보를 8개 로 압축하는 방법은 이모티콘 액티비티에서 설명)
                            emoticonArray[2]=resultArray;
                            break;
                    }
                }
                FileSave();            //이모티콘 세팅 정보를 저장한다.
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.emoticon1:
                //startActivity(emoticonIntent);
                emoticonIntent.putExtra("EMOTICONARRAYNUM",0);                     //첫번째 이모티콘버튼을 눌렀다는 것을 이모티콘 액티비티에 알려준다.
                emoticonIntent.putExtra("EMOTICONARRAY",emoticonArray[0]);         //이모티콘 액티비티에 현재 첫번째 이모티콘이 어떻게 생겼는지 알려준다.
                startActivityForResult(emoticonIntent, EMOTIONRESULTCODE);            //이모티콘 액티비티를 시작한다.
                break;
            case R.id.emoticon2:
                //startActivity(emoticonIntent);
                emoticonIntent.putExtra("EMOTICONARRAYNUM",1);
                emoticonIntent.putExtra("EMOTICONARRAY",emoticonArray[1]);            //마찬가지
                startActivityForResult(emoticonIntent, EMOTIONRESULTCODE);
                break;
            case R.id.emoticon3:
                //startActivity(emoticonIntent);
                emoticonIntent.putExtra("EMOTICONARRAYNUM",2);
                emoticonIntent.putExtra("EMOTICONARRAY",emoticonArray[2]);            //마찬가지
                startActivityForResult(emoticonIntent, EMOTIONRESULTCODE);
                break;
            case R.id.meter:
                mBluetoothGatt.close();
                beaconManager.unbind(this);                                         //미터 텍스트뷰를 눌렀으면 gatt통신을 멈추고 비콘 메니저도 멈춘다.
                startActivity(graphIntent);                                         //그래프 시작
                break;
        }
    }

    @Override
    public void onBeaconServiceConnect() {                                        //비콘을 바인드 시키면(시작시키면) 동작하는 곳이다.  콜백타입
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {   //얘가 거리측정함수
                if(collection.size()>0)
                {
                    //Log.i("range", "first beacon i see is about" + collection.iterator().next().getDistance() + "meters away.");


                    if(startFlag) {                                                  //얘네는 비동기 방식이라 언제 시작할지 알려주지 않으면 이모티콘 전송할때 얘네도 같이 동작한다. 그렇게 되면 이모티콘 값이 이상하게 엉킬수 있다.
                        if(collection.iterator().next().getDistance()>=1.0)                        //거리가 1m이상이면
                        {

                            if(postOutput!=2) {                                       //이전에 전송한 값이 2가 아니라면
                                Log.i("sendData", String.valueOf(2)+" "+collection.iterator().next().getDistance());
                                    for (BluetoothGattService service : services) {
                                        service.getCharacteristics().get(0).setValue(2, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                                        mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));                                    //블루이노 ble측으로 2를 전송한다.
                                    }

                                postOutput=2;                                     //이전에 전송한 값을 2로 세팅한다.
                            }
                        }
                        else if(collection.iterator().next().getDistance()>=0.6)              //거리가 60cm 이상이면
                        {

                            if(postOutput!=1)                                             //이전에 전송한 값이 1이 아니라면
                            {
                                Log.i("sendData", String.valueOf(1)+" "+collection.iterator().next().getDistance());
                                    for (BluetoothGattService service : services) {
                                        service.getCharacteristics().get(0).setValue(1,BluetoothGattCharacteristic.FORMAT_SINT8,0);   //1을 세팅하고
                                        mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));                        //1을 전송한다.
                                    }

                                postOutput=1;            //이전에 전송한 값을 1로 세팅한다.
                            }

                        }
                        else if(collection.iterator().next().getDistance()>=0.3) //마찬가지
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
                        else                                 //만약에 위에것들이 전부 맞지 않으면
                        {
                            Log.i("sendData", String.valueOf(0));
                            for (BluetoothGattService service : services) {
                                service.getCharacteristics().get(0).setValue(-1,BluetoothGattCharacteristic.FORMAT_SINT8,0);    //블루이노쪽으로 -1을 보내서 반응하지 말라고 한다.
                                mBluetoothGatt.writeCharacteristic(service.getCharacteristics().get(0));
                            }
                        }
                    }



                    Message msg=Message.obtain();              //거리를 메세지 클래스에 담는다.
                    msg.obj=collection;
                    mHandler.sendMessage(msg);               //handler쪽으로 보내서 서브스레드에서 메인스레드의 동작인 UI변경을 할 수 있도록 해준다.

                }
            }
        });

        try{
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));             //이 함수를 여기서 호출해줘야 비콘에서 계속해서 데이터를 받는다.
        }
        catch(RemoteException e)
        {
            Log.i("catch","catch");
        }
    }

    private final Handler mHandler=new Handler(){           //안드로이드는 UI작업은 모두 메인스레드에서 돌도록 되어있다. 서브스레드에서 ui를 변경하면 에러가 난다. 하지만 handler를 이용하면 서브스레드에서도 ui를 변경할 수 있다.
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Collection<Beacon> collection= (Collection<Beacon>) msg.obj;
            meter.setText(String.valueOf(String.format("%.2f", collection.iterator().next().getDistance()) + "m"));     //미터 텍스트뷰의 거리를 변경해준다.
        }
    };


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {              //gatt 통신부분

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {    //ble에서 넘어오는 데이터가 변경되면 반응하는 곳 받는 데이터가 없으므로 현재는 안쓰는 곳
            super.onCharacteristicChanged(gatt, characteristic);

            Log.i("onCharacteristicChanged", new String(characteristic.getValue()));


            String[] recvData=new String(characteristic.getValue()).split(" ");


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {    //ble쪽으로 데이터를 보낼때 보내는 동작 이외에도 추가적인 작업이 있을 시에 사용하는 곳 (기본전송만 쓸 것이기 때문에 비워두면 됨)
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {       //연결상태가 변경되면 반응하는곳  수정할곳 없음
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
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {              //연결되면 반응하는 곳
            services = gatt.getServices();                                             //연결되면 service를 받아온다.
            Log.i("onServicesDiscovered", services.toString());

            for (BluetoothGattService service : services) {
                //gatt.readCharacteristic(service.getCharacteristics().get(0));
                mBluetoothGatt.setCharacteristicNotification(service.getCharacteristics().get(0),true);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {                      //디스크립터읽을때 쓰는곳, 안씀
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i("onDescriptorRead", String.valueOf(descriptor.getValue()));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", new String(characteristic.getValue()) + " " + gatt.getDevice().getName());               //케릭터리스틱 읽을때 쓰는 곳 현재 안씀

            //gatt.disconnect();


        }
    };

    @Override
    protected void onResume() {                                                //다른 액티비티로 넘어갔다가 돌아오면 반응하는 곳  비콘 다시 동작 ble 다시 연결
        super.onResume();
        mBluetoothGatt=BLEDataClass.mBluetoothDevice.connectGatt(this,false,mGattCallback);
        beaconManager= BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }


}
