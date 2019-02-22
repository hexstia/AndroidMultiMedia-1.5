package com.wangheart.rtmpfile.rtmp.model;

import com.alibaba.fastjson.JSONObject;
import com.wangheart.rtmpfile.rtmp.encode.ApcMsgBodyEncode;
import com.wangheart.rtmpfile.rtmp.encode.CloseScreenVideoEncode;
import com.wangheart.rtmpfile.rtmp.encode.OpenScreenVideoEncode;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * rss与手机间接口消息模型
 * @author fulr
 *
 */
public class ApcMsg {
	/**
	 * 字符编码
	 */
	public static final CharsetEncoder charsetEncoder= Charset.forName("utf-8").newEncoder();
	/**
	 * 字符解码
	 */
	public static final CharsetDecoder charsetDecoder= Charset.forName("utf-8").newDecoder();
	/**
	 * 包头长度
	 */
	public static final short HEAD_LENGTH = 16;
	
	/**
	 * 心跳
	 */
	public static final byte CMD_ID_HEAT_BEAT = 0x01;
	/**
	 * 获取设备信息
	 */
	public static final byte CMD_ID_GET_DEVICE_INFO = 0x02;
	/**
	 * 未读消息上报
	 */
	public static final byte CMD_ID_UNREAD_MSG_REPORT = 0x03;
	/**
	 * 打开微信会话界面
	 */
	public static final byte CMD_ID_OPEN_WEIXIN_TALK = 0x04;
	/**
	 * 关闭微信会话界面
	 */
	public static final byte CMD_ID_CLOSE_WEIXIN_TALK = 0x05;
	/**
	 * 打开屏幕视频流
	 */
	public static final byte CMD_ID_OPEN_SCREEN_VIDEO = 0x06;
	/**
	 * 关闭屏幕视频流
	 */
	public static final byte CMD_ID_CLOSE_SCREEN_VIDEO = 0x07;
	/**
	 * 键盘/鼠标事件下发
	 */
	public static final byte CMD_ID_INPUT_EVENT = 0x08;
	/**
	 * 向服务器发送，新消息
	 */
	public static final byte CMD_ID_SEND_MESSAGE= 0x09;
	/**
	 * 接受服务器 要发送的微信消息
	 */
	public static final byte CMD_ID_RECV_MESSAGE= 0x0A;
	/**
	 * 获取联系人名单
	 */
	public static final byte  CMD_ID_GET_CONTACT= 0x0B;
	
    private Map<Byte, ApcMsgBodyEncode> bodyEncodeMap = new HashMap<Byte, ApcMsgBodyEncode>();
    

    {
        bodyEncodeMap.put(CMD_ID_OPEN_SCREEN_VIDEO, new OpenScreenVideoEncode());
        bodyEncodeMap.put(CMD_ID_CLOSE_SCREEN_VIDEO, new CloseScreenVideoEncode());
    }
    
    
	/**
	 * 包头
	 */
	protected MsgHead head;
	
	/**
	 * 包体
	 */
	protected JSONObject body;
	
//	public ApcMsg() {
////		head = new MsgHead();
//	}
	
	public ApcMsg(byte cmdId) {
		head = new MsgHead(cmdId);
		
	}

	public ApcMsg(MsgHead head) {
		this.head = head;
	}
	
	public ApcMsg(MsgHead head,byte[] msgBody) throws Exception {
		this.head = head;
		if(msgBody!=null&&msgBody.length>0){
			ApcMsgBodyEncode encode = bodyEncodeMap.get(this.getCmdId());
			if(encode!=null){
				body = encode.praseMsgBody(msgBody);
			}else {
				throw new Exception("命令格式错误！");
			}
			
		}
	}
	

	/**
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] getBytes() throws Exception {
//		updateTotalLen();
		byte[] bodyBytes = null;
		if(body!=null){
			ApcMsgBodyEncode encode = bodyEncodeMap.get(this.getCmdId());
			if(encode!=null){
				bodyBytes = encode.getBytes(body);
			}
			else {
				throw new Exception("命令格式错误！");
			}
			
		}
		short len = HEAD_LENGTH;//
		if(bodyBytes!=null){
			len += bodyBytes.length;
		}
		this.setTotalLen(len);
		byte[] ba = new byte[head.getTotalLen()];
		headFill(ba);
		if(bodyBytes!=null){
			System.arraycopy(bodyBytes, 0, ba, HEAD_LENGTH, this.getTotalLen()-HEAD_LENGTH);
		}
		return ba;
	}
	
//	/**
//	 * 更新包长
//	 */
//	private void updateTotalLen(){
//		//更新包的长度
//		short len = HEAD_LENGTH;//
//		if(body!=null){
//			len +=body.toJSONString().length();
//		}
//		this.setTotalLen(len);
//	}

	public MsgHead getHead() {
		return head;
	}

	public void setHead(MsgHead head) {
		this.head = head;
	}

	public JSONObject getBody() {
		return body;
	}

	public void setBody(JSONObject body) {
		this.body = body;
	}
	
