package com.example.mylibrary;

import com.example.mylibrary.IFrameDataCallBack;

interface IMyRemoteCtrl {

        void setParcelFileDescriptor(in ParcelFileDescriptor pfd);
        void registerFrameByteCallBack(IFrameDataCallBack frameDataCallBack);
        void unregisterFrameByteCallBack(IFrameDataCallBack frameDataCallBack);
        void readFile(String msg);
        void linkToDeath(IBinder binder);
        void unlinkToDeath(IBinder binder);
}
