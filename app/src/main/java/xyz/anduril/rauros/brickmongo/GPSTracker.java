package xyz.anduril.rauros.brickmongo;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

/**
 * Created by Rauros on 7/7/2017.
 */

public class GPSTracker extends Service implements LocationListener {
    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation =  false;

    Location location;
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.context=context;
    }

    public Location getLocation() {
        try {
            // See if GPS and Network location are enabled
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // If Permissions says it's ok, then grant permission
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                if(isGPSEnabled) {
                    if(location == null) {
                        // get current location, 10000 seconds?
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
                        if(locationManager!=null)
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
                // if location is not found from gps, tries to get it from network
                if(location == null) {
                    if(isNetworkEnabled) {

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, this);
                        if(locationManager!=null)
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }

        }catch(Exception ex){

        }
        return location;
    }

    public void onLocationChanged(Location location) {

    }

    public void onStatusChanged(String Provider, int status, Bundle extras) {

    }

    public void onProviderEnabled(String Provider) {

    }

    public void onProviderDisabled(String Provider) {

    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

}
