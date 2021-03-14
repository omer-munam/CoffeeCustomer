package com.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tam.winati.ksa.BaseActivity;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ViewCart;
import com.utils.Constants;
import com.utils.Functions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.R.attr.key;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.ViewCart.cartList;
import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.itemMap;
import static java.util.logging.Logger.global;


public class CartsStoreListing extends ArrayAdapter<JSONObject> {
    Activity context;
    Functions function;
    Set<String> keys                            = Constants.itemMap.keySet();
    List<String> nameList                       = new ArrayList<String>(keys);
    ArrayList<JSONObject> dataList              = new ArrayList<JSONObject>();
    public static Dialog timingDialog;
    ListView deliveryTimeLv;
    ArrayList<Date> allDated                    = new ArrayList<>();
    ArrayList<String> costArrayList             = new ArrayList<>();
    TimingList adapterTiming;
    JSONArray costArray;
    public CartsStoreListing(Activity con, ArrayList<JSONObject> jobj)
    {
        super(con, R.layout.row_products);

        context             = con;
        function            = new Functions(con);
        dataList            = jobj;
        timingDialog        = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
        timingDialog.setContentView(R.layout.dialog_select_category);
        deliveryTimeLv      = (ListView)timingDialog.findViewById(R.id.select_cat);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }




    public class ViewHolderItem {
        FotTextView storename , storelocation , deliverytime,deliverycost;
        CircularImageView image;
        LinearLayout items;
        RelativeLayout storedets;
        Button delivery_time;
        CardView lrp2;
        LinearLayout lrp;
    }

