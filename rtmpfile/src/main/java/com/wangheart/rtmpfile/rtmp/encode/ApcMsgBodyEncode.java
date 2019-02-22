package com.wangheart.rtmpfile.rtmp.encode;


import com.alibaba.fastjson.JSONObject;

public interface ApcMsgBodyEncode {

	public JSONObject praseMsgBody(byte[] bodyBytes);
	
	public byte[] getBytes(JSONObject body);

}
