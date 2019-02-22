package com.ruisasi.core;

import android.graphics.Rect;
import android.util.Log;

import com.genymobile.scrcpy.DesktopConnection;
import com.genymobile.scrcpy.Device;
import com.genymobile.scrcpy.EventController;
import com.genymobile.scrcpy.Options;
import com.genymobile.scrcpy.ScreenEncoder;
import com.wangheart.rtmpfile.rtmp.RtmpHandle;

import java.io.IOException;
import java.net.Socket;


public class VideoStreamSend extends Thread{
    public  Socket socketA;

    public static boolean stopStream = true;
    public static String pixel = "720";
    public ScreenEncoder screenEncoder;
    public static boolean flag = true;
    public VideoStreamSend(Socket socket){
        flag = true;


    socketA = socket;
        Log.i("StartActivity","VideoStreamSend() constructor");
    }

    @Override
    public void run() {
        super.run();
        try {
            Log.i("StartActivity","createOptions()");
            Options options = createOptions(pixel,"8000000",false+"");
            scrcpy(options);
            Log.i("StartActivity","scrcpy()");

        } catch (Exception e) {
            Log.i("StartActivity","VideoStreamSend Exception "+e.getMessage());


        }finally {
            Log.i("StartActivity","VideoStreamSend Thread 退出");

        }

    }


    private static Rect parseCrop(String crop) {
        if (crop.isEmpty()) {
            return null;
        }
        // input format: "width:height:x:y"
        String[] tokens = crop.split(":");
        if (tokens.length != 4) {
            throw new IllegalArgumentException("Crop must contains 4 values separated by colons: \"" + crop + "\"");
        }
        int width = Integer.parseInt(tokens[0]);
        int height = Integer.parseInt(tokens[1]);
        int x = Integer.parseInt(tokens[2]);
        int y = Integer.parseInt(tokens[3]);
        return new Rect(x, y, x + width, y + height);
    }
    @SuppressWarnings("checkstyle:MagicNumber")
    private static Options createOptions(String... args) {
        Options options = new Options();
        if (args.length < 1) {
            return options;
        }
        int maxSize = Integer.parseInt(args[0]) & ~7; // multiple of 8
        options.setMaxSize(maxSize);

        if (args.length < 2) {
            return options;
        }
        int bitRate = Integer.parseInt(args[1]);
        options.setBitRate(bitRate);

        if (args.length < 3) {
            return options;
        }
        // use "adb forward" instead of "adb tunnel"? (so the server must listen)
        boolean tunnelForward = Boolean.parseBoolean(args[2]);
        options.setTunnelForward(tunnelForward);

        if (args.length < 4) {
            return options;
        }
        Rect crop = parseCrop(args[3]);
        options.setCrop(crop);

        return options;
    }
    private static void startEventController(final Device device, final KeyMouseSend kms) {
        EventController.obj = false;
        EventController.flag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new EventController(device, kms).control();
                } catch (IOException e) {
                    // this is expected on close
                    Log.i("StartActivity","VideoStreamSend startEventController() ");


                }
            }
        }).start();
    }
    private  void scrcpy(Options options) throws IOException {

        final Device device = new Device(options);
        boolean tunnelForward = options.isTunnelForward();
        try (DesktopConnection connection = DesktopConnection.open(device, tunnelForward,socketA)) {
            screenEncoder= new ScreenEncoder(options.getBitRate());
            // asynchronous
            KeyMouseSend kms = new KeyMouseSend(this.socketA);
            startEventController(device, kms);
            while(flag) {
            try {
                // synchronous
                    SendVideoSocket.isconned = true;
                    screenEncoder.streamScreen(device, connection.getFd());
                  //  Log.i("StartActivity", "streamScreen() end");


            } catch (IOException e) {
                Log.i("StartActivity","ERROR : Thread_ClientCore scrcpy() line: 91");

                Log.i("StartActivity","streamScreen() stop()");
            }
                try {
                    Log.i("TAG","睡眠2秒");
                    Thread.sleep(2000);

                }catch (InterruptedException e){

                }
            }
            // connection.socket.close();
        }

    }


}
