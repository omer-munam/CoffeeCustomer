 package com.tam.winati.ksa;


 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;

 import com.adapter.CartsStoreListing;
 import com.adapter.LocationList;
 import com.balram.library.FotTextView;
 import com.utils.Constants;
 import com.utils.DatabaseHandler;

 import org.json.JSONArray;
 import org.json.JSONObject;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;

 import static com.utils.Constants.ITEM_DELETED;
 import static com.utils.Constants.itemMap;
 import static com.utils.SharePrefsEntry.LAST_LOCATION;

 public class ViewCart2 extends BaseActivity  {


        ListView listView;
        public static CartsStoreListing adapter;

        ArrayList<JSONObject> storeList                                         = new ArrayList<>();
        ArrayList<JSONObject> myPrevLoc                                         = new ArrayList<>();

        LocationList   myPrevLocAdapter;

        public static HashMap<String ,ArrayList<JSONObject>> cartList           = new HashMap<>();
        public static HashMap<String ,String> deliveryCost                      = new HashMap<>();
        public static HashMap<String , String> deliveryTime                     = new HashMap<>();
        JSONObject jsonObject;
        String prevStore , prevLoc;
        JSONArray finalList;
        boolean triedServerLoc;

        public static RelativeLayout rlLocation;
        ListView lLocation;
        LocationManager mLocationManager;
        boolean locationLoaded;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_viewcart);

            ViewProduct.currentTotal        = (TextView)findViewById(R.id.currentTotal);
            listView                        = (ListView)findViewById(R.id.listview);
            map_address_txt                 = (FotTextView)findViewById(R.id.location);
            lLocation                       = (ListView)findViewById(R.id.lLocation);
            rlLocation                      = (RelativeLayout)findViewById(R.id.rlLocation);

            mLocationManager                = (LocationManager) getSystemService(LOCATION_SERVICE);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0, mLocationListener);

            LocalBroadcastManager.getInstance(base).registerReceiver(qtyChanged,new IntentFilter(ITEM_DELETED));
            myPrevLocAdapter                = new LocationList(getBaseActivity(),myPrevLoc);
            lLocation.setAdapter(myPrevLocAdapter);
            db                              = new DatabaseHandler(this);
            createList();

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
             map_address_txt.setText(".......");
             try
             {
                 addressJSON.put("address",latitude+","+longitude);
                 addressJSON.put("latitude",latitude);
                 addressJSON.put("longitude",latitude);
                 try{BaseActivity.map_address_txt.setText(addressJSON.getString("address"));}catch (Exception e){}
                 try{
                     ViewCart2.map_address_txt.setVisibility(View.VISIBLE);}catch (Exception e){}
             }catch (Exception e2){}

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

             adapter         = new CartsStoreListing(this,storeList);
             listView.setAdapter(adapter);

             map_address_txt.setOnClickListener(new View.OnClickListener() {
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
                 try{BaseActivity.map_address_txt.setText(jobj.getString("address"));}catch (Exception e){}
                 try{
                     ViewCart2.map_address_txt.setVisibility(View.VISIBLE);}catch (Exception e){}
                 try{addressJSON = jobj;}catch (Exception e2){}
             }catch (Exception e){}

         }catch (Exception e){
             e.printStackTrace();
         }

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
                                        loadLocations();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(this.getString(R.string.place_order_with_dl_del)).setPositiveButton(this.getString(R.string.yes), dialogClickListener)
                                .setNegativeButton(this.getString(R.string.change_location), dialogClickListener).show();
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

                     getSharePrefs().setKeyValString(LAST_LOCATION,addressJSON+"");
                     JSONObject jobj         = new JSONObject();
                     jobj.put("data",finalList+"");
                     jobj.put("myid",getSharePrefs().getUid());
                     jobj.put("deliverylocation",addressJSON+"");
                     jobj.put("deliverycost",jsonArray+"");
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
 }
