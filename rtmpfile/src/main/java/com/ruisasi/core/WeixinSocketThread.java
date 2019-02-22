package com.ruisasi.core;

import android.net.LocalSocket;
import android.util.Log;

import com.genymobile.scrcpy.IO;
import com.ruisasi.weChat.DatabaseTools;
import com.ruisasi.weChat.domain.WeChatMessage;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

import java.io.FileDescriptor;
import java.io.ObjectInputStream;

public class WeixinSocketThread extends Thread{

    private FileDescriptor fd;
    private LocalSocket ls;
    public static  boolean flag  = true;
public WeixinSocketThread(FileDescriptor fd,LocalSocket ls) {
    this.fd = fd;
    this.ls = ls;
}



    @Override
    public void run() {
        super.run();
        while(flag) {
            try {
                //功能数据
                if(ls !=null){
                    ObjectInputStream in = new ObjectInputStream(ls.getInputStream());
                    try{
                        while(true) {
                            Log.i("TAG", "正在localscket中读取数据。。。");
                            WeChatMessage wcm = (WeChatMessage) in.readObject();

                            //设置昵称
                           wcm.setNickname(DatabaseTools.select_nickname(MainService.list,wcm.getTalker()));
                            Log.i("TAG", wcm.toString());
//                            //获取包体值
                            WeChatMessage.byteMessage bm  = wcm.getBytes();
                            Log.i("TAG", "包体的长度1 "+wcm.getTotallen());
                            //合成包头
                            ApcMsg apc = new ApcMsg(ApcMsg.CMD_ID_SEND_MESSAGE);
                            //包头包含的长度
                           byte[] head=  CommandParser.SetcombinLen( apc.getBytes(), wcm.getTotallen());
                            Log.i("TAG", "apc lenth  "+head.length);
                            head[5] = 0;
                         String headstr =    CommandParser.bytesToHexFun(head);
                            Log.i("TAG", "headstr "+headstr);
                            //发送包头
                            SocketSendThread.lock1.lock();
                            IO.writeFully(fd,head,0,head.length);
                            //发送包体

                            IO.writeFully(fd,bm.bid,0,bm.bid.length);
                            String bidstr =    CommandParser.bytesToHexFun(bm.bid);
                            Log.i("TAG","bidstr "+bidstr);

                            IO.writeFully(fd,bm.talkerlen,0,bm.talkerlen.length);
                            String talkerlenstr =    CommandParser.bytesToHexFun(bm.talkerlen);
                            Log.i("TAG","talkerlenstr "+talkerlenstr);

                            IO.writeFully(fd,bm.btalker,0,bm.btalker.length);
                            String btalkerstr =    CommandParser.bytesToHexFun(bm.btalker);
                            Log.i("TAG","btalkerstr "+btalkerstr);

                            IO.writeFully(fd,bm.nickname1en,0,bm.nickname1en.length);
                            String nickname1enstr  =    CommandParser.bytesToHexFun(bm.nickname1en);
                            Log.i("TAG","nickname1enstr "+nickname1enstr);

                            IO.writeFully(fd,bm.bnickname,0,bm.bnickname.length);
                            String bnickname  =    CommandParser.bytesToHexFun(bm.bnickname);
                            Log.i("TAG","bnickname "+bnickname);

                            byte[] type = new byte[1];
                            type[0] = bm.type;
                            IO.writeFully(fd,type,0,1);
                            String typestr =    CommandParser.bytesToHexFun(type);
                            Log.i("TAG","typestr "+typestr);

                            IO.writeFully(fd,bm.contenlen,0,bm.contenlen.length);
                            String contenlenstr =    CommandParser.bytesToHexFun(bm.contenlen);
                            Log.i("TAG","contenlenstr "+contenlenstr);

                            IO.writeFully(fd,bm.bcontent,0,bm.bcontent.length);
                            String bcontentstr =    CommandParser.bytesToHexFun(bm.bcontent);
                            Log.i("TAG","bcontentstr "+bcontentstr);

                            IO.writeFully(fd,bm.bcreateTime,0,bm.bcreateTime.length);
                            SocketSendThread.lock1.unlock();
                            String bcreateTimestr =    CommandParser.bytesToHexFun(bm.bcreateTime);
                            Log.i("TAG","bcreateTimestr"+ CommandParser.bytesToHexFun(bm.bcreateTime));
                            Log.i("TAG","createTime "+wcm.getCreateTime());
                            Log.i("TAG", "写入完成 ！！");
                            if(flag == false){
                                in.close();
                                return ;
                            }

                        }
                    }catch (ClassNotFoundException e){
                        Log.i("TAG", " object inputstream exception :"+e.getMessage());
                       in.close();
                       continue;
                    }

                }else {
                    continue;
                }
                //发送数据
            } catch (Exception e) {

            }
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){

            }
        }
        }
}
