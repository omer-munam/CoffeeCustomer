package com.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ViewProduct;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.thumb;
import static com.tam.winati.ksa.BaseActivity.appLog;


public class HomeCat extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list       = new ArrayList<JSONObject>();

    BaseActivity context;
    public HomeCat(BaseActivity con, ArrayList<JSONObject> l)
    {
        super(con, R.layout.row_categories);

        context             = con;
        list                = l;

    }

    @Override
    public int getCount() {
        return list.size();
    }




    public class ViewHolderItem {
        ImageView image;
        FotTextView name ;

    }

    @Override
    public View getView(final int paramInt, View convertView, ViewGroup parent)
    {
        try
        {
            ViewHolderItem viewHolder;
            if(convertView==null)
            {
                LayoutInflater inflater 	= context.getLayoutInflater();
                convertView 				= inflater.inflate(R.layout.row_categories, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.image			= (ImageView)convertView.findViewById(R.id.img);
                viewHolder.name			    = (FotTextView)convertView.findViewById(R.id.name);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }



            final JSONObject jsonObject		= list.get(paramInt);
            final ImageView img             = viewHolder.image;

            viewHolder.name.setText(jsonObject.getString("title"+context.getDefaultLang()));

            Glide.with(context)
                    .load(jsonObject.getString("images"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(viewHolder.image) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            img.setImageDrawable(resource);
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






}