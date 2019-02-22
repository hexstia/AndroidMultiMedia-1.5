//package com.example.neil.audiorecordtest;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//import java.io.File;
//import java.io.FileDescriptor;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.net.SocketImpl;
//import java.net.UnknownHostException;
//import java.nio.ByteBuffer;
//import java.util.Vector;
//
//import android.app.Activity;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.media.MediaRecorder;
//import android.os.Process;
//import android.os.Bundle;
//import android.util.Log;
//
//public class StartService extends Service {
//
//	@Override
//	public IBinder onBind(Intent arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	// 采集音频的设备
//	private static final int AUDIO_RESOURCE = MediaRecorder.AudioSource.REMOTE_SUBMIX;
//	// 音频的采集率
//	private final static int AUDIO_SAMPLE_RATE = 16000;
//	// 音频通道类型 立体声
//	private final static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
//	// 音频格式
//	private final static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//	// 编码的mime值
//	private final static String MIME = "audio/mp4a-latm"; // 录音编码的mime
//	// 编码的key bit rate
//	private final static int RATE = 32000;
//	// //音频采样通道，默认2通道
//	private final static int CHANNELCOUNT = 1;
//
//	private MediaCodec mEnc;
//
//	public int bufferSizeInBytes;
//	public boolean isRecording;
//	private AudioRecord audioRecord;
//	private MediaFormat format;
//	private FileDescriptor fd;
//	FileOutputStream fos;
//	byte[] buffer;
//	private FileDescriptor fd2;
//	private DatagramSocket socket;
//	private InetAddress host;
//	private ReadThread rt;
//	private long index;
//	private long jndex;
//	private int length;
//	private static Vector<byte[]> list = new Vector<byte[]>(2006);
//
//	private boolean stopSendThread = true;
//    private MediaCodec.BufferInfo encodeBufferInfo;
//    private ByteBuffer[] encodeInputBuffers;
//    private ByteBuffer[] encodeOutputBuffers;
//    private byte[] AACAudio = new byte[0];
//
//	public FileDescriptor getFileDescriptor(Socket socket) {
//		Class<? extends Socket> claz = socket.getClass();
//		try {
//			Field impl_field = claz.getDeclaredField("impl");
//			impl_field.setAccessible(true);
//			SocketImpl object = (SocketImpl) impl_field.get(socket);
//			Class<? extends SocketImpl> class1 = object.getClass();
//			Class<?> superclass = class1.getSuperclass();
//			Class<?> superclass2 = superclass.getSuperclass();
//			Class<?> superclass3 = superclass2.getSuperclass();
//			Method declaredMethod = superclass3.getDeclaredMethod("getFileDescriptor");
//			declaredMethod.setAccessible(true);
//			FileDescriptor fd = (FileDescriptor) declaredMethod.invoke(object);
//			return fd;
//
//		} catch (NoSuchFieldException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//	// 初始化音频编码器
//	public void initAudioEncode() throws IOException {
//		Log.i("TTTTT", "initAudioEncode");
//		MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, CHANNELCOUNT);
//		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);// channel =1
//		 format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);// CHANNEL = 1
//		 format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//LC
//		format.setInteger(MediaFormat.KEY_BIT_RATE, RATE);//RATE 32000
//
//		mEnc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
//		mEnc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); // 设置为编码器
//		if(mEnc ==null){
//			Log.e("TTTTT", "create mediaEncode failed");
//
//		}
//	}
//	// 检查手机音频
//	public void checkAudio() {
//			Log.i("TTTTT", "checkAudio");
//		if (AudioRecord.ERROR_BAD_VALUE == bufferSizeInBytes || AudioRecord.ERROR == bufferSizeInBytes) {
//			throw new RuntimeException("Unable to getMinBufferSize");
//		}
//		int state = audioRecord.getState();
//
//		if (state == AudioRecord.STATE_UNINITIALIZED) {
//			throw new RuntimeException("AudioRecord STATE_UNINITIALIZED");
//		}
//	}
//	// 初始化音频录制
//	public void initAudioRecord() {
//
////		bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
//		// bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
//		// CHANNEL_CONFIG, AUDIO_FORMAT);
//		bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
//		audioRecord = new AudioRecord(AUDIO_RESOURCE, AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT,
//				bufferSizeInBytes);
//
//		Log.i("TTTTT", "bufferSizeInBytes" + bufferSizeInBytes);
//		checkAudio();
//	}
///**
//     * PCM数据编码成AAC
//     */
//
//    private void dstAudioFormatFromPCM(byte[] pcmData) {
//    		Log.i("TTTTT", "dstAudioFormatFromPCM");
//        int inputIndex;
//        ByteBuffer inputBuffer;
//        int outputIndex;
//        ByteBuffer outputBuffer;
//        int outBitSize;
//        int outPacketSize;
//        inputIndex = mEnc.dequeueInputBuffer(0);
//        inputBuffer = encodeInputBuffers[inputIndex];
//        inputBuffer.clear();
//        inputBuffer.limit(pcmData.length);
//        inputBuffer.put(pcmData);//PCM数据填充给inputBuffer
//        mEnc.queueInputBuffer(inputIndex, 0, pcmData.length, 0, 0);
//        outputIndex = mEnc.dequeueOutputBuffer(encodeBufferInfo, 0);
//        while (outputIndex >= 0) {
//            outBitSize = encodeBufferInfo.size;
//            outPacketSize = outBitSize + 7;//7为ADTS头部的大小
//            outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
//            outputBuffer.position(encodeBufferInfo.offset);
//            outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
//            AACAudio = new byte[outPacketSize];
//		//如果需要将视频合成MP4等音视频格式，不需要添加ADT头
//            addADTStoPacket(AACAudio, outPacketSize);//添加ADT头
//	   		 outputBuffer.get(AACAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中
//			//数据源存放在AACAudio
//	   			 try{
//	   			 fos.write(AACAudio);
//					}catch(IOException e){}
//            outputBuffer.position(encodeBufferInfo.offset);
//
//            mEnc.releaseOutputBuffer(outputIndex, false);
//
//            outputIndex = mEnc.dequeueOutputBuffer(encodeBufferInfo, 0);
//        }
//
//    }
//	@Override
//	public void onCreate() {
//
//		super.onCreate();
//		Log.i("TTTTT", "START SERVICE onCreate");
//		index = 0;
//		jndex = 0;
//	try {
//		 fos = new FileOutputStream(new File("/sdcard/Example.m4a"));
//
//
//		initAudioEncode();
//		mEnc.start();
//		encodeInputBuffers = mEnc.getInputBuffers();
//		encodeOutputBuffers = mEnc.getOutputBuffers();
//		encodeBufferInfo = new MediaCodec.BufferInfo();
//		// 初始化音频录制
//		initAudioRecord();
//		audioRecord.startRecording();// 开始录制
//			// 初始化音频编码器
//
//	}
//		 catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		rt = new ReadThread();
//
//		rt.start();
//	}
//
//	class ReadThread extends Thread {
//
//		@Override
//		public void run() {
//				Log.i("TTTTT", "ReadThread  run");
//			Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
//			isRecording = true;
//			buffer = new byte[bufferSizeInBytes];
//
//			while (isRecording) {
//				length = audioRecord.read(buffer, 0, bufferSizeInBytes);
//				  //  		 try{
//
//	   		//  			fos.write(buffer);
//
//						// }catch(IOException e){
//
//						// }
//
//				try{
//				dstAudioFormatFromPCM(buffer);
//				}catch(IllegalStateException e){}
//
//			}
//			super.run();
//		}
//	}
//
//
//	/**
//
//     * 添加ADTS头
//
//     *
//
//     * @param packet
//
//     * @param packetLen
//
//     */
//
//    private void addADTStoPacket(byte[] packet, int packetLen) {
//        int profile = 2; // AAC LC
//        int freqIdx = 8; // 16KHz
//        int chanCfg = 1; // CPE
//        // fill in ADTS data
//        packet[0] = (byte) 0xFF;
//        packet[1] = (byte) 0xF1;
//        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
//        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
//        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
//        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
//        packet[6] = (byte) 0xFC;
//    }
//
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		return super.onStartCommand(intent, flags, startId);
//	}
//
//	@Override
//	public void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//	}
//
//}
