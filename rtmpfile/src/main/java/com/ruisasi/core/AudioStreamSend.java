package com.ruisasi.core;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;


import com.genymobile.scrcpy.ScreenEncoder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketImpl;
import java.nio.ByteBuffer;
import java.util.Vector;

public class AudioStreamSend extends Thread{

        // 采集音频的设备
        private static final int AUDIO_RESOURCE = MediaRecorder.AudioSource.REMOTE_SUBMIX;
        // 音频的采集率
        public  static int AUDIO_SAMPLE_RATE = 44100;
        // 音频通道类型 立体声
        public  static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
        // 音频格式
        public final static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        // 编码的mime值
        public final static String MIME = "audio/mp4a-latm"; // 录音编码的mime
        // 编码的key bit rate
        public  static int RATE = 44100;
        // //音频采样通道，默认2通道
        private  static int CHANNELCOUNT = 1;
        private MediaCodec mEnc;
        public static int bufferSizeInBytes;
        public boolean isRecording;
        private AudioRecord audioRecord;
        byte[] buffer;
        private int length;
        private MediaCodec.BufferInfo encodeBufferInfo;


    public  AudioStreamSend(){

              Init();
        }


       public void  Init(){
           try {
               initAudioEncode();
               mEnc.start();
//               encodeInputBuffers = mEnc.getInputBuffers();

//               encodeOutputBuffers = mEnc.getOutputBuffers();
               encodeBufferInfo = new MediaCodec.BufferInfo();
               // 初始化音频录制
               initAudioRecord();
               audioRecord.startRecording();// 开始录制
               // 初始化音频编码器

           }
           catch (FileNotFoundException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
        }

    @Override
    public void run() {
        super.run();
        Log.i("TTTTT", "ReadThread  run");
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        isRecording = true;
        buffer = new byte[bufferSizeInBytes];
        while (isRecording) {
            length = audioRecord.read(buffer, 0, bufferSizeInBytes);
            try{
                dstAudioFormatFromPCM(buffer);
            }catch(IllegalStateException e){}

        }
        Log.i("StartActivity", "Audio Thread 退出");

    }
        // 初始化音频编码器
        public void initAudioEncode() throws IOException {
            Log.i("TTTTT", "initAudioEncode");
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, CHANNELCOUNT);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNELCOUNT);// channel =1
            format.setInteger(MediaFormat.KEY_CHANNEL_MASK, CHANNEL_CONFIG);// CHANNEL = 1
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//LC
            format.setInteger(MediaFormat.KEY_BIT_RATE, RATE);//RATE 32000
            bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
//            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,bufferSizeInBytes+24);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
            mEnc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mEnc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); // 设置为编码器
            if(mEnc ==null){
                Log.e("TTTTT", "create mediaEncode failed");

            }
        }
        // 检查手机音频
        public void checkAudio() {
            Log.i("TTTTT", "checkAudio");
            if (AudioRecord.ERROR_BAD_VALUE == bufferSizeInBytes || AudioRecord.ERROR == bufferSizeInBytes) {
                throw new RuntimeException("Unable to getMinBufferSize");
            }
            int state = audioRecord.getState();

            if (state == AudioRecord.STATE_UNINITIALIZED) {
                throw new RuntimeException("AudioRecord STATE_UNINITIALIZED");
            }
        }
        // 初始化音频录制
        public void initAudioRecord() {

//		bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
            // bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
            // CHANNEL_CONFIG, AUDIO_FORMAT);
            bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            audioRecord = new AudioRecord(AUDIO_RESOURCE, AUDIO_SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT,
                    bufferSizeInBytes);

            Log.i("TTTTT", "bufferSizeInBytes" + bufferSizeInBytes);
            checkAudio();
        }
        /**
         * PCM数据编码成AAC
         */
        private void dstAudioFormatFromPCM(byte[] pcmData) {
//            Log.i("TTTTT", "dstAudioFormatFromPCM");
            int inputIndex;
            ByteBuffer inputBuffer;
            int outputIndex;
            ByteBuffer outputBuffer;
            int outBitSize;
            int outPacketSize;
            inputIndex = mEnc.dequeueInputBuffer(0);
            inputBuffer = mEnc.getInputBuffer(inputIndex);
//            inputBuffer = encodeInputBuffers[inputIndex];
            inputBuffer.clear();
//            Log.i("TTTTT", "  pcmData.length" +   pcmData.length);
            int capacity =inputBuffer.capacity();
//            Log.i("TTTTT", "  capacity :" +   capacity);
            inputBuffer.limit(pcmData.length);

            inputBuffer.put(pcmData);//PCM数据填充给inputBuffer
            mEnc.queueInputBuffer(inputIndex, 0, pcmData.length, 0, 0);
            outputIndex = mEnc.dequeueOutputBuffer(encodeBufferInfo, 0);
            while (outputIndex >= 0) {
                outBitSize = encodeBufferInfo.size;
//                outPacketSize = outBitSize + 7;//7为ADTS头部的大小\
                outPacketSize = outBitSize;
//                outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
                outputBuffer = mEnc.getOutputBuffer(outputIndex);
//                outputBuffer.position(encodeBufferInfo.offset);
//                outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
//                outputBuffer.get(AACAudio, 0, outBitSize);
                Log.i("KeyFrame", "  encodeBufferInfo :" +   (encodeBufferInfo.size -encodeBufferInfo.offset));

                outputBuffer.position(encodeBufferInfo.offset);

                mEnc.releaseOutputBuffer(outputIndex, false);

                outputIndex = mEnc.dequeueOutputBuffer(encodeBufferInfo, 0);

            }

        }
        public void Destroy(){
            isRecording = false;
           if(mEnc!=null){
               mEnc.release();
           }
           if(audioRecord!=null){
               audioRecord.release();
           }

        }
        private void addADTStoPacket(byte[] packet, int packetLen) {
            int profile = 2; // AAC LC
            int freqIdx = 8; // 16KHz
            int chanCfg = 1; // CPE
            // fill in ADTS data
            packet[0] = (byte) 0xFF;
            packet[1] = (byte) 0xF1;
            packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
            packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
            packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
            packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
            packet[6] = (byte) 0xFC;
        }
}
