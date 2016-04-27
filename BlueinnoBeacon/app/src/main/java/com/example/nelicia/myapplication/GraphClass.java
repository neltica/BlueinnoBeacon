package com.example.nelicia.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by nelicia on 2016-04-26.
 */
public class GraphClass extends View {


    private int baseX;
    private int baseY;

    private ArrayList<String> arrayListX;
    private ArrayList<String> arrayListY;


    public GraphClass(Context context) {
        super(context);

        arrayListX=new ArrayList<String>();
        arrayListY=new ArrayList<String>();
    }



    @Override
    public void onDraw(Canvas canvas)
    {
        int baseX=getWidth();
        int baseY=getHeight();
        Paint paint=new Paint();
        paint.setStrokeWidth(10);
        Log.i("BaseY", String.valueOf(baseY));


    }
}
