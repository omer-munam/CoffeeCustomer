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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adapter.CategoryAdapter;
import com.adapter.ProductAdapter;
import com.adapter.SubCategoryAdapter;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.SpecificStore;
import com.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import pl.droidsonroids.gif.GifImageView;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.base;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.tam.winati.ksa.SpecificStore.storeJsonObject;
import static com.utils.Constants.PRODUCT_PRESENT;
import static com.utils.Constants.PRODUCT_RELOAD;
import static com.utils.Constants.QTY_CHANGED;
import static com.utils.Constants.SUBCAT_PRESENT;
import static java.util.logging.Logger.global;

public class Products extends Fragment  implements TextWatcher {
    private View view;
    RecyclerView categories,subcategories,items;

    HashMap<String , ArrayList<JSONObject>>  itemListing        = new HashMap<>();
    HashMap<String , ArrayList<JSONObject>>  catitemListing     = new HashMap<>();
    ArrayList<JSONObject> listCat                               = new ArrayList<>();
    ArrayList<JSONObject> listSub                               = new ArrayList<>();
    ArrayList<JSONObject> listItem                              = new ArrayList<>();
    ArrayList<JSONObject> listSelectedItem                      = new ArrayList<>();
    ArrayList<JSONObject> lastSelectedItem                      = new ArrayList<>();
    ArrayList<JSONObject> searching                             = new ArrayList<>();

    LinearLayoutManager catLayoutManager , subCatLayoutManager;
    StaggeredGridLayoutManager stagLayoutManager;

    CategoryAdapter categoryAdapter;
    SubCategoryAdapter subCategoryAdapter;
    ProductAdapter productAdapter;

    LinearLayout subcatlr;
    GifImageView loader_img;

    int storeid = 0;
    int locid = 0;
    String searchTxt;
    int currentCatid,currentSubcatid;
    int enabled = 0;
    ArrayList<JSONObject> favListBuffer = new ArrayList<>();

    AsyncTask<Void,Void,String> catTask , productTask , favTask;

