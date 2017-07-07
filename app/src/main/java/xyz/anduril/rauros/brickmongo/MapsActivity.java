package xyz.anduril.rauros.brickmongo;

import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private Location mLocation;
    private Marker[] generatedMarkers;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private LatLng wherePlayerIs;

    private boolean debug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        gpsTracker = new GPSTracker(getApplicationContext());
        mLocation = gpsTracker.getLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setupMap(mMap);

        wherePlayerIs = getPlayersLocation();
        mMap.addMarker(new MarkerOptions().position(wherePlayerIs).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.youarehere)));
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(wherePlayerIs,17.5f) );

        drawMarkers();

        CircleOptions circleSeeable = new CircleOptions()
                .center(wherePlayerIs)
                .fillColor(Color.argb(50,77,64,0))
                .strokeColor(Color.argb(130,0,77,64))
                .strokeWidth(2)
                .radius(250); // In meters
        //make a radius around you :) this one is the clickable radius
        CircleOptions circleClickable = new CircleOptions()
                .center(wherePlayerIs)
                .fillColor(Color.argb(130,0,77,64))
                .strokeWidth(2)
                .strokeColor(Color.argb(130,0,77,64))
                .radius(100); // In meters

// Get back the mutable Circle
        Circle circlesee = mMap.addCircle(circleSeeable);
        Circle circleclick = mMap.addCircle(circleClickable);
    }

    public void setupMap(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Cannot find style, Error: ", e);
        }

        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            Marker currentShown;
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(currentShown)) {
                    marker.hideInfoWindow();
                    currentShown = null;
                } else {
                    marker.showInfoWindow();
                    currentShown = marker;
                }
                return true;
            }
        });
        //remove literally all gestures if debug mode is off
        if(!debug) {
            googleMap.getUiSettings().setRotateGesturesEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.getUiSettings().setTiltGesturesEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            //mMap.getUiSettings().setZoomGesturesEnabled(false);
        }
        googleMap.setMinZoomPreference(17);
        googleMap.setMaxZoomPreference(20);
    }

    public void drawMarkers(){
        wherePlayerIs = getPlayersLocation();
        //marker array to store markers for later access
        generatedMarkers = new Marker[60];
        //create random markers around player
        for(int i=0;i<generatedMarkers.length;i++){
            Double randomLat = Math.random()*(.003)+.0001;
            Double randomLon = Math.random()*(.003)+.0001;
            Double sign = Math.random();

            if(sign<=0.25) { } // both positive
            else if(sign<=.5) { randomLat = -1*randomLat; } //lat negative
            else if(sign<=.75) { randomLon = -1*randomLon; } //long negative
            else {
                //both negative
                randomLat = -1*randomLat;
                randomLon = -1*randomLon;
            }
            generatedMarkers[i]=mMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude()+randomLat,mLocation.getLongitude()+randomLon)).title("Generated event").icon(BitmapDescriptorFactory.fromResource(R.drawable.pokepinsmall)).alpha(0.7f).rotation(15));
            generatedMarkers[i].setTitle("Generated Event #"+i);
            //hide all markers outside seeable radius, or in debug mode we will make them grey
            LatLng genPos = generatedMarkers[i].getPosition();
            double distanceInMeters = distAway(wherePlayerIs,genPos);
            if(distanceInMeters>100){
                //circleClickable radius
                generatedMarkers[i].setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pokepinsmallgrey));

            }
            if(distanceInMeters>250){
                //circleSeeable radius
                generatedMarkers[i].setIcon(BitmapDescriptorFactory.fromResource(R.drawable.empty));
            }
        }


    }

    //thanks https://stackoverflow.com/questions/639695/how-to-convert-latitude-or-longitude-to-meters
    public double distAway(LatLng A, LatLng B){  // generally used geo measurement function, measuring A to B using HAVERSINE formula
        double lat1 = A.latitude;
        double lon1 = A.longitude;
        double lat2 = B.latitude;
        double lon2 = B.longitude;
        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }

    public LatLng getPlayersLocation() {
        LatLng wherePlayerIs = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        return wherePlayerIs;
    }
}
