package com.tam.winati.ksa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.adapter.HomeCat;
import com.adapter.HomeSuggested;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.utils.Constants.LOCATION_CHANGED;
import static com.utils.SharePrefsEntry.LOC_DATA;
import static com.utils.SharePrefsEntry.LOC_LATITUDE;
import static com.utils.SharePrefsEntry.LOC_LONGITUDE;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    GoogleMap map;

    JSONArray stores , categories , suggested;
    boolean previousZoomed;

    LinearLayout linearLvLr;
    GridView grid;
    RecyclerView rcv;

    ArrayList<JSONObject> listCat       = new ArrayList<>();
    ArrayList<JSONObject> listSug       = new ArrayList<>();
    HomeCat homeCat;
    HomeSuggested homeSuggested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        linearLvLr          = (LinearLayout)findViewById(R.id.linearLvLr);
        rcv                 = (RecyclerView) findViewById(R.id.rcv);
        grid                = (GridView) findViewById(R.id.grid);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rcv.setLayoutManager(layoutManager);

        OverScrollDecoratorHelper.setUpOverScroll(grid);
        OverScrollDecoratorHelper.setUpOverScroll(rcv,OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);

        try
        {
            stores          = new JSONArray(Splash.splashData.getString("stores"));
            categories      = new JSONArray(Splash.splashData.getString("categories"));
            suggested       = new JSONArray(Splash.splashData.getString("suggested"));

            for(int i=0;i<categories.length();i++)
            {
                listCat.add(categories.getJSONObject(i));
            }

            for(int i=0;i<suggested.length();i++)
            {
                listSug.add(suggested.getJSONObject(i));
            }

            homeCat         = new HomeCat(this,listCat);
            homeSuggested   = new HomeSuggested(this,listSug);

            grid.setAdapter(homeCat);
            rcv.setAdapter(homeSuggested);

        }catch (Exception e){}

        mapFragment     = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);

        for(int i=0;i<stores.length();i++)
        {

            try
            {
                JSONObject jsonObject   = stores.getJSONObject(i);
                JSONArray locs          = new JSONArray(jsonObject.getString("locations"));
                for(int m=0;m<locs.length();m++)
                {
                    JSONObject jLoc     = locs.getJSONObject(m);
                    String name         = jsonObject.getString("title"+getDefaultLang())+"\n"+jLoc.getString("title"+getDefaultLang());
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(jLoc.getDouble("latitude"), jLoc.getDouble("longitude")))
                            .title(name));
                }
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
