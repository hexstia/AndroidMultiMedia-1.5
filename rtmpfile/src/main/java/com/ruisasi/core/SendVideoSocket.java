package com.ruisasi.core;

import android.util.Log;
import android.view.OrientationEventListener;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendVideoSocket extends WebSocketServer {
    private Lock lock = new ReentrantLock();
    private static WebSocket ws =null;
    public static Boolean isconned = true;
    public SendVideoSocket(int port) {
        super(new InetSocketAddress(port));
    }

    public static WebSocket getWebsocket(){
        return ws;
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        Log.i("StartActivity","Websocket Server onOpen()");

        lock.lock();
//        for (WebSocket ws:wss) {
//            ws = conn;
//        }
        if(ws!=null) {
            onClose(ws,1006,"",true);
        }
        ws = conn;
        lock.unlock();

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.i("StartActivity","Websocket Server onClose()");

        lock.lock();
        isconned = false;
        ws = null;
        lock.unlock();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.i("TAG","Websocket Server onMessage()");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.i("TAG","Websocket Server onError()"+ex.getMessage());
    }

    @Override
    public void onStart() {
        Log.i("TAG","Websocket Server onStart()");
    }
}
