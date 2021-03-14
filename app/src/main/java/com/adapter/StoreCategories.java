package com.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ViewProduct;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.tam.winati.ksa.BaseActivity.appLog;


public class StoreCategories extends RecyclerView.Adapter<StoreCategories.CustomViewHolder>
{
	ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    BaseActivity context;
    Dialog dialog;
    ListView dialogLv;
    ArrayList<JSONObject>    subcatList = new ArrayList<JSONObject>();
    SubCategory subcatAdapter;
    JSONObject storeDetails;
	public StoreCategories(BaseActivity con, ArrayList<JSONObject> l,JSONObject sd)
	{
		context             = con;
		list                = l;
        storeDetails        = sd;
        dialog              = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_select_category);
        dialogLv            = (ListView)dialog.findViewById(R.id.select_cat);
        subcatAdapter       = new SubCategory(context,subcatList);
        dialogLv.setAdapter(subcatAdapter);
	}


    @Override
    public int getItemCount() {
        return list.size();
    }



    @Override
    public StoreCategories.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v                  = inflater.inflate(R.layout.row_categories, parent, false);

        CustomViewHolder vh     = new CustomViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        try
        {
            final JSONObject jsonObject		= list.get(position);
            final ImageView img             = holder.image;

            holder.name.setText(jsonObject.getString("title"+context.getDefaultLang()));

            Glide.with(context)
                    .load(jsonObject.getString("images"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(holder.image) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            img.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                    });

            final JSONArray subCatList      = new JSONArray(jsonObject.getString("subcat"));


            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appLog("show onClick");
                    if(subCatList.length() > 0)
                    {
                        showSubCat(subCatList);
                    }
                    else
                    {
                        Intent intent = new Intent(context,ViewProduct.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("data",jsonObject+"");
                        intent.putExtra("store",storeDetails.toString());
                        context.startActivity(intent);
                    }
                }
            });

        }catch (Exception e){e.printStackTrace();}


    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        FotTextView name ;
        RelativeLayout parent;


        public CustomViewHolder(View convertView) {
            super(convertView);
            image = (ImageView) convertView.findViewById(R.id.img);
            name = (FotTextView) convertView.findViewById(R.id.name);
            parent = (RelativeLayout) convertView.findViewById(R.id.parent);
        }


    }

    void showSubCat(final JSONArray jarray)
    {
        try
        {

            appLog("show dialog");
            dialog.show();
            subcatList.clear();
            for(int i=0;i<jarray.length();i++)
            {
                subcatList.add(jarray.getJSONObject(i));
            }

            subcatAdapter.notifyDataSetChanged();

            dialogLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try
                    {
                        dialog.dismiss();
                        Intent intent = new Intent(context,ViewProduct.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("data",jarray.getJSONObject(position)+"");
                        intent.putExtra("store",storeDetails.toString());
                        context.startActivity(intent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

            appLog("show dialog end");
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}