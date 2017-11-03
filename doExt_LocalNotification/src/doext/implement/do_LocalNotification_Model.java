package doext.implement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_LocalNotification_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现LocalNotification_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_LocalNotification_Model extends DoSingletonModule implements do_LocalNotification_IMethod {
	private Activity mActivity;
	private AlarmManager alarmManager = null;
	private SharedPreferences sp;
	Map<Integer, PendingIntent> pendingIntents = new HashMap<Integer, PendingIntent>();

	public do_LocalNotification_Model() throws Exception {
		super();
		mActivity = DoServiceContainer.getPageViewFactory().getAppContext();
		if (alarmManager == null) {
			alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
		}
		sp = mActivity.getSharedPreferences("do_LocalNotification_NotifyID", Context.MODE_PRIVATE);
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("addNotify".equals(_methodName)) {
			addNotify(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("removeNotify".equals(_methodName)) {
			removeNotify(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 添加本地通知；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void addNotify(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		try {
			int _notifyId = DoJsonHelper.getInt(_dictParas, "notifyId", -1);
			String _notifyTime = DoJsonHelper.getString(_dictParas, "notifyTime", "");
			String _repeatMode = DoJsonHelper.getString(_dictParas, "repeatMode", "None");// 非必填
			String _contentTitle = DoJsonHelper.getString(_dictParas, "contentTitle", "");// 非必填
			String _contentText = DoJsonHelper.getString(_dictParas, "contentText", "");
			String _ringing = DoJsonHelper.getString(_dictParas, "ringing", "");
			boolean _isVibrate = DoJsonHelper.getBoolean(_dictParas, "isVibrate", true);

			if (!TextUtils.isEmpty(_ringing)) {
				_ringing = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _ringing);
			}
			JSONObject _extra = DoJsonHelper.getJSONObject(_dictParas, "extra");// 非必填

			SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			Date _notifyTimeDate = _dateFormat.parse(_notifyTime);

			if (TextUtils.isEmpty(_repeatMode) || _repeatMode.equals("None")) {// 为空并且格式为yyyy-MM-dd HH:mm:ss就执行一次
				sendLocalNotification(_isVibrate, _ringing, _notifyTimeDate, _contentTitle, _contentText, _notifyId, _extra, -1);
			} else if (_repeatMode.equals("Day")) {
				sendLocalNotification(_isVibrate, _ringing, _notifyTimeDate, _contentTitle, _contentText, _notifyId, _extra, AlarmManager.INTERVAL_DAY);
			} else if (_repeatMode.equals("Week")) {
				sendLocalNotification(_isVibrate, _ringing, _notifyTimeDate, _contentTitle, _contentText, _notifyId, _extra, (AlarmManager.INTERVAL_DAY) * 7);
			} else if (_repeatMode.equals("Minute")) {
				sendLocalNotification(_isVibrate, _ringing, _notifyTimeDate, _contentTitle, _contentText, _notifyId, _extra, 60000);
			} else if (_repeatMode.equals("Hour")) {
				sendLocalNotification(_isVibrate, _ringing, _notifyTimeDate, _contentTitle, _contentText, _notifyId, _extra, AlarmManager.INTERVAL_HOUR);
			}
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("do_LocalNotification_Model", e);
		}

	}

	private void savaNotifyID(String notifyID, int requestCode) {
		Editor mEditor = sp.edit();
		mEditor.putInt(notifyID, requestCode);
		mEditor.commit();
	}

	private void sendLocalNotification(boolean isVibrate, String ringing, Date notifyTimeDate, String title, String content, int notifyId, JSONObject extra, long time) {
		Intent intent = new Intent(mActivity, do_LocalNotificationReceiver.class);
		intent.putExtra("title", title);
		intent.putExtra("contentText", content);
		intent.putExtra("notifyId", notifyId);
		intent.putExtra("ringing", ringing);
		intent.putExtra("isVibrate", isVibrate);
		if (extra != null) {
			intent.putExtra("extra", extra.toString());
		}

		PendingIntent sender = PendingIntent.getBroadcast(mActivity, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		savaNotifyID(notifyId + "", notifyId);
		pendingIntents.put(notifyId, sender);

		if (time == -1) {
			alarmManager.set(AlarmManager.RTC, notifyTimeDate.getTime(), sender);
		} else {
			alarmManager.setRepeating(AlarmManager.RTC, notifyTimeDate.getTime(), time, sender);
		}
	}

	/**
	 * 移除通知消息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void removeNotify(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		JSONArray _jsonArray = DoJsonHelper.getJSONArray(_dictParas, "notifyIds");
		removeNotify(_jsonArray);

	}

	private void removeNotify(JSONArray notifyIDs) throws JSONException {
		// / 先通过notifyID 从 NotificationManager 移除 Notification
		// / 再通过notifyID 从 AlarmManager 移除 这个任务
		NotificationManager _notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyIDs != null && notifyIDs.length() > 0) {
			for (int i = 0; i < notifyIDs.length(); i++) {
				int _notifyID = notifyIDs.getInt(i);
				removeNotifyByID(_notificationManager, _notifyID);
			}
		} else {
			// 1.清除内存里保存的通知
			Set<Integer> _keys = pendingIntents.keySet();
			for (Integer _key : _keys) {
				if (pendingIntents.containsKey(_key)) {
					_notificationManager.cancel(_key);
					PendingIntent _pendingIntent = pendingIntents.get(_key);
					// 只有设置相同的Intent才会取消该闹钟
					alarmManager.cancel(_pendingIntent);
				}
			}
			pendingIntents.clear();
			// 2.清除sp里保存的通知
			Set<String> _notifyIDs = sp.getAll().keySet();
			for (String _notifyID : _notifyIDs) {
				removeNotifyByID(_notificationManager, Integer.parseInt(_notifyID));
			}

		}
	}

	private void removeNotifyByID(NotificationManager _notificationManager, int _notifyID) {
		_notificationManager.cancel(_notifyID);
		int _requestCode = sp.getInt(_notifyID + "", -1);
		if (_requestCode >= 0) {
			Intent intent = new Intent(mActivity, do_LocalNotificationReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity, _requestCode, intent, 0);
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
		sp.edit().remove(_notifyID + "");
	}

}