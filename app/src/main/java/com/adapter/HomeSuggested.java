package com.adapter;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;

import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.attr.name;
import static android.R.attr.thumb;
import static java.lang.System.load;


public class HomeSuggested extends RecyclerView.Adapter<HomeSuggested.CustomViewHolder>
{
	ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    BaseActivity context;
	public HomeSuggested(BaseActivity con, ArrayList<JSONObject> l)
	{
		context             = con;
		list                = l;
	}


    @Override
    public int getItemCount() {
        return list.size();
    }



    @Override
    public HomeSuggested.CustomViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v                  = inflater.inflate(R.layout.row_suggested, parent, false);

        CustomViewHolder vh     = new CustomViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        try
        {
            final JSONObject jsonObject         = list.get(position);
            holder.name.setText(jsonObject.getString("title"+context.getDefaultLang()));
            final CircularImageView img         = holder.image;
            Glide.with(context)
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
        }catch (Exception e){e.printStackTrace();}


    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircularImageView image;
        FotTextView name ;

        public CustomViewHolder(View convertView) {
            super(convertView);
            image			    = (CircularImageView)convertView.findViewById(R.id.img);
            name			    = (FotTextView)convertView.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v) {

        }
    }

}