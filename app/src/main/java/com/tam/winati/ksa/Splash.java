package com.tam.winati.ksa;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.services.LocationService;
import com.utils.MarshMallowPermission;

import org.json.JSONObject;
import static com.utils.Constants.LOCATION_CHANGED;
import static com.utils.SharePrefsEntry.LOC_DATA;
import static com.utils.SharePrefsEntry.LOC_LATITUDE;
import static com.utils.SharePrefsEntry.LOC_LONGITUDE;

public class Splash extends BaseActivity{



    public static JSONObject splashData;
    MarshMallowPermission marshMallowPermission = new MarshMallowPermission(this);
    boolean allPermissionSet;
    boolean gotLocation;
    boolean gotData;

    LocationManager locationManager;
    boolean gps_enabled         = false;
    boolean network_enabled     = false;

    double latitude , longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        allPermissionSet        = false;
        gotLocation             = false;
        gotData                 = false;
        locationManager         = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,new IntentFilter(LOCATION_CHANGED));

        String oldlang          = getIntialDefault();
        appLog("oldLang :: "+oldlang);
        if(oldlang != null)
            getSharePrefs().setLanguage(oldlang);
    }


    void loadData()
    {
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                return (new JSONObject()).toString();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try
                {
                    globalLog("splash :: "+s);
                    if(s == null)
                    {
                        Toast.makeText(getApplicationContext(),getString(R.string.error),Toast.LENGTH_SHORT).show();
                        Splash.this.finish();
                    }
                    else
                    {
                        splashData          = new JSONObject(s);
                        gotData             = true;
                        loadNextActivity();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),getString(R.string.error),Toast.LENGTH_SHORT).show();
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void checkPermissions() {

        if (!marshMallowPermission.checkPermissionForLocation())
        {
            marshMallowPermission.requestPermissionForLocation();
        }
        else if (!marshMallowPermission.checkPermissionForCalling())
        {
            marshMallowPermission.requestPermissionForCall();
        }
        else
        {
            Intent service          = new Intent(getApplicationContext(),LocationService.class);
            startService(service);
            checkLocation();
        }
    }

    void checkLocation()
    {
        try
        {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try
        {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled)
        {
            AlertDialog.Builder dialog      = new AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getResources().getString(R.string.open), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });

            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {}
            });
            dialog.show();
        }
        else
        {
            allPermissionSet = true;
            loadNextActivity();
        }
    }

    void loadNextActivity()
    {
        if(allPermissionSet && gotData && gotLocation)
        {
            globalLog("splash :: "+getSharePrefs().isLoggedin());
            Intent intent       = null;
            if(getSharePrefs().isLoggedin())
                intent       = new Intent(getBaseContext(),ShowStores.class);
            else
                intent       = new Intent(getBaseContext(),SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            callActivity(Splash.this,intent);
            Splash.this.finish();
        }
        else if(allPermissionSet && gotLocation && !gotData)
        {
            loadData();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);}catch(Exception e){}
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                if(intent.getAction().equals(LOCATION_CHANGED))
                {
                    String locD             =   getSharePrefs().getGeneralString(LOC_DATA);
                    appLog("locD :: "+locD);
                    JSONObject jsonObject   =   new JSONObject(locD);
                    latitude                =   jsonObject.getDouble(LOC_LATITUDE);
                    longitude               =   jsonObject.getDouble(LOC_LONGITUDE);
                    try{LocationService.service.stopSelf();}catch(Exception e){}
                    String data         = intent.getStringExtra("data");
                    gotLocation         = true;
                    loadNextActivity();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };


}
