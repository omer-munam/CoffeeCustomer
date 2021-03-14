package com.tam.winati.ksa;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.adapter.LocationList;
import com.adapter.TimingList;
import com.balram.library.FotButton;
import com.balram.library.FotEditText;
import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.facebook.internal.LockOnGetVariable;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.models.TimingModel;
import com.utils.Constants;
import com.utils.DatabaseHandler;
import com.widget.RoundedImg;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.ITEM_DELETED;
import static com.utils.Constants.itemMap;
import static com.utils.SharePrefsEntry.LAST_LOCATION;

public class ViewCart extends BaseActivity {


    ArrayList<JSONObject> storeList = new ArrayList<>();
    ArrayList<JSONObject> myPrevLoc = new ArrayList<>();

    LocationList myPrevLocAdapter;

    public static HashMap<String, ArrayList<JSONObject>> cartList = new HashMap<>();
    public static HashMap<String, String> deliveryCost = new HashMap<>();
    public static HashMap<String, String> deliveryTime = new HashMap<>();
    JSONObject jsonObject;
    String prevStore, prevLoc;
    JSONArray finalList;
    boolean triedServerLoc;

    public static RelativeLayout rlLocation;
    ListView lLocation;
    LocationManager mLocationManager;

    FotTextView storename, locationname;
    public static FotTextView deliverytimetxt, deliverytmtxt;
    String storeid, locid;

    ListView deliveryTimeLv;
    ArrayList<Date> allDated = new ArrayList<>();
    ArrayList<String> costArrayList = new ArrayList<>();
    TimingList adapterTiming;
    JSONArray costArray;
    public static Dialog timingDialog;
    RoundedImg imageView;
    LinearLayout lrcontent;
    private LayoutInflater inflate;
    DecimalFormat form = new DecimalFormat("0.00");
    public static FotButton submitValues, couponCheck, couponsubmit;
    FotEditText couponCode;
    Double distance, money;
    public static Double discount;

