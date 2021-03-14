package com.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.balram.library.FotTextView;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ViewCart;
import com.utils.Functions;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.fragmentManager;


public class LocationList extends ArrayAdapter<Date>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();
    BaseActivity context;
    Functions function;
    SimpleDateFormat    def;
    public LocationList(BaseActivity con, ArrayList<JSONObject> locations)
    {
        super(con, R.layout.row_stores);

        context             = con;
        list                = locations;
        function            = new Functions(con);
        def                 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public int getCount() {
        return list.size();
    }




    public class ViewHolderItem {
        FotTextView tming;

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
                convertView 				= inflater.inflate(R.layout.row_llocation, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.tming			= (FotTextView)convertView.findViewById(R.id.tming);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }



            final JSONObject jobj		    = list.get(paramInt);

            if(jobj.getString("address").equals("-1"))
            {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            context.latitude = 255;
                            context.latitude = 255;
                            context.openLocation();
                        }catch (Exception e2){
                            e2.printStackTrace();
                        }
                    }
                });

                viewHolder.tming.setText(context.getResources().getString(R.string.select_new_location));
                viewHolder.tming.setTextColor(Color.parseColor("#333333"));

            }
            else
            {
                viewHolder.tming.setText(jobj.getString("address"));
                viewHolder.tming.setTextColor(Color.parseColor("#333333"));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{BaseActivity.map_address_txt.setText(jobj.getString("address"));}catch (Exception e){}
                        try{ViewCart.rlLocation.setVisibility(View.GONE);}catch (Exception e){}
                        try{ViewCart.map_address_txt.setVisibility(View.VISIBLE);}catch (Exception e){}
                        try{context.addressJSON = jobj;}catch (Exception e2){}

                    }
                });
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

}