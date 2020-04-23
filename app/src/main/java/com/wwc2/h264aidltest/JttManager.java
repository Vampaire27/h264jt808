package com.wwc2.h264aidltest;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.azhon.jtt808.JTT808Manager;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.bean.VideoPlaybackBean;
import com.azhon.jtt808.bean.VideoPlaybackUploadBean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.live.LiveClient;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author zhongdao
 * @date 2020-04-13 11:25
 * Description TODO
 */
public class JttManager implements OnConnectionListener {
    private static final String TAG = "JttManager";

    /**
     * 终端手机号
     * YK:013496388888
     */
    private static final String PHONE = "013888888801";
    /**
     * 制造商ID
     */
    private static final String MANUFACTURER_ID = "TYUAN";

    /**
     * 终端型号
     */
    private static final String TERMINAL_MODEL = "U47DPZAMSWAAHQCQ0000";

    /**
     * 终端ID
     * YK:LMY74DT
     */
    private static final String TERMINAL_ID = "LMY74DT";

    private static final int CMD_STOP_LIVE = 0;
    private static final int CMD_START_LIVE = 3;

    private static final int MAX_CHANNEL_COUNT = 4;

    private static JttManager jttManager;
    private WeakReference<Context> mContextRef;
    public JTT808Manager manager;

    private ExecutorService mAcceptThreadPool;
    private HashMap<String, Future> mFutures;
    //private HashMap<String, StreamAcceptTask> mTasks;
    private HashMap<String, LiveClient> mLiveClients;

    private JttManager() {
        mFutures = new HashMap<>(MAX_CHANNEL_COUNT);
       // mTasks = new HashMap<>(MAX_CHANNEL_COUNT);
        mLiveClients = new HashMap<>(MAX_CHANNEL_COUNT);
    }

    public static synchronized JttManager getInstance() {
        if (jttManager == null) {
            jttManager = new JttManager();
        }
        return jttManager;
    }

    void connect(Context context) {
        // YK "129.204.23.58"
        final String ip = "129.204.23.58";
        // YK:7611
        final int port = 7611;
        mContextRef = new WeakReference<>(context);
        manager = JTT808Manager.getInstance();
        manager.setOnConnectionListener(this).init(PHONE, TERMINAL_ID, ip, port);

        //TODO:建立连接时先绑定沃特沃德的视频服务
        WtwdVideoManager.getInstance().bindService(mContextRef.get());
    }

    @Override
    public void onConnectionSateChange(int state) {
        switch (state) {
            case OnConnectionListener.CONNECTED:
                manager.register(MANUFACTURER_ID, TERMINAL_MODEL);
                break;
            case OnConnectionListener.DIS_CONNECT:
                Log.d(TAG, "断开连接");
                break;
            case OnConnectionListener.RE_CONNECT:
                Log.d(TAG, "重连");
                break;
            default:
                break;
        }
    }

    @Override
    public void receiveData(JTT808Bean jtt808Bean) {

    }

    @Override
    public void videoPlayback(VideoPlaybackBean playbackBean) {

    }

    @Override
    public void videoPlaybackUpload(VideoPlaybackUploadBean playbackUploadBean) {

    }

    @Override
    public void alarmReceiveText(String text) {

    }

    @Override
    public void terminalParams(List<TerminalParamsBean> params) {

    }

    @Override
    public void audioVideoLive(String ip, int port, int channelNum, int dataType) {
        String channel = "" + channelNum;
        LiveClient client = getLiveClient(channel);
        if (client == null) {
            client = new LiveClient(ip, port);
            mLiveClients.put(channel, client);
        }

        startLive(channelNum);
    }

    @Override
    public void audioVideoLiveControl(int channelNum, int control, int closeAudio, int switchStream) {
        Log.d(TAG, "audioVideoLiveControl: " + control);
        switch (control) {
            case CMD_START_LIVE:
                startLive(channelNum);
                break;
            case CMD_STOP_LIVE:
                stopLive(channelNum);
                break;
            default:
                break;
        }
    }

    /**
     * 开始实时视频
     */
    private void startLive(int channelNum) {
        Log.d(TAG, "startLive通道：" + channelNum);
        // 因为本地通道号与平台通道号不匹配
        String channel = "" + channelNum;
        LiveClient client = getLiveClient(channel);
        if (client == null) {
            Log.e(TAG, "startLive: live client is null");
            return;
        }

        String wtwdChannel = getWtwdChannel(channelNum);
        if (wtwdChannel == null) {
            client.release();
            mLiveClients.remove(channel);
            return;
        }
        Log.d(TAG, "startLive: 开启接受沃特沃德数据线程通道 " + wtwdChannel);
        //开启沃特沃德视频通道
        WtwdVideoManager.getInstance().registerCallBack(wtwdChannel);
    }

    /**
     * 停止实时视频
     */
//    private void stopLive(int channelNum) {
//        Log.d(TAG, "stopLive: ");
//        String channel = "" + channelNum;
//        LiveClient client = getLiveClient(channel);
//        if (client != null) {
//            client.release();
//            mLiveClients.remove(channel);
//        }
//
//        String wtwdChannel = getWtwdChannel(channelNum);
//        Log.d(TAG, "stopLive: 停止接受沃特沃德数据线程通道 " + wtwdChannel);
//        //关闭沃特沃德视频通道
//        WtwdVideoManager.getInstance().unregisterCallBack(wtwdChannel);
//    }

    /**
     * 停止实时视频
     */
    private void stopLive(int channelNum) {
        Log.d(TAG, "stopLive: ");
        String channel = "" + channelNum;
        LiveClient client = getLiveClient(channel);
        if (client == null) {
            return;
        }
        client.release();
        mLiveClients.remove(channel);
        String wtwdChannel = getWtwdChannel(channelNum);
        if (wtwdChannel == null) {
            return;
        }
        Log.d(TAG, "stopLive: 停止接受沃特沃德数据线程通道 " + wtwdChannel);
        //关闭沃特沃德视频通道
        WtwdVideoManager.getInstance().unregisterCallBack(wtwdChannel);
    }



    public LiveClient getLiveClient(String channel) {
        return mLiveClients.get(channel);
    }

    static void release() {
        if (jttManager != null) {
            jttManager = null;
        }
    }


    /**
     * 返回沃特沃德视频通道号
     * @param channelNum
     * @return
     */
    private String getWtwdChannel(int channelNum) {

        String wtwdChannel = null;
        switch (channelNum) {
            case 1:
                wtwdChannel = WtwdVideoManager.LOCAL_H264_FONT_TYPE;
                break;

            case 2:
                wtwdChannel = WtwdVideoManager.LOCAL_H264_BACK_TYPE;
                break;

            case 3:
                wtwdChannel = WtwdVideoManager.LOCAL_H264_LEFT_TYPE;
                break;

            case 4:
                wtwdChannel = WtwdVideoManager.LOCAL_H264_RIGHT_TYPE;
                break;

                default:
                    break;
        }
        return wtwdChannel;
    }

}