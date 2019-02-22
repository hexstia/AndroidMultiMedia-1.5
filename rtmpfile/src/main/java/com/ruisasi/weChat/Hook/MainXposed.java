/*
 * ************************************************************
 * 文件：MainXposed.java  模块：app  项目：WeChatGenius
 * 当前修改时间：2018年08月19日 17:06:09
 * 上次修改时间：2018年08月19日 17:06:09
 * 作者：大路
 * Copyright (c) 2018
 * ************************************************************
 */

package com.ruisasi.weChat.Hook;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.ruisasi.core.CommandParser;
import com.ruisasi.weChat.WeChatUtils;
import com.ruisasi.weChat.domain.GroupMessage;
import com.ruisasi.weChat.domain.WeChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainXposed implements IXposedHookLoadPackage {
    private static int a= 0;
    //微信主进程名
    private static final String WECHAT_PROCESS_NAME = "com.tencent.mm";

    public static Object page = null ;
    public boolean readloop = true;
    //微信数据库包名称
    private static final String WECHAT_DATABASE_PACKAGE_NAME = "com.tencent.wcdb.database.SQLiteDatabase";
    public static ObjectOutputStream out = null;
    public static ObjectInputStream in = null;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.processName.equals(WECHAT_PROCESS_NAME)) {
            return;
        }
        XposedBridge.log("进入微信进程：" + lpparam.processName);
        hookDatabaseInsert(lpparam);
        HookMainActivity(lpparam);
    }

    //hook数据库插入操作
    private void hookDatabaseInsert(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_PACKAGE_NAME, loadPackageParam.classLoader);

        if (classDb == null) {
            XposedBridge.log("hook数据库insert操作：未找到类" + WECHAT_DATABASE_PACKAGE_NAME);
            return;
        }

        //功能：接受微信好友发来的消息
        XposedHelpers.findAndHookMethod(classDb,
                "insertWithOnConflict",
                String.class, String.class, ContentValues.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

//                        XposedBridge.log("进入insert method hook");
                        String tableName = (String) param.args[0];
                        ContentValues contentValues = (ContentValues) param.args[2];
                        if (tableName == null || tableName.length() == 0 || contentValues == null) {
                            return;
                        }
                        //过滤掉非聊天消息
                        if (!tableName.equals("message")) {
//                            XposedBridge.log("tablename :"+tableName);
                            return;
                        }
                        //提取消息内容
                        //1：表示是自己发送的消息
                        int isSend = contentValues.getAsInteger("isSend");
                        if(isSend == 1){
                            return ;
                        }
                        int msgId = contentValues.getAsInteger("msgId");
                        //消息内容
                        String content = contentValues.getAsString("content");
                        //说话人ID
                        String talker = contentValues.getAsString("talker");
                        //不接受群聊
                        if((talker.lastIndexOf("@chatroom"))!=-1){
                        return ;
                        }
                        //收到消息，进行回复（要判断不是自己发送的、不是群消息、不是公众号消息，才回复）
                        long createTime = contentValues.getAsLong("createTime");
                        int  type = contentValues.getAsInteger("type");
                        XposedBridge.log("isSend :"+isSend);
                        XposedBridge.log("msgId :"+msgId);
                        XposedBridge.log("content :"+content);
                        XposedBridge.log("talker :"+talker);
                        XposedBridge.log("createTime :"+createTime);
                        byte[] contentlen =  CommandParser.int2Bytes_weixin(content.getBytes("UTF-8").length);
                        int ct = (int)(createTime/1000);
                        WeChatMessage wcm = new WeChatMessage(msgId,talker,(byte)type,contentlen,content,ct);
                        if(out!=null){
                            out.writeObject(wcm);
                            out.flush();

                            XposedBridge.log("数据已发送");
                        }

                    }
                });
    }

  //功能： 截取微信主界面的加载
  private void HookMainActivity(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
      XposedBridge.log("HookMainActivity：" );
      Class<?> classDb = XposedHelpers.findClassIfExists("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader);

      if (classDb == null) {
          XposedBridge.log("com.tencent.mm.ui.LauncherUI：未找到类" );
          return;
      }
      if(a ==0) {
          new Thread(new Runnable() {
              @Override
              public void run() {
                  while (true) {
                      readloop = true;
                      LocalSocket socket = new LocalSocket();
                      try {
                          socket.connect(new LocalSocketAddress("weixin_socket"));
                          if (socket.isConnected()) {
                              out = new ObjectOutputStream(socket.getOutputStream());
                              in = new ObjectInputStream(socket.getInputStream());
                              while (readloop) {

                                  XposedBridge.log("正在读取数据中.....");
                                  Object obj =  in.readObject();
                                  XposedBridge.log("读取到数据.....");
                                  if(obj instanceof WeChatMessage){

                                      WeChatMessage wcm = (WeChatMessage)obj;
                                      if(wcm.getId() ==-1){
                                          XposedBridge.log("聊天者ID:"+wcm.getTalker());
                                          if(page!=null) {
                                              createChatRoom(page,loadPackageParam,wcm.getTalker());
                                          }else{
                                              XposedBridge.log("没有进入微信主页面");
                                          }
                                      }else{
                                          XposedBridge.log("参数传入功能..... talker :"+wcm.getTalker()+" content :"+wcm.getContent());
                                          WeChatUtils.replyTextMessage(loadPackageParam,wcm.getContent(),wcm.getTalker());
                                      }

                                  }else if(obj instanceof GroupMessage){
                                      GroupMessage gm = (GroupMessage)obj;
                                      String[] talkers = gm.getTalkers();
                                      for (String talker :talkers) {
                                          WeChatUtils.replyTextMessage(loadPackageParam,gm.getContent(),talker);
                                      }

                                  }else  if(obj instanceof  String){
                                      String str= (String)obj;
                                      if(str.equals("abc")) {
                                          readloop = false;
                                          socket.close();
                                          out.close();
                                          in.close();
                                      }
                                  }
                              }
                          } else {
                              Thread.sleep(1000);
                              continue;
                          }
                      } catch (IOException e) {
                          XposedBridge.log(" IOException  :"+e.getMessage());
                          try {
                              Thread.sleep(1000);
                          }catch (InterruptedException e1){}
                          continue;

                      }catch (InterruptedException e){
                          XposedBridge.log(" InterruptedException  :"+e.getMessage());
                          try {
                              Thread.sleep(1000);
                          }catch (InterruptedException e1){}
                          continue;
                      }catch (ClassNotFoundException e){
                          XposedBridge.log(" ClassNotFoundException  :"+e.getMessage());
                          try {
                              Thread.sleep(1000);
                          }catch (InterruptedException e1){}
                          continue;
                      }
                  }
              }
          }).start();
          a++;
      }
      XposedBridge.log("进入com.tencent.mm.ui.LauncherUI onResume：" );

      XposedHelpers.findAndHookMethod(classDb,
              "onResume",
              new XC_MethodHook() {
                  @Override
                  protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                  }

                  @Override
                  protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                      XposedBridge.log("进入com.tencent.mm.ui.LauncherUI onResume： afterHookedMethod" );
                      page =  param.thisObject;


                  }
              });


  }
//功能： 创建微信聊天界面
public static void createChatRoom(Object obj,final XC_LoadPackage.LoadPackageParam loadPackageParam,String username){
    XposedBridge.log("进入createChatRoom");
    Object context = XposedHelpers.callMethod(obj,"getApplicationContext");
    if(context == null){
        XposedBridge.log("方法调用不成功");
        return;
    }
    Class<?> ChattingUI_class =XposedHelpers.findClassIfExists("com.tencent.mm.ui.chatting.ChattingUI",loadPackageParam.classLoader);
    if (ChattingUI_class == null) {
        XposedBridge.log("com.tencent.mm.ui.chatting.ChattingUI：未找到类");
        return;
    }

    if(context instanceof Context){
        Context context1 = (Context) context;
        XposedBridge.log("类型转换");
        Intent i = new Intent(context1,ChattingUI_class);
//            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("Chat_Mode",1);
        i.putExtra("Chat_User",username);
        context1.startActivity(i);
    }
}
}


