package doext.module.do_LocalNotification.implement.service;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


public class MyService extends Service {
    String TAG = "MyService";

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, new Notification());
        startService(new Intent(this, BootstrapService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindService(new Intent(this, MyService2.class), mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "建立连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            startService(new Intent(MyService.this, MyService2.class));
            bindService(new Intent(MyService.this, MyService2.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };
}