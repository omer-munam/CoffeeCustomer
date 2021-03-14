package com.utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.SpecificStore;
import com.tam.winati.ksa.ViewCart;
import com.tam.winati.ksa.ViewProduct;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static android.R.attr.format;
import static android.R.attr.key;
import static android.graphics.Bitmap.createBitmap;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.globalLog;
import static com.tam.winati.ksa.R.id.qty;
import static com.tam.winati.ksa.R.id.store;
import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.ITEM_DELETED;
import static com.utils.Constants.PRODUCT_PRESENT;
import static com.utils.Constants.QTY_CHANGED;
import static java.util.logging.Logger.global;

/**
 * Created by mac on 15/10/2017.
 */

public class Functions {

    Context context;
    Dialog dialog;
    public Functions(Context con)
    {
        context = con;
    }


    public void showDialog()
    {
        try
        {
            dialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
            dialog.setContentView(R.layout.dialog_loader);
            dialog.setCancelable(false);
            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void dismissDialog()
    {
        try{dialog.dismiss();}catch (Exception e){e.printStackTrace();}
    }

    public ImageView[]  getRatingStars(View view)
    {
        ImageView rat[]                     = new ImageView[5];
        rat[0]		                        = (ImageView)view.findViewById(R.id.rating_s1);
        rat[1]		                        = (ImageView)view.findViewById(R.id.rating_s2);
        rat[2]		                        = (ImageView)view.findViewById(R.id.rating_s3);
        rat[3]		                        = (ImageView)view.findViewById(R.id.rating_s4);
        rat[4]		                        = (ImageView)view.findViewById(R.id.rating_s5);

        return rat;
    }

    public void showRating(ImageView rat[] , double rate)
    {
        for(int i=0;i<rat.length;i++)
        {
            rat[i].setImageResource(R.drawable.rating_blank);
        }

        if(rate == 0.5)
            rat[0].setImageResource(R.drawable.rating_half);
        else if(rate == 1)
            rat[0].setImageResource(R.drawable.rating_full);
        else if(rate == 1.5)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_half);
        }
        else if(rate == 2)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
        }
        else if(rate == 2.5)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
            rat[2].setImageResource(R.drawable.rating_half);
        }
        else if(rate == 3)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
            rat[2].setImageResource(R.drawable.rating_full);
        }
        else if(rate == 3.5)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
            rat[2].setImageResource(R.drawable.rating_full);
            rat[3].setImageResource(R.drawable.rating_half);
        }
        else if(rate == 4)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
            rat[2].setImageResource(R.drawable.rating_full);
            rat[3].setImageResource(R.drawable.rating_full);
        }
        else if(rate == 4.5)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
            rat[2].setImageResource(R.drawable.rating_full);
            rat[3].setImageResource(R.drawable.rating_full);
            rat[4].setImageResource(R.drawable.rating_half);
        }
        else if(rate == 5)
        {
            rat[0].setImageResource(R.drawable.rating_full);
            rat[1].setImageResource(R.drawable.rating_full);
            rat[2].setImageResource(R.drawable.rating_full);
            rat[3].setImageResource(R.drawable.rating_full);
            rat[4].setImageResource(R.drawable.rating_full);
        }

    }

    public int[] getKeys(JSONObject jsonObject)
    {
            int returnArray[]       = new int[]{0,0,0,0,0};
            String keyarray[]       = new String[]{"id","storeid","locid","catid","subcatid"};

            for(int i=0;i<returnArray.length;i++)
            {
                try{returnArray[i]  = Integer.parseInt(jsonObject.getString(keyarray[i]));}catch (Exception e){}
            }

            return returnArray;
    }

    public String createKeyHashCat(int[] ids)
    {
        String key              = ids[2]+"@@"+ids[1]+"@@"+ids[3];
        return key;
    }

    public String createKeyHash(int[] ids)
    {
        String key              = ids[2]+"@@"+ids[1]+"@@"+ids[3]+"@@"+ids[4];
        return key;
    }

    public String createProductKey(int[] ids)
    {
        String key              = ids[0]+"@@"+ids[2]+"@@"+ids[1]+"@@"+ids[3]+"@@"+ids[4];
        return key;
    }



    public String createProductKey(String storeid,String locid,int productid,String catid,String subcatid)
    {
        String key              = productid+"@@"+locid+"@@"+storeid+"@@"+catid+"@@"+subcatid;
        return key;
    }



    public void changeQty(String storeid,String locid,String catid,String subcatid,EditText qty , JSONObject jobj , boolean increase)
    {
        try
        {
            int productId           = Integer.parseInt(jobj.getString("id"));
            String key              = createProductKey(storeid,locid,productId,catid,subcatid);
            int currentQty          = Constants.itemqty.get(key);
            jobj.put("storeid",storeid);
            jobj.put("locid",locid);
            jobj.put("catid",catid);
            jobj.put("subcatid",subcatid);
            jobj.put("keyvalue",key);


            if(increase)
                currentQty++;
            else
                currentQty--;

            if(currentQty < 0)
                currentQty          = 0;

            Constants.itemqty.put(key,currentQty);
            currentQty          = Constants.itemqty.get(key);
            if(currentQty > 0)
                Constants.itemMap.put(key,jobj);

            appLog("qtyset :: "+currentQty+" -- "+key);
            if(currentQty == 0)
            {
                Intent intent = new Intent(ITEM_DELETED);
                intent.setAction(ITEM_DELETED);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            if(currentQty > 0)
                qty.setTextColor(context.getResources().getColor(R.color.green));
            else
                qty.setTextColor(context.getResources().getColor(R.color.txt));

            qty.setText(String.valueOf(currentQty));
            loadQty(storeid,locid);

        }catch (Exception e){
            e.printStackTrace();
        }




    }


    public void loadQty(String storeid,String locid)
    {
        JSONObject jobj;
        float price                     = 0;
        float disc                      = 0;
        Constants.totalPrice            = 0;
        int currentQty                  = 0;
        int productCount                = 0;
        Set<String> keys                = Constants.itemMap.keySet();
        for(String i: keys)
        {
            try
            {
                String splitted[]       = i.split("@@");
                globalLog("splitted :: "+splitted[2]+" -- "+splitted[1]+" -- "+storeid+" -- "+locid);
                if(splitted[2].equals(storeid) && splitted[1].equals(locid))
                {

                    price                   = 0;
                    jobj                    = Constants.itemMap.get(i);
                    currentQty              = Constants.itemqty.get(i);
                    price                   = Float.parseFloat(jobj.getString("price"));
                    disc                    = Float.parseFloat(jobj.getString("discount_price"));
                    if(disc > 0 && price > disc)
                        Constants.totalPrice   += disc * currentQty;
                    else
                        Constants.totalPrice   += price * currentQty;
                    Constants.formatted     = String.valueOf(round2decimal(Constants.totalPrice));
                    try
                    {
                        if(Constants.totalPrice >= SpecificStore.MIN_DEL)
                        {
                                ViewCart.delNeeded = "1";
                                ViewCart.submitValues.setVisibility(View.VISIBLE);
                                ViewCart.couponCheck.setVisibility(View.VISIBLE);
                                ViewCart.min_msg.setVisibility(View.GONE);
                        }
                        else
                        {
                            ViewCart.delNeeded = "0";
                            ViewCart.submitValues.setVisibility(View.GONE);
                            ViewCart.couponCheck.setVisibility(View.GONE);
                            ViewCart.min_msg.setVisibility(View.VISIBLE);
                        }

                        float totalWitDel;
                        if (ViewCart.coupondiscount.getVisibility() == View.VISIBLE){
                            float coupon = Float.parseFloat(String.valueOf(ViewCart.discount));
                            totalWitDel  = (Constants.totalPrice * ((100 - coupon)/100)) + ViewCart.delivery_Cost;
                        }
                        else {
                            totalWitDel  = Constants.totalPrice+ViewCart.delivery_Cost;
                        }
                        String disp         = String.valueOf(round2decimal(totalWitDel));
                        ViewProduct.currentTotal.setText(DEF_CURRENCY+" "+disp);
                        ViewCart.total.setText(DEF_CURRENCY+" "+String.valueOf(round2decimal(Constants.totalPrice)));


                    }catch (Exception e){}

                    if(currentQty > 0)
                        productCount++;
                }

            }catch (Exception e){
                e.printStackTrace();
            }


        }

        if(Constants.totalPrice > 0)
        {
            try{SpecificStore.totalPrice.setText(DEF_CURRENCY+" "+Constants.formatted);}catch (Exception e){}
            try{BaseActivity.amount_cart.setText(DEF_CURRENCY+" "+Constants.formatted);}catch (Exception e){}
        }
        else
        {
            try{SpecificStore.totalPrice.setText("");}catch (Exception e){}
            try{BaseActivity.amount_cart.setText("");}catch (Exception e){}
        }

        appLog("qtyset :: 2  "+currentQty);
        Intent intent = new Intent(QTY_CHANGED);
        intent.setAction(QTY_CHANGED);
        intent.putExtra("qty",productCount);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }




    public String generateCartKey(String storeid,String locid)
    {
        String key      = storeid+"||@@||"+locid;
        return Base64.encodeToString(key.getBytes(),Base64.DEFAULT);
    }

    public Bitmap scaleCenterCrop(Bitmap bmp,int newHeight, int newWidth) {
        int sourceWidth = bmp.getWidth();
        int sourceHeight = bmp.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = createBitmap(newWidth, newHeight, bmp.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(bmp, null, targetRect, null);

        return dest;
    }




    public float dpToPx(int v)
    {
        Resources resources         = context.getResources();
        DisplayMetrics metrics      = resources.getDisplayMetrics();
        float px                    = v * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public void logout()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.logout_from_app)).setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();

    }

    public String getCostString(String dt , JSONArray costArray)
    {
        try
        {
            SimpleDateFormat format         = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date                       = format.parse(dt);
            String cost                     = getCost(date,costArray);
            appLog("datedobjec :: date "+date+" -- "+dt+" -- "+cost);
            return cost;
        }catch (Exception e){
            e.printStackTrace();
        }

        return "--";
    }

    public String getCost(Date dated, JSONArray costArray)
    {
        try
        {
            long currTime               = System.currentTimeMillis();
            int minTime                 = 0;
            int maxTime                 = 0;
            long minTime2               = 0;
            long maxTime2               = 0;
            JSONObject jobj2            = null;
            float diff                  = (dated.getTime() - currTime);
            for(int j=0;j<costArray.length();j++)
            {
                jobj2                   = costArray.getJSONObject(j);

                minTime                 = Integer.parseInt(jobj2.getString("mintime"));
                minTime2                = minTime * 60 * 60 * 1000;


                maxTime                 = Integer.parseInt(jobj2.getString("maxtime"));
                maxTime2                = maxTime * 60 * 60 * 1000;


                if(diff >= minTime2 && diff < maxTime2 )
                {
                    float dde           = diff/(1000 * 60 * 60);
                    String abc          = jobj2.getString("cost");
                    appLog("timing :: "+abc+" -- "+minTime+" -- "+maxTime+" -- "+dde);
                    return abc;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return "0.00";

    }

    public String round2decimal(float nTotal) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat form       = new DecimalFormat("0.00",symbols);
        return  form.format(nTotal);
    }
}
