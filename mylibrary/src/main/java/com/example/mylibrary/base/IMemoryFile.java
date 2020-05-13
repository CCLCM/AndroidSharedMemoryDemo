package com.example.mylibrary.base;

import android.os.ParcelFileDescriptor;

import com.example.mylibrary.callback.IReadBufferCallBack;

public interface IMemoryFile {
    void setBufferYv12CallBack(IReadBufferCallBack callBack);

    ParcelFileDescriptor getParcelFileDescriptor();

    void readShareBuffer();

    void release();
}
