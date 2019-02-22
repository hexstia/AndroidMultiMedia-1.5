package com.ruisasi.core.DataPacket;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.genymobile.scrcpy.IO;
import com.ruisasi.core.CommandParser;
import com.ruisasi.core.SocketSendThread;
import com.ruisasi.weChat.domain.Contact;
import com.ruisasi.weChat.domain.GroupMessage;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class DataCombine {
    public static JSONObject praseMsgBodyOpenOrCloseApp(byte[] bodyBytes) {
        JSONObject body = new JSONObject();
        int index = 0;

        //type U8   1byte
        byte type=(byte) (bodyBytes[0]&0xff);
        index += 1;

        //appPackageName STR_L  len 1byte ; content
        int len = bodyBytes[index]&0xff;
        index++;
        String appPackageName = ApcMsg.byte2str(bodyBytes, index, len);
        //index += len;

        Log.e("TAG", "praseMsgBodyTaskId type " + type +" appPackageName = " + appPackageName );

        body.put("type", type);
        body.put("appPackageName", appPackageName);

        return body;
    }
    public static void CombinContact_send(byte[] bs, ArrayList<Contact>list, FileDescriptor fd) throws IOException {

        //code +contactsData
        // 1+2+ x*contentlen
        //联系人数量
        int contact_count = list.size();

        //根据联系人数量获取每一个联系人
        int contact_item_len = 0 ;
        for(int i =0;i<list.size();i++){
           String username =  list.get(i).getUsername();
            String alias =  list.get(i).getAlias();
            String nickname =  list.get(i).getNickname();
            Log.i("TAG","Alias byte len:"+alias.getBytes().length);
            contact_item_len +=   (username.getBytes("UTF-8").length+nickname.getBytes("UTF-8").length+alias.getBytes("UTF-8").length+4);
        }
        //SocketSendThread.lock1.lock();
    //生成头部字节
       byte[] head =  CommandParser.SetcombinLen(bs,1+2+contact_item_len);
        Log.i("TAG_SEND","包体的长度"+(1+2+contact_item_len));
        IO.writeFully(fd,head,0,head.length);
        Log.i("TAG_SEND","Head Byte :"+CommandParser.bytesToHexFun(head));
//===========================================================================

        //包体 1 code
        byte[] code  = new byte[1];
        code[0] = 1;
        IO.writeFully(fd,code,0,code.length);
        Log.i("TAG_SEND","code Byte :"+CommandParser.bytesToHexFun(code));
        //包体 2.1 LEN
        byte[] Len =  CommandParser.int2Bytes_weixin(contact_count);
        IO.writeFully(fd,Len,0,Len.length);
        Log.i("TAG_SEND","BYTE Len :"+CommandParser.bytesToHexFun(Len));

        //包体 2.2
        for(int i =0;i<list.size();i++){
            byte[] type = new byte[1];
//            type[0] =  (byte) Integer.parseInt(list.get(i).getType());
//            Integer.parseInt(list.get(i).getType()) ==2;

            if((list.get(i).getUsername().lastIndexOf("@chatroom"))!=-1){
                //群房间
                type[0] =2;
            }else {
                //好友
                type[0] = 1;
            }
            IO.writeFully(fd,type,0,type.length);
            Log.i("TAG_SEND","type Byte :"+CommandParser.bytesToHexFun(type));
            //usernmae len
            byte[] username_len = new byte[1];
            username_len[0] =  (byte)list.get(i).getUsername().getBytes("UTF-8").length;
            IO.writeFully(fd,username_len,0,username_len.length);
            Log.i("TAG_SEND","username_len Byte :"+CommandParser.bytesToHexFun(username_len));
            //username
            byte[] username =  list.get(i).getUsername().getBytes("UTF-8");
            IO.writeFully(fd,username,0,username.length);
            Log.i("TAG_SEND","username Byte :"+CommandParser.bytesToHexFun(username));
            //alias len
            byte[] alias_len = new byte[1];
            alias_len[0] =  (byte)list.get(i).getAlias().getBytes("UTF-8").length;
            IO.writeFully(fd,alias_len,0,alias_len.length);
            Log.i("TAG_SEND","alias_len Byte :"+CommandParser.bytesToHexFun(alias_len));
            //alias
            if(alias_len[0] !=0) {
                byte[] alias = list.get(i).getAlias().getBytes("UTF-8");
                IO.writeFully(fd, alias, 0, alias.length);
                Log.i("TAG_SEND","alias Byte :"+CommandParser.bytesToHexFun(alias));
            }
            //nickname len
            byte[] nickname_len = new byte[1];
            nickname_len[0] =  (byte)list.get(i).getNickname().getBytes("UTF-8").length;
            IO.writeFully(fd,nickname_len,0,nickname_len.length);
            Log.i("TAG_SEND","nickname_len Byte :"+CommandParser.bytesToHexFun(nickname_len));
            //nickname
            if(nickname_len[0]!=0) {
                byte[] nickname = list.get(i).getNickname().getBytes("UTF-8");
                IO.writeFully(fd, nickname, 0, nickname.length);
                Log.i("TAG_SEND","nickname Byte :"+CommandParser.bytesToHexFun(nickname));
                Log.i("TAG_SEND","nickname string :"+nickname);
            }
          //  SocketSendThread.lock1.unlock();
            Log.i("TAG_SEND","发送完第"+i+"个好友");
        }
        Log.i("TAG_SEND","发送数据成功");
    }
//接受服务器发送来的微信消息
    public static String[] CombinMessage_recv(ApcMsg apc, FileDescriptor fd) throws IOException {
        int bodylen  = apc.getTotalLen()-16;
        Log.i("TAG_SEND","包体长度"+bodylen);
        byte[] body = new byte[bodylen];
        IO.readFully(fd,body,0,bodylen);
        Log.i("TAG_SEND","包体字节内容 :"+CommandParser.bytesToHexFun(body));
        //talker
        int talkerlen = CommandParser.toUnsignedInt(body[0]);
        Log.i("TAG_SEND","聊天者长度:"+talkerlen);
        String talker = new String(body,1,talkerlen,"UTF-8");
        Log.i("TAG_SEND","聊天者名称:"+talker);
        int offset = talkerlen+1;
        //type
        int type =   CommandParser.toUnsignedInt(body[offset]);
        Log.i("TAG_SEND","类型值:"+type);
        byte[] contentlength = new byte[2];
        contentlength[0] = body[offset+1];
        contentlength[1] = body[offset+2];
        //contentLen
        int contentlen =   CommandParser.byte2int_weixin(contentlength);
        Log.i("TAG_SEND","内容长度 :"+contentlen);
        String content1 = new String(body,offset+3,contentlen,"UTF-8");
        Log.i("TAG_SEND","内容 :"+content1);
        String[] Message = new String[2];
        Message[0]= talker;
        Message[1]= content1;
        return Message;
    }
//获取服务器要打开的聊天窗口 好友
    public static String getWindowTalker(ApcMsg apc, FileDescriptor fd) throws Exception {
       int bodylen = apc.getTotalLen()-16;
       byte[] body = new byte[bodylen];
        IO.readFully(fd,body,0,bodylen);
        int str_len =CommandParser.toUnsignedInt(body[0]);
        String talker = new String(body,1,str_len,"UTF-8");
        return  talker;
    }
    //获取服务器要群发的消息
    public static GroupMessage get_Group_Talkers(byte[] body) throws Exception{
        Log.i("TAG","进入group函数 :");
        Log.i("TAG","包体的内容 BYTE"+CommandParser.bytesToHexFun(body));
        int talkerlen = CommandParser.byte2int_weixin(body); //body 使用2 0 1‘
        Log.i("TAG","talkerlen :"+talkerlen);//整个联系人的长度
        ArrayList<String> list_talkers = new ArrayList<String>();
        int offset = 2;

      String talkers =  new String(body,offset,talkerlen,"UTF-8");
        String[] talker_arr =  talkers.split(",");
        for (String talker:talker_arr) {
            Log.i("TAG","talker :"+talker);
        }
        offset +=talkerlen;
        byte type = body[offset];
        Log.i("TAG","type :"+type);
        offset++;
        byte[] contenlen = new byte[2];
        System.arraycopy(body,offset,contenlen,0,2);
        int contentlen = CommandParser.byte2int_weixin(contenlen); //body 使用2 0 1‘
        Log.i("TAG","contentlen :"+contentlen);
        offset+=2;
      String content =   new String(body,offset,contentlen,"UTF-8");
        Log.i("TAG","content :"+content);
      GroupMessage gm = new GroupMessage(talker_arr,type,content);
    return  gm;
    }
    public static int Pause_Group_Send(byte[] body)throws Exception{
            int str_len  = body[0];
            String str = new String(body,1,str_len,"UTF-8");
            int type = body[1+str_len];

            return type;
    }

}
