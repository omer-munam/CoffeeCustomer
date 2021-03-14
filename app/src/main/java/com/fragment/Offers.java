package com.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.adapter.CategoryAdapter;
import com.adapter.ProductAdapter;
import com.adapter.SubCategoryAdapter;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.SpecificStore;
import com.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import pl.droidsonroids.gif.GifImageView;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.base;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.tam.winati.ksa.SpecificStore.storeJsonObject;
import static com.utils.Constants.PRODUCT_PRESENT;
import static com.utils.Constants.PRODUCT_RELOAD;
import static com.utils.Constants.SUBCAT_PRESENT;

public class Offers extends Fragment  {
    private View view;
    RecyclerView items;

    HashMap<String , ArrayList<JSONObject>>  itemListing        = new HashMap<>();
    ArrayList<JSONObject> listItem                              = new ArrayList<>();
    ArrayList<JSONObject> listSelectedItem                      = new ArrayList<>();

    StaggeredGridLayoutManager stagLayoutManager;

    ProductAdapter productAdapter;

    GifImageView loader_img;

    int storeid = 0;
    int locid = 0;
    int enabled = 0;

    AsyncTask<Void,Void,String> catTask , productTask , favTask;

    public static Offers newInstance(String fragmentName) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FRAGMENT_NAME, fragmentName);
        Offers fragment = new Offers();
        fragment.setArguments(arguments);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view 				= inflater.inflate(R.layout.fragment_offers,container, false);


        try
        {
            enabled             = 0;
            items               = (RecyclerView) view.findViewById(R.id.items);
            loader_img          = (GifImageView) view.findViewById(R.id.loader_img);


            try{storeid         = Integer.parseInt(storeJsonObject.getString("id"));}catch (Exception ee){}
            try{locid           = Integer.parseInt(storeJsonObject.getString("slid"));}catch (Exception ee){}
            try{enabled         = Integer.parseInt(storeJsonObject.getString("status"));}catch (Exception ee){}

            stagLayoutManager   = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);

            reloadFrag();

        }catch (Exception e){
            e.printStackTrace();
        }

        return view;
    }

    void reloadFrag()
    {
        loader_img.setVisibility(View.VISIBLE);
        try{catTask.cancel(true);}catch (Exception e){}
        try{favTask.cancel(true);}catch (Exception e){}
        try{productTask.cancel(true);}catch (Exception e){}


        productAdapter      = new ProductAdapter(base,listSelectedItem,enabled);
        items.setLayoutManager(stagLayoutManager);

        items.setAdapter(productAdapter);

        OverScrollDecoratorHelper.setUpOverScroll(items,OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        getProducts();
    }




    void getProducts()
    {
        productTask = new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("pagenumber",listItem.size());
                    jobj.put("screenwidth",base.screenwidth);
                    jobj.put("screenheight",base.screenheight);
                    jobj.put("onlyfavorites","1");
                    jobj.put("data",storeJsonObject+"");
                    jobj.put("myid",base.getSharePrefs().getUid());
                    jobj.put("caller","getStoreProducts");


                    String encrypted        = base.getConnection().getEncryptedString(jobj.toString());
                    res                     = base.getConnection().sendPostData(encrypted,null);
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
                    appLog("tmp :: "+s);
                    itemListing.clear();
                    listItem.clear();
                    JSONArray jArray                                = new JSONArray(s);
                    for(int i=0;i<jArray.length();i++)
                    {
                        try
                        {
                            JSONObject jobj                         = jArray.getJSONObject(i);
                            int  keyAll[]                           = base.getFunction().getKeys(jobj);
                            String key                              = base.getFunction().createKeyHash(keyAll);
                            ArrayList<JSONObject>   tmp             = itemListing.get(key);
                            appLog("tmp :: "+key+" -- "+tmp);
                            if(tmp == null)
                                tmp                                 = new ArrayList<JSONObject>();

                            tmp.add(jobj);
                            itemListing.put(key,tmp);
                            listItem.add(jobj);

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }


                    listSelectedItem.clear();
                    listSelectedItem.addAll(listItem);
                    items.getRecycledViewPool().clear();
                    productAdapter.notifyDataSetChanged();


                    loader_img.setVisibility(View.GONE);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
