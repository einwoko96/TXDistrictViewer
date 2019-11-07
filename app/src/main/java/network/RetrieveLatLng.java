package network;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import static java.net.HttpURLConnection.HTTP_OK;

public class RetrieveLatLng extends AsyncTask<String, Void, JSONObject> {

    private Activity context;
    private AsyncResponse delegate;

    public interface AsyncResponse {
        void retrievalFinished(HashMap<String, Double> output, Activity context);
    }

    public RetrieveLatLng(Activity context, AsyncResponse delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    protected JSONObject doInBackground(String... urls) {
        String queryURL = urls[0];

        URL url = null;
        try {
            url = new URL(queryURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) return null;

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (con == null) return null;

        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        StringBuilder content = null;
        try {
            int status = con.getResponseCode();
            if (status != HTTP_OK)
                return null;

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (content == null) return null;

        try {
            JSONObject infoObject = new JSONObject(content.toString());
            JSONArray infoArray = infoObject.getJSONArray("results");
            JSONObject repInfo = infoArray.getJSONObject(0);
            JSONObject latLng = repInfo.getJSONObject("geometry").getJSONObject("location");

            return latLng;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject latLng) {
        if (latLng == null) return;

        HashMap<String, Double> info = new HashMap<>();
        try {
            info.put("lat", latLng.getDouble("lat"));
            info.put("long", latLng.getDouble("lng"));

            delegate.retrievalFinished(info, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
