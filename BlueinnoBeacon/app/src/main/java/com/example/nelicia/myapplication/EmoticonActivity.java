package com.example.nelicia.myapplication;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class EmoticonActivity extends AppCompatActivity {

    private LinearLayout[] layer;
    private ToggleButton[] btn;
    private int emotionNum;
    private Intent intent;

    public EmoticonActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoticon);
        setTitle("EmoticonActivity");

        layer=new LinearLayout[8];
        layer[0]=(LinearLayout)findViewById(R.id.layer1);
        layer[1]=(LinearLayout)findViewById(R.id.layer2);
        layer[2]=(LinearLayout)findViewById(R.id.layer3);
        layer[3]=(LinearLayout)findViewById(R.id.layer4);
        layer[4]=(LinearLayout)findViewById(R.id.layer5);
        layer[5]=(LinearLayout)findViewById(R.id.layer6);
        layer[6]=(LinearLayout)findViewById(R.id.layer7);
        layer[7]=(LinearLayout)findViewById(R.id.layer8);

        btn=new ToggleButton[64];

        for (int i=0;i<64;i++)
        {
            btn[i]=new ToggleButton(this);
            btn[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.unchecked));
            btn[i].setTextOn("");
            btn[i].setTextOff("");
            final int finalI = i;
            btn[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        btn[finalI].setBackgroundDrawable(getResources().getDrawable(R.drawable.checked));
                    }
                    else
                    {
                        btn[finalI].setBackgroundDrawable(getResources().getDrawable(R.drawable.unchecked));
                    }
                }
            });
            btn[i].setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.125));

            layer[i/8].addView(btn[i]);
        }

        intent=getIntent();

        int[] arr=intent.getExtras().getIntArray("EMOTICONARRAY");
        emotionNum=intent.getExtras().getInt("EMOTICONARRAYNUM");

        for(int i=0;i<8;i++)
        {
            int unit=128;
            for(int j=0;j<8;j++)
            {
                if((arr[i]&unit)==unit)
                {
                    btn[(i*8)+j].setChecked(true);
                }
                else
                {
                    btn[(i*8)+j].setChecked(false);
                }
                unit/=2;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.i("backpress", "backpress");
        int[] emotionArray={getEmoticon(0),getEmoticon(1),getEmoticon(2),getEmoticon(3),getEmoticon(4),getEmoticon(5),getEmoticon(6),getEmoticon(7)};
        intent=new Intent();
        intent.putExtra("EMOTICONARRAYNUM", emotionNum);
        intent.putExtra("EMOTICONRESULT", emotionArray);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("pause", "pause");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("emoticon","resume");

        intent=getIntent();

        int[] arr=intent.getExtras().getIntArray("EMOTICONARRAY");
        emotionNum=intent.getExtras().getInt("EMOTICONARRAYNUM");

        for(int i=0;i<8;i++)
        {
            int unit=128;
            for(int j=0;j<8;j++)
            {
                if((arr[i]&unit)==unit)
                {
                    btn[(i*8)+j].setChecked(true);
                }
                else
                {
                    btn[(i*8)+j].setChecked(false);
                }
                unit/=2;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("onStop", "onStop");
        //finish();
    }

    public int getEmoticon(int row)
    {
        int result=0;
        int startIndex=row*8;
        int unit=128;
        for(int i=startIndex;i<startIndex+8;i++)
        {
            if(btn[i].isChecked())
            {
                result+=unit;
            }
            unit/=2;
        }
        return result;
    }
}