	private void headFill(byte[] ba) {
		head.headFill(ba);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(head.toString());
		sb.append("body:").append(body).append("\n");
		return sb.toString();
	}

	public byte getVersion() {
		return head.getVersion();
	}

	public void setVersion(byte version) {
		head.setVersion(version);
	}

	public byte getCmdId() {
		return head.getCmdId();
	}

	public void setCmdId(byte cmdId) {
		head.setCmdId(cmdId);
	}

	public int getSeqNum() {
		return head.getSeqNum();
	}

	public void setSeqNum(int seqNum) {
		head.setSeqNum(seqNum);
	}

	public short getTotalLen() {
		return head.getTotalLen();
	}

	public void setTotalLen(short totalLen) {
		head.setTotalLen(totalLen);
	}

	public int[] getReserve() {
		return head.getReserve();
	}

	public void setReserve(int[] reserve) {
		head.setReserve(reserve);
	}
	
	static protected int getUINT4(byte[] ba, int start) {
		if (ba.length <= start + 3)
			return 0;

		int r = 0;
		r |= 0x00FF & ba[start];
		r = r << 8;
		r |= 0x00FF & ba[start + 1];
		r = r << 8;
		r |= 0x00FF & ba[start + 2];
		r = r << 8;
		r |= 0x00FF & ba[start + 3];
		return r;
	}

	static protected void setUINT4(byte[] ba, int start, int value) {
		if (ba.length <= start + 3)
			return;
		ba[start] = (byte) (value >> 24 & 0xFF);
		ba[start + 1] = (byte) (value >> 16 & 0xFF);
		ba[start + 2] = (byte) (value >> 8 & 0xFF);
		ba[start + 3] = (byte) (value & 0xFF);
	}

	static protected short getUSHORT2(byte[] ba, int start) {
		if (ba.length <= start + 1)
			return 0;

		short r = 0;
		r |= 0x00FF & ba[start];
		r = (short) (r << 8);
		r |= 0x00FF & ba[start + 1];
		return r;
	}

	static protected void setUSHORT2(byte[] ba, int start, int value) {
		if (ba.length <= start + 1)
			return;
		ba[start] = (byte) (value >> 8 & 0xFF);
		ba[start + 1] = (byte) (value & 0xFF);
	}

	static public String byte2str(byte[] ba, int start, int len) {
		if (ba.length <= start + len - 1)
			return null;
		String r = new String();
		for (int i = 0; i < len; i++) {
			if (ba[start + i] == '\0')
				break;
			r += (char) ba[start + i];
		}
		return r;
	}

//	static protected void str2byte(byte[] ba, String s, int start, int len) {
//		if (ba.length <= start + len - 1)
//			return;
//
//		if (s.length() < len) {
//			for (int i = 0; i < s.length(); i++) {
//				ba[start + i] = (byte) s.charAt(i);
//			}
//			for (int i = s.length(); i < len; i++) {
//				ba[start + i] = 0;
//			}
//		} else {
//			for (int i = 0; i < len; i++) {
//				ba[start + i] = (byte) s.charAt(i);
//			}
//		}
//	}
	
	

/**
 * 包头部分总长度16字节
 * 
 */
public static class MsgHead {
	
	private byte version; // 版本
	private byte cmdId; // 命令ID
	private int seqNum; // 序列号
	private short totalLen = HEAD_LENGTH; // 消息的总长度(字节)
	private int[] reserve; // 预留8字节

	public MsgHead() {
		version = 0x1;
		totalLen = HEAD_LENGTH;
		reserve = new int[2];
	}
	
	public MsgHead(byte cmdId) {
		version = 0x1;
		totalLen = HEAD_LENGTH;
		reserve = new int[2];
		this.cmdId =  cmdId;
		this.seqNum = nextSeq();
	}

	public MsgHead(byte[] ba) {
		version = ba[0];
		cmdId = ba[1];
		seqNum = getUINT4(ba, 2);
		totalLen = getUSHORT2(ba, 6);
		reserve = new int[2];
		reserve[0] = getUINT4(ba, 8);
		reserve[1] = getUINT4(ba, 12);
	}

	protected void headFill(byte[] ba) {
		ba[0] = version;
		ba[1] = cmdId;
		setUINT4(ba, 2, seqNum);
		setUSHORT2(ba, 6, totalLen);
		setUINT4(ba, 8, reserve[0]);
		setUINT4(ba, 12, reserve[1]);

//		return 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" version  = ").append(version).append("\n");
		sb.append(" cmdId    = ").append(cmdId).append("\n");
		sb.append(" seqNum = ").append(seqNum).append("\n");
		sb.append(" totalLen = ").append(totalLen).append("\n");
		return sb.toString();
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getCmdId() {
		return cmdId;
	}

	public void setCmdId(byte cmdId) {
		this.cmdId = cmdId;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public short getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(short totalLen) {
		this.totalLen = totalLen;
	}

	public int[] getReserve() {
		return reserve;
	}

	public void setReserve(int[] reserve) {
		this.reserve = reserve;
	}
	
	private static int nextSeq = 1;
	private synchronized static int nextSeq(){
		return nextSeq++;
	}

}

}
