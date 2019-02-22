package com.ruisasi;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.genymobile.scrcpy.IO;
import com.ruisasi.core.CommandParser;
import com.ruisasi.core.SetDeviceInfoEncode;
import com.wangheart.rtmpfile.rtmp.model.ApcMsg;

import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;

import androidx.annotation.RequiresPermission;


public class DeviceControl {



    public    String serialValue,macAddressValue,manufacturerValue,brandValue,deviceValue,modelValue,boardValue;
    public static  String TAG = "TAG";

    public void resultServer(FileDescriptor fd, ApcMsg apc, byte code) {
        try{
            Log.e(TAG, "resultServer 返回服务器数据code= "+code);
            JSONObject json = new JSONObject();
            json.put("code", code);
            //byte[] jsonByte = new GetDeviceInfoEncode().getBytes(json);

            byte[] jsonByte = new byte[1];
            jsonByte[0]=code ;

            byte[] headByte = CommandParser.SetcombinLen(apc.getBytes(), jsonByte.length);

                IO.writeFully(fd,headByte,0,headByte.length);
                IO.writeFully(fd,jsonByte,0,jsonByte.length);
            //socket.shutdownOutput();
        } catch (Exception e) {
            Log.e(TAG, "fail 连接失败"+ e.getMessage());
        }
    }

    public void getDeviceInfo(Context context){
        macAddressValue = Settings.Global.getString(context.getContentResolver(), getSettings("PRODUCT_MACADRESS"));
        if(macAddressValue == null){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            //boolean hasMacAddress = wifiInfo != null && wifiInfo.hasRealMacAddress();
            boolean hasMacAddress = wifiInfo != null;
            macAddressValue = hasMacAddress ? wifiInfo.getMacAddress() : null;
            Log.e(TAG, "getDeviceInfo--> macAddressValue= "+macAddressValue);
        }
        serialValue = Build.getSerial();
        boardValue = getProperty("persist.product.board","unknown");
        modelValue = getProperty("persist.product.model","unknown");
        deviceValue = getProperty("persist.product.device","unknown");
        brandValue = getProperty("persist.product.brand","unknown");
        manufacturerValue = getProperty("persist.product.manufacturer","unknown");
        Log.e(TAG, "getDeviceInfo---> macAddressValue= "+macAddressValue +" ;serialValue= "+serialValue+" ;modelValue= "+modelValue
                +" ;brandValue= "+brandValue +" ;deviceValue= "+deviceValue +" ;boardValue= "+boardValue
                +" ;manufacturerValue= "+manufacturerValue);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, "unknown" ));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return value;
        }
    }

    public String getSettings(String fieldmac) {
        String productMac = null;
        try {
            Class<?> objClass = Class.forName("android.provider." + "Settings" + "$"+ "Global");

            Field declaredField = objClass.getDeclaredField(fieldmac);
            Object object = declaredField.get(objClass);
            productMac = object.toString();
        } catch (Exception e) {
            Log.e(TAG, "getSettings  fail :"+ e.getMessage());//e.printStackTrace();
        } finally {
            return productMac;
        }
    }

    public static String getIntentAction(String classname ,String action) {
        String actionName = null;
        try {
            Class<?> objClass = Class.forName(classname);//classname

            Field declaredField = objClass.getDeclaredField(action);
            Object object = declaredField.get(objClass);

            Log.d(TAG, "属性值： actionName, =" + declaredField.getName()  + ",   value=" + object.toString());

            actionName = object.toString();
        } catch (Exception e) {
            Log.e(TAG, "getSettings  fail :"+ e.getMessage());//e.printStackTrace();
        } finally {
            return actionName;
        }
    }

    public byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;

    }

    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendDeviceInfo(FileDescriptor fd, ApcMsg apc){
        while(true){
            try {
                Log.e(TAG, "sendDeviceInfo 连接已经建立");

                //向服务器端发送数据
                JSONObject body = new JSONObject();
                body.put("mac", macAddressValue);
                body.put("serial",serialValue);
                body.put("model", modelValue);
                body.put("brand", brandValue);
                body.put("device", deviceValue);
                body.put("board", boardValue);
                body.put("manufacturer", manufacturerValue);

                //包体字节数组
                byte[] bodyByte = new SetDeviceInfoEncode().getBytes(body);

                //包头  合成长度(包头长度+包体长度)
                byte[] headByte = CommandParser.SetcombinLen(apc.getBytes(), bodyByte.length);


                IO.writeFully(fd,headByte,0,headByte.length);

                IO.writeFully(fd,bodyByte,0,bodyByte.length);


                //socket.shutdownOutput();
                break;

            } catch (Exception e) {
                Log.e(TAG, "send 连接失败"+ e.getMessage());
                break;
            }
        }
    }

}
