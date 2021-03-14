package com.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.tam.winati.ksa.StoreDetails;
import com.utils.Functions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.tam.winati.ksa.BaseActivity.appLog;


public class OffersAdapter extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    Activity context;
    Functions function;
    public OffersAdapter(Activity con, ArrayList<JSONObject> l)
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
        FotTextView name , data , uploadedOn;

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
                convertView 				= inflater.inflate(R.layout.row_offers, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.image			= (ImageView)convertView.findViewById(R.id.img);
                viewHolder.name			    = (FotTextView)convertView.findViewById(R.id.name);
                viewHolder.uploadedOn		= (FotTextView)convertView.findViewById(R.id.uploadedOn);
                viewHolder.data			    = (FotTextView)convertView.findViewById(R.id.data);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }



            final JSONObject jsonObject		= list.get(paramInt);

            final JSONObject storeJson      = new JSONArray(jsonObject.getString("store")).getJSONObject(0);

            final ImageView img             = viewHolder.image;


            viewHolder.name.setText(storeJson.getString("title"+BaseActivity.getDefLang(context)));
            viewHolder.data.setText(jsonObject.getString("data"));
            viewHolder.uploadedOn.setText(jsonObject.getString("dated"));

            Glide.with(context)
                    .load(storeJson.getString("images"))
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


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, StoreDetails.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("data",storeJson+"");
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