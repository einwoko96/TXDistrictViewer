package tx.gov.whorepresentsme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import network.RetrieveCensusDataTask;
import network.RetrieveRepInfo;

public class ResultOfQuery extends AppCompatActivity {

    private static String FCC_URL = "https://geo.fcc.gov/api/census/area?";

    public RetrieveRepInfo repInfo = null;
    public RetrieveCensusDataTask asyncCensusTask = null;
    AtomicInteger cnt = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_of_query);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        double longitude = 0, latitude = 0;
        longitude = intent.getDoubleExtra(Constants.LONGITUDE, longitude);
        latitude = intent.getDoubleExtra(Constants.LATITUDE, latitude);

        String queryURL = constructQueryURL(latitude, longitude);

        asyncCensusTask = new RetrieveCensusDataTask(this,
                new RetrieveCensusDataTask.AsyncResponse() {

                    @Override
                    public void retrievalFinished(HashMap<String, Integer> repDistrict, Activity context) {
                        //TODO: Start a service to display results
                        getRepInfo("cd", repDistrict.get("CONGRESS"), context);
                        getRepInfo("sldu", repDistrict.get("SENATE"), context);
                        getRepInfo("sldl", repDistrict.get("HOUSE"), context);
                    }
                });

        asyncCensusTask.execute(queryURL);
    }

    protected String constructQueryURL(double latitude, double longitude) {
        String queryURL = FCC_URL;
        queryURL = String.format("%slat=%s", queryURL, latitude);
        queryURL = String.format("%s&lon=%s", queryURL, longitude);
        queryURL = String.format("%s&format=json", queryURL);

        return queryURL;
    }

    protected void getRepInfo(String districtType, Integer districtNum, Activity context) {
        repInfo = new RetrieveRepInfo(context,
                new RetrieveRepInfo.AsyncResponse() {

                    @Override
                    public void retrievalFinished(HashMap<String, Object> repInfo, Activity context) {
                        //TODO: Start a service to display results
                        LinearLayout linearLayout = context.findViewById(R.id.linearLayout);

                        Bitmap photoBmp = (Bitmap) repInfo.get("photoBmp");
                        if (photoBmp != null) {
                            ImageView imageView = new ImageView(context);
                            imageView.setImageBitmap(photoBmp);
                            imageView.setId(cnt.addAndGet(1));
                            linearLayout.addView(imageView);
                        }

                        String name = (String) repInfo.get("name");
                        String party = (String) repInfo.get("party");
                        if (name != null) {
                            TextView textView = new TextView(context);
                            textView.setId(cnt.addAndGet(1));
                            if (party != null) {
                                String partyLetter = String.valueOf(party.charAt(0));
                                name = String.format("%s (%s)", name, partyLetter);
                            }
                            textView.setText(name);
                            textView.setGravity(Gravity.CENTER);
                            linearLayout.addView(textView);
                        }
                        String email = (String) repInfo.get("email");
                        if (email != null) {
                            TextView textView = new TextView(context);
                            textView.setText(email);
                            textView.setId(cnt.addAndGet(1));
                            textView.setGravity(Gravity.CENTER);
                            linearLayout.addView(textView);
                        }
                        String phone = (String) repInfo.get("phoneNumber");
                        if (phone != null) {
                            TextView textView = new TextView(context);
                            textView.setText(phone);
                            textView.setId(cnt.addAndGet(1));
                            textView.setGravity(Gravity.CENTER);
                            linearLayout.addView(textView);
                        }
                        String webpageUrl = (String) repInfo.get("webpageUrl");
                        if (webpageUrl != null) {
                            TextView textView = new TextView(context);
                            textView.setText(webpageUrl);
                            textView.setId(cnt.addAndGet(1));
//                            textView.setClickable(true);
//                            textView.setMovementMethod(LinkMovementMethod.getInstance());
//                            textView.setText(Html.fromHtml(webpageUrl, Html.FROM_HTML_MODE_COMPACT));
                            textView.setGravity(Gravity.CENTER);
                            linearLayout.addView(textView);
                        }

                        TextView textView = new TextView(context);
                        textView.setText("");
                        textView.setId(cnt.addAndGet(1));
                        textView.setGravity(Gravity.CENTER);
                        linearLayout.addView(textView);
                    }
                });

        repInfo.execute(Constants.OCDID_URL + districtType + "%3A" + districtNum + "?key=AIzaSyAiuDMopFjHXsR3i9B0m6iw_ACYJa9QKxs");
    }
}
