package com.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.balram.library.FotButton;
import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.utils.ConnectionClass;
import com.utils.Constants;
import com.utils.Functions;
import com.utils.SharePrefsEntry;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.R.id.ordered_date;
import static com.tam.winati.ksa.R.id.price;
import static com.tam.winati.ksa.R.id.product;
import static com.tam.winati.ksa.R.id.status;
import static com.tam.winati.ksa.R.id.store;


public class OrdersAdapter extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    Activity context;
    Functions function;
    LayoutInflater inflater;
    long currentTime;
    String defaultLang;
    SharePrefsEntry sp;
    ConnectionClass cc;
    Dialog details;
    public OrdersAdapter(Activity con, ArrayList<JSONObject> l)
    {
        super(con, R.layout.row_stores);

        context             = con;
        list                = l;
        function            = new Functions(con);
        sp                  = new SharePrefsEntry(con);
        cc                  = new ConnectionClass(con);
        inflater 	        = context.getLayoutInflater();
        defaultLang         = BaseActivity.getDefLang(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }




    public class ViewHolderItem {
        RelativeLayout parentLr;
        FotTextView product,store,ordered_date,status,price ,qty;
        CircularImageView img;
        FotButton remind;

    }

    @Override
    public View getView(final int paramInt, View convertView, ViewGroup parent)
    {
        try
        {
            ViewHolderItem viewHolder;
            if(convertView==null)
            {
                convertView 				= inflater.inflate(R.layout.row_orders_single_row, parent, false);
                viewHolder 					= new ViewHolderItem();
                viewHolder.parentLr			= (RelativeLayout)convertView.findViewById(R.id.parentLr);
                viewHolder.product          = (FotTextView)convertView.findViewById(product);
                viewHolder.store            = (FotTextView)convertView.findViewById(store);
                viewHolder.price            = (FotTextView)convertView.findViewById(price);
                viewHolder.qty              = (FotTextView)convertView.findViewById(R.id.qty);
                viewHolder.ordered_date     = (FotTextView)convertView.findViewById(ordered_date);
                viewHolder.status           = (FotTextView)convertView.findViewById(status);
                viewHolder.remind           = (FotButton)convertView.findViewById(R.id.send_reminder);
                viewHolder.img              = (CircularImageView)convertView.findViewById(R.id.img);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					= (ViewHolderItem) convertView.getTag();
            }


            final RelativeLayout parentL    = viewHolder.parentLr;
            appLog("called");
            final JSONObject productObj     = list.get(paramInt);

            viewHolder.product.setText(productObj.getString("title"+defaultLang));
            viewHolder.store.setText(productObj.getString("store"+defaultLang)+" "+productObj.getString("location"+defaultLang));
            viewHolder.ordered_date.setText(productObj.getString("deliverytime"));
            viewHolder.price.setText(String.format("%.02f",Float.parseFloat(productObj.getString("price"))));
            viewHolder.qty.setText(productObj.getString("qty"));
            if(productObj.getString("deadlinecrossed").equals(Constants.DEADLINEOVER))
                viewHolder.ordered_date.setTextColor(Color.parseColor("#ff5555"));
            else
                viewHolder.ordered_date.setTextColor(Color.parseColor("#005500"));

            if(productObj.getString("status").equals(Constants.PLACED))
            {
                viewHolder.remind.setVisibility(View.VISIBLE);
                viewHolder.status.setText(context.getResources().getString(R.string.pending));
                viewHolder.status.setTextColor(Color.parseColor("#ff5555"));
            }
            else
            {
                viewHolder.remind.setVisibility(View.GONE);
                viewHolder.status.setText(context.getResources().getString(R.string.delivered));
                viewHolder.status.setTextColor(Color.parseColor("#005500"));
            }


            final CircularImageView img     = viewHolder.img;

            Glide.with(context)
                    .load(productObj.getString("images"))
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


            viewHolder.remind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendReminder(productObj);
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
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