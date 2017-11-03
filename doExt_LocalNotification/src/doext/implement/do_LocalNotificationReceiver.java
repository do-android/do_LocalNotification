package doext.implement;

import java.io.File;

import org.json.JSONObject;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.PowerManager;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.object.DoInvokeResult;
import core.object.DoModule;
import doext.app.do_LocalNotification_App;

public class do_LocalNotificationReceiver extends BroadcastReceiver {
	private String content;
	private String title;
	private String extra;
	private PackageManager pm;
	private PackageInfo info;
	private ApplicationInfo ainfo;
	private int notifyId;
	private DoModule module;
	private String ringing;
	boolean isVibrate;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null) {
			context = DoServiceContainer.getPageViewFactory().getAppContext();
		}
		pm = context.getPackageManager();
		try {
			info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ainfo = pm.getApplicationInfo(info.packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		content = intent.getStringExtra("contentText");
		title = intent.getStringExtra("title");
		notifyId = intent.getIntExtra("notifyId", -1);
		extra = intent.getStringExtra("extra");
		ringing = intent.getStringExtra("ringing");
		isVibrate = intent.getBooleanExtra("isVibrate", true);
		NotificationManager _notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification _notification = new Notification();
		_notification.icon = ainfo.icon;
		_notification.tickerText = title;
		_notification.flags = Notification.FLAG_AUTO_CANCEL;
		if (TextUtils.isEmpty(ringing)) {
			_notification.defaults = Notification.DEFAULT_SOUND; // 仅系统声音
		} else {
			// 自定义铃声
			Uri uri = Uri.fromFile(new File(ringing));
			_notification.sound = uri;
		}
		if (isVibrate) {
			_notification.vibrate = new long[] { 0, 300, 500, 700 };
		}

		fireEventMessage();

		wakeUpAndUnlock(context);
		Intent _clickIntent = new Intent(context, do_LocalNotificationClickReceiver.class); // 点击通知之后要发送的广播
		_clickIntent.setAction("NOTIFICATION_CLICK");
		_clickIntent.putExtra("title", title);
		_clickIntent.putExtra("contentText", content);
		_clickIntent.putExtra("extra", extra);
		_clickIntent.putExtra("notifyId", notifyId + "");
		PendingIntent _contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), notifyId, _clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		_notification.setLatestEventInfo(context, title, content, _contentIntent);
		_notificationManager.notify(notifyId, _notification);
	}

	private void wakeUpAndUnlock(Context context) {
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		// 解锁
		kl.disableKeyguard();
		// 获取电源管理器对象
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		// 点亮屏幕
		wl.acquire();
		// 释放
		wl.release();
	}

	private void fireEventMessage() {
		try {
			if (module == null) {
				String _typeId = do_LocalNotification_App.getInstance().getTypeID();
				module = DoServiceContainer.getSingletonModuleFactory().getSingletonModuleByID(null, _typeId);
			}

			DoInvokeResult _jsonResult = new DoInvokeResult(module.getUniqueKey());
			JSONObject _json = new JSONObject();
			_json.put("contentTitle", title);
			_json.put("contentText", content);
			_json.put("notifyId", notifyId + "");
			if (extra != null) {
				JSONObject _jsono = new JSONObject(extra);
				_json.put("extra", _jsono);
			}
			_jsonResult.setResultNode(_json);
			module.getEventCenter().fireEvent("message", _jsonResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
