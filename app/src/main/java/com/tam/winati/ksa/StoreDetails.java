package com.tam.winati.ksa;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.adapter.StoreCategories;
import com.adapter.StoreProducts;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.widget.EndlessRecyclerViewScrollListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class StoreDetails extends BaseActivity{
    RecyclerView grid;
    RecyclerView rcv;

    ArrayList<JSONObject> listCat       = new ArrayList<>();
    ArrayList<JSONObject> listProd      = new ArrayList<>();
    ArrayList<String> pIds              = new ArrayList<>();
    ArrayList<String> cIds              = new ArrayList<>();

    TextView name , loc , distance , next;
    LinearLayout rating , linearLvLr;
    ImageView rat[];
    JSONObject jsonObject;
    LinearLayoutManager layoutManager;
    StaggeredGridLayoutManager stagLayoutManager;

    JSONObject jobj1 , jobj2;
    StoreCategories storeCat;
    StoreProducts storeProducts;
    boolean execCats , execProducts;
    ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storedetails);


        try
        {
            img                 = (ImageView)findViewById(R.id.img);
            rating              = (LinearLayout)findViewById(R.id.rating);
            linearLvLr          = (LinearLayout)findViewById(R.id.linearLvLr);
            name                = (TextView)findViewById(R.id.storename);
            next                = (TextView)findViewById(R.id.next);
            loc                 = (TextView)findViewById(R.id.location);
            distance            = (TextView)findViewById(R.id.distance);
            rat                 = getFunction().getRatingStars(rating);
            rcv                 = (RecyclerView) findViewById(R.id.rcv);
            grid                = (RecyclerView) findViewById(R.id.grid);

            jsonObject          = new JSONObject(getIntent().getExtras().getString("data"));

            getFunction().showRating(rat,jsonObject.getDouble("rating"));

            next.setText(getResources().getString(R.string.next_delivery)+" "+jsonObject.getString("next"));
            name.setText(jsonObject.getString("title"+getDefaultLang()));
            loc.setText(jsonObject.getString("loc"+getDefaultLang()));
            distance.setText(jsonObject.getString("distance")+" "+jsonObject.getString("dunit"));

            layoutManager       = new LinearLayoutManager(this);
            stagLayoutManager   = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);

            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rcv.setLayoutManager(layoutManager);
            grid.setLayoutManager(stagLayoutManager);

            Glide.with(this)
                    .load(jsonObject.getString("images"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(img) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            img.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                    });


            OverScrollDecoratorHelper.setUpOverScroll(grid,OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
            OverScrollDecoratorHelper.setUpOverScroll(rcv,OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);

            storeCat            = new StoreCategories(this,listCat,jsonObject);
            storeProducts       = new StoreProducts(this,listProd,jsonObject);

            grid.setAdapter(storeCat);
            rcv.setAdapter(storeProducts);
            getProducts();
            getCategories();

            linearLvLr.setVisibility(View.GONE);

            rcv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItem           = layoutManager.getItemCount();
                    int lastVisibleItem     = layoutManager.findLastVisibleItemPosition();

                    if (lastVisibleItem == totalItem - 1) {
                        getProducts();
                    }
                }
            });

            grid.setOnScrollListener(new EndlessRecyclerViewScrollListener(stagLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    getCategories();
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    void getProducts()
    {
        if(execProducts)
            return;

        execProducts = true;
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("pagenumber",listCat.size());
                    jobj.put("screenwidth",screenwidth);
                    jobj.put("screenheight",screenheight);
                    jobj.put("data",jsonObject+"");
                    jobj.put("myid",getSharePrefs().getUid());
                    jobj.put("caller","getStoreProducts");

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
                    ArrayList<JSONObject> tmp                       = new ArrayList<>();
                    JSONArray jArray                                = new JSONArray(s);
                    for(int i=0;i<jArray.length();i++)
                    {
                        try
                        {
                            jobj1 						            = jArray.getJSONObject(i);
                            tmp.add(jobj1);
                            if(!pIds.contains(jobj1.getString("id")))
                                pIds.add(jobj1.getString("id"));

                        }catch (Exception e){}

                    }

                    listProd.clear();
                    listProd.addAll(tmp);
                    rcv.getRecycledViewPool().clear();
                    storeProducts.notifyDataSetChanged();

                    if(listProd.size() > 0)
                        linearLvLr.setVisibility(View.VISIBLE);

                }catch (Exception e){
                    e.printStackTrace();
                }

                execProducts    = false;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void getCategories()
    {
        if(execCats)
            return;

        execCats = true;

        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("pagenumber",listCat.size());
                    jobj.put("screenwidth",screenwidth);
                    jobj.put("screenheight",screenheight);
                    jobj.put("data",jsonObject+"");
                    jobj.put("myid",getSharePrefs().getUid());
                    jobj.put("caller","getStoreCategories");

                    appLog("jobj :: 22 "+jobj);
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
                    ArrayList<JSONObject> tmp                       = new ArrayList<>();
                    JSONArray jArray                                = new JSONArray(s);
                    for(int i=0;i<jArray.length();i++)
                    {
                        try
                        {
                            jobj2 						            = jArray.getJSONObject(i);
                            tmp.add(jobj2);
                            if(!cIds.contains(jobj2.getString("id")))
                                cIds.add(jobj2.getString("id"));

                        }catch (Exception e){}

                    }

                    listCat.clear();
                    listCat.addAll(tmp);
                    grid.getRecycledViewPool().clear();
                    storeCat.notifyDataSetChanged();

                }catch (Exception e){
                    e.printStackTrace();
                }

                execCats        = false;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
