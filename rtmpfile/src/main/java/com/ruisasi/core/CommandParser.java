package com.ruisasi.core;

public class CommandParser {


    public static int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }


    public static String bytesToHexFun(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return buf.toString();
    }

    public static byte[] int4Bytes_weixin(int i ){

        byte[] arr = new byte[4] ;

        arr[3] = (byte)i ;         //通过debug可以看到arr[0] = -23,也就是10010111

        arr[2] = (byte)(i >> 8) ;  //通过debug可以看到arr[1] = -18,也就是10010010

        arr[1] = (byte)(i >> 16) ; //通过debug可以看到arr[2] = 51, 也就是00110011
//
        arr[0] = (byte)(i >> 24) ; //通过debug可以看到arr[3] = 1,  也就是00000001
        return arr;
    }
    public static byte[] int4Bytes(int i ){

        byte[] arr = new byte[4] ;

        arr[0] = (byte)i ;         //通过debug可以看到arr[0] = -23,也就是10010111

        arr[1] = (byte)(i >> 8) ;  //通过debug可以看到arr[1] = -18,也就是10010010

        arr[2] = (byte)(i >> 16) ; //通过debug可以看到arr[2] = 51, 也就是00110011
//
       arr[3] = (byte)(i >> 24) ; //通过debug可以看到arr[3] = 1,  也就是00000001
        return arr;
    }
    public static byte[] int2Bytes_weixin(int i ){

        byte[] arr = new byte[2] ;

        arr[1] = (byte)i ;         //通过debug可以看到arr[0] = -23,也就是10010111

        arr[0] = (byte)(i >> 8) ;  //通过debug可以看到arr[1] = -18,也就是10010010


        return arr;
    }
    public static byte[] int2Bytes(int i ){

        byte[] arr = new byte[2] ;

        arr[0] = (byte)i ;         //通过debug可以看到arr[0] = -23,也就是10010111

        arr[1] = (byte)(i >> 8) ;  //通过debug可以看到arr[1] = -18,也就是10010010

//        arr[2] = (byte)(i >> 16) ; //通过debug可以看到arr[2] = 51, 也就是00110011
//
//        arr[3] = (byte)(i >> 24) ; //通过debug可以看到arr[3] = 1,  也就是00000001
        return arr;
    }
    public static int byte2int_weixin( byte[] bytes){

        int i0= bytes[1] & 0xFF  ;

        int i1 = (bytes[0] & 0xFF) << 8 ;

//        int i2 = (bytes[2] & 0xFF) << 16 ;
//
//        int i3 = (bytes[3] & 0xFF) << 24 ;

//        System.out.println( i0 | i1 | i2 | i3 ); //输出20180713
        return i1|i0;
    }
    public static int byte2int( byte[] bytes){

        int i0= bytes[0] & 0xFF  ;

        int i1 = (bytes[1] & 0xFF) << 8 ;

//        int i2 = (bytes[2] & 0xFF) << 16 ;
//
//        int i3 = (bytes[3] & 0xFF) << 24 ;

//        System.out.println( i0 | i1 | i2 | i3 ); //输出20180713
        return i0|i1;
    }

    public static byte[] SetcombinLen(byte b[],int len){
            int combinlen = 16+len;
            if(combinlen <=255){
                b[7] = (byte)combinlen;
            }else{
                byte[] bs =int2Bytes(combinlen);
                b[6] = bs[1];
                b[7] = bs[0];
            }
    return b;
    }
    public static byte[] HeartPack(String IP, String MAC) {
        byte[] res = new byte[16];
        String[] tmp = IP.split("\\.");
        int i =12;
        for (String t : tmp) {
                int s = Integer.parseInt(t, 10);
                byte as = (byte)s;
                res[i] = as;
                int m = res[i]&0xff;
               // Log.i(StartActivity.TAG, "IP "+i+" :  "+ m);
           i++;
        }
        //=============================================================
        String[] tmp1 = MAC.split(":");
        byte[] bs = new byte[12];
        String str = new String();
        for (String  t: tmp1) {
            str+=t;
        }
        bs = str.getBytes();
        for(i =0;i<12;i++){
            res[i] = bs[i];
        }
return res;
    }

    public static byte[] Videopacket(boolean Switch) {
        byte[] res = new byte[1];
        if(Switch){
            res[0] = 1;
        }else{
            res[0] = 0;
        }

        return res;
    }
    public static byte[] group_packet_re(int code ,String taskid)throws Exception {
            byte bcode = (byte)code;
            byte len = (byte)taskid.getBytes("UTF-8").length;
            byte[] content = new byte[2+taskid.getBytes("UTF-8").length];
            content[0] = bcode;
            content[1] = len;
            System.arraycopy(taskid.getBytes("UTF-8"),0,content,2,len);


        return content;
    }
}
