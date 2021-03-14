package com.tam.winati.ksa;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import static com.utils.SharePrefsEntry.LOC_DATA;
import static com.utils.SharePrefsEntry.LOC_LATITUDE;
import static com.utils.SharePrefsEntry.LOC_LONGITUDE;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    GoogleMap map;
    boolean previousZoomed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        mapFragment     = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);

        for(int i=0;i<ShowStores.list.size();i++)
        {

            try
            {

                JSONObject jsonObject   = ShowStores.list.get(i);
                String name             = jsonObject.getString("title"+getDefaultLang());
                name                    = name+"\n"+jsonObject.getString("loc"+getDefaultLang());
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")))
                        .title(name));

            }catch (Exception e){
                e.printStackTrace();
            }
        }


        if(map != null && !previousZoomed)
        {
            try
            {
                JSONObject jsonObject   =   new JSONObject(getSharePrefs().getGeneralString(LOC_DATA));
                CameraUpdate center     =   CameraUpdateFactory.newLatLng(new LatLng(jsonObject.getDouble(LOC_LATITUDE),jsonObject.getDouble(LOC_LONGITUDE)));
                CameraUpdate zoom       =   CameraUpdateFactory.zoomTo(10);

                map.moveCamera(center);
                map.animateCamera(zoom);
                previousZoomed          =   true;
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }




}
