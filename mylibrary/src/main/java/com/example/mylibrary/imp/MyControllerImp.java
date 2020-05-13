package com.example.mylibrary.imp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.mylibrary.IReadDataCallBack;
import com.example.mylibrary.IMyRemoteCtrl;
import com.example.mylibrary.base.Controller;
import com.example.mylibrary.callback.IReadBufferCallBack;

public class MyControllerImp implements Controller {
    protected MemoryFileImp mMemoryFile;
    private IReadBufferCallBack mCallBack;
    private IMyRemoteCtrl mMyRemoteCtrl;
    private boolean isbind;
    private volatile static Controller mInstance;
    protected Context mContext;

    private IReadDataCallBack mFrameDataCallBack = new IReadDataCallBack.Stub() {
        @Override
        public void canReadFileData() throws RemoteException {
            if (mMemoryFile != null) {
                Log.d("mysdk", " 服务端返回  sdk  mFrameDataCallBack  ");
                mMemoryFile.readShareBuffer();
            }
        }
    };

    private MyControllerImp() {
    }

    @Override
    public int init(Context context) {
        //初始化服务
        Log.d("mysdk", " sdk  初始化服务  ");
        mContext = context;
        return initService();
    }


    private int initService() {
        Log.d("mysdk", " sdk  initService  ");
        Intent intent = new Intent("com.example.sharedmemorydemo.service.ServerClientService");
        intent.setClassName("com.example.sharedmemorydemo", "com.example.sharedmemorydemo.service.ServerClientService");
        isbind = mContext.bindService(intent, this, Service.BIND_AUTO_CREATE);
        return 0;
    }

    @Override
    public void readFile(String msg) {
        try {
            Log.d("mysdk", " sdk  setMessage  ");
            mMyRemoteCtrl.readFile(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setBackBufferCallBack(IReadBufferCallBack callBack) {
        mCallBack = callBack;
        mMemoryFile = MemoryFileImp.getInstance();
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("mysdk", " sdk  onServiceConnected  ");
        if (service == null) {
            if (mMyRemoteCtrl != null) {
                try {
                    mMyRemoteCtrl.unlinkToDeath(mFrameDataCallBack.asBinder());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mMyRemoteCtrl = null;
        } else {
            mMyRemoteCtrl = IMyRemoteCtrl.Stub.asInterface(service);
            if (mMyRemoteCtrl != null) {
                try {
                    mMyRemoteCtrl.linkToDeath(mFrameDataCallBack.asBinder());
                    Log.d("mysdk", " sdk  onServiceConnected  setBackBufferCallBack ");
                    if (mCallBack != null) {
                        mMyRemoteCtrl.setParcelFileDescriptor(mMemoryFile.getParcelFileDescriptor());
                        mMyRemoteCtrl.registerFrameByteCallBack(mFrameDataCallBack);
                        mMemoryFile.setReadBufferCallBack(mCallBack);
                    } else {
                        mMyRemoteCtrl.unregisterFrameByteCallBack(mFrameDataCallBack);
                        mMemoryFile.release();
                    }
                    Log.d("mysdk", " sdk  onServiceConnected  setBackBufferCallBack  eld ");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("mysdk", " sdk  onServiceDisconnected  ");
        if (mMyRemoteCtrl != null) {
            try {
                mMyRemoteCtrl.unlinkToDeath(mFrameDataCallBack.asBinder());
                mMyRemoteCtrl.unregisterFrameByteCallBack(mFrameDataCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mMyRemoteCtrl = null;
    }

    public static Controller getInstance() {
        if (mInstance == null) {
            synchronized (MyControllerImp.class) {
                if (mInstance == null) {
                    mInstance = new MyControllerImp();
                }
            }
        }
        return mInstance;
    }
}
