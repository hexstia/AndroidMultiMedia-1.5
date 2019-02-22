package com.ruisasi.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;

public class AudioStreamService extends Service {

    private AudioStreamSend ass;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("StartActivity","正在执行 AudioStreamService onstartcommand()");
        ass = new AudioStreamSend();
        ass.start();//音频数据
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ass.Destroy();
    }
}
