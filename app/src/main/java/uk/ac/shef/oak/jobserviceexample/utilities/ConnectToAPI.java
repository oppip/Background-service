package uk.ac.shef.oak.jobserviceexample.utilities;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ConnectToAPI {

    public static class Connect extends AsyncTask<Void, Void, JSONArray>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(Void... params)
        {
            String str = "";
            if (Build.FINGERPRINT.contains("generic"))
            {
                str = "http://10.0.2.2:5000/getjobs/emulator";
            }
            else
            {
                str = "http://192.168.1.138:5000/getjobs/hardware";
            }

            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(str);
                urlConn = url.openConnection();
                //if (urlConn.getR)
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONArray(stringBuffer.toString());
            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
                return null;
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(JSONArray response)
        {
            if(response != null)
            {
                Log.e("App", "Success: " + response.toString());
            }
        }
    }
}
