package com.ruisasi.core;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wangheart.rtmpfile.rtmp.encode.ApcMsgBodyEncode;

public class SetDeviceInfoEncode implements ApcMsgBodyEncode {

    public static final String TAG = "DeviceInfoService";

    @Override
    public JSONObject praseMsgBody(byte[] bodyBytes) {
        JSONObject body = new JSONObject();
        byte type=(byte) (bodyBytes[0]&0xff);
        Log.e(TAG, "SetDeviceInfoEncode type " + type);
        body.put("type", type);
        return body;
    }

    @Override
    //服务器获取设备信息    向服务器发送send设备信息
    public byte[] getBytes(JSONObject body) {
        String mac = body.getString("mac");
        byte macLen = 17;

        String serial = body.getString("serial");
        byte serialLen = (byte) serial.getBytes().length;

        String model = body.getString("model");
        byte modeLen = (byte) model.getBytes().length;

        String brand = body.getString("brand");
        byte brandLen = (byte) brand.getBytes().length;

        String device = body.getString("device");
        byte deviceLen = (byte) device.getBytes().length;

        String board = body.getString("board");
        byte boardLen = (byte) board.getBytes().length;

        String manufacturer = body.getString("manufacturer");
        byte manufacturerLen = (byte) manufacturer.getBytes().length;
        Log.e(TAG, "SetDeviceInfoEncode mac= " + mac +"; macLen="+macLen+ " ;serial= " + serial +"; serialLen="+serialLen
                + " ;model= " + model +"; modelLen="+modeLen
                + " ;brand= " + brand +"; brandLen="+brandLen
                + " ;device= " + device +"; deviceLen="+deviceLen
                + " ;board= " + board +"; boardLen="+boardLen
                + " ;manufacturer= " + manufacturer +"; manufacturerLen="+manufacturerLen);

        int len = macLen ;//mac
        len += serialLen+1;
        len += modeLen+1;
        len += brandLen+1;
        len += deviceLen+1;
        len += boardLen+1;
        len += manufacturerLen+1;
        byte[] res = new byte[len];
        //mac
        int index=0;
        System.arraycopy(mac.getBytes(), 0, res, index, macLen);
        index += macLen;

        //serial
        res[index]=serialLen;
        index++;
        System.arraycopy(serial.getBytes(), 0, res, index, serialLen);
        index += serialLen;

        //model
        res[index]=modeLen;
        index++;
        System.arraycopy(model.getBytes(), 0, res, index, modeLen);
        index += modeLen;

        //brand
        res[index]=brandLen;
        index++;
        System.arraycopy(brand.getBytes(), 0, res, index, brandLen);
        index += brandLen;

        //device
        res[index]=deviceLen;
        index++;
        System.arraycopy(device.getBytes(), 0, res, index, deviceLen);
        index += deviceLen;

        //board
        res[index]=boardLen;
        index++;
        System.arraycopy(board.getBytes(), 0, res, index, boardLen);
        index += boardLen;

        //manufacturer
        res[index]=manufacturerLen;
        index++;
        System.arraycopy(manufacturer.getBytes(), 0, res, index, manufacturerLen);
        index += manufacturerLen;

        return res;
    }

}
