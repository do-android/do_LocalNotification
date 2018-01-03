package doext.module.do_LocalNotification.implement.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class BootstrapService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(this);
        // stop self to clear the notification
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void startForeground(Service context) {
        NotificationManager _notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        Notification _notification = builder.getNotification();
        _notificationManager.notify(1, _notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
