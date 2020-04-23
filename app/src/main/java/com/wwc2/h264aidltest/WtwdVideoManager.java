package com.wwc2.h264aidltest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;

import com.wwc2.dvr.IRawDataCallback;
import com.wwc2.dvr.IRawDataManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author zhongdao
 * @Date 2020-04-22 16:37
 * Description TODO
 */
public class WtwdVideoManager {

    private String TAG = "hzy ";
    private static volatile WtwdVideoManager SINGLETON = null;

    private IRawDataManager mIRawDataManager;
//    static final byte sps[]= {0x00,0x00,0x00,0x01,0x67,0x64,0x00,0x0a,
//                             (byte)0xac,0x1b,0x1a,(byte)0x80,(byte)0xa0,0x2f,(byte)0xf9,0x66,
//                             (byte)0xa0,(byte)0xa0,0x40,0x40,(byte)0xf0,(byte)0x88,0x46,(byte)0xe0};
//
//    static final byte pps[]={0x00,0x00,0x00,0x01,0x68,(byte)0xea,0x43,(byte)0xcb};

    static final byte sps[]= {0x00,0x00,0x00,0x01,0x67,0x64,0x00,0x29,
                             (byte)0xac,(byte)0x1b,0x1a,(byte)0x80,(byte)0xa0,(byte)0x2f,(byte)0xf9,0x66,
                             (byte)0xa0,(byte)0xa0,0x40,0x40,(byte)0xf0,(byte)0x88,0x46,(byte)0xe0};

    static final byte pps[]={0x00,0x00,0x00,0x01,0x68,(byte)0xea,0x43,(byte)0xcb};


    private ArrayMap<String, RemoteCallBack> mRemoteCallBackMap = new ArrayMap<>();

    public static final String LOCAL_H264_FONT_TYPE = "front";
    public static final String LOCAL_H264_BACK_TYPE = "back";
    public static final String LOCAL_H264_LEFT_TYPE = "left";
    public static final String LOCAL_H264_RIGHT_TYPE = "right";
    private static final String h264name = ".h264";

    private WtwdVideoManager() {
    }


    private IBinder.DeathRecipient  mDeathRecipient = new  IBinder.DeathRecipient(){
        @Override
        public void binderDied() {
            mIRawDataManager.asBinder().unlinkToDeath(mDeathRecipient,0);
            mRemoteCallBackMap.clear();
            mIRawDataManager = null;
        }
    };

    public static WtwdVideoManager getInstance() {
        if (SINGLETON == null) {
            synchronized (WtwdVideoManager.class) {
                if (SINGLETON == null) {
                    SINGLETON = new WtwdVideoManager();
                }
            }
        }
        return SINGLETON;
    }

    public void bindService(Context context) {
        Intent intent = new Intent();
        intent.setPackage("com.wwc2.dvr");
        intent.setAction("com.wwc2.dvr.CameraRawService");
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"bindService");
    }

    //
    public int getYKChannel(String type) {

        int channelNum = 0;
        if (LOCAL_H264_FONT_TYPE.equals(type)) {
            return 1;
        } else if (LOCAL_H264_BACK_TYPE.equals(type)) {
            return 2;
        } else if (LOCAL_H264_LEFT_TYPE.equals(type)) {
            return 3;
        } else if (LOCAL_H264_RIGHT_TYPE.equals(type)) {
            return 4;
        }

        return channelNum;
    }


    void registerCallBack(String type){


       if(mRemoteCallBackMap.get(type) != null){
           return;
        }

        //YK
        int channelNum = getYKChannel(type);

        RemoteCallBack callBack = new RemoteCallBack(type+h264name, channelNum);

        mRemoteCallBackMap.put(type,callBack);
        if(mIRawDataManager != null) {
            try {
                mIRawDataManager.register(callBack, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    void unregisterCallBack(String type){
        RemoteCallBack callBack  =mRemoteCallBackMap.remove(type);
        if(callBack != null) {
            try {
                if (mIRawDataManager != null) {
                    mIRawDataManager.unregister(callBack, type);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.d(TAG,"onService Connected  ");
            mIRawDataManager =  IRawDataManager.Stub.asInterface(service);

            try {
                service.linkToDeath(mDeathRecipient,0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onService Disconnected ");
            mRemoteCallBackMap.clear();
            mIRawDataManager = null;

        }
    };

    public class RemoteCallBack extends IRawDataCallback.Stub{

        int channelNum;
        boolean isSPS = true;

        RemoteCallBack(String mfliename, int channelNum){
            this.channelNum = channelNum;
        }

//        public String byteToHex(byte[] bytes){
//            String strHex = "";
//            StringBuilder sb = new StringBuilder("");
//            for (int n = 0; n < bytes.length; n++) {
//                strHex = Integer.toHexString(bytes[n] & 0xFF);
//                sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
//            }
//            return sb.toString().trim();
//        }

        @Override
        public void onDataFrame(ParcelFileDescriptor pfd, int size) throws RemoteException {

            byte data[] = new byte[size];

            FileInputStream fis = new FileInputStream(pfd.getFileDescriptor());
            try {
                int  sz = fis.read(data,0,size);
                if(sz != size){
                    Log.d(TAG," read error ,only  read  = " + sz + ", but  need read  " +size);
                }
                Log.d(TAG," read =" + sz + " , need read size= " +size  + "channelNum =" +channelNum);

                //YK
                //传输JTT1078数据
                String channel = "" + channelNum;
                if(isSPS){
                    isSPS =false;

//                    String a= byteToHex(data);
//                    Log.d(TAG,"1ns frame =" + a);
//
//                    byte data1[] = new byte[24];
//
//                    System.arraycopy(data, 0, data1, 0, 24);
                    JttManager.getInstance().manager.videoLive(sps, channelNum, JttManager.getInstance().getLiveClient(channel));

//                    Log.d(TAG," data1 =" +data1.toString());
//
//                    byte data2[] = new byte[8];
//                    System.arraycopy(data, 24, data2, 0, 8);

//                    Log.d(TAG," data2 = " +data2.toString());
                    JttManager.getInstance().manager.videoLive(pps, channelNum, JttManager.getInstance().getLiveClient(channel));
                }else {
                    JttManager.getInstance().manager.videoLive(data, channelNum, JttManager.getInstance().getLiveClient(channel));
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

}