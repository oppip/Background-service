package uk.ac.shef.oak.jobserviceexample.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import uk.ac.shef.oak.jobserviceexample.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class httpPOST {

    static Context context = MyApplication.getContext();
    public static SharedPreferences prefs = context.getSharedPreferences("uk.ac.shef.oak.ServiceRunning", MODE_PRIVATE);
    public static SharedPreferences.Editor editor = prefs.edit();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Post(String url, String sendThis) throws IOException, JSONException {

        String str = "", jsonstr;



        if (Build.FINGERPRINT.contains("generic")) {
            str = "http://10.0.2.2:5000/postresults";
        } else {
            str = "http://192.168.1.138:5000/postresults";
        }
        URL postURL = new URL(str);
        HttpURLConnection con = (HttpURLConnection) postURL.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "text/plain; charset=utf-8");
        con.setDoOutput(true);

        JSONObject obj = new JSONObject();

        String[] saved = prefs.getString("result", "").split(";");
        if (saved.length >=3)
        {
            saved[0] = saved[1];
            saved[1] = saved[2];
            saved[2] = sendThis;
            editor.putString("result", String.join(";", saved));
            editor.apply();
        }
        else {
            if (saved.length > 0 ) {
                editor.putString("result", prefs.getString("result", "") + ";" + sendThis);
                editor.apply();
            }
        }

        jsonstr = prefs.getString("result", sendThis);
        obj.put("result", jsonstr);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = obj.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
                jsonstr = response.toString();
            }
            Log.i("RESPONSE FROM SERVER", response.toString());
        }
        if (jsonstr == "")
        {
            Log.i("Server", "The server is offline");
        }
        else
        {
            Log.i("ELSE", "AAAAAAAAAA");
            editor.remove("result");
            editor.commit();
        }
    }
}
