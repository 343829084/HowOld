package com.vann.howold;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.vann.howold.util.DetectedUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity implements View.OnClickListener{

    public static final int  REQUEST_CODE= 0x01;
    public static final int SUCCESS = 0x02;
    public static final int ERROR=0x03;



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


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           switch (msg.what){
               case SUCCESS:
                   mWaitting.setVisibility(View.GONE);
                   JSONObject json = (JSONObject) msg.obj;
                   jsonRsBitmap(json);
                   mImvShow.setImageBitmap(mBitmap);
                   break;
               case ERROR:
                   mWaitting.setVisibility(View.GONE);
                   String errorMsg = (String) msg.obj;
                   Toast.makeText(MainActivity.this,errorMsg,Toast.LENGTH_LONG).show();
                   break;
               default:
                   break;
           }
        }
    };

    private void jsonRsBitmap(JSONObject json) {
        Bitmap bm = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),mBitmap.getConfig());
        Canvas canvas = new Canvas(bm);
        canvas.drawBitmap(mBitmap,0,0,null);
        try {
            JSONArray faces =json.getJSONArray("face");
            int count = faces.length();
            mNum.setText("人数："+count);
            for(int i=0;i<count;i++){
                JSONObject face = faces.getJSONObject(i);
                JSONObject position = face.getJSONObject("position");
                float x = (float) position.getJSONObject("center").getDouble("x");
                float y = (float) position.getJSONObject("center").getDouble("y");
                float w = (float) position.getDouble("width");
                float h = (float) position.getDouble("height");
                x = x/100 * bm.getWidth();
                y = y/100 * bm.getHeight();
                w = w/100 * bm.getWidth();
                h = h/100 * bm.getHeight();
                Paint paint = new Paint();
                paint.setStrokeWidth(3);
                paint.setColor(0xffffffff);
                canvas.drawLine(x-w/2,y-h/2,x-w/2,y+h/2,paint);
                canvas.drawLine(x-w/2,y-h/2,x+w/2,y-h/2,paint);
                canvas.drawLine(x-w/2,y+h/2,x+w/2,y+h/2,paint);
                canvas.drawLine(x+w/2,y-h/2,x+w/2,y+h/2,paint);
                int age  = json.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = json.getJSONObject("attribute").getJSONObject("gender").getString("value");
                Bitmap ageBitmap = createBitmap(age,"Male".equals(gender));
                int width =ageBitmap.getWidth();
                int height = ageBitmap.getHeight();
                if(bm.getWidth()<mBitmap.getWidth() && bm.getHeight()<mBitmap.getHeight()){
                    float radio =Math.max(bm.getWidth()*1.0f/mBitmap.getWidth(),bm.getHeight()*1.0f/mBitmap.getHeight());
                    ageBitmap = ageBitmap.createScaledBitmap(ageBitmap,(int)(width*radio),(int)(height*radio),false);
                }
                canvas.drawBitmap(ageBitmap,x-ageBitmap.getWidth(),y-h/2-ageBitmap.getHeight(),null);
                mBitmap = bm;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *  创建年龄性别的Bitmap
     * @param age
     * @param isMale
     * @return
     */
    private Bitmap createBitmap(int age, boolean isMale) {
        TextView age_gender = (TextView) mWaitting.findViewById(R.id.tv_age_and_gender);
        age_gender.setText(String.valueOf(age));
        if(isMale){
            age_gender.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male),null,null,null);
        }else{
            age_gender.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female),null,null,null);
        }
        age_gender.setDrawingCacheEnabled(true);
        Bitmap ageBitmap = Bitmap.createBitmap(age_gender.getDrawingCache());
        age_gender.setDrawingCacheEnabled(false);
        return ageBitmap;
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.btn_photo: //获取照片
               Intent intent = new Intent(Intent.ACTION_PICK);
               intent.setType("image/*");
               startActivityForResult(intent,REQUEST_CODE);
               break;
           case R.id.btn_detected:  //人脸识别
               mWaitting.setVisibility(View.VISIBLE);
               if(imgPath != null && !imgPath.equals("")){
                   resizePhoto();
               }else{
                   mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.girl);
               }
               DetectedUtil.detected(mBitmap,new DetectedUtil.CallBackListener() {
                   @Override
                   public void onSuccess(JSONObject jsonObject) {
                       Message msg = Message.obtain();
                       msg.obj = jsonObject;
                       msg.what =SUCCESS;
                       handler.sendMessage(msg);
                   }

                   @Override
                   public void onError(FaceppParseException e) {
                       Message msg = Message.obtain();
                       msg.what = ERROR;
                       msg.obj = e.getErrorMessage();
                       handler.sendMessage(msg);
                   }
               });
               break;
           default:
               break;
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            if( data != null){
                Uri uri =data.getData();
                Cursor cursor = getContentResolver().query(uri,null,null,null,null);
//                if(cursor.moveToFirst()){
                cursor.moveToFirst();
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    imgPath = cursor.getString(index);
                Log.e("TAG","onActivityForResult()--->"+imgPath);
//                }
                if(cursor != null ){
                    cursor.close();
                }
                resizePhoto();
                mImvShow.setImageBitmap(mBitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *  获取并压缩图片Bitmap
     */
    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds =true;
        BitmapFactory.decodeFile(imgPath,options);
        double radio = Math.max(options.outWidth*1.0d/1024f,options.outHeight*1.0d/1024f);
        options.inSampleSize = (int) Math.ceil(radio);
        options.inJustDecodeBounds =false;
        mBitmap = BitmapFactory.decodeFile(imgPath,options);
    }
}
