package uk.ac.shef.oak.jobserviceexample.utilities;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TimerTask;

public class Job{

    public static TimerTask PING(final String host, final int count , final int packetSize)
    {
        return new TimerTask(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                try {
                    String pingCmd = "ping -s " + packetSize + " -c " + count + " " + host;
                    Log.i("PING THIS SITE", pingCmd);
                    String pingResult = "";
                    Runtime r = Runtime.getRuntime();
                    Process p = r.exec(pingCmd);
                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(p.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        pingResult += inputLine;
                    }
                    Log.d("Ping result", pingResult);
                    in.close();

                    try {
                        httpPOST.Post("", pingResult.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }//try
                catch (IOException e) {
                    System.out.println(e);
                }
            }
    };
    }
}
