package com.ruisasi.core;

import android.util.Log;

import com.genymobile.scrcpy.IO;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

import java.io.FileDescriptor;
import java.util.concurrent.locks.ReentrantLock;

public class SocketSendThread extends Thread {
    public boolean flag =true;
    private FileDescriptor fd;
    private  SystemInfo si;
    public static String MAC = "i0:08:22:98:CD:FB";
    public static  ReentrantLock lock1 =  new ReentrantLock();
    public SocketSendThread(FileDescriptor fd,SystemInfo si){
             flag = true;
            this.fd = fd;
            this.si = si;
    }
    @Override
    public void run() {
        super.run();
        while(flag) {
            //生成头部
            Log.i("StartActivity","SocketSendThread run()" );
            ApcMsg apc = new ApcMsg(ApcMsg.CMD_ID_HEAT_BEAT);
            //6 7位定义长度
            // 生成 身体 身体长度位10
//            byte[] body = CommandParser.HeartPack(si.IP,"i0:08:22:98:CD:FB");
            byte[] body = CommandParser.HeartPack(si.IP,MAC);

            try {
//                合成长度
                byte[] head = CommandParser.SetcombinLen(apc.getBytes(), body.length);
//                public static native void arraycopy(Object src,  int  srcPos,
//                Object dest, int destPos,
//                int length);
                int len = head.length+body.length;
                byte[] content = new byte[len];//32
                for(int i = 0;i<head.length;i++) {
                    content[i] =head[i];

                }
                //
                int x = 0;
                for(int j = head.length;j<len;j++) {
                    content[j] =body[x++];
                }

                IO.writeFully(fd, content, 0, content.length);


            }catch (Exception e){
                Log.i("StartActivity","StartActivity Exception 86line" +e.getMessage());
               break;
            }
            try {
                Thread.sleep(30000);
            }catch (InterruptedException e ){
                break;
            }


        }
        Log.i("StartActivity","socket 发送线程退出" );

    }
}
