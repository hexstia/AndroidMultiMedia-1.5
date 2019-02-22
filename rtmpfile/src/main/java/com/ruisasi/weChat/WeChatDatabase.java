package com.ruisasi.weChat;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class WeChatDatabase {
    private   Context context;
    private  String uin = "";
    public WeChatDatabase(Context context){
        this.context = context;
    }
//获取数据库密码
public  String getPassword() {

    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String deviceid = tm.getDeviceId();
//    String deviceid = "0123456789ABCDEF";//456112026798718
    //chmod -R 777 /data/data/com.tencent.mm
    //java代码
    Log.i("TAG","deviceid :"+deviceid);

    //获取uin   auth_info_key_prefs.xml
    try {
        boolean flag = WeChatUtils.shellCommand("  cp /data/data/com.tencent.mm/shared_prefs/auth_info_key_prefs.xml /sdcard/");
//        Log.i("TAG","权限添加: "+flag);
//        File file = new File("/data/data/com.tencent.mm/shared_prefs/auth_info_key_prefs.xml");
        File file = new File("/sdcard/auth_info_key_prefs.xml");
        Document doc =  Jsoup.parse(file, "UTF-8");
        Elements elements =  doc.select("int");
        Element e = elements.get(0);
        if(e.attr("name").equals("_auth_uin")){
            uin = e.attr("value");
        }else{
             e =  elements.get(1);
             uin =e.attr("value");

        }
    }catch (IOException e){

    }
    Log.i("TAG","UIN: "+uin);
    String dbPwd = WeChatUtils.getMD5Str(deviceid + uin).substring(0, 7);
    if (dbPwd != null) {
        Log.i("TAG", "数据库密码：" + dbPwd);
    } else {
        Log.i("TAG", "数据库破解密码失败：");
        return null;
    }
    return dbPwd;
}

    public void copyDatabase() {

//获取数据库 父级文件名：
        String uinEnc = WeChatUtils.getMD5Str("mm" + uin);
//
        Log.i("TAG", "uinEnc :"+uinEnc);

        String Path = "/data/user/0/com.tencent.mm/MicroMsg/" + uinEnc+"/*";

        boolean flag = WeChatUtils.shellCommand("  cp "+Path+" /sdcard/ -r");
//拷贝出来
    }
    public SQLiteDatabase openDatabase(String dbPwd){
    File file = new File("/sdcard/EnMicroMsg.db");
    Log.i("TAG","正在打开微信数据库，请稍候...");
    //打开数据库
    SQLiteDatabase.loadLibs(context.getApplicationContext());
    SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
        @Override
        public void preKey(SQLiteDatabase database) {

        }

        @Override
        public void postKey(SQLiteDatabase database) {
            database.rawExecSQL("PRAGMA cipher_migrate;");// 兼容2.0的数据库
        }
    };

    SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase(file,dbPwd,null,hook);
    if(db ==null){
        Log.i("TAG","数据库对象获取失败");
        return null;
    }
//    openContactTable(db);

        return db;
}
}
