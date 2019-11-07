package network;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import tx.gov.whorepresentsme.MainPage;

import static java.net.HttpURLConnection.HTTP_OK;

public class RetrieveCensusDataTask extends AsyncTask<String, Void, String> {

    private Activity context;
    private AsyncResponse delegate;

    public interface AsyncResponse {
        void retrievalFinished(HashMap<String, Integer> output, Activity context);
    }

    public RetrieveCensusDataTask(Activity context, AsyncResponse delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    protected String doInBackground(String... urls) {
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
            JSONObject censusData = new JSONObject(content.toString());
            JSONArray results = censusData.getJSONArray("results");
            if (results != null && results.length() > 0) {
                Object blockFIPS = results.getJSONObject(0).get("block_fips");
                if (blockFIPS != null) return (String) blockFIPS;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String blockFIPS) {
        if (!blockFIPS.startsWith("48")) return;

        int SBOE, HOUSE, SENATE, CONGRESS, FIPSExists;
        long FIPS = Long.valueOf(blockFIPS);

        FIPSExists = searchFile(FIPS);
        if (FIPSExists == -1) return;

        SBOE = getDistrict(MainPage.SBOE_CSV.get(FIPSExists));
        HOUSE = getDistrict(MainPage.HOUSE_CSV.get(FIPSExists));
        SENATE = getDistrict(MainPage.SENATE_CSV.get(FIPSExists));
        CONGRESS = getDistrict(MainPage.CONGRESS_CSV.get(FIPSExists));

        HashMap<String, Integer> repDistrict = new HashMap<>();
        repDistrict.put("SBOE", SBOE);
        repDistrict.put("HOUSE", HOUSE);
        repDistrict.put("SENATE", SENATE);
        repDistrict.put("CONGRESS", CONGRESS);

        delegate.retrievalFinished(repDistrict, context);
    }

    private int searchFile(long searchValue) {
        String valueAsString = String.valueOf(searchValue);
        if (!valueAsString.startsWith("48")) return -1; // Means the address is not in Texas

        int i;
        for (i = 0; i < MainPage.SBOE_CSV.size(); i++) {
            String sctbkey = String.format("\"%s\"", valueAsString);
            if (MainPage.SBOE_CSV.get(i).startsWith(sctbkey)) break;
        }

        if (i < MainPage.SBOE_CSV.size()) return i;

        return -1;
    }

    private int getDistrict(String csvLine) {
        String[] split = csvLine.split(",");

        return Integer.valueOf(split[1]);
    }
}
