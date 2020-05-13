package com.example.mylibrary.base;

import android.content.Context;
import android.content.ServiceConnection;

import com.example.mylibrary.callback.IReadBufferCallBack;

public interface Controller extends ServiceConnection {
    /**
     * init sdk
     *
     * @param context context
     * @return result
     */
    int init(Context context);

    /**
     * read file
     * @param msg
     */
    void readFile(String msg);
    /**
     * setBackBufferCallBack
     *
     * @param callBack callback
     */
    void setBackBufferCallBack(IReadBufferCallBack callBack);

}
