package com.example.mylibrary;

import com.example.mylibrary.IReadDataCallBack;

interface IMyRemoteCtrl {

        void setParcelFileDescriptor(in ParcelFileDescriptor pfd);
        void registerFrameByteCallBack(IReadDataCallBack frameDataCallBack);
        void unregisterFrameByteCallBack(IReadDataCallBack frameDataCallBack);
        void readFile(String msg);
        void linkToDeath(IBinder binder);
        void unlinkToDeath(IBinder binder);
}
