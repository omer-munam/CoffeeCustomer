package com.tam.winati.ksa;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balram.library.FotTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.utils.ConnectionClass;
import com.utils.Constants;
import com.utils.DatabaseHandler;
import com.utils.Functions;
import com.utils.SharePrefsEntry;

import org.json.JSONObject;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity implements View.OnTouchListener, GoogleApiClient.OnConnectionFailedListener {
    ConnectionClass cc;
    static SharePrefsEntry sp;
    Functions fun;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    public int screenwidth , screenheight;
    RelativeLayout cart_rl;
    public static TextView amount_cart;
    View rootView;
    public static FragmentManager fragmentManager;
    public  JSONObject addressJSON ;
    DatabaseHandler db;
    public static BaseActivity base;

    LinearLayout mDrawerListView;
    public DrawerLayout mDrawer;
    LinearLayout menu_profile, menu_account , menu_share , menu_about , menu_language;
    GoogleApiClient mGoogleApiClient;
    public double latitude,longitude;
    private int PLACE_PICKER_REQUEST = 1;
    public static FotTextView map_address_txt , locationspecific;;
    boolean locationLoaded;
    Dialog addressDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Constants.DEF_CURRENCY  = getResources().getString(R.string.currency)+" ";
        base            = this;
        cc              = new ConnectionClass(this);
        sp              = new SharePrefsEntry(this);
        fun             = new Functions(this);

        sp.loadLocalLanguage();

        latitude        = 24.774265;
        longitude       = 46.738586;
        locationLoaded  = false;

        fragmentManager = getSupportFragmentManager();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenheight    = displayMetrics.heightPixels;
        screenwidth     = displayMetrics.widthPixels;

        rootView        = getWindow().getDecorView().getRootView();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        addressDetails   = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        addressDetails.setContentView(R.layout.dialog_address_name);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        appLog("onactivity :: "+requestCode+" -- "+resultCode);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                try
                {
                    Place place         = PlacePicker.getPlace(data, this);
                    appLog("onactivity :: "+place);
                    addressJSON         = new JSONObject();
                    String placename    = String.format("%s", place.getName());
                    String llt          = String.valueOf(place.getLatLng().latitude);
                    String lng          = String.valueOf(place.getLatLng().longitude);
                    latitude            = place.getLatLng().latitude;
                    longitude           = place.getLatLng().longitude;
                    String address      = String.format("%s", place.getAddress());
                    addressJSON.put("name",placename);
                    addressJSON.put("latitude",latitude);
                    addressJSON.put("longitude",longitude);
                    addressJSON.put("address",address);
                    String finalClass = getPackageName()+".ViewCart";
                    globalLog("finalClass :: "+finalClass);
                    if(getClass().getCanonicalName().equals(finalClass))
                    {
                        intializeDialog(address);
                        map_address_txt.setText(address);
                    }
                    else
                        map_address_txt.setText(address);
                    try{locationspecific.setText(placename);}catch (Exception e3){}
                    map_address_txt.setVisibility(View.VISIBLE);
                    ViewCart.rlLocation.setVisibility(View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    public void intializeDialog(final String address)
    {
        final EditText dataEtxt     = (EditText)addressDetails.findViewById(R.id.address_details);
        Button cancel               = (Button)addressDetails.findViewById(R.id.cancel);
        Button done                 = (Button)addressDetails.findViewById(R.id.done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addD = dataEtxt.getText().toString();
                if(addD.equals(""))
                {
                    Toast.makeText(BaseActivity.this,getResources().getString(R.string.provide_speicific_details),Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try{addressJSON.put("name",addD);}catch (Exception e){}
                    locationspecific.setText(addD);
                    map_address_txt.setText(address);
                    addressDetails.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addressDetails.dismiss();
            }
        });

        addressDetails.show();
    }

    public void openLocation()
    {
        try
        {
            if(!locationLoaded)
            {
                Toast.makeText(this,getResources().getString(R.string.latest_location_not_available),Toast.LENGTH_SHORT).show();
            }
            else
            {
                globalLog("XLOCATION :: "+latitude+" -- "+longitude);
                PlacePicker.IntentBuilder builder   = new PlacePicker.IntentBuilder();
                if(latitude != 255 && longitude != 255)
                    builder.setLatLngBounds(new LatLngBounds(new LatLng(latitude,longitude),new LatLng(latitude,longitude)));
                startActivityForResult(builder.build(getBaseActivity()), PLACE_PICKER_REQUEST);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        appLog("onconnection failed");
    }


    public void logout(View v)
    {
        getFunction().logout();
    }





    public static void callActivity(Context context, Intent intent)
    {
        context.startActivity(intent);
        try{((Activity)context).overridePendingTransition( R.anim.activity_entry_anim, R.anim.activity_exit_anim );}catch(Exception e){}
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition( R.anim.activity_entry_anim, R.anim.activity_exit_anim );
    }

    public ConnectionClass getConnection()
    {
        return cc;
    }


    public SharePrefsEntry getSharePrefs()
    {
        return sp;
    }

    public Functions getFunction(){return fun;}

    public String getIntialDefault()
    {
        if(sp.getLanguage().equals(""))
            return Locale.getDefault().getLanguage();
        else
            return null;
    }

    public static String getDefaultLang()
    {
        if(sp.getLanguage().equals(""))
            return "_"+Locale.getDefault().getLanguage();
        else
            return "_"+sp.getLanguage();
    }

    public static String getDefLang(Context ctx)
    {
        String abc          = "";
        if(sp == null)
            sp              = new SharePrefsEntry(ctx);
        return getDefaultLang();
    }

    public void gotoAccountPage()
    {
        Intent intent = new Intent(this,SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        callActivity(this,intent);
    }

    public void viewOnMap(View v)
    {
        Intent intent = new Intent(this,MapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public static void appLog(String msg)
    {
        Log.e("AndroidRuntime",msg);
    }


    public BaseActivity getBaseActivity()
    {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        try
        {
            menu_profile    = (LinearLayout) findViewById(R.id.menu_profile);
            menu_account    = (LinearLayout) findViewById(R.id.menu_account);
            menu_share      = (LinearLayout) findViewById(R.id.menu_share);
            menu_about      = (LinearLayout) findViewById(R.id.menu_about);
            menu_language   = (LinearLayout) findViewById(R.id.menu_language);
            mDrawerListView = (LinearLayout) findViewById(R.id.left_drawer);
            mDrawer         = (DrawerLayout) findViewById(R.id.drawer_layout);
        }catch (Exception e){
            e.printStackTrace();
        }


        try
        {
            if(getSharePrefs().isLoggedin())
            {
                menu_profile.setVisibility(View.VISIBLE);
                menu_account.setVisibility(View.GONE);
            }
            else
            {
                menu_profile.setVisibility(View.GONE);
                menu_account.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        try
        {
            cart_rl         = (RelativeLayout) findViewById(R.id.cart_rl);
            amount_cart     = (FotTextView) findViewById(R.id.amount_cart);

            amount_cart.setText("0.00");
            if(Constants.totalPrice > 0)
            {
                amount_cart.setText(Constants.formatted);
            }

        }catch (Exception e){}

    }

    public void viewCart(View v)
    {
        Intent intent = new Intent(this,ViewCart.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        callActivity(this,intent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                int y                               = (int)event.getY();
                int x                               = (int)event.getX();

                cart_rl.getLayoutParams().height    = 20;
                cart_rl.getLayoutParams().width     = 20;

                cart_rl.layout(x, y, x+48, y+48);
                break;
        }
        return true;
    }


    public SharePrefsEntry getSharePref()
    {
        return sp;
    }

    public void toggleMenu(View v)
    {
        if(getSharePref().getLanguage() == "ar")
        {
            try
            {
                if(mDrawer.isDrawerOpen(Gravity.RIGHT))
                    mDrawer.closeDrawer(Gravity.RIGHT);
                else
                    mDrawer.openDrawer(Gravity.RIGHT);
            }catch (Exception e){
                if(mDrawer.isDrawerOpen(Gravity.LEFT))
                    mDrawer.closeDrawer(Gravity.LEFT);
                else
                    mDrawer.openDrawer(Gravity.LEFT);
            }

        }
        else
        {
            try
            {
                if(mDrawer.isDrawerOpen(Gravity.LEFT))
                    mDrawer.closeDrawer(Gravity.LEFT);
                else
                    mDrawer.openDrawer(Gravity.LEFT);
            }catch (Exception e){
                if(mDrawer.isDrawerOpen(Gravity.RIGHT))
                    mDrawer.closeDrawer(Gravity.RIGHT);
                else
                    mDrawer.openDrawer(Gravity.RIGHT);
            }

        }

    }

    public void menuClicked(View v)
    {
         int tag    = -1;
         try{ tag   = Integer.parseInt(v.getTag().toString());}catch (Exception e){}

        if( tag == 1)
        {
            Intent intent = new Intent(this,EditProfile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            callActivity(this,intent);
        }
        else if( tag == 10)
        {
            Intent intent = new Intent(this,ShowStores.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            callActivity(this,intent);
        }
        else if(tag == 2)
            gotoAccountPage();
        else if(tag == 3)
        {
            if(getDefaultLang().contains("_ar"))
                getSharePrefs().setLanguage("en");
            else
                getSharePrefs().setLanguage("ar");


            getSharePrefs().loadLocalLanguage();

            String finalClass = getPackageName()+".SpecificStore";
            globalLog("className :: "+finalClass);
            if(getClass().getCanonicalName().equals(finalClass))
            {
                Intent intent = new Intent(this,SpecificStore.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("data",SpecificStore.storeJsonObject+"");
                startActivity(intent);
            }
            else
                recreate();
        }
        else if(tag == 33)
        {
            getFunction().logout();
        }
        else if(tag == 4)
        {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out this new app at: https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        else if(tag == 5)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dalilalqahwa.com/"));
            startActivity(browserIntent);
        }
        else if(tag == 48)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dalilalqahwa.com/app/help.php"));
            startActivity(browserIntent);
        }

        try{mDrawer.closeDrawer(Gravity.RIGHT);}catch (Exception e){}
        try{mDrawer.closeDrawer(Gravity.LEFT);}catch (Exception e){}

    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        try{db.closeConnection();}catch (Exception e){}

    }

    public static void globalLog(String msg)
    {
        Log.e("AndroidRuntime",msg);
    }



}
