package com.example.mylibrary;

import android.content.Context;

import com.example.mylibrary.base.Controller;
import com.example.mylibrary.callback.IReadBufferCallBack;
import com.example.mylibrary.imp.MyControllerImp;

public class SharedMemoryLibSDK {
    private volatile static SharedMemoryLibSDK mInstance;
    private Controller mController;

    private SharedMemoryLibSDK() {
    }

    public static SharedMemoryLibSDK getInstance() {
        if (mInstance == null) {
            synchronized (SharedMemoryLibSDK.class) {
                if (mInstance == null) {
                    mInstance = new SharedMemoryLibSDK();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化SDK
     *
     * @param context context
     * @return result
     */
    public int init(Context context) {
        mController = MyControllerImp.getInstance();
        return mController.init(context);
    }

    /***
     * 发送消息
     */
    public void readFile(String msg) {
        if (mController != null) {
            mController.readFile(msg);
        }
    }


    /**
     * setBackBufferCallBack
     *
     * @param callBack callback
     */
    public void setBackBufferCallBack(IReadBufferCallBack callBack) {
        if (mController != null) {
            mController.setBackBufferCallBack(callBack);
        }
    }

}
