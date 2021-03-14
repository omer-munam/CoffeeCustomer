package com.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.utils.SharePrefsEntry;

import org.json.JSONObject;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.utils.Constants.LOCATION_CHANGED;
import static com.utils.SharePrefsEntry.LOC_ACCURACY;
import static com.utils.SharePrefsEntry.LOC_ALTITUDE;
import static com.utils.SharePrefsEntry.LOC_BEARING;
import static com.utils.SharePrefsEntry.LOC_DATA;
import static com.utils.SharePrefsEntry.LOC_LATITUDE;
import static com.utils.SharePrefsEntry.LOC_LONGITUDE;
import static com.utils.SharePrefsEntry.LOC_PROVIDER;
import static com.utils.SharePrefsEntry.LOC_SPEED;
import static com.utils.SharePrefsEntry.LOC_TIMING;

public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,android.location.LocationListener{

    int INTERVAL              = 1000;
    int FASTEST_INTERVAL      = 1000;
    int SMALLEST_DISPLACEMENT = 0;
    int MAX_WAIT_TIME         = 1000;

    int repeatedTimes         = 0;

    public static LocationService service;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    SharePrefsEntry sp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        service             = this;
        sp                  = new SharePrefsEntry(this);
        mLocationRequest    = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);


        mGoogleApiClient.connect();

        return START_STICKY;

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try{mGoogleApiClient.disconnect();}catch(Exception e){e.printStackTrace();}
        try{LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);}catch(Exception e){e.printStackTrace();}
    }

    @Override
    public void onConnected(Bundle bundle) {
        try
        {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            appLog("connected");
        }catch (Exception e){}


    }

    @Override
    public void onConnectionSuspended(int i) {
        appLog("onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        appLog("onConnectionFailed :: "+connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        appLog("location :: ch "+location.getAccuracy()+" -- "+repeatedTimes);


        if(location.getAccuracy() > 50)
        {
            if(repeatedTimes < 3)
            {
                repeatedTimes++;
                return;
            }
        }


        try
        {
            JSONObject jobj         = new JSONObject();
            jobj.put(LOC_LATITUDE,location.getLatitude());
            jobj.put(LOC_LONGITUDE,location.getLongitude());
            jobj.put(LOC_ACCURACY,location.getAccuracy());
            jobj.put(LOC_ALTITUDE,location.getAltitude());
            jobj.put(LOC_BEARING,location.getBearing());
            jobj.put(LOC_TIMING,location.getElapsedRealtimeNanos());
            jobj.put(LOC_PROVIDER,location.getProvider());
            jobj.put(LOC_SPEED,location.getSpeed());

            sp.setGeneralString(LOC_DATA,jobj+"");

            Intent intent       = new Intent(LOCATION_CHANGED);
            intent.putExtra("data",jobj.toString());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
