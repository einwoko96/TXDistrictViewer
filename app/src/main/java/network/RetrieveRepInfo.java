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

public class RetrieveRepInfo extends AsyncTask<String, Void, JSONObject> {

    private Bitmap photoBmp;
    private Activity context;
    private AsyncResponse delegate;

    public interface AsyncResponse {
        void retrievalFinished(HashMap<String, Object> output, Activity context);
    }

    public RetrieveRepInfo(Activity context, AsyncResponse delegate) {
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
            JSONArray infoArray = infoObject.getJSONArray("officials");
            JSONObject repInfo = infoArray.getJSONObject(0);

            photoBmp = getBitmapStream(getStringValue(repInfo, "photoUrl"));
            return repInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject repInfo) {
        if (repInfo == null) return;

        String name, party, email, phoneNumber, webpageUrl;

        name = getStringValue(repInfo, "name");
        party = getStringValue(repInfo, "party");
        email = getListValue(repInfo, "emails");
        phoneNumber = getListValue(repInfo, "phones");
        webpageUrl = getListValue(repInfo, "urls");

        HashMap<String, Object> info = new HashMap<>();
        info.put("photoBmp", photoBmp);
        info.put("name", name);
        info.put("party", party);
        info.put("email", email);
        info.put("phoneNumber", phoneNumber);
        info.put("webpageUrl", webpageUrl);

        delegate.retrievalFinished(info, context);
    }

    private String getStringValue(JSONObject repInfo, String key) {
        String value = null;
        try {
            value = repInfo.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return value;
    }

    private String getListValue(JSONObject repInfo, String key) {
        JSONArray value;
        try {
            value = (JSONArray) repInfo.get(key);
            if (value != null && value.length() > 0) {
                return (String) value.get(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap getBitmapStream(String photoUrl) {
        if (photoUrl != null) {
            URL url = null;
            try {
                url = new URL(photoUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap bmp = null;
            try {
                assert url != null;
                HttpURLConnection con = null;
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int status = con.getResponseCode();

                if (status != HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER) {
                        String newUrl = con.getHeaderField("Location");
                        // open the new connnection again
                        con = (HttpURLConnection) new URL(newUrl).openConnection();
                    }
                }

                InputStream inputStream = con.getInputStream();
                bmp = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmp;
        }

        return null;
    }
}
