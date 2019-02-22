package com.ruisasi.weChat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ruisasi.core.MainService;
import com.ruisasi.core.NotifyThread;
import com.ruisasi.core.SocketSendThread;
import com.ruisasi.core.VideoStreamSend;
import com.wangheart.rtmpfile.R;

public class ProjectActivtiy extends Activity implements RadioGroup.OnCheckedChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//               WeChatDatabase wd = new WeChatDatabase(getApplicationContext());
//               String databasePwd =  wd.getPassword();
//               wd.copyDatabase();
//               SQLiteDatabase db = wd.openDatabase(databasePwd);
//               DatabaseTools.openContactTable(db);
//            }
//        }).start();
        setContentView(R.layout.abc);
     Button start =  findViewById(R.id.btn_start);
     RadioGroup rg = findViewById(R.id.rg);
        RadioGroup rg1 = findViewById(R.id.rg1);
        RadioGroup rg2 = findViewById(R.id.rg2);
        RadioGroup rg3 = findViewById(R.id.rg3);
//        rg3.setOnCheckedChangeListener(this);
     rg.setOnCheckedChangeListener(this);
     rg3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(RadioGroup group, int checkedId) {
             switch (checkedId){
                 case R.id.rb_720:
                     Toast.makeText(ProjectActivtiy.this, "分辨率已设置为344*720", Toast.LENGTH_SHORT).show();
                     VideoStreamSend.pixel = "720";
                     break;
                 case R.id.rb_1080:
                     Toast.makeText(ProjectActivtiy.this,"分辨率已设置为520*1080", Toast.LENGTH_SHORT).show();
                     VideoStreamSend.pixel = "1080";
                     break;
             }
         }
     });
     rg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(RadioGroup group, int checkedId) {
             switch (checkedId){
                 case R.id.rb_86:
                     Toast.makeText(ProjectActivtiy.this, "端口已设置为16886", Toast.LENGTH_SHORT).show();
                    MainService.port = 16886;
                     break;
                 case R.id.rb_87:
                     Toast.makeText(ProjectActivtiy.this,"端口已设置为16887", Toast.LENGTH_SHORT).show();
                     MainService.port = 16887;
                     break;
                 case R.id.rb_88:
                     Toast.makeText(ProjectActivtiy.this, "端口已设置为16888", Toast.LENGTH_SHORT).show();
                     MainService.port = 16888;
                     break;
             }
         }
     });
        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_ip1:
                        Toast.makeText(ProjectActivtiy.this, "IP已设置为10.0.1.254", Toast.LENGTH_SHORT).show();
                        NotifyThread.IP = "10.0.1.254";
                        break;
                    case R.id.rb_ip2:
                        Toast.makeText(ProjectActivtiy.this,"IP已设置为222.222.120.169", Toast.LENGTH_SHORT).show();
                        NotifyThread.IP = "222.222.120.169";
                        break;
                    case R.id.rb_ip3:
                        Toast.makeText(ProjectActivtiy.this, "IP已设置为www.baidu.com", Toast.LENGTH_SHORT).show();
                        NotifyThread.IP = "www.baidu.com";
                        break;
                }
            }
        });

        Button end =  findViewById(R.id.btn_end);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainService.class);
                startService(i);
            }
        });
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainService.class);
                stopService(i);
            }
        });

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.rb_fb:
                Toast.makeText(this, "设备已设置为FB", Toast.LENGTH_SHORT).show();
                SocketSendThread.MAC = "i0:08:22:98:CD:FB";
                break;
            case R.id.rb_fc:
                Toast.makeText(this, "设备已设置为FC", Toast.LENGTH_SHORT).show();
                SocketSendThread.MAC = "i0:08:22:98:CD:FC";
                break;
            case R.id.rb_fd:
                Toast.makeText(this, "设备已设置为FD", Toast.LENGTH_SHORT).show();
                SocketSendThread.MAC = "i0:08:22:98:CD:FD";
                break;
        }
    }
}
