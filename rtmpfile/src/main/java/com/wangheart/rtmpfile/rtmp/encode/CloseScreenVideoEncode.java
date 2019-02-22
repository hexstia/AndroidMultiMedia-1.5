package com.wangheart.rtmpfile.rtmp.encode;


import com.alibaba.fastjson.JSONObject;

public class CloseScreenVideoEncode implements ApcMsgBodyEncode {

	@Override
	public JSONObject praseMsgBody(byte[] bodyBytes) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 错误码,=0 表示失败 =1 表示成功 
	 */
	@Override
	public byte[] getBytes(JSONObject body) {
		byte[] res = new byte[1];
//		res[0] = body.getByteValue("code");
		return res;
	}

}
