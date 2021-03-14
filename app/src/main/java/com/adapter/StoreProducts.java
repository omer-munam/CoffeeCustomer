package com.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.StoreDetails;
import com.tam.winati.ksa.ViewProduct;

import org.json.JSONObject;

import java.util.ArrayList;


public class StoreProducts extends RecyclerView.Adapter<StoreProducts.CustomViewHolder>
{
	ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    BaseActivity context;
    JSONObject storeDetails;
	public StoreProducts(BaseActivity con, ArrayList<JSONObject> l,JSONObject sd)
	{
		context             = con;
		list                = l;
        storeDetails        = sd;
	}


    @Override
    public int getItemCount() {
        return list.size();
    }



    @Override
    public StoreProducts.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,ViewProduct.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("data",jsonObject+"");
                    intent.putExtra("store",storeDetails.toString());
                    context.startActivity(intent);
                }
            });
        }catch (Exception e){e.printStackTrace();}


    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        CircularImageView image;
        FotTextView name ;
        RelativeLayout parent;

        public CustomViewHolder(View convertView) {
            super(convertView);
            image			    = (CircularImageView)convertView.findViewById(R.id.img);
            name			    = (FotTextView)convertView.findViewById(R.id.name);
            parent			    = (RelativeLayout)convertView.findViewById(R.id.parent);
        }

    }

}