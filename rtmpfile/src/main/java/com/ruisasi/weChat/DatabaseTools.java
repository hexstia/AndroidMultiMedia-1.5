package com.ruisasi.weChat;

import android.util.Log;

import com.ruisasi.weChat.domain.Contact;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;

public class DatabaseTools {

//根据微信名获取nickname
public static   String  select_nickname( ArrayList<Contact> list,String name){
    for (Contact c:list) {
        if(c.getUsername().equals(name)){
            return c.getNickname();
        }
        
    }
    return "";
       
}
    // 打开联系人表
    public static   ArrayList<Contact>  openContactTable( SQLiteDatabase db){
        ArrayList<Contact> list = new ArrayList<Contact>();
        // type = 1 自己， type=2 群    type =33 微信功能  type =4 非好友
        Cursor cursor =  db.rawQuery("select * from rcontact where "+
                "type != ? and " +

                "type != ? and " +

                "type != ? and " +

                "type != ? and " +

                "verifyFlag = ? and " +

                "username not like 'gh_%' ",new String[]{"1","33","4","0","0"});
//        and  username not like '%@chatroom'
        if(cursor.getCount() >0){
            while(cursor.moveToNext()){
                String username = cursor.getString(cursor.getColumnIndex("username"));
                String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String conRemark  = cursor.getString(cursor.getColumnIndex("conRemark"));
                String alias  = cursor.getString(cursor.getColumnIndex("alias"));
                String chatroomFlag  = cursor.getString(cursor.getColumnIndex("chatroomFlag"));
                Contact c = new Contact(username,nickname,type,alias,conRemark,chatroomFlag);
                Log.i("TAG","username"+username+" nickname"+nickname+" type :"+type+"alias"+alias);
                list.add(c);
            }
        }
        cursor.close();
        return list;
    }
}
