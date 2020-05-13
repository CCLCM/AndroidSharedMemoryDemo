package com.example.sharedmemorydemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.mylibrary.IMyRemoteCtrl;
import com.example.mylibrary.IReadDataCallBack;
import com.example.mylibrary.utils.MemoryFileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ServerClientService extends Service {
    private byte[] isCanRead = new byte[1];
    private MemoryFile memoryFile = null;
    private MyRemoteCtrlImpl mCarcorderRemoteCtrl = new MyRemoteCtrlImpl();
    private IReadDataCallBack mIReadDataCallBack = null;
    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private int MEMORY_SIZE = 13729413 + 1;
    private IBinder mBinder = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //返回内部代理对象给调用端
        return mCarcorderRemoteCtrl.asBinder();
    }


    public class MyRemoteCtrlImpl extends IMyRemoteCtrl.Stub {


        @Override
        public void setParcelFileDescriptor(ParcelFileDescriptor pfd) throws RemoteException {
            Log.d("mysdk"," 服务端 接收到 setParcelFileDescriptor  == null ? " + (pfd==null) );
            if (memoryFile != null) {
                memoryFile.close();
                memoryFile = null;
            }
            mParcelFileDescriptor = pfd;
        }

        @Override
        public void registerFrameByteCallBack(IReadDataCallBack frameDataCallBack) throws RemoteException {
            mIReadDataCallBack = frameDataCallBack;
        }

        @Override
        public void unregisterFrameByteCallBack(IReadDataCallBack frameDataCallBack) throws RemoteException {
            if (mIReadDataCallBack == null) return;

            if (memoryFile != null) {
                memoryFile.close();
                memoryFile = null;
            }
            mIReadDataCallBack = null;
        }

        @Override
        public void readFile(String msg) throws RemoteException {
            Log.d("mysdk"," mParcelFileDescriptor  = null ? " + (mParcelFileDescriptor == null));
            if (mParcelFileDescriptor != null) {
                memoryFile = MemoryFileHelper.openMemoryFile(mParcelFileDescriptor, MEMORY_SIZE, 0x3);
            }
            Log.d("mysdk"," memoryFile  = null ? " + (memoryFile == null));
            try {
                InputStream open = getResources().getAssets().open("IMG.JPG");
                byte[] buffer = new byte[open.available()];
                Log.d("mysdk"," 服务端 buffer " + buffer.length );
                open.read(buffer);
                readImage(buffer);
                open.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void linkToDeath(IBinder binder) throws RemoteException {
            Log.d("mysdk"," 服务端 建立死亡链接  ");
            mBinder = binder;
            binder.linkToDeath(mDeathRecipient, 0);
        }


        @Override
        public void unlinkToDeath(IBinder binder) throws RemoteException {
            Log.d("mysdk"," 服务端 断开客户端死亡链接  ");
            binder.unlinkToDeath(mDeathRecipient, 0);
            mBinder = null;
        }
    }

    private void readImage(byte[] frame) {
        if (memoryFile != null) {
            try {
                memoryFile.readBytes(isCanRead, 0, 0, 1);
                if (isCanRead[0]== 0) {
                    memoryFile.writeBytes(frame, 0, 1, frame.length);
                    isCanRead[0] = 1;
                    memoryFile.writeBytes(isCanRead, 0, 0, 1);
                }
                Log.d("mysdk"," 服务端 canReadFrameData " );
                mIReadDataCallBack.canReadFileData();
            } catch (RemoteException e ) {
                Log.d("mysdk"," 服务端 readImage RemoteException " + e.getMessage() );
                e.printStackTrace();
            } catch (IOException e ) {
                Log.d("mysdk"," 服务端 readImage IOException " + e.getMessage() );
                e.printStackTrace();
            } catch (Exception e ) {
                Log.d("mysdk"," 服务端 Exception  "  + e.getMessage()  );
                e.printStackTrace();
            }
        }
    }

    IBinder.DeathRecipient mDeathRecipient =  new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                //客户端可以执行释放操作
                Log.d("mysdk"," 调用端已经死亡可以做释放调用端的逻辑 ");
                if (mBinder != null) {
                    mBinder.unlinkToDeath(this, 0);
                }
                if (memoryFile != null) {
                    memoryFile.close();
                    memoryFile = null;
                }
            }
        };
}
