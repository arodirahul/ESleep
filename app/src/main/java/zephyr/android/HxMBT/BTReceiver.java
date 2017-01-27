package zephyr.android.HxMBT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BTReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("Intent", intent.getAction());
    }
}
