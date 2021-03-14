package com.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.widget.RoundedImg;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.attr.tag;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.tam.winati.ksa.R.id.img;
import static com.utils.Constants.PRODUCT_PRESENT;
import static com.utils.Constants.SUBCAT_PRESENT;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CustomViewHolder>
{
	ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    BaseActivity context;
    int selectedIndex;
    public static View allView;
	public CategoryAdapter(BaseActivity con, ArrayList<JSONObject> l)
	{
        selectedIndex       = 1;
		context             = con;
		list                = l;
	}


    @Override
    public int getItemCount() {
        return list.size();
    }



    @Override
    public CategoryAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v                  = inflater.inflate(R.layout.row_cat, parent, false);

        CustomViewHolder vh     = new CustomViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        try
        {
            final JSONObject jsonObject		= list.get(position);

            holder.name.setText(jsonObject.getString("title"+context.getDefaultLang()));
            globalLog("catadapter :: "+jsonObject);
            final JSONArray subCatList      = new JSONArray(jsonObject.getString("subcat"));
            final int catidvalue            = Integer.parseInt(jsonObject.getString("id"));

            if(catidvalue == -1)
            {
                holder.fav_added.setVisibility(View.VISIBLE);
                holder.img_name.setVisibility(View.GONE);
            }
            else
            {
                holder.fav_added.setVisibility(View.GONE);
                holder.img_name.setVisibility(View.VISIBLE);

                try
                {
                    holder.img.setVisibility(View.VISIBLE);
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
                }catch (Exception e){
                    holder.img.setImageResource(R.drawable.logo);
                }

            }


            if(selectedIndex == position)
            {
                holder.name.setTextColor(Color.WHITE);
                holder.fav_added.setColorFilter(Color.WHITE);
                holder.parent.setBackgroundResource(R.drawable.rounded_category_selected);
            }
            else
            {
                holder.name.setTextColor(context.getResources().getColor(R.color.orange));
                holder.fav_added.setColorFilter(context.getResources().getColor(R.color.orange));
                holder.parent.setBackgroundResource(R.drawable.rounded_category_unselected);
            }


            if(position == 1)
                allView     = holder.parent;

            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedIndex                       = position;
                    notifyDataSetChanged();
                    SubCategoryAdapter.selectedIndex    = -1;
                    SubCategoryAdapter.thisAdapter.notifyDataSetChanged();

                    if(subCatList.length() == 0)
                    {

                    }

                    Intent intent2 = new Intent(PRODUCT_PRESENT);
                    intent2.setAction(PRODUCT_PRESENT);
                    intent2.putExtra("cat",jsonObject.toString());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);

                    Intent intent = new Intent(SUBCAT_PRESENT);
                    intent.setAction(SUBCAT_PRESENT);
                    intent.putExtra("data",subCatList.toString());
                    intent.putExtra("cat",jsonObject.toString());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);




                }
            });

        }catch (Exception e){e.printStackTrace();}


    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        FotTextView name ;
        View parent;
        ImageView fav_added;
        LinearLayout img_name;
        RoundedImg img;



        public CustomViewHolder(View convertView) {
            super(convertView);
            name        = (FotTextView) convertView.findViewById(R.id.name);
            fav_added   = (ImageView)convertView.findViewById(R.id.fav_added);
            img         = (RoundedImg)convertView.findViewById(R.id.img);
            img_name    = (LinearLayout)convertView.findViewById(R.id.img_name);
            parent      = (View) convertView.findViewById(R.id.parent);
        }


    }
}