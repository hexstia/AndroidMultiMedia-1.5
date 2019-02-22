package com.genymobile.scrcpy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.NetworkInfo.DetailedState;

public class InitTools {
	private static long lastTotalRxBytes = 0;
	private static long lastTimeStamp = 0;

	public static ServerData getxmlData() {
		// 解析xml文件 获得服务器数据
//		ArrayList<Type> parse = parsexml.parse(null);；//解析函数
		ServerData sd = ServerData.getInstatce();
		sd.setIP("192.168.255.105");
		sd.setPort_heart(28001);// 心跳包 状态信息通信
		sd.setPort_Scem_cont(27183);// 屏幕与反控
		return sd;
	}

	public static Dataformat parseServerSendData(String str) {// 设置数据
		Dataformat df = new Dataformat();
		String[] split = str.split(";");
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("")) {

			} else {
				df.setValue(i, split[i]);
			}
		}
		return df;
	}

	public static String getNetSpeed(Context context) {// 获取网速 分析分辨率
		String netSpeed = "0 kb/s";
		long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED
				? 0
				: (TrafficStats.getTotalRxBytes() / 1024);// 转为KB;
		long nowTimeStamp = System.currentTimeMillis();
		long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));// 毫秒转换

		lastTimeStamp = nowTimeStamp;
		lastTotalRxBytes = nowTotalRxBytes;
		netSpeed = String.valueOf(speed) + " kb/s";
		return netSpeed;
	}

	public static boolean CheckUpNetStatus(Context context) {// android 网络状态检查
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network[] allNetworks = connMgr.getAllNetworks();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < allNetworks.length; i++) {
			// 获取ConnectivityManager对象对应的NetworkInfo对象
			NetworkInfo networkInfo = connMgr.getNetworkInfo(allNetworks[i]);
			DetailedState detailedState = networkInfo.getDetailedState();
			String string = detailedState.toString();
			sb.append(string + " connect is " + networkInfo.isConnected());
			return true;
		}
		return false;
	}
}
