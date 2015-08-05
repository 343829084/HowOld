package com.vann.howold.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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


    public interface CallBackListener{
        void onSuccess(JSONObject jsonObject);
        void onError(FaceppParseException e);
    }

    public static void detected(Bitmap bitmap,CallBackListener listener){
        try {
            HttpRequests requests  = new HttpRequests(FaceConstant.KEY,FaceConstant.SECRET,true,true);
            Bitmap bm = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG,100,bos);
            PostParameters params = new PostParameters();
            params.setImg(bos.toByteArray());
            JSONObject json =requests.detectionDetect(params);
            if(listener != null ){
                listener.onSuccess(json);
            }
        } catch (FaceppParseException e) {
            if(listener != null){
                listener.onError(e);
            }
            e.printStackTrace();
        }
    }
}
