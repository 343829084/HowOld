package com.vann.howold;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener{

    public static final int  REQUEST_CODE= 0x01;



    /**人脸识别*/
    private Button mBtnDetect;
    /**获取照片*/
    private Button mBtnPhoto;
    /**人脸数*/
    private TextView mNum;
    private FrameLayout mWaitting;
    private ImageView mImvShow;

    private String imgPath;
    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initlistener();
    }

    private void initlistener() {
        mBtnPhoto.setOnClickListener(this);
        mBtnDetect.setOnClickListener(this);
    }

    private void initView() {
        mBtnDetect = (Button) findViewById(R.id.btn_detected);
        mBtnPhoto = (Button) findViewById(R.id.btn_photo);
        mNum = (TextView) findViewById(R.id.tvNum);
        mWaitting = (FrameLayout) findViewById(R.id.fl_waitting);
        mImvShow = (ImageView) findViewById(R.id.imv_show);
    }


    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.btn_detected:
               Intent intent = new Intent(Intent.ACTION_PICK);
               intent.setType("image/*");
               startActivityForResult(intent,REQUEST_CODE);
               break;
           case R.id.btn_photo:
               break;
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK && data != null){
                Uri uri =data.getData();
                Cursor cursor = getContentResolver().query(uri,null,null,null,null);
                if(cursor.moveToFirst()){
                    int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imgPath = cursor.getString(index);
                }
                if(cursor != null ){
                    cursor.close();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
