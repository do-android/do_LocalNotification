package doext.module.do_LocalNotification.implement.keeplive;

import android.content.Context;
import android.content.Intent;

public class KeepLiveManager {
    public static void startKeepLiveActivity(Context ctx) {
        Intent keepLive = new Intent(ctx, KeepLiveActivity.class);
        keepLive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(keepLive);
    }

    public static void finishKeepLiveActivity() {
        if (KeepLiveActivity.mInstance != null) {
            KeepLiveActivity.mInstance.finish();
        }
    }
}
