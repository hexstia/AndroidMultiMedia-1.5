package com.genymobile.scrcpy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class Thread_ServerSend implements Runnable {
	private Thread_ClientInfo cit;
	private InputStream is;
	private OutputStream os = null;
	private Handler handler;

	public Thread_ServerSend(Thread_ClientInfo cit, Handler handler) {
		this.cit = cit;
		this.handler = handler;
	}

	@Override
	public void run() {
		while (true) {
			Socket socket = cit.getSocket();// 心跳包socket
			if (socket == null || !socket.isConnected()) {// 确认心跳包socketA连接成功
				continue;
			}
			// 读取服务发送过来的数据
			// 数据格式为用||分隔,
			// on;480;8000000;false|
			//
			try {
				Thread.sleep(5000);
				is = socket.getInputStream();
				byte[] b = new byte[64];
				int len = is.read(b, 0, 64);
				if (len == -1) {// 如果有休眠机制就好了，当有数据来临时，唤醒并发送数据
//					is.close();
					is = null;
					continue;
				}
				String str = new String(b, 0, len);
				if (str.lastIndexOf("|") == -1) {// 本次数据没有读取完整 发送给socket 0xaa
					os = socket.getOutputStream();
					os.write(0xaa);
					os.flush();
//					os.close();
					os = null;
					continue;
				}
				System.out.println("接受pc数据:" + str);
				// 发送数据
				Looper loop = Looper.myLooper();
				Message msg = Message.obtain();
				if (msg == null) {
					System.out.println("message is null");
					continue;
				}
				if (handler == null) {
					System.out.println("handler is null");
					continue;
				}
				msg.obj = str;
				handler.sendMessage(msg);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
