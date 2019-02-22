package com.genymobile.scrcpy;

import android.util.Log;

import com.ruisasi.core.CommandParser;
import com.ruisasi.core.SocketRecvThread;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ControlEventReader {

    private static final int KEYCODE_PAYLOAD_LENGTH = 9;
    private static final int MOUSE_PAYLOAD_LENGTH = 13;
    private static final int SCROLL_PAYLOAD_LENGTH = 16;
    private static final int COMMAND_PAYLOAD_LENGTH = 1;

    public static final int TEXT_MAX_LENGTH = 300;
    private static final int RAW_BUFFER_SIZE = 256;

    private final byte[] rawBuffer = new byte[RAW_BUFFER_SIZE];
    private final ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
    private final byte[] textBuffer = new byte[TEXT_MAX_LENGTH];
    public static int byte2int( byte[] bytes){

        int i0= bytes[1] & 0xFF  ;

        int i1 = (bytes[0] & 0xFF) << 8 ;

//        int i2 = (bytes[2] & 0xFF) << 16 ;
//
//        int i3 = (bytes[3] & 0xFF) << 24 ;

//        System.out.println( i0 | i1 | i2 | i3 ); //输出20180713
        return i0|i1;
    }
    public static int byte4int( byte[] bytes){

        int i0= bytes[3] & 0xFF  ;

        int i1 = (bytes[2] & 0xFF) << 8 ;

        int i2 = (bytes[1] & 0xFF) << 16 ;

        int i3 = (bytes[0] & 0xFF) << 24 ;

//        System.out.println( i0 | i1 | i2 | i3 ); //输出20180713
        return i0|i1|i2|i3;
    }
    public ControlEventReader() {
        // invariant: the buffer is always in "get" mode
        buffer.limit(0);
    }

    public boolean isFull() {
        return buffer.remaining() == rawBuffer.length;
    }
    public boolean test(byte[]  rawBuffer,int r){
        byte[] typea = new byte[1];
        byte[] actiona = new byte[1];
        byte[] buttonsa = new byte[4];
        byte[] xa = new byte[2];
        byte[] ya = new byte[2];
        byte[] wa = new byte[2];
        byte[] ha = new byte[2];
        System.arraycopy(rawBuffer,0,typea,0,1);
        System.arraycopy(rawBuffer,1,actiona,0,1);
        System.arraycopy(rawBuffer,2,buttonsa,0,4);
        System.arraycopy(rawBuffer,6,xa,0,2);
        System.arraycopy(rawBuffer,8,ya,0,2);
        System.arraycopy(rawBuffer,10,wa,0,2);
        System.arraycopy(rawBuffer,12,ha,0,2);
        Log.i("StartActivity","content"+": "+typea[0]);
        Log.i("StartActivity","content"+": "+actiona[0]);
        Log.i("StartActivity","content"+": "+byte4int(buttonsa));
        Log.i("StartActivity","content"+": "+byte2int(xa));
        Log.i("StartActivity","content"+": "+byte2int(ya));
        Log.i("StartActivity","content"+": "+byte2int(wa));
        Log.i("StartActivity","content"+": "+byte2int(ha));
        if(((byte2int(wa)==0)||(byte2int(ha)==0))&&r!=10){
            return false;
        }
        return true;
    }
    public boolean  readFrom(InputStream input) throws IOException {
        boolean flag = false;
        Log.i("StartActivity","ControlEventReader readFrom function success");
        if (isFull()) {
            throw new IllegalStateException("Buffer full, call next() to consume");
        }
        buffer.clear();
        buffer.compact();
       buffer.position(0);
       int head = buffer.position();
      //  Log.i("StartActivity"," buffer head  :" +head);
    //    Log.i("StartActivity"," buffer len  :" +(rawBuffer.length - head));
        int r = input.read(rawBuffer, head, rawBuffer.length - head);
        //解锁 让 接受线程继续读取数据
//        try {
//            Log.i("StartActivity","body  :" + CommandParser.bytesToHexFun(rawBuffer));
//        }catch (Exception e){
//
//        }
       Log.i("StartActivity","r length ="+r);
//        if(r==14||r==10){
//            flag =  true;
//        }else{
//            flag= false;
//        }
        flag = test(rawBuffer,r);

        if (r == -1) {
            throw new EOFException("Event controller socket closed");
        }
        buffer.position(head + r);
        buffer.flip();
        return true;
    }


    public ControlEvent next() {
        if (!buffer.hasRemaining()) {
            return null;
        }
        int savedPosition = buffer.position();

        int type = buffer.get();
        ControlEvent controlEvent;
        switch (type) {
            case ControlEvent.TYPE_KEYCODE:
                controlEvent = parseKeycodeControlEvent();
                break;
            case ControlEvent.TYPE_TEXT:
                controlEvent = parseTextControlEvent();
                break;
            case ControlEvent.TYPE_MOUSE:
                controlEvent = parseMouseControlEvent();
                break;
            case ControlEvent.TYPE_SCROLL:
                controlEvent = parseScrollControlEvent();
                break;
            case ControlEvent.TYPE_COMMAND:
                controlEvent = parseCommandControlEvent();
                break;
            default:
                Ln.w("Unknown event type: " + type);
                controlEvent = null;
                break;
        }

        if (controlEvent == null) {
            // failure, reset savedPosition
            buffer.position(savedPosition);
        }
        return controlEvent;
    }

    private ControlEvent parseKeycodeControlEvent() {
        if (buffer.remaining() < KEYCODE_PAYLOAD_LENGTH) {
            return null;
        }
        int action = toUnsigned(buffer.get());
        int keycode = buffer.getInt();
        int metaState = buffer.getInt();
        return ControlEvent.createKeycodeControlEvent(action, keycode, metaState);
    }

    private ControlEvent parseTextControlEvent() {
        if (buffer.remaining() < 1) {
            return null;
        }
        int len = toUnsigned(buffer.getShort());
        if (buffer.remaining() < len) {
            return null;
        }
        buffer.get(textBuffer, 0, len);
        String text = new String(textBuffer, 0, len, StandardCharsets.UTF_8);
        return ControlEvent.createTextControlEvent(text);
    }

    private ControlEvent parseMouseControlEvent() {
        if (buffer.remaining() < MOUSE_PAYLOAD_LENGTH) {
            return null;
        }
        int action = toUnsigned(buffer.get());
        int buttons = buffer.getInt();
        Position position = readPosition(buffer);
        return ControlEvent.createMotionControlEvent(action, buttons, position);
    }

    private ControlEvent parseScrollControlEvent() {
        if (buffer.remaining() < SCROLL_PAYLOAD_LENGTH) {
            return null;
        }
        Position position = readPosition(buffer);
        int hScroll = buffer.getInt();
        int vScroll = buffer.getInt();
        return ControlEvent.createScrollControlEvent(position, hScroll, vScroll);
    }

    private ControlEvent parseCommandControlEvent() {
        if (buffer.remaining() < COMMAND_PAYLOAD_LENGTH) {
            return null;
        }
        int action = toUnsigned(buffer.get());
        return ControlEvent.createCommandControlEvent(action);
    }

    private static Position readPosition(ByteBuffer buffer) {
        int x = toUnsigned(buffer.getShort());
        int y = toUnsigned(buffer.getShort());
        int screenWidth = toUnsigned(buffer.getShort());
        int screenHeight = toUnsigned(buffer.getShort());
        return new Position(x, y, screenWidth, screenHeight);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static int toUnsigned(short value) {
        return value & 0xffff;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private static int toUnsigned(byte value) {
        return value & 0xff;
    }
}
