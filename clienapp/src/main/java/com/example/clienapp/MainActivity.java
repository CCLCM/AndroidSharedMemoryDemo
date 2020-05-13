package com.example.clienapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.example.mylibrary.MyLibSDK;
import com.example.mylibrary.callback.IReadBufferCallBack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

public class MainActivity extends AppCompatActivity {
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         iv = findViewById(R.id.iv);
        MyLibSDK.getInstance().init(this);
        MyLibSDK.getInstance().setBackBufferCallBack(new IReadBufferCallBack() {
            @Override
            public void onReadBuffer(final byte[] bytes, int i) {
                Log.d("mysdk"," 客户端 读取到客户写到共享内存的大小为: " + bytes.length);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = byteToBitmap(bytes);
                        iv.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }


    public static Bitmap byteToBitmap(byte[] imgByte) {  
         InputStream input = null;  
         Bitmap bitmap = null;  
         BitmapFactory.Options options = new BitmapFactory.Options();  
         options.inSampleSize = 8;  
         input = new ByteArrayInputStream(imgByte);
         SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(  
                                  input, null, options));  
         bitmap = (Bitmap) softRef.get();  
         if (imgByte != null) {  
             imgByte = null;  
         }  
         try {
             if (input != null) {  
                  input.close();  
             }  
         } catch (IOException e) {
             // TODO Auto-generated catch block  
             e.printStackTrace();  
         }  
         return bitmap;  
    }

    public void  readFIle(View view) {
        Log.d("mysdk"," 客户端  调用服务端的 readFIle  " );
        MyLibSDK.getInstance().readFile("我是客户端");
    }
}
