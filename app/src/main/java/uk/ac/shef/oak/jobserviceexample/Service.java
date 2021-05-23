/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.oak.jobserviceexample;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import uk.ac.shef.oak.jobserviceexample.utilities.ConnectToAPI;
import uk.ac.shef.oak.jobserviceexample.utilities.Job;
import uk.ac.shef.oak.jobserviceexample.utilities.Notification;

public class Service extends android.app.Service {
    protected static final int NOTIFICATION_ID = 1337;
    private static String TAG = "Service";
    private static Service mCurrentService;
    private int counter = 0;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public Service() {
        super();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartForeground();
        }
        mCurrentService = this;
        prefs = getSharedPreferences("uk.ac.shef.oak.ServiceRunning", MODE_PRIVATE);
        editor = prefs.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "restarting Service !!");
        //counter = 0;
        counter = prefs.getInt("counter", 0);
        Log.i("Previous value of timer", String.valueOf(counter));
        // it has been killed by Android and now it is restarted. We must make sure to have reinitialised everything
        if (intent == null) {
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(this);
        }

        // make sure you call the startForeground on onStartCommand because otherwise
        // when we hide the notification on onScreen it will nto restart in Android 6 and 7
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            restartForeground();
        }

        startTimer();

        // return start sticky so if it is killed by android, it will be restarted with Intent null
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * it starts the process in foreground. Normally this is done when screen goes off
     * THIS IS REQUIRED IN ANDROID 8 :
     * "The system allows apps to call Context.startForegroundService()
     * even while the app is in the background.
     * However, the app must call that service's startForeground() method within five seconds
     * after the service is created."
     */
    public void restartForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "restarting foreground");
            try {
                Notification notification = new Notification();
                startForeground(NOTIFICATION_ID, notification.setNotification(this, "Service notification",
                        "This is the service's notification", R.drawable.ic_sleep));
                Log.i(TAG, "restarting foreground successful");
                startTimer();
            } catch (Exception e) {
                Log.e(TAG, "Error in notification " + e.getMessage());
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
        // restart the never ending service
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }


    /**
     * this is called when the process is killed by Android
     *
     * @param rootIntent
     */

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved called");
        // restart the never ending service
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        sendBroadcast(broadcastIntent);
        // do not call stoptimertask because on some phones it is called asynchronously
        // after you swipe out the app and therefore sometimes
        // it will stop the timer after it was restarted
        // stoptimertask();
    }


    /**
     * static to avoid multiple timers to be created when the service is called several times
     */
    private static Timer timer;
    private static TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        Log.i(TAG, "Starting timer");

        //set a new Timer - if one is already running, cancel it to avoid two running at the same time
        stoptimertask();
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        Log.i(TAG, "Scheduling...");
        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 600000); // Do this task every 10 minutes,
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        Log.i(TAG, "****Checking connectivity****");
        timerTask = new TimerTask() {
            public void run() {
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (!isConnected)
                {
                    Log.i(TAG, "The device is NOT connected to the interweb");
                }
                else {
                        Log.i(TAG, "Device is connected");

                   // final JSONObject result = ConnectToAPI.yourDataTask.execute(void, void, JSONObject k);
                    ConnectToAPI.Connect loader = new ConnectToAPI.Connect();
                    loader.execute();
                    JSONArray result = null;
                    try {
                        result = loader.get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    JSONObject node;
                    int count = 0, packetSize = 0, jobPeriod = 0;
                    String host = null, jobType = null, date = null;
                    for (int i=0; i < result.length(); i++) {

                        try {
                            node = result.getJSONObject(i);
                            date = node.getString("date");
                            host = node.getString("host");
                            jobType = node.getString("jobType");
                            count = node.getInt("count");
                            packetSize = node.getInt("packetSize");
                            jobPeriod = node.getInt("jobPeriod");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /*Class<?> jobClass = null;
                        Object job = null;
                        Method setNameMethod = null;
                        String jobClassName = "uk.ac.shef.oak.jobserviceexample.utilities.Job." + jobType;

                        try {
                            jobClass = Class.forName(jobClassName); // convert string classname to class
                            job = jobClass.newInstance(); // invoke empty constructor
                            setNameMethod = job.getClass().getMethod(jobType, String.class);

                            timer.schedule((TimerTask) setNameMethod.invoke(job, host, count, packetSize), 1000, jobPeriod);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/

                        timer.schedule(Job.PING(host, count, packetSize), 1000, jobPeriod*1000);

                    }

                    Log.i("LOOK AT THIS", result.toString());

                    Notification notification = new Notification();
                        startForeground(NOTIFICATION_ID, notification.setNotification(getApplicationContext(), "Device is connected",
                                "JSON: " + jobType, R.drawable.ic_launcher_background));
                    }

            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static Service getmCurrentService() {
        return mCurrentService;
    }

    public static void setmCurrentService(Service mCurrentService) {
        Service.mCurrentService = mCurrentService;
    }


}
