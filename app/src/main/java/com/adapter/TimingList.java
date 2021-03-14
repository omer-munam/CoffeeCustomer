package com.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.balram.library.FotTextView;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ShowStores;
import com.tam.winati.ksa.SpecificStore;
import com.tam.winati.ksa.ViewCart;
import com.utils.Constants;
import com.utils.Functions;
import com.utils.SharePrefsEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.fragmentManager;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.SharePrefsEntry.LAST_LOCATION;


public class TimingList extends ArrayAdapter<Date>
{
    ArrayList<Date> allDateList = new ArrayList<Date>();
    ArrayList<String> costDataList = new ArrayList<String>();
    Activity context;
    private final SharePrefsEntry sp;
    Functions function;
    SimpleDateFormat    def;
    String storeid , locid;
    String delTimeTxt;
    public TimingList(Activity con, ArrayList<Date> dateList,String storeID , String locID, ArrayList<String> costList)
    {
        super(con, R.layout.row_stores);

        context             = con;
        allDateList         = dateList;
        costDataList        = costList;
        function            = new Functions(con);
        Locale locale       = Locale.ENGLISH;
        def                 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",locale);
        delTimeTxt          = "";//context.getResources().getString(R.string.order_delivery_date);

        sp                  = new SharePrefsEntry(context);
        storeid             = storeID;
        locid               = locID;
    }

    @Override
    public int getCount() {
        return allDateList.size();
    }




    public class ViewHolderItem {
        FotTextView dated,deltime,cost;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        try
        {
            ViewHolderItem viewHolder;
            if(convertView==null)
            {
                LayoutInflater inflater 	= context.getLayoutInflater();
                convertView 				= inflater.inflate(R.layout.row_timing, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.dated			= (FotTextView)convertView.findViewById(R.id.deliverydate);
                viewHolder.deltime			= (FotTextView)convertView.findViewById(R.id.deliverytime);
                viewHolder.cost			    = (FotTextView)convertView.findViewById(R.id.cost);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }



            final Date dated		        = allDateList.get(position);
            if(dated != null)
            {
                final String formattedDate  = def.format(dated.getTime());
                String splittedTime[]       = formattedDate.split(" ");
                viewHolder.dated.setText(splittedTime[0]);
                viewHolder.deltime.setText(splittedTime[1]);


                String cst  = costDataList.get(position).trim();
                try
                {
                    float f2 = Float.parseFloat(cst);
                    if(f2 == 0)
                        cst     = context.getString(R.string.free);
                    else
                        cst     = DEF_CURRENCY+" "+ costDataList.get(position);
                }catch (Exception e){cst = DEF_CURRENCY+" "+ costDataList.get(position);}



                final String costF = cst;
                viewHolder.cost.setText(cst);
                viewHolder.cost.setCompoundDrawablesWithIntrinsicBounds( R.drawable.del_icon, 0, 0, 0);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{ViewCart.timingDialog.dismiss();}catch (Exception e){}
                        ViewCart.deliveryTime.put(function.generateCartKey(storeid ,locid),formattedDate);
                        try
                        {
                            String delTime     = ViewCart.deliveryTime.get(function.generateCartKey(storeid ,locid));
                            String splittedTime[]       = delTime.split(" ");
                            ViewCart.deliverytmtxt.setText(splittedTime[1]);
                            ViewCart.price_dl.setText(costF);
//                            ViewCart.delivery_cst.setText(costF);
                            if(sp.getLanguage().equals("ar"))
                                ViewCart.deliverytimetxt.setText(splittedTime[0]+delTimeTxt);
                            else
                                ViewCart.deliverytimetxt.setText(delTimeTxt+splittedTime[0]);
                        }catch (Exception ee){
                            ee.printStackTrace();
                        }
                        globalLog("dated :: "+formattedDate);
                        ViewCart.delivery_Cost  = Float.parseFloat(costDataList.get(position));
                        ViewCart.delivery_Time  = formattedDate;
                        function.loadQty(SpecificStore.STOREID,SpecificStore.LOCID);
                    }
                });
            }
            else
            {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{ViewCart.timingDialog.dismiss();}catch (Exception e){}
                        showDate();
                    }
                });

                viewHolder.cost.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, 0);
                viewHolder.dated.setText(context.getResources().getString(R.string.select_custom_date));
                viewHolder.dated.setTextColor(Color.parseColor("#555555"));
                viewHolder.cost.setText("");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


    void showDate()
    {
        final SwitchDateTimeDialogFragment dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                context.getString(R.string.select_custom_date),
                "OK",
                "Cancel"
        );

        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setMinimumDateTime(allDateList.get(0));
        dateTimeFragment.setDefaultDateTime(allDateList.get(0));

        try
        {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            appLog("dateexception :: "+e.getMessage());
        }

        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                String formattedDate    = def.format(date.getTime());
                ViewCart.deliveryTime.put(function.generateCartKey(storeid ,locid),formattedDate);
                try{dateTimeFragment.dismiss();}catch (Exception e){}
                try{String delTime     = ViewCart.deliveryTime.get(function.generateCartKey(storeid ,locid));}catch (Exception e){}

                try
                {
                    String splittedTime[]       = formattedDate.split(" ");
                    ViewCart.deliverytmtxt.setText(splittedTime[1]);
                    if(sp.getLanguage().equals("ar"))
                        ViewCart.deliverytimetxt.setText(splittedTime[0]+delTimeTxt);
                    else
                        ViewCart.deliverytimetxt.setText(delTimeTxt+splittedTime[0]);
                }catch (Exception ee){
                    ee.printStackTrace();
                }

            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Date is get on negative button click
            }
        });

        dateTimeFragment.show(fragmentManager, "dialog_time");
    }


}