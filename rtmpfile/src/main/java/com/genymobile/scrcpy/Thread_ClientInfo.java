package com.genymobile.scrcpy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.TrafficStats;
import android.renderscript.ScriptGroup.Input;
import android.util.Log;

public class Thread_ClientInfo extends Thread {
	private Socket socket;
	private OutputStream outputStream;// 输出流，用于发送心跳
	private String IP;
	private int PORT;

	public Thread_ClientInfo(String IP, int PORT) {
		this.IP = IP;
		this.PORT = PORT;
	}

	public void getserver(Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void SocketInit() throws SocketException {
		socket = new Socket();
		if (!socket.getKeepAlive())
			socket.setKeepAlive(true);// true 若长时间没有连接则断开
		if (!socket.getOOBInline())
			socket.setOOBInline(true);// true,允许发送紧急数据，不做处理
		if (!socket.getTcpNoDelay())
			socket.setTcpNoDelay(true);// 关闭缓冲区，及时发送数据；
		if (!socket.getReuseAddress())
			socket.setReuseAddress(true);// 底层的Socket 不会立即释放本地端口
	}

	public boolean socketLink() {
		boolean connected = false;
		try {
			SocketInit();
			InetSocketAddress insa = new InetSocketAddress(IP, PORT);
			socket.connect(insa, 0);
		} catch (SocketTimeoutException e) {
			System.out.println("SocketTimeoutException");
		} catch (IOException e) {
			System.out.println("IOexception");
		} finally {
			connected = socket.isConnected();
			System.out.println(connected ? "已连接" : "未链接");
			if (!connected) {
				socket = null;
			}
		}

		return connected;
	}

	public Socket getSocket() {
		return socket;
	}

	public void SendHeartbeat() throws IOException, InterruptedException {

		while (true) {
			OutputStream os = socket.getOutputStream();
			int b = 0xff;
			os.write(b);
			os.flush();
//			os.close();
			os= null;
			Thread.sleep(5000);// 心跳包发送的时间为31秒为最合适

		}
	}

	@Override
	public void run() {
		try {

			while (!socketLink()) {// 如果连不上socket
//				while (!CheckUpNetStatus()) {// 如果连不上网，则循环检测
//					Thread.sleep(1000);
//				}
				Thread.sleep(1000);
			}
			SendHeartbeat();

		} catch (SocketException | InterruptedException e) {
			e.printStackTrace();
			run();
		} catch (IOException e) {
			e.printStackTrace();
			run();
		}
	}

}
