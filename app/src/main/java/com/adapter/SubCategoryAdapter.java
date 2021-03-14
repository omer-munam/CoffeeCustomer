package com.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.balram.library.FotTextView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ViewCart;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.utils.Constants.PRODUCT_PRESENT;
import static com.utils.Constants.SUBCAT_PRESENT;


public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.CustomViewHolder>
{
	ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    BaseActivity context;
    public static int selectedIndex;
    public static SubCategoryAdapter thisAdapter;
	public SubCategoryAdapter(BaseActivity con, ArrayList<JSONObject> l)
	{
        selectedIndex       = -1;
		context             = con;
		list                = l;
        thisAdapter         = this;
	}


    @Override
    public int getItemCount() {
        return list.size();
    }



    @Override
    public SubCategoryAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v                  = inflater.inflate(R.layout.row_catsub, parent, false);

        CustomViewHolder vh     = new CustomViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        try
        {
            final JSONObject jsonObject		= list.get(position);

            if(selectedIndex == position)
            {
                holder.name.setTextColor(Color.WHITE);
                holder.parent.setBackgroundResource(R.drawable.rounded_cat_selected);
            }

            else
            {
                holder.parent.setBackgroundResource(R.drawable.rounded_cat_unselected);
                holder.name.setTextColor(context.getResources().getColor(R.color.orange));
            }

            holder.name.setText(jsonObject.getString("title"+context.getDefaultLang()));

            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appLog("show onClick");
                    selectedIndex = position;
                    notifyDataSetChanged();
                    Intent intent = new Intent(PRODUCT_PRESENT);
                    intent.setAction(PRODUCT_PRESENT);
                    intent.putExtra("subcat",jsonObject.toString());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });

        }catch (Exception e){e.printStackTrace();}


    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        FotTextView name ;
        View parent;


        public CustomViewHolder(View convertView) {
            super(convertView);
            name = (FotTextView) convertView.findViewById(R.id.name);
            parent = (View) convertView.findViewById(R.id.parent);
        }


    }
}