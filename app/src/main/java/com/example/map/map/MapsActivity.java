package com.example.map.map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private GoogleMap mMap;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    public static Activity mContext;

    private LatLng position;
    private Marker mMarker;

    public double turkLat;
    public double turkLon;

    private String username;
    private String usertype;

    GoogleApiClient mGoogleApiClient;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private MenuItem selected;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    public static ProgressBar progressBar;

    public static ProgressBar getProgressBar(){
        return progressBar;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.buildGoogleApiClient();
        mGoogleApiClient.connect();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

      //  progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {

                } else {

                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, GCMIntentService.class);
            startService(intent);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mTitle = mDrawerTitle = getTitle();

        mContext = this;
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = sharedPreferences.getString("username", "");
        usertype = sharedPreferences.getString("usertype", "");


        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
// Notification
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
// SignIn
        if(username.equals(""))
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
// SignOut
        if(!username.equals("")) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        }



        // Recycle the typed array
        navMenuIcons.recycle();

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        ((AppCompatActivity) mContext).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) mContext).getSupportActionBar().setHomeButtonEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_media_pause, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                ((AppCompatActivity) mContext).getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                ((AppCompatActivity) mContext).getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        //mMap = mapFragment.getMapAsync(this);
        registerForContextMenu(findViewById(R.id.map));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng p) {
                System.out.println("Map Long Click    " + p.latitude + "  " + p.longitude);

               // mMarker = mMap.addMarker(new MarkerOptions().position(position).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mMarker.setPosition(p);
                openContextMenu(findViewById(R.id.map));
                position = p;


               /* ConnectionPost con = new ConnectionPost(position);
                con.execute();
                ConnectionGet con = new ConnectionGet();
                con.execute(); */
            }
        });

        Button btn_find = (Button) findViewById(R.id.btn_find);
        OnClickListener findClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting reference to EditText to get the user input location
                EditText etLocation = (EditText) findViewById(R.id.et_location);
                System.out.println("Inside onclick ...guess map click");
                // Getting user input location
                String location = etLocation.getText().toString();

                if(location!=null && !location.equals("")){
                    new GeocoderTask().execute(location);
                }
            }
        };

        // Setting button click event listener for the find button
        btn_find.setOnClickListener(findClickListener);
        mapFragment.getMapAsync(this);

        if(getIntent().getBundleExtra("usertype") != null) {
            usertype = getIntent().getBundleExtra("usertype").toString();
            username = getIntent().getBundleExtra("username").toString();
        }
    }


    @Override
    public void onContextMenuClosed (Menu menu){
        if(selected != null && selected.getItemId() == 1) {
            try {
                //ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
                //ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
               // progressBar.setVisibility(View.VISIBLE);
                Connection con = new Connection();
                Connection.ConnectionGet getWeather = con.new ConnectionGet(position.latitude, position.longitude);
                getWeather.execute(mMap);
                //String weather = (String) getWeather.execute().get();
//                System.out.println("onContextItemSelected " + weather);
//               // progressBar.setVisibility(View.INVISIBLE);
//                MarkerOptions marker = new MarkerOptions().position(new LatLng(position.latitude, position.longitude)).title(weather);
//                weather = weather.toLowerCase().replace(' ', '_');
//                marker.icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier(weather, "drawable", "com.example.map.map")));
//                mMap.addMarker(marker);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void reportWeather(MenuItem item){

        Connection con = new Connection();
        Connection.ConnectionPostWeatherToStats postWeather;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        // Add a marker in Sydney and move the camera
        LatLng sydney ;
     //   boolean no = sharedPreferences.getBoolean("turknotification", false);
       // if(no){
            Double lat = Double.parseDouble(sharedPreferences.getString("turklat", "0"));
            Double lon = Double.parseDouble(sharedPreferences.getString("turklng", "0"));
            sydney = new LatLng(lat,lon);
       // }

        mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .icon(BitmapDescriptorFactory.
                                fromResource(getResources().getIdentifier((String)item.getTitle(), "drawable", "com.example.map.map"))));
                postWeather = con.new ConnectionPostWeatherToStats(sydney, (String)item.getTitle());
                postWeather.execute();
//        switch (item.getItemId()){
//            case R.id.rain:
//                mMap.addMarker(new MarkerOptions()
//                        .position(sydney)
//                        .icon(BitmapDescriptorFactory.
//                                fromResource(R.drawable.rain)));
//                postWeather = con.new ConnectionPostWeatherToStats(sydney, "rain");
//                postWeather.execute();
//                break;
//            case R.id.no_rain:
//                mMap.addMarker(new MarkerOptions()
//                        .position(sydney)
//                        .icon(BitmapDescriptorFactory.
//                                fromResource(R.drawable.no_rain)));
//                postWeather = con.new ConnectionPostWeatherToStats(sydney, "no rain");
//                postWeather.execute();
//                break;
//            case R.id.snow:
//                mMap.addMarker(new MarkerOptions()
//                        .position(sydney)
//                        .icon(BitmapDescriptorFactory.
//                                fromResource(R.drawable.snow)));
//                postWeather = con.new ConnectionPostWeatherToStats(sydney, "snow");
//                postWeather.execute();
//                break;
//            case R.id.no_snow:
//                mMap.addMarker(new MarkerOptions()
//                        .position(sydney)
//                        .icon(BitmapDescriptorFactory.
//                                fromResource(R.drawable.no_snow)));
//                postWeather = con.new ConnectionPostWeatherToStats(sydney, "no snow");
//                postWeather.execute();
//                break;
//            default:
//                break;
//        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu,v,menuInfo);
        menu.add(Menu.NONE, 1, 0, "Request Weather Info");
        menu.add(Menu.NONE, 2, 1, "Get Stats");

        System.out.print("onCreateContextMenu");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
    /*    switch (item.getItemId()) {
            case R.string.rain:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
        reportWeather(item);
        return true;
    }


    public boolean onContextItemSelected(MenuItem item) {
        selected = item;
        try{
            switch (item.getItemId()) {
                case 1:     //get weather info

                    return true;

                case 2:     //request stats
                    Intent i = new Intent(MapsActivity.this, Graph.class);
                    startActivity(i);
                    finish();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return super.onContextItemSelected(item);
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
      //  menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ((AppCompatActivity) mContext).getSupportActionBar().setTitle(mTitle);
        ((AppCompatActivity) mContext).getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    public void method(){
        try {

            URL url = new URL("http://10.0.2.2:3000/turks/get");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }


    }
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        // Add a marker in Sydney and move the camera
        LatLng sydney ;
        boolean no = sharedPreferences.getBoolean("turknotification", false);
        if(no){
            Double lat = Double.parseDouble(sharedPreferences.getString("turklat", "0"));
            Double lon = Double.parseDouble(sharedPreferences.getString("turklng", "0"));
            sydney = new LatLng(lat,lon);
        }
        else
            sydney= new LatLng(34.03603603603604,-118.2812945258802);

        mMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Los Angeles USC"));
        System.out.println("loation la");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            this.turkLat = mLastLocation.getLatitude();
            this.turkLon = mLastLocation.getLongitude();
        }
        sharedPreferences.edit().putString("turklat",String.valueOf(turkLat)).apply();
        sharedPreferences.edit().putString("turklng", String.valueOf(turkLon)).apply();
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        String token = sharedPreferences.getString("gcmToken", "dummy");
        Connection con = new Connection();
        // Connection.ConnectionPost post = con.new ConnectionPost(Profile.getLocation(), token);
        Connection.ConnectionPost post = con.new ConnectionPost(new LatLng(turkLat,turkLon), token);
        post.execute();
        System.out.println("token>>>>>>>" + token);
        System.out.println("Latitude>>>>>>>" + turkLat);
        System.out.println("Longitude>>>>>>>" + turkLon);

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection to the play store is suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        System.out.println("Connection to the play store failed");
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
          //  Geocoder geocoder = new Geocoder(getBaseContext());
            Geocoder geocoder = new Geocoder(MapsActivity.this);
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                System.out.println("Komal Longitute lattude "+latLng.toString()+"  "+latLng.longitude+" "+latLng.latitude);
                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                mMap.addMarker(markerOptions);

                // Locate the first location
                if(i==0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new HomeFragment();
                break;
            case 2:
                fragment = new HomeFragment();
                break;
            case 3:
                fragment = new ProfileFragment();
//                Intent res = new Intent(MapsActivity.this, Profile.class);
//                startActivity(res);
//                finish();
                break;
            case 4:
                fragment = new HomeFragment();
                break;
            case 5:
                fragment = new HomeFragment();
                break;

            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    /**
     * Slide menu item click listener
     * */
    class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);

            if(position == 2){
                if(username.equals("")){//login button activity
                    Intent res = new Intent(MapsActivity.this, SignInActivity.class);
                    startActivity(res);
                    finish();
                }else{
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
                    sharedPreferences.edit().putString("username","").apply();
                    sharedPreferences.edit().putString("usertype","").apply();
                    Intent res = new Intent(MapsActivity.this, MapsActivity.class);
                    startActivity(res);
                    finish();
                }
            }
        }
    }
}