    public static float delivery_Cost;
    public static String delivery_Time;
    public static int delivery_Type;
    public static View dellr;
    public static String delNeeded;
    public static LinearLayout bottom_lr;
    public static TextView min_msg, price_dl, total, delivery_cst, couponValue;
    public static RelativeLayout coupondiscount;
    RelativeLayout loc_specific, loc_main;
    static Dialog coupon;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewcart);

        ViewProduct.currentTotal = (TextView) findViewById(R.id.currentTotal);
        map_address_txt = (FotTextView) findViewById(R.id.location);
        price_dl = (FotTextView) findViewById(R.id.price_dl);
        storename = (FotTextView) findViewById(R.id.storename);
        locationname = (FotTextView) findViewById(R.id.locationname);
        locationspecific = (FotTextView) findViewById(R.id.locationspecific);
        total = (FotTextView) findViewById(R.id.total);
        delivery_cst = (FotTextView) findViewById(R.id.deliverycost);
        deliverytimetxt = (FotTextView) findViewById(R.id.deliverydate);
        deliverytmtxt = (FotTextView) findViewById(R.id.deliverytime);
        lLocation = (ListView) findViewById(R.id.lLocation);
        rlLocation = (RelativeLayout) findViewById(R.id.rlLocation);
        loc_specific = (RelativeLayout) findViewById(R.id.loc_specific);
        loc_main = (RelativeLayout) findViewById(R.id.loc_main);
        imageView = (RoundedImg) findViewById(R.id.img);
        dellr = (View) findViewById(R.id.dellr);
        lrcontent = (LinearLayout) findViewById(R.id.lrcontent);
        submitValues = (FotButton) findViewById(R.id.submitvalues);
        couponCheck = findViewById(R.id.couponCheck);
        bottom_lr = (LinearLayout) findViewById(R.id.bottom_lr);
        min_msg = (TextView) findViewById(R.id.min_order_msg);
        coupondiscount = findViewById(R.id.coupondiscount);
        couponValue = findViewById(R.id.couponValue);
        discount = 0.0;
        coupondiscount.setVisibility(View.GONE);

        coupon             = new Dialog(ViewCart.this,android.R.style.Theme_Translucent_NoTitleBar);
        coupon.setCanceledOnTouchOutside(false);
        coupon.setCancelable(true);
        coupon.setContentView(R.layout.dialog_coupon);
        couponsubmit = coupon.findViewById(R.id.submitCoupon);
        couponCode = coupon.findViewById(R.id.couponCode);

        String minmsg = getString(R.string.min_order_msg);
        minmsg = minmsg.replace("[MONEY]", DEF_CURRENCY + " " + SpecificStore.MIN_DEL + "");
        min_msg.setText(minmsg);
        min_msg.setTextColor(Color.RED);

        delNeeded = "1";
        loc_specific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    intializeDialog(addressJSON.getString("address"));
                } catch (Exception e) {
                }
            }
        });

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        timingDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        timingDialog.setContentView(R.layout.dialog_select_category);
        deliveryTimeLv = (ListView) timingDialog.findViewById(R.id.select_cat);

        inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup header = (ViewGroup) inflate.inflate(R.layout.del_header, deliveryTimeLv, false);
        deliveryTimeLv.addHeaderView(header, null, false);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0, mLocationListener);

            LocalBroadcastManager.getInstance(base).registerReceiver(qtyChanged,new IntentFilter(ITEM_DELETED));
            myPrevLocAdapter                = new LocationList(getBaseActivity(),myPrevLoc);
            lLocation.setAdapter(myPrevLocAdapter);
            db                              = new DatabaseHandler(this);
            createList();

             submitValues.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     gotoNext(null);
                 }
             });
            getDistance();
            delivery_cst.setText("Calculating...");

            couponCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    coupon.show();
                }
            });

            couponsubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String coupon = couponCode.getText().toString();
                    getFunction().showDialog();
                    checkCoupon(coupon);
                }
            });

        }

    private void checkCoupon(final String coupon1) {
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("locid", locid);
                    jobj.put("coupon",coupon1);
                    jobj.put("caller","checkCoupon");
                    appLog("jobj :: "+jobj);
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    res                     = getConnection().sendPostData(encrypted,null);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                getFunction().dismissDialog();
                try
                {
                    if(s != null)
                    {
                        JSONObject jsonObject                 = new JSONObject(s);
                        Log.d("orsers", jsonObject.toString());
                        if(jsonObject.getString("success").equals("1"))
                        {
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            if (jsonObject.getString("found").equals("1")){
                                JSONObject data = jsonObject.getJSONObject("data");
                                couponImplement(data);
                            }
                        }
                        else
                            Toast.makeText(getBaseActivity(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getBaseActivity(), getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getBaseActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
                coupon.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void couponImplement(JSONObject data) {
        try {
            discount = data.getDouble("coupen_value");
            coupondiscount.setVisibility(View.VISIBLE);
            couponValue.setText(String.valueOf(discount) + "%");
            getFunction().loadQty(storeid, locid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDistance(){
            new AsyncTask<Void,Void,String>()
            {
                @Override
                protected String doInBackground(Void... params) {
                    String res = null;
                    try
                    {
                        JSONObject jobj         = new JSONObject();
                        jobj.put("storeid", storeid);
                        jobj.put("locid", locid);
                        jobj.put("myid",getSharePrefs().getUid());
                        jobj.put("caller","deliverdistance");
                        appLog("jobj :: "+jobj);
                        String encrypted        = cc.getEncryptedString(jobj.toString());
                        res                     = getConnection().sendPostData(encrypted,null);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return res;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    try
                    {
                        if(s != null)
                        {
                            JSONObject jsonObject                 = new JSONObject(s);
                            Log.d("orsers", jsonObject.toString());
                            if(jsonObject.getString("success").equals("1"))
                            {
                                distance = jsonObject.getDouble("distance");
                                money = jsonObject.getDouble("money");
                                delivery_Cost = money.floatValue();
                                if (money == 0.0){
                                    delivery_cst.setText(getString(R.string.free));
                                }
                                else{
                                    delivery_cst.setText(DEF_CURRENCY + String.valueOf(money));
                                }
                                getFunction().loadQty(SpecificStore.STOREID,SpecificStore.LOCID);
                            }
                            else
                                Toast.makeText(getBaseActivity(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(getBaseActivity(), getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getBaseActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


     @Override
     protected void onPause() {
         super.onPause();
     }

     @Override
     protected void onResume() {
         super.onResume();
         String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
         if (locationProviders == null || locationProviders.equals("")) {
             startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
         }
     }

     private final LocationListener mLocationListener = new LocationListener() {
         @Override
         public void onLocationChanged(final Location location) {
             latitude           = location.getLatitude();
             longitude          = location.getLongitude();
             locationLoaded     = true;
             globalLog("XLOCATION :: "+location+" -- "+locationLoaded);
//             map_address_txt.setText(".......");
//             try
//             {
//                 addressJSON.put("address",latitude+","+longitude);
//                 addressJSON.put("latitude",latitude);
//                 addressJSON.put("longitude",latitude);
//                 try{BaseActivity.map_address_txt.setText(getResources().getString(R.string.delivery_location)+" "+addressJSON.getString("address"));}catch (Exception e){}
//                 try{ViewCart.map_address_txt.setVisibility(View.VISIBLE);}catch (Exception e){}
//             }catch (Exception e2){}

             try{mLocationManager.removeUpdates(mLocationListener);}catch (Exception e){}




         }

         @Override
         public void onStatusChanged(String s, int i, Bundle bundle) {

         }

         @Override
         public void onProviderEnabled(String s) {

         }

         @Override
         public void onProviderDisabled(String s) {

         }
     };

     @Override
     public void onDestroy() {
         super.onDestroy();
         try{db.closeConnection();}catch (Exception e){}
         try{LocalBroadcastManager.getInstance(base).unregisterReceiver(qtyChanged);}catch (Exception e){}
         try{mLocationManager.removeUpdates(mLocationListener);}catch (Exception e){}
         delivery_Time = null;
         delivery_Cost = 0;

     }

     public BroadcastReceiver qtyChanged = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(ITEM_DELETED)) {
                 createList();
             }
         }
     };



     void createList()
     {
         try
         {
             prevStore                       = "";
             prevLoc                         = "";

             storeList.clear();
             db.reCreate();
             Set<String> keys                = Constants.itemqty.keySet();
             for(String i: keys)
             {
                 try
                 {
                     String splitted[]       = i.split("@@");
                     globalLog("splitted :: "+splitted[2]+" -- "+splitted[1]+" -- "+SpecificStore.STOREID+" -- "+SpecificStore.LOCID);
                     if(splitted[2].equals(SpecificStore.STOREID) && splitted[1].equals(SpecificStore.LOCID))
                     {
                         appLog("Constants.itemqty.get(i) :: "+i+" -- "+Constants.itemqty.get(i));
                         if(Constants.itemqty.get(i) > 0)
                         {
                             JSONObject jobj         = itemMap.get(i);
                             if(jobj != null)
                                 db.addToCart(jobj,getDefaultLang());
                         }

                     }

                 }catch (Exception e){}


             }

             getFunction().loadQty(SpecificStore.STOREID,SpecificStore.LOCID);

             Cursor cursor                   = db.getStores();



             while(cursor.moveToNext())
             {

                 String storeid      = cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_STOREID));

                 Cursor cursor2      = db.getLocations(storeid);
                 while(cursor2.moveToNext())
                 {
                     try
                     {
                         jsonObject          = new JSONObject(cursor2.getString(cursor2.getColumnIndex(DatabaseHandler.KEY_DATA)));
                         String locid        = jsonObject.getString("locid");
                         String delivery     = jsonObject.getString("delivery");
                         deliveryTime.put(getFunction().generateCartKey(storeid ,locid), delivery);


                         storeList.add(jsonObject);
                         Log.d("orsers","storelist: "+jsonObject.toString());

                         ArrayList<JSONObject> cartJsonList                                      = new ArrayList<>();
                         Cursor cursor3      = db.getFullCart(storeid,locid);
                         while(cursor3.moveToNext())
                         {
                             jsonObject      = new JSONObject(cursor3.getString(cursor3.getColumnIndex(DatabaseHandler.KEY_DATA)));
                             cartJsonList.add(jsonObject);
                         }

                         cartList.put(getFunction().generateCartKey(storeid ,locid),cartJsonList);

                     }catch (Exception e) {
                         e.printStackTrace();
                     }

                 }
             }

             if(storeList.size() == 0)
             {
                 ViewCart.this.finish();
                 return;
             }

             JSONObject storeJSON = storeList.get(0);

             storename.setText(storeJSON.getString("store"+BaseActivity.getDefLang(this)));
             locationname.setText(storeJSON.getString("loc"+BaseActivity.getDefLang(this)));

             globalLog("storedetails :: "+locationname.getText());
             storeid                          = storeJSON.getString("storeid");
             locid                            = storeJSON.getString("locid");
             costArray                        = storeJSON.getJSONArray("costarray");
             String delTime                   = deliveryTime.get(getFunction().generateCartKey(storeid ,locid));
             deliverytimetxt.setText(getResources().getString(R.string.select_delivery_time));


             lrcontent.removeAllViews();
             for(int i=0;i < cartList.get(getFunction().generateCartKey(storeid ,locid)).size();i++)
             {
                 View view 				            = inflate.inflate(R.layout.row_cartproducts, null);
                 view                                = getChildView(view, cartList.get(getFunction().generateCartKey(storeid ,locid)).get(i));
                 lrcontent.addView(view);
             }

             Glide.with(this)
                     .load(storeJSON.getString("storeimg"))
                     .placeholder(R.drawable.logo)
                     .into(new GlideDrawableImageViewTarget(imageView) {
                         @Override
                         public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                             super.onResourceReady(resource, animation);
                             imageView.setImageDrawable(resource);
                         }

                         @Override
                         public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                     });


             deliverytimetxt.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     showDeliveryTimings(jsonObject);
                 }
             });

             dellr.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     deliverytimetxt.performClick();
                 }
             });

             deliverytmtxt.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     deliverytimetxt.performClick();
                 }
             });

             loc_main.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     try
                     {
                         latitude = Double.parseDouble(addressJSON.getString("latitude"));
                         latitude = Double.parseDouble(addressJSON.getString("latitude"));
                         openLocation();
                     }catch (Exception e){

                     }
                 }
             });

             try
             {
                 JSONObject jobj = new JSONObject(getSharePref().getGeneralString(LAST_LOCATION));
                 try{BaseActivity.locationspecific.setText(jobj.getString("name"));}catch (Exception ee){}
                 try{BaseActivity.map_address_txt.setText(jobj.getString("address"));}catch (Exception e){}
                 try{ViewCart.map_address_txt.setVisibility(View.VISIBLE);}catch (Exception e){}
                 try{addressJSON = jobj;}catch (Exception e2){}
             }catch (Exception e){}

         }catch (Exception e){
             e.printStackTrace();
         }

     }


     public View getChildView(View convertView,final JSONObject jsonObject)
     {
         try
         {
             CircularImageView image	    = (CircularImageView)convertView.findViewById(R.id.img);
             FotTextView name			= (FotTextView)convertView.findViewById(R.id.name);
             FotTextView price			= (FotTextView)convertView.findViewById(R.id.nameloc);
             FotTextView discounted	    = (FotTextView)convertView.findViewById(R.id.discounted);
             FotTextView catsub			= (FotTextView)convertView.findViewById(R.id.distance);
             ImageView increase			= (ImageView)convertView.findViewById(R.id.increase);
             ImageView decrease			= (ImageView)convertView.findViewById(R.id.decrease);
             EditText qty			    = (EditText) convertView.findViewById(R.id.qty);
             ImageView garbage           = (ImageView)convertView.findViewById(R.id.garbage);


             final CircularImageView imageView       = image;
             final EditText qtyBox                   = qty;
             int productId                           = Integer.parseInt(jsonObject.getString("id"));
             final String storeid                    = jsonObject.getString("storeid");
             final String locid                      = jsonObject.getString("locid");
             final String catid                      = jsonObject.getString("catid");
             final String subcatid                   = jsonObject.getString("subcatid");


             String key                              = getFunction().createProductKey(storeid,locid,productId,catid,subcatid);
             if(Constants.itemqty.get(key) == null)
                 Constants.itemqty.put(key,0);

             final int currentQty                    = Constants.itemqty.get(key);

             qtyBox.setText(String.valueOf(currentQty));
             if(currentQty > 0)
                 itemMap.put(key,jsonObject);

             increase.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     getFunction().changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,true);
                 }
             });

             decrease.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     getFunction().changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,false);

                 }
             });

             garbage.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             switch (which){
                                 case DialogInterface.BUTTON_POSITIVE:
                                     try
                                     {
                                         int productId           = Integer.parseInt(jsonObject.getString("id"));
                                         String key              = getFunction().createProductKey(storeid,locid,productId,catid,subcatid);
                                         Constants.itemqty.put(key,1);
                                         getFunction().changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,false);
                                     }catch (Exception e){
                                         e.printStackTrace();
                                     }
                                     break;

                                 case DialogInterface.BUTTON_NEGATIVE:
                                     //No button clicked
                                     break;
                             }
                         }
                     };

                     AlertDialog.Builder builder = new AlertDialog.Builder(ViewCart.this);
                     builder.setMessage(ViewCart.this.getString(R.string.remove_product)).setPositiveButton(ViewCart.this.getString(R.string.yes), dialogClickListener)
                             .setNegativeButton(ViewCart.this.getString(R.string.no), dialogClickListener).show();


                 }
             });

             if(Constants.itemqty.get(key) == null)
                 Constants.itemqty.put(key,0);

             if(Constants.itemqty.get(key) > 0)
                 qtyBox.setTextColor(getResources().getColor(R.color.green));
             else
                 qtyBox.setTextColor(getResources().getColor(R.color.txt));

             qtyBox.setText(String.valueOf(Constants.itemqty.get(key)));

             name.setText(jsonObject.getString("title"+BaseActivity.getDefLang(ViewCart.this)));
             price.setText(DEF_CURRENCY+getFunction().round2decimal(Float.parseFloat(jsonObject.getString("price"))));

             float disc                  = Float.parseFloat(jsonObject.getString("discount_price"));
             float prc                   = Float.parseFloat(jsonObject.getString("price"));
             if(disc > 0 && prc > disc)
             {
                 discounted.setVisibility(View.VISIBLE);
                 discounted.setTextColor(getResources().getColor(R.color.green));
                 discounted.setText(DEF_CURRENCY+form.format(disc));
                 price.setTextColor(getResources().getColor(R.color.red));
                 price.setText(DEF_CURRENCY+form.format(prc));
                 price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

             }
             else
             {
                 discounted.setVisibility(View.GONE);
                 price.setTextColor(getResources().getColor(R.color.green));
                 price.setText(DEF_CURRENCY+form.format(prc));
             }

             if(jsonObject.getString("s"+BaseActivity.getDefLang(ViewCart.this)) != null)
             {
                 if(!jsonObject.getString("s"+BaseActivity.getDefLang(ViewCart.this)).equals("null"))
                     catsub.setText(jsonObject.getString("s"+BaseActivity.getDefLang(ViewCart.this)));//jsonObject.getString("ca"+BaseActivity.getDefLang(ViewCart.this))+" , "+
                 else
                     catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(ViewCart.this)));
             }
             else
                 catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(ViewCart.this)));

             Glide.with(ViewCart.this)
                     .load(jsonObject.getString("images"))
                     .placeholder(R.drawable.logo)
                     .into(new GlideDrawableImageViewTarget(image) {
                         @Override
                         public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                             super.onResourceReady(resource, animation);
                             imageView.setImageDrawable(resource);
                         }

                         @Override
                         public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                     });


         }
         catch (Exception e) {
             e.printStackTrace();
         }



         return convertView;
     }



     public void gotoNext(View v)
     {
         if(storeList.size() == 0)
            return;


         if(!getSharePref().isLoggedin())
         {
                    Intent intent  = new Intent(this,SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    callActivity(this,intent);
                    this.finish();
         }
         else
         {
                     finalList                       = new JSONArray();
                     Set<String> keys                = Constants.itemqty.keySet();
                     for(String i: keys)
                     {
                         try
                         {
                             String splitted[]       = i.split("@@");
                             globalLog("splitted :: "+splitted[2]+" -- "+splitted[1]+" -- "+SpecificStore.STOREID+" -- "+SpecificStore.LOCID);
                             if(splitted[2].equals(SpecificStore.STOREID) && splitted[1].equals(SpecificStore.LOCID))
                             {
                                 if(Constants.itemqty.get(i) > 0)
                                 {
                                     JSONObject jobj         = itemMap.get(i);
                                     if(jobj != null)
                                     {
                                         jobj.put("qty",Constants.itemqty.get(i));
                                         finalList.put(jobj);
                                     }
                                 }
                             }

                         }catch (Exception e){}


                     }

                    if(addressJSON == null)
                    {
                        loadLocations();
                    }
                    else if(delivery_Time == null && delNeeded.equals("1"))
                    {
                        deliverytimetxt.performClick();
                        Toast.makeText(this, getResources().getString(R.string.select_delivery_time), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        placeOrder();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(this.getString(R.string.place_order_with_dl_del)).setPositiveButton(this.getString(R.string.yes), dialogClickListener)
                                .setNegativeButton(this.getString(R.string.no), dialogClickListener).show();
                    }

         }
     }

     void loadLocations()
     {
         if(!triedServerLoc)
         {
             getFunction().showDialog();
             rlLocation.setVisibility(View.VISIBLE);
             new AsyncTask<Void,Void,String>()
             {
                 @Override
                 protected String doInBackground(Void... params) {
                     String res = null;
                     try
                     {
                         JSONObject jobj         = new JSONObject();
                         jobj.put("myid",getSharePrefs().getUid());
                         jobj.put("caller","getMyAddress");
                         appLog("jobj :: "+jobj);
                         String encrypted        = cc.getEncryptedString(jobj.toString());
                         res                     = getConnection().sendPostData(encrypted,null);
                     }catch (Exception e){
                         e.printStackTrace();
                     }

                     return res;
                 }

                 @Override
                 protected void onPostExecute(String s) {
                     super.onPostExecute(s);
                     getFunction().dismissDialog();

                     try
                     {
                         if(s != null)
                         {
                             JSONObject jsonObject                 = new JSONObject(s);
                             myPrevLoc.clear();
                             if(jsonObject.getString("success").equals("1"))
                             {
                                 JSONArray jarray                 = new JSONArray(jsonObject.getString("locations"));
                                 for(int i=0;i<jarray.length();i++)
                                 {
                                     myPrevLoc.add(jarray.getJSONObject(i));
                                 }

                                 myPrevLocAdapter.notifyDataSetChanged();
                                 triedServerLoc     = true;
                             }
                             else
                                 Toast.makeText(getBaseActivity(), getString(R.string.error_loading_prev_locations), Toast.LENGTH_SHORT).show();
                         }
                         else
                             Toast.makeText(getBaseActivity(), getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                     }catch (Exception e){
                         e.printStackTrace();
                         Toast.makeText(getBaseActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                     }
                 }
             }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
         }
         else
             rlLocation.setVisibility(View.VISIBLE);

     }

     void placeOrder()
     {
         getFunction().showDialog();
         new AsyncTask<Void,Void,String>()
         {
             @Override
             protected String doInBackground(Void... params) {
                 String res = null;
                 try
                 {
                     JSONArray jsonArray             = new JSONArray();
                     try
                     {
                         Set<String> keys                = deliveryCost.keySet();
                         for(String i: keys)
                         {
                             JSONObject job           = new JSONObject();
                             job.put(i,deliveryCost.get(i));
                             jsonArray.put(job);
                         }
                     }catch (Exception e3){}

                     globalLog("addressjson :: "+addressJSON + delivery_Type);

                     JSONObject jobj         = new JSONObject();
                     jobj.put("data",finalList+"");
                     jobj.put("myid",getSharePrefs().getUid());
                     jobj.put("delneeded",delNeeded+"");
                     jobj.put("deliverylocation",addressJSON+"");
                     jobj.put("deliverycost",jsonArray+"");
                     jobj.put("delcost",delivery_Cost+"");
                     jobj.put("coupondiscount", discount+"");
                     jobj.put("deltype", delivery_Type+"");
                     jobj.put("deltime",delivery_Time+"");
                     jobj.put("caller","placeOrder");
                     appLog("jobj :: "+jobj);
                     String encrypted        = cc.getEncryptedString(jobj.toString());
                     res                     = getConnection().sendPostData(encrypted,null);
                     if(res != null)
                         res                 = res.trim();

                     appLog("placeorder :: "+res);
                 }catch (Exception e){
                     e.printStackTrace();
                 }

                 return res;
             }

             @Override
             protected void onPostExecute(String s) {
                 super.onPostExecute(s);
                 getFunction().dismissDialog();
                 try
                 {
                     if(s != null)
                     {
                         JSONObject jsonObject                 = new JSONObject(s);
                         if(jsonObject.getString("success").equals("1"))
                         {

                             getSharePrefs().setKeyValString(LAST_LOCATION,addressJSON+"");
                             getSharePref().updateDefAddress(addressJSON);
                             HashMap<String, Integer> tempList      = Constants.itemqty;
                             HashMap<String, JSONObject> tempList2  = Constants.itemMap;

                             Iterator<Map.Entry<String, Integer>> itr = tempList.entrySet().iterator();
                             while(itr.hasNext())
                             {
                                 Map.Entry<String, Integer> entry       = itr.next();
                                 String key                             = entry.getKey();
                                 String splitted[]                      = key.split("@@");
                                 if(splitted[2].equals(SpecificStore.STOREID) && splitted[1].equals(SpecificStore.LOCID))
                                 {
                                     System.out.println("Key : "+entry.getKey()+" Removed.");
                                     itr.remove();  // Call Iterator's remove method.
                                 }
                             }

                             Iterator<Map.Entry<String, JSONObject>> itr2 = tempList2.entrySet().iterator();
                             while(itr2.hasNext())
                             {
                                 Map.Entry<String, JSONObject> entry    = itr2.next();
                                 String key                             = entry.getKey();
                                 String splitted[]                      = key.split("@@");
                                 if(splitted[2].equals(SpecificStore.STOREID) && splitted[1].equals(SpecificStore.LOCID))
                                 {
                                     itr2.remove();
                                 }
                             }

                             Constants.itemqty  = tempList;
                             Constants.itemMap  = tempList2;

                             Constants.totalPrice     = 0;
                             Toast.makeText(getBaseActivity(),"Purchased",Toast.LENGTH_SHORT).show();
                             Intent intent  = new Intent(getBaseActivity(),ShowStores.class);
                             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                             callActivity(getBaseActivity(),intent);
                             getBaseActivity().finish();
                         }
                         else
                             Toast.makeText(getBaseActivity(), getString(R.string.error_placing_order), Toast.LENGTH_SHORT).show();
                     }
                     else
                         Toast.makeText(getBaseActivity(), getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                 }catch (Exception e){
                     e.printStackTrace();
                     Toast.makeText(getBaseActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                 }
             }
         }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }

     @Override
     public void onBackPressed() {
         if(rlLocation.getVisibility() == View.VISIBLE)
             rlLocation.setVisibility(View.GONE);
         else
             super.onBackPressed();
     }

     void showDeliveryTimings(JSONObject jsonObject)
     {
         try
         {
             long currTime               = System.currentTimeMillis();
             long threeHours             = 1800000;
             JSONArray jarray            = jsonObject.getJSONArray("alldelivery");
             ArrayList<Date> today       = new ArrayList<>();
             ArrayList<Date> tomrw       = new ArrayList<>();


             for(int i=0;i<jarray.length();i++)
             {

                 Calendar c              = Calendar.getInstance();
                 SimpleDateFormat df     = new SimpleDateFormat("yyyy-MM-dd");
                 SimpleDateFormat df2     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 String formattedDate    = df.format(c.getTime());
                 String curr             = jarray.getString(i);
                 formattedDate           = formattedDate+" "+curr;





                 c.add(Calendar.DAY_OF_YEAR, 1);
                 Date tomorrow           = c.getTime();
                 String formattedDate2   = df.format(tomorrow)+" "+curr;

                 Date tod                = (Date)df2.parse(formattedDate);
                 Date tomm               = (Date)df2.parse(formattedDate2);

                 long diff1              = (tod.getTime() - currTime);
                 long diff2              = (tomm.getTime() - currTime);
                 if(diff1 > threeHours)
                     today.add(tod);
                 if(diff2 > threeHours)
                     tomrw.add(tomm);


             }

             costArrayList.clear();
             allDated.clear();
             for(int i=0;i<today.size();i++)
             {
                 Date dated              = today.get(i);
                 costArrayList.add(getFunction().getCost(dated,costArray));
                 allDated.add(dated);
             }

             for(int i=0;i<tomrw.size();i++)
             {
                 Date dated              = tomrw.get(i);
                 costArrayList.add(getFunction().getCost(dated,costArray));
                 allDated.add(tomrw.get(i));
             }

             adapterTiming       = new TimingList(this,allDated,storeid,locid,costArrayList);
             deliveryTimeLv.setAdapter(adapterTiming);


             //timingDialog.show();

             TimingModel timingModel = new TimingModel(allDated,storeid,locid,costArrayList);
             Intent intent = new Intent(this, DeliveryTimeSelectionActivity.class);
             intent.putExtra("currentTimeObject", timingModel);
             callActivity(this, intent);


         }catch (Exception e){
             e.printStackTrace();
         }

     }
 }
