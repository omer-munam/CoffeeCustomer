 package com.tam.winati.ksa;


 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;

 import com.adapter.ProductsListing;

 import org.json.JSONArray;
 import org.json.JSONObject;

 import java.util.ArrayList;


 public class ViewProduct extends BaseActivity  {

     JSONObject jsonObject , storeDetails;
     ListView listView;
     ProductsListing adapter;
     ArrayList<JSONObject> list = new ArrayList<JSONObject>();
     public static TextView currentTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewproduct);

        listView        = (ListView)findViewById(R.id.listview);
        currentTotal    = (TextView)findViewById(R.id.currentTotal);

        getFunction().loadQty(SpecificStore.STOREID,SpecificStore.LOCID);





        try
        {
            jsonObject      = new JSONObject(getIntent().getExtras().getString("data"));
            storeDetails    = new JSONObject(getIntent().getExtras().getString("store"));
            adapter         = new ProductsListing(this,list,jsonObject.getString("storeid"),jsonObject.getString("locid"),jsonObject.getString("catid"),jsonObject.getString("subid"));

            appLog("storeDetails :: "+storeDetails);

            listView.setAdapter(adapter);
            new AsyncTask<Void,Void,String>()
            {
                @Override
                protected String doInBackground(Void... params) {
                    String res = null;
                    try
                    {
                        JSONObject jobj         = new JSONObject();
                        jobj.put("catid",jsonObject.getString("catid"));
                        jobj.put("subid",jsonObject.getString("subid"));
                        jobj.put("storeid",jsonObject.getString("storeid"));
                        jobj.put("locid",jsonObject.getString("locid"));

                        jobj.put("loc_ar",storeDetails.getString("loc_ar"));
                        jobj.put("loc_en",storeDetails.getString("loc_en"));
                        jobj.put("store_ar",storeDetails.getString("title_ar"));
                        jobj.put("store_en",storeDetails.getString("title_en"));
                        jobj.put("storeimgs",storeDetails.getString("images"));
                        jobj.put("latitude",storeDetails.getString("latitude"));
                        jobj.put("longitude",storeDetails.getString("longitude"));
                        jobj.put("storeid",storeDetails.getString("id"));
                        jobj.put("locid",storeDetails.getString("slid"));
                        jobj.put("next",storeDetails.getString("next"));
                        jobj.put("timing",storeDetails.getString("time"));
                        jobj.put("alldelivery",storeDetails.getString("alldelivery"));


                        jobj.put("myid",getSharePrefs().getUid());
                        jobj.put("caller","getProductDetail");

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
                        JSONObject jobj1;
                        ArrayList<JSONObject> tmp                       = new ArrayList<>();
                        JSONArray jArray                                = new JSONArray(s);
                        for(int i=0;i<jArray.length();i++)
                        {
                            try
                            {
                                jobj1 						            = jArray.getJSONObject(i);
                                tmp.add(jobj1);
                            }catch (Exception e){}

                        }

                        list.clear();
                        list.addAll(tmp);
                        adapter.notifyDataSetChanged();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void showCart(View v)
    {
        viewCart(v);
    }
}
