package com.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.SpecificStore;
import com.tam.winati.ksa.StoreDetails;
import com.utils.Functions;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.tam.winati.ksa.BaseActivity.appLog;


public class StoresAdapter extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    Activity context;
    Functions function;
    public StoresAdapter(Activity con, ArrayList<JSONObject> l)
    {
        super(con, R.layout.row_stores);

        context             = con;
        list                = l;
        function            = new Functions(con);
    }

    @Override
    public int getCount() {
        return list.size();
    }




    public class ViewHolderItem {
        ImageView image;
        FotTextView name , distance , nameloc , next;
        FotTextView availability;
        ImageView rating[]  = new ImageView[5];

    }

    @Override
    public View getView(final int paramInt, View convertView, ViewGroup parent)
    {
        try
        {
            final ViewHolderItem viewHolder;
            if(convertView==null)
            {
                LayoutInflater inflater 	= context.getLayoutInflater();
                convertView 				= inflater.inflate(R.layout.row_stores, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.image			= (ImageView)convertView.findViewById(R.id.img);
                viewHolder.name			    = (FotTextView)convertView.findViewById(R.id.name);
                viewHolder.next			    = (FotTextView)convertView.findViewById(R.id.mext);
                viewHolder.nameloc			= (FotTextView)convertView.findViewById(R.id.nameloc);
                viewHolder.distance			= (FotTextView)convertView.findViewById(R.id.distance);
                viewHolder.availability     = (FotTextView) convertView.findViewById(R.id.availability);
                viewHolder.rating           = function.getRatingStars(convertView);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }



            final JSONObject jsonObject		= list.get(paramInt);

            appLog("jsonobj :: "+jsonObject);
            final ImageView img             = viewHolder.image;
            final ImageView rat[]           = viewHolder.rating;
            double rate                     = jsonObject.getDouble("rating");
            function.showRating(rat,rate);

            if(jsonObject.getString("status").equals("1"))
            {
                viewHolder.availability.setText(context.getString(R.string.available));
                viewHolder.availability.setTextColor(Color.parseColor("#3e9e39"));
            }
            else
            {
                viewHolder.availability.setText(context.getString(R.string.not_available));
                viewHolder.availability.setTextColor(Color.parseColor("#ff5555"));
            }



            viewHolder.next.setText(context.getResources().getString(R.string.next_delivery)+" "+jsonObject.getString("next"));
            viewHolder.name.setText(jsonObject.getString("title"+BaseActivity.getDefLang(context)));
            viewHolder.nameloc.setText(jsonObject.getString("loc"+BaseActivity.getDefLang(context)));
            viewHolder.distance.setText(jsonObject.getString("distance")+" "+jsonObject.getString("dunit"));
//            Log.d("orsers", "begin loading "+  jsonObject.getString("slimage"));
            Glide.with(context)
                    .load(jsonObject.getString("images"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(viewHolder.image) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
//                            Log.d("orsers", "loaded");
                            viewHolder.image.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                            Log.d("orsers", "Failed");
                        }
                    });


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SpecificStore.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("data",jsonObject+"");
                    BaseActivity.callActivity(context,intent);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


}