package com.example.mylibrary.imp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.MemoryFile;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.mylibrary.base.IMemoryFile;
import com.example.mylibrary.callback.IReadBufferCallBack;
import com.example.mylibrary.utils.MemoryFileHelper;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;

public class MemoryFileImp implements IMemoryFile {
    private IReadBufferCallBack  mReadBufferCallBack;
    private Handler mHander;
    private static  MemoryFileImp sMemoryFile;
    protected final String TAG = "mysdk " + this.getClass().getSimpleName();
    protected MemoryFile mMemoryFile;
    private byte[] isCanRead = new byte[1];
    private byte[] mBuffer = null;
    private byte[] FIleBuffer = null;
    private HandlerThread mHandlerThread;
    private boolean needRead;
    private ParcelFileDescriptor parcelFileDescriptor;
    private FileDescriptor mFD;
    protected boolean mIsStream;
    private int mReadNum;



    public static MemoryFileImp getInstance() {
        if (sMemoryFile == null) {
            synchronized (MemoryFileImp.class) {
                if (sMemoryFile == null) {
                    sMemoryFile = new MemoryFileImp();
                }
            }
        }
        return sMemoryFile;
    }


    protected MemoryFileImp() {
        try {
            mMemoryFile = getMemoryFile();
            mBuffer = getBuffer();
            FIleBuffer = getFIleBuffer();
            mFD = getFD();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandlerThread = getHandlerThread();
        mHandlerThread.start();

        mHander = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg == null) return;
                switch (msg.what) {
                    case 0:
                        readShareBufferMsg();
                        break;
                    case 1:
                        readShareBufferCallback();
                        break;
                }
            }
        };
    }


    protected FileDescriptor getFD() {
        try {
            Field mFDFiled = mMemoryFile.getClass().getDeclaredField("mFD");
            mFDFiled.setAccessible(true);
            return (FileDescriptor) mFDFiled.get(mMemoryFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected HandlerThread getHandlerThread() {
        return new HandlerThread("video_thread");
    }

    protected byte[] getFIleBuffer() {
        return new byte[13729413];
    }

    protected byte[] getBuffer() {
        return new byte[13729413];
    }

    protected MemoryFile getMemoryFile() {
        try {
            return new MemoryFile(TAG, 13729414);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.w(TAG, "create MemoryFile Fail");
        return null;
    }

    protected void setMemorySharedFD(FileDescriptor fd) {

    }

    @Override
    public void readShareBuffer() {
        if (mHander != null) {
            mHander.sendEmptyMessage(1);
        }
    }

    private void readShareBufferCallback() {
        try {
            if (mMemoryFile != null) {
                int count = mMemoryFile.readBytes(isCanRead, 0, 0, 1);
                if ((mReadNum++ % 255) == 0)
                    Log.d(TAG, " isCanRead = " + isCanRead[0] + " mReadNum:" + mReadNum);
                if (isCanRead[0] == 1) {
                    mMemoryFile.readBytes(mBuffer, 1, 0, mBuffer.length);
                    System.arraycopy(mBuffer, 0, FIleBuffer, 0, mBuffer.length);
                    processData();
                    isCanRead[0] = 0;
                    mMemoryFile.writeBytes(isCanRead, 0, 0, 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void readShareBufferMsg() {
        try {
            if (mMemoryFile != null) {
                int count = mMemoryFile.readBytes(isCanRead, 0, 0, 1);
                if ((mReadNum++ % 5) == 0)
                    Log.d(TAG, " isCanRead = " + isCanRead[0]);
                if (isCanRead[0] == 1) {
                    mMemoryFile.readBytes(mBuffer, 1, 0, mBuffer.length);
                    System.arraycopy(mBuffer, 0, FIleBuffer, 0, mBuffer.length);
                    processData();
                    isCanRead[0] = 0;
                    mMemoryFile.writeBytes(isCanRead, 0, 0, 1);
                }
            }

            if (mHander != null && needRead) {
                mHander.removeCallbacksAndMessages(0);
                mHander.sendEmptyMessageDelayed(0, true ? 65 : 25);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processData() {
        if ( mReadBufferCallBack != null) {
             mReadBufferCallBack.onReadBuffer(FIleBuffer, FIleBuffer.length);
        }
    }

    public void startStream() {
        if (mIsStream) return;
        mIsStream = true;
        mHander.removeCallbacksAndMessages(0);
        needRead = true;
        setMemorySharedFD(mFD);
    }

    public void stopStream() {
        needRead = false;
        mIsStream = false;
        setMemorySharedFD(null);
        mHander.removeCallbacksAndMessages(0);
    }

    public void release() {
        mHander.removeCallbacksAndMessages(0);
        mHandlerThread.quit();
        mHander = null;
        if (mMemoryFile != null) {
            mMemoryFile.close();
            mMemoryFile = null;
        }
    }

    public ParcelFileDescriptor getParcelFileDescriptor() {
        if (parcelFileDescriptor == null) {
            parcelFileDescriptor = MemoryFileHelper.getParcelFileDescriptor(mMemoryFile);
        }
        return parcelFileDescriptor;
    }

    @Override
    public void setBufferYv12CallBack(IReadBufferCallBack callBack) {
         mReadBufferCallBack = callBack;
        if (callBack == null) {
            stopStream();
        } else {
            startStream();
        }
    }
}
