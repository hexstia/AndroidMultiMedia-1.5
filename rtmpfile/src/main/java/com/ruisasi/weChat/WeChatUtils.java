package com.ruisasi.weChat;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WeChatUtils {
    //回复文本消息
    public static void replyTextMessage(XC_LoadPackage.LoadPackageParam loadPackageParam,
                                        String strContent, final String strChatroomId) {
        XposedBridge.log("准备回复消息内容：content:" + strContent + ",chatroomId:" + strChatroomId);

        if (strContent == null || strChatroomId == null
                || strContent.length() == 0 || strChatroomId.length() == 0) {
            return;
        }

        //构造new里面的参数：l iVar = new i(aao, str, hQ, i2, mVar.cvb().fD(talkerUserName, str));
        Class<?> classiVar = XposedHelpers.findClassIfExists("com.tencent.mm.modelmulti.h", loadPackageParam.classLoader);
        Object objectiVar = XposedHelpers.newInstance(classiVar,
                new Class[]{String.class, String.class, int.class, int.class, Object.class},
                strChatroomId, strContent, 1, 1, new HashMap<String, String>() {{
                    put(strChatroomId, strChatroomId);
                }});
        Object[] objectParamiVar = new Object[]{objectiVar, 0};

        //创建静态实例对象au.DF()，转换为com.tencent.mm.ab.o对象
        Class<?> classG = XposedHelpers.findClassIfExists(" com.tencent.mm.model.au", loadPackageParam.classLoader);
        Object objectG = XposedHelpers.callStaticMethod(classG, "Dk");
//        Object objectdpP = XposedHelpers.getObjectField(objectG, "dpP");

// com.tencent.mm.ah.p DK()
        //查找au.DF().a()方法
        Class<?> classDF = XposedHelpers.findClassIfExists("com.tencent.mm.ah.p", loadPackageParam.classLoader);
        Class<?> classI = XposedHelpers.findClassIfExists("com.tencent.mm.ah.m", loadPackageParam.classLoader);
        Method methodA = XposedHelpers.findMethodExactIfExists(classDF, "a", classI, int.class);

        //调用发消息方法
        try {
            XposedBridge.invokeOriginalMethod(methodA, objectG, objectParamiVar);
            XposedBridge.log("invokeOriginalMethod()执行成功");
        } catch (Exception e) {
            XposedBridge.log("调用微信消息回复方法异常");
            XposedBridge.log(e);
        }
    }

    public static Boolean shellCommand( String command)  {

        Process process = null;

        DataOutputStream os= null;

        try {

            process = Runtime.getRuntime().exec("su");
            if(process !=null) {
                os = new DataOutputStream(process.getOutputStream());
            }
            os.writeBytes(command + "\n");

            os.writeBytes("exit\n");

            os.flush();

            process.waitFor();

        } catch (Exception e) {

           Log.i("TAG","SU :"+e.getMessage());

        } finally {

            try {

                if (os != null) {

                    os.close();

                }
                if(process!=null) {
                    process.destroy();
                }
            } catch ( Exception e) {

                Log.i("TAG","SU 1:"+e.getMessage());

            }

        }

        return true;

    }

    //获取MD5码
    public static String getMD5Str(String str){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            if(messageDigest!=null){
                messageDigest.reset();
                messageDigest.update(str.getBytes(Charset.forName("UTF-8")));
            }

        }catch (Exception e){

        }
        if(messageDigest!=null){
            byte[] byteArray =  messageDigest.digest();


            StringBuffer md5StrBuff = new StringBuffer();
            for (int i =0;i<byteArray.length;i++){
                Integer ii = new Integer(byteArray[i]);
                if(Integer.toHexString(0xff&ii).length() ==1){
                    md5StrBuff.append("0").append(Integer.toHexString(0xff&ii));
                }else
                {
                    md5StrBuff.append(Integer.toHexString(0xff&ii));
                }
            }
            return md5StrBuff.toString();
        }
        return null;
    }
    //拷贝文件类
    public static void copyFile(String oldPath, String newPath) {
        int n = 0;
        try {
            File file = new File(oldPath);
            if(file.exists()){
                Log.i("TAG","文件不存在");
            }
            FileInputStream fis = new FileInputStream(oldPath);
            FileOutputStream fos = new FileOutputStream(newPath);
            byte[] b = new byte[1024];
            int len = 0;
            while((len = fis.read(b))!=-1){
                fos.write(b,0,len);
                fos.flush();
            }
            fis.close();
            fos.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
