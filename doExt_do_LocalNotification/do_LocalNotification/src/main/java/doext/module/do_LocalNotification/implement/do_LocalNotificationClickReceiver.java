package doext.module.do_LocalNotification.implement;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import org.json.JSONObject;

import java.util.List;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.object.DoInvokeResult;
import core.object.DoModule;
import doext.module.do_LocalNotification.app.do_LocalNotification_App;

public class do_LocalNotificationClickReceiver extends BroadcastReceiver {
    private String content;
    private String title;
    private String extra;
    private DoModule module;
    private String notifyId;

    @Override
    public void onReceive(Context context, Intent intent) {
        content = intent.getStringExtra("contentText");
        title = intent.getStringExtra("title");
        extra = intent.getStringExtra("extra");
        notifyId = intent.getStringExtra("notifyId");

        if (intent.getAction().equals("NOTIFICATION_CLICK")) {
            try {
                JSONObject _json = new JSONObject();
                _json.put("contentTitle", title);
                _json.put("contentText", content);
                _json.put("notifyId", notifyId);
                if (extra != null) {

                    JSONObject _jsono = new JSONObject(extra);
                    _json.put("extra", _jsono);
                }

                wakeUpApp(context, _json);
                if (module == null) {
                    String _typeId = do_LocalNotification_App.getInstance().getTypeID();
                    module = DoServiceContainer.getSingletonModuleFactory().getSingletonModuleByID(null, _typeId);
                }
                DoInvokeResult _jsonResult = new DoInvokeResult(module.getUniqueKey());
                _jsonResult.setResultNode(_json);
                module.getEventCenter().fireEvent("messageClicked", _jsonResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void wakeUpApp(Context context, JSONObject json) throws NameNotFoundException {
        Intent _resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        _resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        _resolveIntent.setPackage(context.getPackageName());
        List<ResolveInfo> _apps = context.getPackageManager().queryIntentActivities(_resolveIntent, 0);
        String _notifityContent = DoJsonHelper.getText(json, "");
        if (_apps.size() != 0) {
            Intent _intent = new Intent(Intent.ACTION_MAIN);
            ResolveInfo _resolveInfo = _apps.iterator().next();
            String _packageName = _resolveInfo.activityInfo.packageName;
            String _className = _resolveInfo.activityInfo.name;
            ComponentName _cn = new ComponentName(_packageName, _className);
            _intent.addCategory(Intent.CATEGORY_LAUNCHER);
            _intent.setComponent(_cn);
            _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            _intent.putExtra("locaLNotificationData", _notifityContent);
            context.startActivity(_intent);
        }
    }

}