    @Override
    public View getView(final int paramInt, View convertView, ViewGroup parent)
    {
        ViewHolderItem viewHolder = null;
        try
        {



            if(convertView==null)
            {
                LayoutInflater inflater 	        = context.getLayoutInflater();
                convertView 				        = inflater.inflate(R.layout.row_cartstores, parent, false);
                viewHolder 					        = new ViewHolderItem();
                viewHolder.image			        = (CircularImageView)convertView.findViewById(R.id.img);
                viewHolder.storename                = (FotTextView)convertView.findViewById(R.id.storename);
                viewHolder.storelocation            = (FotTextView)convertView.findViewById(R.id.storelocation);
                viewHolder.deliverytime             = (FotTextView)convertView.findViewById(R.id.deliverytime);
                viewHolder.deliverycost             = (FotTextView)convertView.findViewById(R.id.deliverycosttx);
                viewHolder.items                    = (LinearLayout)convertView.findViewById(R.id.items);
                viewHolder.storedets                = (RelativeLayout) convertView.findViewById(R.id.storedets);
                viewHolder.delivery_time            = (Button) convertView.findViewById(R.id.delivery_time);
                viewHolder.lrp                      = (LinearLayout)convertView.findViewById(R.id.lrp);
                viewHolder.lrp2                     = (CardView)convertView.findViewById(R.id.lrp2);


                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					        = (ViewHolderItem) convertView.getTag();
            }

            final JSONObject jsonObject		        = dataList.get(paramInt);
            final String storeid                    = jsonObject.getString("storeid");
            final String locid                      = jsonObject.getString("locid");
            final CircularImageView imageView       = viewHolder.image;
            final LinearLayout itemsLr              = viewHolder.items;


            itemsLr.removeAllViews();
            for(int i=0;i < cartList.get(function.generateCartKey(storeid ,locid)).size();i++)
            {
                LayoutInflater inflate 	            = context.getLayoutInflater();
                View view 				            = inflate.inflate(R.layout.row_cartproducts, null);
                view                                = getChildView(view, cartList.get(function.generateCartKey(storeid ,locid)).get(i));
                itemsLr.addView(view);
            }

            appLog("jsonObject :: "+jsonObject);
            String delTime                          = ViewCart.deliveryTime.get(function.generateCartKey(storeid ,locid));
            viewHolder.storename.setText(jsonObject.getString("store"+BaseActivity.getDefLang(context)));
            viewHolder.storelocation.setText(jsonObject.getString("loc"+BaseActivity.getDefLang(context)));
            viewHolder.deliverytime.setText(delTime);
            costArray                               = jsonObject.getJSONArray("costarray");
            String costing                          = DEF_CURRENCY+function.getCostString(delTime,costArray);
            appLog("costing :: "+costing);

            ViewCart.deliveryCost.put(storeid+"@@"+locid,costing);
            viewHolder.deliverycost.setText(costing);




            viewHolder.delivery_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeliveryTimings(jsonObject);
                }
            });

            Glide.with(context)
                    .load(jsonObject.getString("storeimg"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(viewHolder.image) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            imageView.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                    });

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }




    public View getChildView(View convertView,final JSONObject jsonObject)
    {
        try
        {
            CircularImageView image	    = (CircularImageView)convertView.findViewById(R.id.img);
            FotTextView name			= (FotTextView)convertView.findViewById(R.id.name);
            FotTextView price			= (FotTextView)convertView.findViewById(R.id.nameloc);
            FotTextView catsub			= (FotTextView)convertView.findViewById(R.id.distance);
            ImageView increase			= (ImageView)convertView.findViewById(R.id.increase);
            ImageView decrease			= (ImageView)convertView.findViewById(R.id.decrease);
            EditText qty			    = (EditText) convertView.findViewById(R.id.qty);
            ImageView garbage           = (ImageView)convertView.findViewById(R.id.garbage);


            final CircularImageView imageView       = image;
            final EditText qtyBox                   = qty;
            int productId                           = Integer.parseInt(jsonObject.getString("id"));
            final String storeid                    = jsonObject.getString("storeid");
            final String locid                      = jsonObject.getString("locid");
            final String catid                      = jsonObject.getString("catid");
            final String subcatid                   = jsonObject.getString("subcatid");


            String key                              = function.createProductKey(storeid,locid,productId,catid,subcatid);
            if(Constants.itemqty.get(key) == null)
                Constants.itemqty.put(key,0);

            final int currentQty                    = Constants.itemqty.get(key);

            qtyBox.setText(String.valueOf(currentQty));
            if(currentQty > 0)
                itemMap.put(key,jsonObject);




            increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    function.changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,true);
                }
            });

            decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    function.changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,false);

                }
            });

            garbage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try
                    {
                        int productId           = Integer.parseInt(jsonObject.getString("id"));
                        String key              = function.createProductKey(storeid,locid,productId,catid,subcatid);
                        Constants.itemqty.put(key,1);
                        function.changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,false);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

            if(Constants.itemqty.get(key) == null)
                Constants.itemqty.put(key,0);

            qtyBox.setText(String.valueOf(Constants.itemqty.get(key)));



            name.setText(jsonObject.getString("title"+BaseActivity.getDefLang(context)));
            price.setText(DEF_CURRENCY+function.round2decimal(Float.parseFloat(jsonObject.getString("price"))));

            if(jsonObject.getString("s"+BaseActivity.getDefLang(context)) != null)
            {
                if(!jsonObject.getString("s"+BaseActivity.getDefLang(context)).equals("null"))
                    catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context))+" , "+jsonObject.getString("s"+BaseActivity.getDefLang(context)));
                else
                    catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context)));
            }
            else
                catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context)));

            Glide.with(context)
                    .load(jsonObject.getString("images"))
                    .placeholder(R.drawable.logo)
                    .into(new GlideDrawableImageViewTarget(image) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            imageView.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {}
                    });


        }
        catch (Exception e) {
            e.printStackTrace();
        }



        return convertView;
    }


    void showDeliveryTimings(JSONObject jsonObject)
    {
        try
        {
                final String storeid        = jsonObject.getString("storeid");
                final String locid          = jsonObject.getString("locid");
                long currTime               = System.currentTimeMillis();
                long threeHours             = 1800000;
                JSONArray jarray            = jsonObject.getJSONArray("alldelivery");
                ArrayList<Date> today       = new ArrayList<>();
                ArrayList<Date> tomrw       = new ArrayList<>();


                for(int i=0;i<jarray.length();i++)
                {

                    Calendar c              = Calendar.getInstance();
                    SimpleDateFormat df     = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat df2     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate    = df.format(c.getTime());
                    String curr             = jarray.getString(i);
                    formattedDate           = formattedDate+" "+curr;





                    c.add(Calendar.DAY_OF_YEAR, 1);
                    Date tomorrow           = c.getTime();
                    String formattedDate2   = df.format(tomorrow)+" "+curr;

                    Date tod                = (Date)df2.parse(formattedDate);
                    Date tomm               = (Date)df2.parse(formattedDate2);

                    long diff1              = (tod.getTime() - currTime);
                    long diff2              = (tomm.getTime() - currTime);
                    if(diff1 > threeHours)
                        today.add(tod);
                    if(diff2 > threeHours)
                        tomrw.add(tomm);


                }

                costArrayList.clear();
                allDated.clear();
                for(int i=0;i<today.size();i++)
                {
                    Date dated              = today.get(i);
                    costArrayList.add(function.getCost(dated,costArray));
                    allDated.add(dated);
                }

                for(int i=0;i<tomrw.size();i++)
                {
                    Date dated              = tomrw.get(i);
                    costArrayList.add(function.getCost(dated,costArray));
                    allDated.add(tomrw.get(i));
                }

                allDated.add(null);
                adapterTiming       = new TimingList(context,allDated,storeid,locid,costArrayList);
                deliveryTimeLv.setAdapter(adapterTiming);
                timingDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }




}