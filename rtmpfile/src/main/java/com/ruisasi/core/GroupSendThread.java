package com.ruisasi.core;

import android.util.Log;

import com.genymobile.scrcpy.IO;
import com.ruisasi.core.DataPacket.DataCombine;
import com.ruisasi.weChat.domain.GroupMessage;
import com.ruisasi.weChat.domain.WeChatMessage;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GroupSendThread extends Thread{
    private FileDescriptor fd;
    private ApcMsg apc;
    public static int pause =2;
    private ObjectOutputStream oos;
    private byte[] body;
    private GroupMessage gm;

    public GroupSendThread(FileDescriptor fd, ApcMsg apc, ObjectOutputStream oos) {
        this.fd = fd;
        this.apc = apc;
        this.oos = oos;
        int bodylen =  apc.getTotalLen()-16;
        Log.i("TAG","读取的body len :"+bodylen);
        body = new byte[bodylen];
        try {
              IO.readFully(fd, body, 0, body.length);
             gm = DataCombine.get_Group_Talkers(body);
        }catch (Exception e){
        }
    }
    @Override
    public void run() {
        super.run();
        groupsend();

    }
    public void groupsend(){
//        pause = 1;//测试
        try {
            for (String talker:gm.getTalkers()) {
                while(pause ==1){//暂停
                    Thread.sleep(1000);
                }
                if(pause ==3){
                    break;
                }
                WeChatMessage wcm = new WeChatMessage(talker,gm.getContent(),(byte) gm.getType());

                oos.writeObject(wcm);
                oos.flush();
            }
            try {
                Log.i("TAG","进入回复");
                //回复 服务器
                String taskid = new String("A");
                int tasklen = taskid.getBytes("UTF-8").length;
                Log.i("TAG","tasklen ：" +tasklen);
                //回复包头
                byte[] head = CommandParser.SetcombinLen(apc.getBytes(), 2 + tasklen);
                Log.i("TAG","head content :"+CommandParser.bytesToHexFun(head));
                IO.writeFully(fd, head, 0, head.length);

                //回复包体
                byte[] rebody = CommandParser.group_packet_re(1, taskid);
                Log.i("TAG","rebody content :"+CommandParser.bytesToHexFun(rebody));
                IO.writeFully(fd, rebody, 0, rebody.length);
            }catch (Exception e){

            }


        }catch (IOException e){

        }catch (Exception e) {
        }
    }
}
