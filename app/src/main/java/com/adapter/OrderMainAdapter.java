package com.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.balram.library.FotButton;
import com.balram.library.FotEditText;
import com.balram.library.FotTextView;
import com.github.ornolfr.ratingview.RatingView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.utils.ConnectionClass;
import com.utils.Constants;
import com.utils.Functions;
import com.utils.SharePrefsEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.ORDERS_RELOAD;


public class OrderMainAdapter extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    Activity context;
    Functions function;
    LayoutInflater inflater;
    long currentTime;
    String defaultLang;
    SharePrefsEntry sp;
    ConnectionClass cc;
    Dialog details, Return;
    int selectedIndex;
    Bitmap screen;

    public OrderMainAdapter(Activity con, ArrayList<JSONObject> l)
    {
        super(con, R.layout.row_order_main);

        context             = con;
        list                = l;
        function            = new Functions(con);
        sp                  = new SharePrefsEntry(con);
        cc                  = new ConnectionClass(con);
        inflater 	        = context.getLayoutInflater();
        defaultLang         = BaseActivity.getDefLang(context);

        details             = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
        details.setCanceledOnTouchOutside(false);
        details.setCancelable(true);
        details.setContentView(R.layout.dialog_order_details);
    }

    @Override
    public int getCount() {
        return list.size();
    }




    public class ViewHolderItem {
        FotTextView orderid,ordered_date,status,deldate ,qty , deltime , locationname , location;

    }

    @Override
    public View getView(final int paramInt, View convertView, ViewGroup parent)
    {
        try
        {
            ViewHolderItem viewHolder;
            if(convertView==null)
            {
                convertView 				= inflater.inflate(R.layout.row_order_main, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.orderid          = (FotTextView)convertView.findViewById(R.id.orderid);
                viewHolder.status           = (FotTextView)convertView.findViewById(R.id.status);
                viewHolder.ordered_date     = (FotTextView)convertView.findViewById(R.id.dated);
                viewHolder.qty              = (FotTextView)convertView.findViewById(R.id.itemcount);
                viewHolder.deldate          = (FotTextView)convertView.findViewById(R.id.deliverydate);
                viewHolder.deltime          = (FotTextView)convertView.findViewById(R.id.deliverytime);
                viewHolder.locationname     = (FotTextView)convertView.findViewById(R.id.locationname);
                viewHolder.location         = (FotTextView)convertView.findViewById(R.id.location);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }


            final JSONObject productObj     = list.get(paramInt);

            if(!productObj.getString("delid").equals("0"))
            {
                String timeSplitted[]           = productObj.getString("deliverytime").split(" ");
                viewHolder.deldate.setText(context.getString(R.string.order_delivery_date)+" "+timeSplitted[0]);
                viewHolder.deltime.setText(timeSplitted[1]);
                viewHolder.locationname.setText(Uri.decode(productObj.getString("add_name")));
                viewHolder.location.setText(Uri.decode(productObj.getString("del_address")));

                viewHolder.deltime.setVisibility(View.VISIBLE);
                viewHolder.deldate.setVisibility(View.VISIBLE);
                viewHolder.locationname.setVisibility(View.VISIBLE);
                viewHolder.location.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.deltime.setVisibility(View.GONE);
                viewHolder.deldate.setVisibility(View.GONE);
                viewHolder.locationname.setVisibility(View.GONE);
                viewHolder.location.setVisibility(View.GONE);
            }

//            Log.d("prod", productObj.toString());
            if (productObj.getString("del_type").equals("3")){
                viewHolder.deldate.setVisibility(View.VISIBLE);
                viewHolder.deltime.setVisibility(View.GONE);
                viewHolder.deldate.setText("Pickup");
            }else if (productObj.getString("del_type").equals("2")){
                viewHolder.deldate.setVisibility(View.VISIBLE);
                viewHolder.deltime.setVisibility(View.GONE);
                viewHolder.deldate.setText("Deliver Now");
            }else{
                viewHolder.deldate.setVisibility(View.VISIBLE);
                viewHolder.deltime.setVisibility(View.VISIBLE);
            }



            viewHolder.orderid.setText(productObj.getString("orderid"));
            viewHolder.ordered_date.setText(context.getString(R.string.order_ordered_date)+" "+productObj.getString("dated"));
            viewHolder.qty.setText(context.getString(R.string.order_qty)+" "+productObj.getString("itemcount"));


            setStatusMessage(productObj,viewHolder.status);


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOrderDetails(productObj,paramInt);
                }
            });


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


    void showOrderDetails(final JSONObject productObj,int in)
    {
        details.show();
        try
        {
            selectedIndex                = in;
            FotTextView orderid          = (FotTextView)details.findViewById(R.id.orderid);
            FotTextView status           = (FotTextView)details.findViewById(R.id.status);
            FotTextView ordered_date     = (FotTextView)details.findViewById(R.id.dated);
            FotTextView deltime          = (FotTextView)details.findViewById(R.id.deliverytime);
            FotTextView locationname     = (FotTextView)details.findViewById(R.id.locationname);

            FotTextView deliverycost     = (FotTextView)details.findViewById(R.id.deliverycost);
            FotTextView totcost          = (FotTextView)details.findViewById(R.id.totcost);

            FotTextView qty              = (FotTextView)details.findViewById(R.id.itemcount);
            FotTextView deldate          = (FotTextView)details.findViewById(R.id.deliverydate);
            FotButton location           = (FotButton)details.findViewById(R.id.location);
            FotButton callstore          = (FotButton)details.findViewById(R.id.callstore);
            FotButton cancelorder        = (FotButton)details.findViewById(R.id.cancelorder);
            final FotButton receiveorder        = (FotButton)details.findViewById(R.id.recieveOrder);
            FotTextView total            = (FotTextView)details.findViewById(R.id.total);
            ListView itemslv             = (ListView)details.findViewById(R.id.itemslv);
            RatingView ratingbar         = (RatingView)details.findViewById(R.id.ratingbar);
            FotButton returnOrder          = details.findViewById(R.id.returnOrder);
            FotButton resolved             = details.findViewById(R.id.solvedorder);
            RelativeLayout coupondiscount= details.findViewById(R.id.coupondiscount);
            FotTextView couponValue            = (FotTextView)details.findViewById(R.id.couponValue);
            coupondiscount.setVisibility(View.GONE);

            resolved.setVisibility(View.GONE);

            Return             = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
            Return.setCanceledOnTouchOutside(false);
            Return.setCancelable(true);
            Return.setContentView(R.layout.dialog_return);

            returnOrder.setVisibility(View.GONE);

            receiveorder.setVisibility(View.GONE);

            OrdersProductsAdapter oAdap  = new OrdersProductsAdapter(context,new JSONArray(productObj.getString("details")));
            itemslv.setAdapter(oAdap);

            float netTotal               = 0;
            netTotal = Float.parseFloat(productObj.getString("deliverycost")) + Float.parseFloat(productObj.getString("total"));
            if (Float.parseFloat(productObj.getString("coupon_discount")) > 0){
                netTotal = Float.parseFloat(productObj.getString("deliverycost")) + (Float.parseFloat(productObj.getString("total")) * ((100 - Float.parseFloat(productObj.getString("coupon_discount")))/100));
                coupondiscount.setVisibility(View.VISIBLE);
                couponValue.setText(productObj.getString("coupon_discount") + "%");
            }


            if(!productObj.getString("delid").equals("0"))
            {
                String timeSplitted[]           = productObj.getString("deliverytime").split(" ");
                deldate.setText(context.getString(R.string.order_delivery_date)+" "+timeSplitted[0]);
                deltime.setText(timeSplitted[1]);
                locationname.setText(Uri.decode(productObj.getString("add_name")));
                location.setText(Uri.decode(productObj.getString("del_address")));

                deldate.setVisibility(View.VISIBLE);
                deltime.setVisibility(View.VISIBLE);
                locationname.setVisibility(View.VISIBLE);
                location.setVisibility(View.VISIBLE);
            }
            else
            {
                deldate.setVisibility(View.GONE);
                deltime.setVisibility(View.GONE);
                locationname.setVisibility(View.GONE);
                location.setVisibility(View.GONE);
            }

            if (productObj.getString("del_type").equals("3")){
                deldate.setVisibility(View.VISIBLE);
                deltime.setVisibility(View.GONE);
                deldate.setText("Pickup");
            }else if (productObj.getString("del_type").equals("2")){
                deldate.setVisibility(View.VISIBLE);
                deltime.setVisibility(View.GONE);
                deldate.setText("Deliver Now");
            }else{
                deldate.setVisibility(View.VISIBLE);
                deltime.setVisibility(View.VISIBLE);
            }



            ratingbar.setRating(productObj.getInt("myrating"));
            totcost.setText(DEF_CURRENCY+netTotal);
            deliverycost.setText(DEF_CURRENCY+productObj.getString("deliverycost"));
            total.setText(DEF_CURRENCY+productObj.getString("total"));
            setStatusMessage(productObj,status);
            callstore.setText(productObj.getString("phonenumber"));
            orderid.setText(productObj.getString("orderid"));
            ordered_date.setText(context.getString(R.string.order_ordered_date)+" "+productObj.getString("dated"));
            qty.setText(context.getString(R.string.order_qty)+" "+productObj.getString("itemcount"));

            try
            {
                globalLog("ABC :: "+productObj.getString("status") +" -- "+ productObj.getInt("myrating"));
                if(productObj.getString("status").equals(Constants.CLOSED) && productObj.getInt("myrating") == 0)
                {
                    ratingbar.setVisibility(View.VISIBLE);
                    callstore.setVisibility(View.GONE);
                    cancelorder.setVisibility(View.GONE);

                    ratingbar.setOnRatingChangedListener(new RatingView.OnRatingChangedListener() {
                        @Override
                        public void onRatingChange(float oldRating, float newRating) {
                            if(newRating != oldRating)
                                changeRating(productObj,newRating);
                        }
                    });

                }
                else
                {
                    ratingbar.setVisibility(View.GONE);
                    callstore.setVisibility(View.VISIBLE);
                    cancelorder.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){
                ratingbar.setVisibility(View.GONE);
            }

            callstore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        Intent callIntent       = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + productObj.getString("phonenumber")));
                        context.startActivity(callIntent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("www.google.com").appendPath("maps").appendPath("dir").appendPath("").appendQueryParameter("api", "1")
                                .appendQueryParameter("origin", Double.parseDouble(productObj.getString("store_lati")) + "," + Double.parseDouble(productObj.getString("store_longi")))
                                .appendQueryParameter("destination", Double.parseDouble(productObj.getString("del_lati")) + "," + Double.parseDouble(productObj.getString("del_longi")) );
                        String url = builder.build().toString();
                        Log.d("Directions", url);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        //context.startActivity(i);

                        String uri = "http://maps.google.com/?q=" + Double.parseDouble(productObj.getString("del_lati")) + "," + Double.parseDouble(productObj.getString("del_longi"));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

            if(productObj.getString("status").equals(Constants.PLACED))
                cancelorder.setVisibility(View.VISIBLE);
            else
                cancelorder.setVisibility(View.GONE);

            if(productObj.getString("status").equals(Constants.DELIVERED))
                receiveorder.setVisibility(View.VISIBLE);
            else
                receiveorder.setVisibility(View.GONE);

            if(productObj.getString("status").equals("4")){
                returnOrder.setVisibility(View.VISIBLE);
            }
            else{
                returnOrder.setVisibility(View.GONE);
            }

            if (productObj.getString("status").equals("6"))
                resolved.setVisibility(View.VISIBLE);
            else
                resolved.setVisibility(View.GONE);

            resolved.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    details.dismiss();
                                    resolvedIssue(productObj);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Has Your Issue Been Resolved?").setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
                }
            });

            returnOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Return.show();
                    orderReturn(Return, productObj);
                }
            });

            receiveorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    details.dismiss();
                                    receiveOrder(productObj);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Have You Received The Order?").setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
                }
            });

            cancelorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    details.dismiss();
                                    cancelOrder(productObj);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getString(R.string.cancel_order)).setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
                }
            });

            details.show();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void resolvedIssue(final JSONObject productObj) {
        function.showDialog();
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("data",productObj+"");
                    jobj.put("myid",sp.getUid());
                    jobj.put("caller","resolvedOrder");
                    Log.d("jobj", jobj.toString());
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    res                     = cc.sendPostData(encrypted,null);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                function.dismissDialog();
                details.dismiss();
                try
                {
                    if(s != null)
                    {
                        JSONObject jsonObject                 = new JSONObject(s);
                        Toast.makeText(context,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                        context.recreate();
                    }
                    else
                        Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void orderReturn(final Dialog Return, final JSONObject productObj) {
        final FotEditText issue = Return.findViewById(R.id.Subject);
        final FotEditText details = Return.findViewById(R.id.returndetail);
        FotButton submit = Return.findViewById(R.id.submitReturn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Subject = issue.getText().toString();
                final String detail = details.getText().toString();

                if (Subject.equals("") || detail.equals("")){
                    Toast.makeText(context, "All Fields Are Mandatory", Toast.LENGTH_LONG).show();
                }
                else{
                    function.showDialog();
                    new AsyncTask<Void,Void,String>()
                    {
                        @Override
                        protected String doInBackground(Void... params) {
                            String res = null;
                            try
                            {
                                JSONObject jobj         = new JSONObject();
                                jobj.put("data",productObj+"");
                                jobj.put("myid",sp.getUid());
                                jobj.put("subject", Subject);
                                jobj.put("details", detail);
                                jobj.put("caller","returnOrder");
                                Log.d("jobj", jobj.toString());
                                String encrypted        = cc.getEncryptedString(jobj.toString());
                                res                     = cc.sendPostData(encrypted,null);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            return res;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            function.dismissDialog();
                            try
                            {
                                if(s != null)
                                {
                                    JSONObject jsonObject                 = new JSONObject(s);
                                    Toast.makeText(context,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                                    if(jsonObject.getString("success").equals("1"))
                                    {

                                    }
                                }
                                else
                                    Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                            }catch (Exception e){
                                Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                context.recreate();
                Return.dismiss();
            }
        });
    }

    private void receiveOrder(final JSONObject productObj) {
        function.showDialog();
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("data",productObj+"");
                    jobj.put("myid",sp.getUid());
                    jobj.put("caller","receivedOrder");
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    res                     = cc.sendPostData(encrypted,null);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                function.dismissDialog();
                try
                {
                    if(s != null)
                    {
                        JSONObject jsonObject                 = new JSONObject(s);
                        Toast.makeText(context,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                        if(jsonObject.getString("success").equals("1"))
                        {
                            Intent intent = new Intent(ORDERS_RELOAD);
                            intent.setAction(ORDERS_RELOAD);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    }
                    else
                        Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void cancelOrder(final  JSONObject jsonObject)
    {
        function.showDialog();
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("data",jsonObject+"");
                    jobj.put("myid",sp.getUid());
                    jobj.put("caller","`");
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    res                     = cc.sendPostData(encrypted,null);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                function.dismissDialog();
                try
                {
                    if(s != null)
                    {
                        JSONObject jsonObject                 = new JSONObject(s);
                        Toast.makeText(context,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                        if(jsonObject.getString("success").equals("1"))
                        {
                            Intent intent = new Intent(ORDERS_RELOAD);
                            intent.setAction(ORDERS_RELOAD);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    }
                    else
                        Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    void setStatusMessage(JSONObject productObj,TextView status)
    {
        try
        {
            if(productObj.getString("status").equals(Constants.PLACED))
            {
                status.setText(context.getResources().getString(R.string.order_placed));
                status.setTextColor(Color.parseColor("#c6b92a"));
            }
            else if(productObj.getString("status").equals(Constants.ACCEPTED))
            {
                status.setText(context.getResources().getString(R.string.order_accepted));
                status.setTextColor(Color.parseColor("#005500"));
            }
            else if(productObj.getString("status").equals(Constants.READY))
            {
                status.setText(context.getResources().getString(R.string.order_ready));
                status.setTextColor(Color.parseColor("#000055"));
            }
            else if(productObj.getString("status").equals(Constants.DELIVERED))
            {
                status.setText(context.getResources().getString(R.string.order_delivered));
                status.setTextColor(Color.parseColor("#005500"));
            }
            else if(productObj.getString("status").equals(Constants.CLOSED))
            {
                status.setText(context.getResources().getString(R.string.order_closed));
                status.setTextColor(Color.parseColor("#005500"));
            }
            else if(productObj.getString("status").equals(Constants.CANCELLED))
            {
                status.setText(context.getResources().getString(R.string.order_cancelled));
                status.setTextColor(Color.parseColor("#ff5555"));
            }
            else if (productObj.getString("status").equals("6")){
                status.setText("Return Pending");
                status.setTextColor(Color.parseColor("#ff5555"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    void changeRating(final  JSONObject productObj,final float ratingValue)
    {
        final Dialog rating;
        rating             = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
        rating.setCanceledOnTouchOutside(false);
        rating.setCancelable(true);
        rating.setContentView(R.layout.dialog_comments);
        FotButton submit = rating.findViewById(R.id.submitRating);
        final FotEditText review = rating.findViewById(R.id.reviewText);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                function.showDialog();
                final String rvw = review.getText().toString();
                new AsyncTask<Void,Void,String>()
                {
                    @Override
                    protected String doInBackground(Void... params) {
                        String res = null;
                        try
                        {
                            JSONObject jobj         = new JSONObject();
                            jobj.put("data",productObj+"");
                            jobj.put("myid",sp.getUid());
                            jobj.put("rating",ratingValue);
                            jobj.put("review", rvw);
                            jobj.put("caller","rateStore");
                            String encrypted        = cc.getEncryptedString(jobj.toString());
                            res                     = cc.sendPostData(encrypted,null);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        return res;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        function.dismissDialog();
                        try
                        {
                            if(s != null)
                            {
                                JSONObject jobj                 = new JSONObject(s);
                                Toast.makeText(context,jobj.getString("message"),Toast.LENGTH_SHORT).show();
                                if(jobj.getString("success").equals("1"))
                                {
                                    try{details.dismiss();}catch (Exception e3){}
                                    productObj.put("myrating",ratingValue);
                                    list.set(selectedIndex,productObj);
                                    notifyDataSetChanged();

                                }
                            }
                            else
                                Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                        }catch (Exception e){
                            Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                        }
                        rating.dismiss();
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        rating.show();
    }

    void sendReminder(final JSONObject jobj)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        remindFunction(jobj);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_you_sure)).setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
    }

    void remindFunction(final JSONObject jsonObject)
    {
        function.showDialog();
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                String res = null;
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("data",jsonObject+"");
                    jobj.put("myid",sp.getUid());
                    jobj.put("caller","sendreminder");
                    jobj.put("lang",BaseActivity.getDefLang(context));
                    appLog("jobj :: "+jobj);
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    res                     = cc.sendPostData(encrypted,null);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                function.dismissDialog();
                try
                {
                    if(s != null)
                    {
                        JSONObject jsonObject                 = new JSONObject(s);
                        Toast.makeText(context,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

//    SimpleDateFormat simpleDateFormat   = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//    Date date                           = simpleDateFormat.parse(productObj.getString("deliverytime"));
//    long startDt                        = date.getTime();
}