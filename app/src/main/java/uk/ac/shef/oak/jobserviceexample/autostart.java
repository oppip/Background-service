package uk.ac.shef.oak.jobserviceexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import uk.ac.shef.oak.jobserviceexample.restarter.RestartServiceBroadcastReceiver;

public class autostart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1) {
        if (arg1.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            Intent intent = new Intent(context, Service.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
                RestartServiceBroadcastReceiver.scheduleJob(context);
            } else {
                context.startService(intent);
            }
            Log.i("Autostart", "started");
        }
    }
}