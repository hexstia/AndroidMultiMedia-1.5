package com.ruisasi.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TAG","进入MyReceiver");
        Log.i("TAG","action :"+intent.getAction());
        if(intent.getAction().equals("com.ruisasi.core.destroy")){
            Intent intent1  = new Intent(context,MainService.class);
            Log.i("TAG","服务已被重新启动");
            context.startService(intent1);
        }
    }
}
