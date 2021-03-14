package com.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.SpecificStore;
import com.utils.ConnectionClass;
import com.utils.Constants;
import com.utils.Functions;
import com.utils.SharePrefsEntry;
import com.widget.RoundedImg;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.R.attr.key;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.PRODUCT_PRESENT;
import static com.utils.Constants.PRODUCT_RELOAD;
import static com.utils.Constants.SUBCAT_PRESENT;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.CustomViewHolder>
{
	ArrayList<JSONObject>    list = new ArrayList<JSONObject>();
    DecimalFormat form            = new DecimalFormat("0.00");
    BaseActivity context;
    Functions function;
    SharePrefsEntry sp;
    ConnectionClass cc;
    int isEnabled;
	public ProductAdapter(BaseActivity con, ArrayList<JSONObject> l,int ie)
	{
		context             = con;
		list                = l;
        isEnabled           = ie;
        function            = new Functions(context);
        cc                  = new ConnectionClass(context);
        sp                  = new SharePrefsEntry(context);
	}


    @Override
    public int getItemCount() {
        return list.size();
    }



    @Override
    public ProductAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v                  = inflater.inflate(R.layout.row_product, parent, false);

        CustomViewHolder vh     = new CustomViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        try
        {
            final JSONObject jsonObject		= list.get(position);

            final String storeid            = jsonObject.getString("storeid");
            final String locid              = jsonObject.getString("locid");
            final String catid              = jsonObject.getString("catid");
            final String subcatid           = jsonObject.getString("subcatid");
            final String key                = jsonObject.getString("key");


            globalLog("productjson :: "+jsonObject);
            holder.favimg.setVisibility(View.VISIBLE);
            if(jsonObject.getInt("currentfav") == 1)
            {
                holder.favimg.setColorFilter(context.getResources().getColor(R.color.green));
                holder.favimg.setImageResource(R.drawable.fav_added);
            }
            else
            {
                holder.favimg.setColorFilter(context.getResources().getColor(R.color.red));
                holder.favimg.setImageResource(R.drawable.fav_not_added_yet);
            }
//            Log.d("orsers", jsonObject.toString());
//            holder.productName.setVisibility(View.VISIBLE);
            holder.name.setText(jsonObject.getString("title"+context.getDefaultLang()));

            holder.price.setPaintFlags(0);
            float disc                  = Float.parseFloat(jsonObject.getString("discount_price"));
            float prc                   = Float.parseFloat(jsonObject.getString("price"));
            if(disc > 0 && prc > disc)
            {
                holder.discounted.setVisibility(View.VISIBLE);
                holder.discounted.setTextColor(context.getResources().getColor(R.color.green));
                holder.discounted.setText(DEF_CURRENCY+form.format(disc));
                holder.productName.setText(jsonObject.getString("title"+context.getDefaultLang()));
                holder.price.setTextColor(context.getResources().getColor(R.color.red));
                holder.price.setText(DEF_CURRENCY+form.format(prc));
                holder.price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

            }
            else
            {
                holder.discounted.setVisibility(View.GONE);
                holder.productName.setText(jsonObject.getString("title"+context.getDefaultLang()));
                holder.price.setTextColor(context.getResources().getColor(R.color.green));
                holder.price.setText(DEF_CURRENCY+form.format(prc));
            }

            int cQty                                = 0;
            try{cQty                                = Constants.itemqty.get(key);}catch (Exception e){}
            final int currentQty                    = cQty;
            holder.qty.setText(String.valueOf(currentQty));
            if(currentQty > 0)
                Constants.itemMap.put(key,jsonObject);

            if(isEnabled == 1)
            {
                holder.increase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        function.changeQty(storeid,locid,catid,subcatid,holder.qty,jsonObject,true);
                    }
                });

                holder.decrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        function.changeQty(storeid,locid,catid,subcatid,holder.qty,jsonObject,false);

                    }
                });
            }
            else
                holder.addProduct.setVisibility(View.GONE);




            if(Constants.itemqty.get(key) == null)
                Constants.itemqty.put(key,0);

            if(Constants.itemqty.get(key) > 0)
                holder.qty.setTextColor(context.getResources().getColor(R.color.green));
            else
                holder.qty.setTextColor(context.getResources().getColor(R.color.txt));

            holder.qty.setText(String.valueOf(Constants.itemqty.get(key)));

            Glide.with(context)
                    .load(jsonObject.getString("images"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(holder.img) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            holder.img.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                    });


            holder.favimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFav(holder.favimg,jsonObject,position);
                }
            });


        }catch (Exception e){e.printStackTrace();}


    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        FotTextView name , price , discounted, productName ;
        RoundedImg img;
        RelativeLayout parent;
        ImageView increase,decrease;
        EditText qty;
        ImageView favimg;
        RelativeLayout imglr;
        LinearLayout addProduct;


        public CustomViewHolder(View convertView) {
            super(convertView);
            name            = (FotTextView) convertView.findViewById(R.id.name);
            productName     = (FotTextView) convertView.findViewById(R.id.NameProduct);
            price           = (FotTextView) convertView.findViewById(R.id.price);
            discounted      = (FotTextView) convertView.findViewById(R.id.discounted);
            img             = (RoundedImg) convertView.findViewById(R.id.img);
            increase		= (ImageView)convertView.findViewById(R.id.increase);
            decrease		= (ImageView)convertView.findViewById(R.id.decrease);
            qty			    = (EditText) convertView.findViewById(R.id.qty);
            parent          = (RelativeLayout) convertView.findViewById(R.id.parent);
            favimg			= (ImageView)convertView.findViewById(R.id.favimg);
            imglr		    = (RelativeLayout) convertView.findViewById(R.id.imglr);
            addProduct		= (LinearLayout) convertView.findViewById(R.id.addProductlr);

        }


    }


    void addToFav(final ImageView favImg,final JSONObject jsonObject,final int paramInt)
    {

        if(!SpecificStore.categoriesLoaded)
            return;


        function.showDialog();
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("data",jsonObject+"");
                    jobj.put("key",jsonObject.getString("key")+"");
                    jobj.put("myid",sp.getUid());
                    jobj.put("lang",BaseActivity.getDefLang(context));
                    jobj.put("caller","addToFav");
                    appLog("jobj :: "+jobj);
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    res                     = cc.sendPostData(encrypted,null);
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
                    function.dismissDialog();
                    if(s != null)
                    {
                        JSONObject jsonObject                 = new JSONObject(s);
                        if(jsonObject.getInt("currentfav") == 1)
                            favImg.setImageResource(R.drawable.fav_added);
                        else
                            favImg.setImageResource(R.drawable.fav_not_added_yet);

                        JSONObject jsonObject1               = list.get(paramInt);
                        jsonObject1.put("currentfav",jsonObject.getInt("currentfav"));
                        list.set(paramInt,jsonObject1);
                        notifyDataSetChanged();
                        SpecificStore.needFavLoad = true;
                        Intent intent2 = new Intent(PRODUCT_RELOAD);
                        intent2.setAction(PRODUCT_RELOAD);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                    }
                    else
                    {

                        Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();
                    }


                }catch (Exception e){
                    function.dismissDialog();
                    Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}