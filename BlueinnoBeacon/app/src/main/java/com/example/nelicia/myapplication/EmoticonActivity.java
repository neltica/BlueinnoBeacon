package com.example.nelicia.myapplication;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class EmoticonActivity extends AppCompatActivity {

    private LinearLayout[] layer;
    private ToggleButton[] btn;

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
            btn[i].setText(String.valueOf(i));
            btn[i].setTextOn(String.valueOf(i));
            btn[i].setTextOff(String.valueOf(i));
            btn[i].setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.125));

            layer[i/8].addView(btn[i]);
        }




    }
}
