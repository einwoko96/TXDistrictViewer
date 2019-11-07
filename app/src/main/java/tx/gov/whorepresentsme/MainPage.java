package tx.gov.whorepresentsme;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import file_io.CSVFile;
import network.RetrieveLatLng;

public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Intent displayResults;
    public RetrieveLatLng asyncLocTask = null;
    private AddressResultReceiver mResultReceiver;

    public static final String DEST_URL = "DEST_URL";
    public static final String REP_RESULTS = "REP_RESULTS";
    public static final int REQUEST_CODE = 1;
    private static String districtType = "ALL";

    public static ArrayList<String> SBOE_CSV = new ArrayList<>();
    public static ArrayList<String> HOUSE_CSV = new ArrayList<>();
    public static ArrayList<String> SENATE_CSV = new ArrayList<>();
    public static ArrayList<String> CONGRESS_CSV = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

        final Button location_button = (Button) findViewById(R.id.location_button);
        location_button.setOnClickListener(this);

        // INSTANTIATE SPINNER VALUES AND LISTENERS
        Spinner spinner = (Spinner) findViewById(R.id.district_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.district_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner = (Spinner) findViewById(R.id.district_type);
        spinner.setOnItemSelectedListener(this);

        CSVFile sboeTask = new CSVFile(getResources(), SBOE_CSV);
        sboeTask.execute(R.raw.state_boe);

        CSVFile houseTask = new CSVFile(getResources(), HOUSE_CSV);
        houseTask.execute(R.raw.state_house);

        CSVFile senateTask = new CSVFile(getResources(), SENATE_CSV);
        senateTask.execute(R.raw.state_senate);

        CSVFile congressTask = new CSVFile(getResources(), CONGRESS_CSV);
        congressTask.execute(R.raw.texas_congress_del);
    }

    @Override
    public void onClick(View v) {
        int view_id = v.getId();

        if (view_id == R.id.button) {
            if (!sanitizeInputs()) {
                Snackbar.make(v, "Please enter in all required information correctly",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                String streetAddress = retrieveStreetAddress();
                String city = retrieveCity();
                String zipCode = retrieveZipCode();

                String constructedURL = constructURL(streetAddress, city, zipCode);
                asyncLocTask = new RetrieveLatLng(this,
                        new RetrieveLatLng.AsyncResponse() {
                            @Override
                            public void retrievalFinished(HashMap<String, Double> output, Activity context) {
                                displayResults = new Intent(context, ResultOfQuery.class);
                                displayResults.putExtra(Constants.LONGITUDE, output.get("long"));
                                displayResults.putExtra(Constants.LATITUDE, output.get("lat"));
                                startActivity(displayResults);
                            }
                        });
                asyncLocTask.execute(constructedURL);
            }

        } else if (view_id == R.id.location_button) {
            Intent intent = new Intent(this, MapsDetect.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public boolean sanitizeInputs() {
        TextView userStreetAddress = (TextView) findViewById(R.id.street_address);
        String streetAddress = userStreetAddress.getText().toString().trim();
        if (streetAddress.equals("")) {
            return false;
        }

        TextView userCity = (TextView) findViewById(R.id.city);
        String city = userCity.getText().toString().trim();
        if (city.equals("")) {
            return false;
        }

        TextView userZipCode = (TextView) findViewById(R.id.zip_code);
        String zipCode = userZipCode.getText().toString().trim();

        return zipCode.length() == 5 || zipCode.length() == 10;
    }

    public String retrieveStreetAddress() {
        TextView userStreetAddress = (TextView) findViewById(R.id.street_address);
        String streetAddress = userStreetAddress.getText().toString().trim();

        return streetAddress;
    }

    public String retrieveCity() {
        TextView userCity = (TextView) findViewById(R.id.city);
        String city = userCity.getText().toString().trim();

        return city;
    }

    public String retrieveZipCode() {
        TextView userZipCode = (TextView) findViewById(R.id.zip_code);
        String zipCode = userZipCode.getText().toString().trim();

        return zipCode;
    }

    public String constructURL(String streetAddress, String city, String zipCode) {
        String constructedURL = "https://maps.googleapis.com/maps/api/geocode/json?address=";

        streetAddress = streetAddress.replace(" ", "+");
        constructedURL = constructedURL.concat(streetAddress);
        constructedURL = constructedURL.concat(",");

        city = city.replace(" ", "+");
        constructedURL = constructedURL.concat(city);

        constructedURL = constructedURL.concat("&key=AIzaSyAiuDMopFjHXsR3i9B0m6iw_ACYJa9QKxs");

        return constructedURL;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Location lastKnownLocation = data.getParcelableExtra(Constants.KEY_LOCATION);
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                displayResults = new Intent(this, ResultOfQuery.class);
                displayResults.putExtra(Constants.LONGITUDE, longitude);
                displayResults.putExtra(Constants.LATITUDE, latitude);
                startActivity(displayResults);

                // Retrieves street address; currently not needed
                /* startIntentService(lastKnownLocation); */
            }

            else if (resultCode == Activity.RESULT_CANCELED) {
                View v = findViewById(R.id.main_page_view);
                Snackbar.make(v, "Google Maps could not find your location. Please try again!",
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                districtType = "ALL";
                break;

            case 1:
                districtType = "SENATE";
                break;

            case 2:
                districtType = "HOUSE";
                break;

            case 3:
                districtType = "CONGRESS";
                break;

            case 4:
                districtType = "SBOE";
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Another interface callback
    }

    protected void startIntentService(Location lastKnownLocation) {
        mResultReceiver = new AddressResultReceiver(new Handler());

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastKnownLocation);
        startService(intent);

        displayResults = new Intent(this, ResultOfQuery.class);
    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            if (resultCode == Constants.SUCCESS_RESULT) {
                String location = resultData.getString(Constants.RESULT_DATA_KEY);
                if (location != null) {
                    String address = location.split(System.getProperty("line.separator"))[0];
                    String[] addressFragments = address.split(",");

                    String streetAddress = addressFragments[0];
                    String city = addressFragments[1];
                    String zipCode = addressFragments[2];

                    String constructedURL = constructURL(streetAddress, city, zipCode);

                    displayResults.putExtra(DEST_URL, constructedURL);
                    startActivity(displayResults);
                }
            } else {
                //TODO: Say Google could not find location
            }
        }
    }
}
