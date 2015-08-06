package com.vann.howold.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.vann.howold.Constant.FaceConstant;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * @Author: wenlong.bian 2015-08-05
 * @E-mail: bxl049@163.com
 */
public class DetectedUtil  {

    public static final String TAG="DetectedUtil";

    public interface CallBackListener{
        void onSuccess(JSONObject jsonObject);
        void onError(FaceppParseException e);
    }

    public static void detected(final Bitmap bitmap,final CallBackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpRequests requests  = new HttpRequests(FaceConstant.Key,FaceConstant.Secret,true,true);
                    Bitmap bm = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG,100,bos);
                    byte[] array = bos.toByteArray();
                    PostParameters params = new PostParameters();
                    params.setImg(array);
                    JSONObject json =requests.detectionDetect(params);
                    Log.e(TAG,"Messaage:---"+json.toString());
                    if(listener != null ){
                        listener.onSuccess(json);
                    }
                } catch (FaceppParseException e) {
                        e.printStackTrace();
                        Log.e(TAG,"Error:---"+e.getErrorMessage());
                        if(listener != null){
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }
}
