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

import org.json.JSONObject;

import java.util.ArrayList;

import static com.tam.winati.ksa.R.id.img;


public class SubCategory extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    Activity context;
    Functions function;
    public SubCategory(Activity con, ArrayList<JSONObject> l)
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
        FotTextView subcat , cat;

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
                convertView 				= inflater.inflate(R.layout.row_subcat, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.subcat			= (FotTextView)convertView.findViewById(R.id.subcatname);
                viewHolder.cat			    = (FotTextView)convertView.findViewById(R.id.cat);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }



            final JSONObject jsonObject		= list.get(paramInt);

            viewHolder.subcat.setText(jsonObject.getString("title"+BaseActivity.getDefLang(context)));
            viewHolder.cat.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


}