    public static Products newInstance(String fragmentName) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FRAGMENT_NAME, fragmentName);
        Products fragment = new Products();
        fragment.setArguments(arguments);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view 				= inflater.inflate(R.layout.fragment_products,container, false);


        try
        {
            enabled             = 0;
            categories          = (RecyclerView) view.findViewById(R.id.categories);
            subcategories       = (RecyclerView) view.findViewById(R.id.subcategories);
            items               = (RecyclerView) view.findViewById(R.id.items);
            subcatlr            = (LinearLayout) view.findViewById(R.id.subcatlr);
            loader_img          = (GifImageView) view.findViewById(R.id.loader_img);


            try{storeid         = Integer.parseInt(storeJsonObject.getString("id"));}catch (Exception ee){}
            try{locid           = Integer.parseInt(storeJsonObject.getString("slid"));}catch (Exception ee){}
            try{enabled         = Integer.parseInt(storeJsonObject.getString("status"));}catch (Exception ee){}

            catLayoutManager    = new LinearLayoutManager(getActivity());
            subCatLayoutManager = new LinearLayoutManager(getActivity());
            stagLayoutManager   = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);



            SpecificStore.products.addTextChangedListener(this);



            LocalBroadcastManager.getInstance(base).registerReceiver(categoryClicked,new IntentFilter(SUBCAT_PRESENT));
            LocalBroadcastManager.getInstance(base).registerReceiver(productClicked,new IntentFilter(PRODUCT_PRESENT));
            LocalBroadcastManager.getInstance(base).registerReceiver(productReload,new IntentFilter(PRODUCT_RELOAD));


            reloadFrag();

        }catch (Exception e){
            e.printStackTrace();
        }

        return view;
    }

    void reloadFrag()
    {

        itemListing        = new HashMap<>();
        catitemListing     = new HashMap<>();
        listCat            = new ArrayList<>();
        listSub            = new ArrayList<>();
        listItem           = new ArrayList<>();
        listSelectedItem   = new ArrayList<>();
        lastSelectedItem   = new ArrayList<>();
        searching          = new ArrayList<>();
        loader_img.setVisibility(View.VISIBLE);
        try{catTask.cancel(true);}catch (Exception e){}
        try{favTask.cancel(true);}catch (Exception e){}
        try{productTask.cancel(true);}catch (Exception e){}

        currentCatid        = 0;
        currentSubcatid     = 0;
        subcatlr.setVisibility(View.GONE);

        listSelectedItem.clear();
        categoryAdapter     = new CategoryAdapter(base,listCat);
        subCategoryAdapter  = new SubCategoryAdapter(base,listSub);
        productAdapter      = new ProductAdapter(base,listSelectedItem,enabled);

        catLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        subCatLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        categories.setLayoutManager(catLayoutManager);
        subcategories.setLayoutManager(subCatLayoutManager);
        items.setLayoutManager(stagLayoutManager);

        items.setAdapter(productAdapter);
        categories.setAdapter(categoryAdapter);
        subcategories.setAdapter(subCategoryAdapter);

        OverScrollDecoratorHelper.setUpOverScroll(categories,OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        OverScrollDecoratorHelper.setUpOverScroll(subcategories,OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        OverScrollDecoratorHelper.setUpOverScroll(items,OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        getProducts(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        try{productAdapter.notifyDataSetChanged();}catch(Exception e){}
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try{productAdapter.notifyDataSetChanged();}catch(Exception e){}
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        try{productAdapter.notifyDataSetChanged();}catch(Exception e){}
    }

    void getCategories()
    {
        catTask = new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("pagenumber",listCat.size());
                    jobj.put("screenwidth",base.screenwidth);
                    jobj.put("screenheight",base.screenheight);
                    jobj.put("data",storeJsonObject+"");
                    jobj.put("myid",base.getSharePrefs().getUid());
                    jobj.put("caller","getStoreCategories");

                    appLog("jobj :: 22 "+jobj);
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
                    appLog("onpost :: "+s);
                    listCat.clear();
                    JSONArray jArray                                = new JSONArray(s);
                    for(int i=0;i<jArray.length();i++)
                    {
                        try{listCat.add(jArray.getJSONObject(i));}catch (Exception e){}

                    }

                    categories.getRecycledViewPool().clear();
                    categoryAdapter.notifyDataSetChanged();

                }catch (Exception e){
                    e.printStackTrace();
                }

                loader_img.setVisibility(View.GONE);
                SpecificStore.categoriesLoaded = true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    void getProducts(final boolean callCat)
    {
        productTask = new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("pagenumber",listCat.size());
                    jobj.put("screenwidth",base.screenwidth);
                    jobj.put("screenheight",base.screenheight);
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
                    appLog("productsall :: "+s);
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

                            String keyCatOnly                       = base.getFunction().createKeyHashCat(keyAll);
                            ArrayList<JSONObject>   tmp2            = catitemListing.get(keyCatOnly);
                            if(tmp2 == null)
                                tmp2                                = new ArrayList<JSONObject>();

                            tmp2.add(jobj);
                            catitemListing.put(keyCatOnly,tmp2);

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }


                    listSelectedItem.clear();
                    listSelectedItem.addAll(listItem);
                    items.getRecycledViewPool().clear();
                    productAdapter.notifyDataSetChanged();



                }catch (Exception e){
                    e.printStackTrace();
                }

                try{base.getFunction().dismissDialog();}catch (Exception e){}
                if(callCat)
                    getCategories();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{LocalBroadcastManager.getInstance(base).unregisterReceiver(categoryClicked);}catch (Exception e){}
        try{LocalBroadcastManager.getInstance(base).unregisterReceiver(productClicked);}catch (Exception e){}
        try{LocalBroadcastManager.getInstance(base).unregisterReceiver(productReload);}catch (Exception e){}

    }


    public BroadcastReceiver categoryClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SUBCAT_PRESENT))
            {
                try
                {
                    SpecificStore.products.setText("");
                    JSONObject singleObj = null;
                    listSub.clear();
                    try
                    {
                        JSONArray jsonArray = new JSONArray(intent.getExtras().getString("data"));
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            singleObj       = jsonArray.getJSONObject(i);
                            try{listSub.add(jsonArray.getJSONObject(i));}catch (Exception e){}
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(listSub.size() > 0 && subcatlr.getVisibility() == View.GONE)
                        subcatlr.setVisibility(View.VISIBLE);
                    if(listSub.size() == 0 && subcatlr.getVisibility() == View.VISIBLE)
                        subcatlr.setVisibility(View.GONE);

                    subcategories.getRecycledViewPool().clear();
                    subCategoryAdapter.notifyDataSetChanged();


                    int catid           = 0;
                    try
                    {
                        appLog("cat :: sub " + singleObj.getString("catid"));
                        catid           = Integer.parseInt(singleObj.getString("catid"));
                    } catch (Exception e) {
                    }
                    int ids[]       = new int[]{0, storeid, locid, catid, 0};
                    String key      = base.getFunction().createKeyHashCat(ids);

                    appLog("cat  :: sub  key " + key);
                    currentCatid    = catid;
                    currentSubcatid = 0;

                    if(catid > 0)
                    {
                        listSelectedItem.clear();
                        if(catitemListing.get(key) != null)
                        {
                            if(catitemListing.get(key).size() > 0)
                                listSelectedItem.addAll(catitemListing.get(key));
                        }
                        productAdapter.notifyDataSetChanged();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }


            }


        }
    };



    public BroadcastReceiver productReload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PRODUCT_RELOAD)) {
                reloadFrag();
            }
        }
    };


    public BroadcastReceiver productClicked = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(PRODUCT_PRESENT)) {
                try
                {
                    SpecificStore.products.setText("");
                    int catid = 0;
                    int subcatid = 0;

                    try
                    {
                        JSONObject jobj = new JSONObject(intent.getExtras().getString("cat"));
                        appLog("cat :: " + jobj);
                        catid = Integer.parseInt(jobj.getString("id"));
                    } catch (Exception e) {
                    }

                    try
                    {
                        JSONObject jobj = new JSONObject(intent.getExtras().getString("subcat"));
                        appLog("cat :: sub " + jobj);
                        catid = Integer.parseInt(jobj.getString("catid"));
                        subcatid = Integer.parseInt(jobj.getString("subid"));
                    } catch (Exception e) {
                    }

                    int ids[] = new int[]{0, storeid, locid, catid, subcatid};
                    String key = base.getFunction().createKeyHash(ids);

                    appLog("cat :: key " + key);

                    currentCatid        = catid;
                    currentSubcatid     = subcatid;
                    listSelectedItem.clear();
                    favListBuffer.clear();
                    globalLog("catselected :: "+catid);
                    if(catid == -1)
                    {
                        JSONObject jobj = new JSONObject(intent.getExtras().getString("cat"));
                        JSONArray jsonArray = new JSONArray(jobj.getString("favitems"));
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            favListBuffer.add(jsonArray.getJSONObject(i));
                        }
                        listSelectedItem.addAll(favListBuffer);
                        productAdapter.notifyDataSetChanged();
                    }
                    else if (catid > 0)
                        listSelectedItem.addAll(itemListing.get(key));
                    else if(catid == 0)
                        listSelectedItem.addAll(listItem);

                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                productAdapter.notifyDataSetChanged();
            }

        }
    };


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        searchTxt               = SpecificStore.products.getText().toString();
        appLog("before :: "+searchTxt);
        if(searchTxt.equals(""))
        {
            lastSelectedItem.clear();
            lastSelectedItem.addAll(listSelectedItem);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        onSearch();

    }

    void onSearch()
    {
        searchTxt               = SpecificStore.products.getText().toString();
        listSelectedItem.clear();
        if(!searchTxt.equals(""))
        {

            globalLog("catselected :: "+currentCatid);
            searching.clear();
            for(int i=0;i<listItem.size();i++)
            {
                try
                {

                    boolean nameComp     = (listItem.get(i).getString("title_ar").startsWith(searchTxt) || listItem.get(i).getString("title_en").toLowerCase().startsWith(searchTxt.toLowerCase()));
//                    if(currentCatid > 0 && currentSubcatid == 0)
//                    {
//                        if(nameComp && listItem.get(i).getString("catid").equals(String.valueOf(currentCatid)))
//                            searching.add(listItem.get(i));
//                    }
//                    else if(currentCatid > 0 && currentSubcatid > 0)
//                    {
//                        if(nameComp && listItem.get(i).getString("catid").equals(String.valueOf(currentCatid)) && listItem.get(i).getString("subcatid").equals(String.valueOf(currentSubcatid)))
//                            searching.add(listItem.get(i));
//                    }
//                    else if(currentCatid == -1)
//                    {
//                        nameComp     = (favListBuffer.get(i).getString("title_ar").startsWith(searchTxt) || favListBuffer.get(i).getString("title_en").toLowerCase().startsWith(searchTxt.toLowerCase()));
//                        if(nameComp)
//                            searching.add(favListBuffer.get(i));
//                    }
//                    else

                     if(nameComp)
                        searching.add(listItem.get(i));


                }catch (Exception e){e.printStackTrace();}

            }


            listSelectedItem.addAll(searching);

        }
        else
            listSelectedItem.addAll(lastSelectedItem);

        productAdapter.notifyDataSetChanged();
    }


}
