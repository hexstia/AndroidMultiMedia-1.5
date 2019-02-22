package com.ruisasi.weChat.domain;

import android.util.Log;

import com.ruisasi.core.CommandParser;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class WeChatMessage implements Serializable {
    private int id;
    private String talker;
    private String nickname;
    private byte type;
    private byte[] contenlen;
    private String content;
    private  int  createTime;
    public WeChatMessage(String talker){
        this.talker = talker;
        this.id = -1;
    }
    public WeChatMessage(String talker,String content){
        this.talker = talker;
        this.content  =content;
    }
    public WeChatMessage(String talker,String content,byte type){
        this.talker = talker;
        this.content  =content;
        this.type = type;
    }

    private int totallen;
    public int getTotallen(){
        return  totallen;
    }
    public class byteMessage{
       public  byte[] bid =null;
        public byte[] talkerlen =null;
        public  byte[] btalker =null;
        public   byte[] nickname1en =null;
        public    byte[] bnickname =null;
        public   byte type;
        public  byte[] contenlen =null;
        public  byte[] bcontent =null;
        public  byte[] bcreateTime =null;
    }
    public byteMessage getBytes(){
        byteMessage bm = new byteMessage();
//id  4
        byte[] bid = CommandParser.int4Bytes_weixin(id);
        bm.bid = bid;
//talker 1en
        byte[] talkerlen = new byte[1];
        talkerlen[0] = (byte)talker.length();
        bm.talkerlen = talkerlen;
//talker content
        byte[]  btalker=   talker.getBytes();
        bm.btalker = btalker;
//nickname1en
        byte[] nickname1en = new byte[1];
        try {
            nickname1en[0] = (byte) nickname.getBytes("UTF-8").length;
            bm.nickname1en = nickname1en;
        }catch (UnsupportedEncodingException e){
            Log.i("TAG"," 编码UTF-8 失败");
        }
//nickname content
        try {
            byte[] bnickname = nickname.getBytes("UTF-8");
            bm.bnickname = bnickname;
        }catch (UnsupportedEncodingException e){
            Log.i("TAG"," 编码UTF-8 失败");
        }

//type b
        bm.type = this.type;

        bm.contenlen = this.contenlen;
//content
        Log.i("TAG","content :"+content);
        try {

            byte[] bcontent =  content.getBytes("UTF-8");
            Log.i("TAG","BYTE "+Arrays.toString(bcontent));
            bm.bcontent = bcontent;
        }catch (UnsupportedEncodingException e){
            Log.i("TAG"," 编码UTF-8 失败");
        }
//createTime
        Log.i("TAG","createTime :"+createTime);
        byte[] bcreateTime = CommandParser.int4Bytes_weixin(createTime);

        bm.bcreateTime = bcreateTime;
        totallen = bid.length+talkerlen.length+btalker.length+nickname1en.length+ bm.bnickname.length+1+contenlen.length+bm.bcontent.length+bcreateTime.length;

return bm;
    }
    @Override
    public String toString() {
        return "WeChatMessage{" +
                "id=" + id +
                ", talker='" + talker + '\'' +
                ", nickname='" + nickname + '\'' +
                ", type=" + type +
                ", contenlen=" + Arrays.toString(contenlen) +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public WeChatMessage(int id, String talker, String nickname, byte type, byte[] contenlen, String content, int createTime) {
        this.id = id;
        this.talker = talker;
        this.nickname = nickname;
        this.type = type;
        this.contenlen = contenlen;
        this.content = content;
        this.createTime = createTime;
    }
    public WeChatMessage(int id, String talker, byte type, byte[] contenlen, String content, int createTime) {
        this.id = id;
        this.talker = talker;
        this.type = type;
        this.contenlen = contenlen;
        this.content = content;
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTalker() {
        return talker;
    }

    public void setTalker(String talker) {
        this.talker = talker;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getContenlen() {
        return contenlen;
    }

    public void setContenlen(byte[] contenlen) {
        this.contenlen = contenlen;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }
}
