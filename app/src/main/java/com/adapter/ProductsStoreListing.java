package com.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.itemMap;


public class ProductsStoreListing extends ArrayAdapter<JSONObject>
{
    Activity context;
    Functions function;
    Set<String> keys                            = Constants.itemMap.keySet();
    List<String> nameList                       = new ArrayList<String>(keys);
    ArrayList<JSONObject> dataList              = new ArrayList<JSONObject>();
    public ProductsStoreListing(Activity con, ArrayList<JSONObject> jobj)
    {
        super(con, R.layout.row_products);

        context             = con;
        function            = new Functions(con);
        dataList            = jobj;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }




    public class ViewHolderItem {
        FotTextView name , price , catsub;
        FotTextView storename , storelocation , deliverytime;
        CircularImageView image;
        ImageView increase,decrease;
        EditText qty;
    }

    @Override
    public View getView(final int paramInt, View convertView, ViewGroup parent)
    {
        try
        {
            ViewHolderItem viewHolder;
            if(convertView==null)
            {
                LayoutInflater inflater 	        = context.getLayoutInflater();
                convertView 				        = inflater.inflate(R.layout.row_cartproducts, parent, false);
                viewHolder 					        = new ViewHolderItem();
                viewHolder.image			        = (CircularImageView)convertView.findViewById(R.id.img);
                viewHolder.name			            = (FotTextView)convertView.findViewById(R.id.name);
                viewHolder.price			        = (FotTextView)convertView.findViewById(R.id.nameloc);
                viewHolder.catsub			        = (FotTextView)convertView.findViewById(R.id.distance);
                viewHolder.increase			        = (ImageView)convertView.findViewById(R.id.increase);
                viewHolder.decrease			        = (ImageView)convertView.findViewById(R.id.decrease);
                viewHolder.qty			            = (EditText) convertView.findViewById(R.id.qty);
                viewHolder.storename                = (FotTextView)convertView.findViewById(R.id.storename);
                viewHolder.storelocation            = (FotTextView)convertView.findViewById(R.id.storelocation);
                viewHolder.deliverytime             = (FotTextView)convertView.findViewById(R.id.deliverytime);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					        = (ViewHolderItem) convertView.getTag();
            }

            final JSONObject jsonObject		        = dataList.get(paramInt);
            final CircularImageView imageView       = viewHolder.image;
            final EditText qtyBox                   = viewHolder.qty;
            int productId                           = Integer.parseInt(jsonObject.getString("id"));
            final String storeid                    = jsonObject.getString("storeid");
            final String locid                      = jsonObject.getString("locid");
            final String catid                      = jsonObject.getString("catid");
            final String subcatid                   = jsonObject.getString("subcatid");


            viewHolder.storename.setText(jsonObject.getString("store"+BaseActivity.getDefLang(context)));
            viewHolder.storelocation.setText(jsonObject.getString("loc"+BaseActivity.getDefLang(context)));
            viewHolder.deliverytime.setText(ViewCart.deliveryTime.get(storeid+"_"+locid));

            String key                              = function.createProductKey(storeid,locid,productId,catid,subcatid);
            if(Constants.itemqty.get(key) == null)
                Constants.itemqty.put(key,0);

            final int currentQty                    = Constants.itemqty.get(key);

            qtyBox.setText(String.valueOf(currentQty));
            if(currentQty > 0)
                itemMap.put(key,jsonObject);

            viewHolder.increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    function.changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,true);
                }
            });

            viewHolder.decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    function.changeQty(storeid,locid,catid,subcatid,qtyBox,jsonObject,false);

                }
            });

            if(Constants.itemqty.get(key) == null)
                Constants.itemqty.put(key,0);

            qtyBox.setText(String.valueOf(Constants.itemqty.get(key)));

            viewHolder.name.setText(jsonObject.getString("title"+BaseActivity.getDefLang(context)));
            viewHolder.catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context))+" , "+jsonObject.getString("s"+BaseActivity.getDefLang(context)));
            viewHolder.price.setText(DEF_CURRENCY+String.format("%.2f",Float.parseFloat(jsonObject.getString("price"))));

            Glide.with(context)
                    .load(jsonObject.getString("images"))
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


}