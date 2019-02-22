package com.ruisasi.core;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketTimeoutException;

public class MySocket {
    public static Socket socket = null;
    private static FileDescriptor socketFd = null;

    public static Socket getSocket(){

        return socket;
    }
    public  void initSocketFd(){
        if(socket !=null){
            socketFd =getFileDescriptor(socket);
        }

    }
    public static FileDescriptor getSocketFd(){
        if(socket !=null){
            return socketFd;
        }
        return null;
    }

    //获取socket的fd
    public FileDescriptor getFileDescriptor(Socket socket) {
        Class<? extends Socket> claz = socket.getClass();
        try {
            Field impl_field = claz.getDeclaredField("impl");
            impl_field.setAccessible(true);
            SocketImpl object = (SocketImpl) impl_field.get(socket);
            Class<? extends SocketImpl> class1 = object.getClass();
            Class<?> superclass = class1.getSuperclass();
            Class<?> superclass2 = superclass.getSuperclass();
            Class<?> superclass3 = superclass2.getSuperclass();
            Method declaredMethod = superclass3.getDeclaredMethod("getFileDescriptor");
            declaredMethod.setAccessible(true);
            FileDescriptor fd = (FileDescriptor) declaredMethod.invoke(object);
            return fd;

        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //创建socket
    public  void  SocketCreat() {
//        try {
             socket = new Socket();
        Log.i("StartActivity"," SocketCreat() ");
//            if (!socket.getKeepAlive())
//                socket.setKeepAlive(true);// true 若长时间没有连接则断开
//            if (!socket.getOOBInline())
//                socket.setOOBInline(true);// true,允许发送紧急数据，不做处理
//            if (!socket.getTcpNoDelay())
//                socket.setTcpNoDelay(true);// 关闭缓冲区，及时发送数据；
//            if (!socket.getReuseAddress())
//                socket.setReuseAddress(true);// 底层的Socket 不会立即释放本地端
//        }catch (SocketException e){
//
//        }

    }
    //连接socket
    public boolean socketLink(String IP,int PORT) {
        Log.i("StartActivity"," socketLink() ");
        boolean connected = false;
        try {
            InetSocketAddress insa = new InetSocketAddress(IP, PORT);
            socket.connect(insa,10000);

        } catch (SocketTimeoutException e) {
            Log.i("StartActivity"," socketLink() SocketTimeoutException");
        } catch (IOException e) {

        } finally {
            connected = socket.isConnected();
            Log.i("StartActivity"," connected: "+connected);

        }
        return connected;
    }
}
