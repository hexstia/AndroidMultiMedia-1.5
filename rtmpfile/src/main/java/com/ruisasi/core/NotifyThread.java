package com.ruisasi.core;

import android.net.Network;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotifyThread extends Thread {

    public static boolean flag =true;
    public static String IP ="10.0.1.254";
    public Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.i("Notify" , "网络访问失败!");
            MainService.sleepBitcon = false;
            MainService.reLink = true;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.i("Notify" , "网络访问成功!");
            MainService.sleepBitcon = true;
            MainService.reLink = false;
        }
    };

    @Override
    public void run() {
        super.run();
//        OkHttpClient client  =   new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).writeTimeout(3,TimeUnit.SECONDS).readTimeout(3,TimeUnit.SECONDS).build();
//        Request request = new  Request.Builder().url("http://baidu.com").build();
//
        while(flag) {
//            Call call =client.newCall(request);
//            call.enqueue(callback);
            try {
                ping(IP);
            }catch (IOException e){
                    Log.i("StartActivity","NotifyThread IOException");
            }
            MainService.Sleep(5000);
//            call.cancel();
            }

    }
    public void ping (String address) throws IOException{
        Process process = Runtime.getRuntime().exec("ping -c 4 "+address);
        InputStreamReader r = new InputStreamReader(process.getInputStream());
        LineNumberReader returnData = new LineNumberReader(r);
        String returnMsg="";
        String line = "";
        while ((line = returnData.readLine()) != null) {
            System.out.println(line);
            returnMsg += line;
        }

        if(returnMsg.indexOf("100% packet loss")!=-1){
            Log.i("StartActivity","与 " +address +" 连接不畅通.");
            MainService.sleepBitcon = false;
            MainService.reLink = true;
        }  else{
//            Log.i("StartActivity","与 " +address +" 连接畅通.");
            MainService.sleepBitcon = true;
            MainService.reLink = false;
        }

    }
}
