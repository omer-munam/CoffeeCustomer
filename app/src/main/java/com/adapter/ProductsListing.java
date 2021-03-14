package com.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.widget.RoundedImg;

import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.attr.key;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.R.id.img;
import static com.utils.Constants.DEF_CURRENCY;


public class ProductsListing extends ArrayAdapter<JSONObject>
{
    ArrayList<JSONObject>    list = new ArrayList<JSONObject>();

    Activity context;
    Functions function;
    String storeid,locid , catid,subcatid;
    SharePrefsEntry sp;
    ConnectionClass cc;
    public ProductsListing(Activity con, ArrayList<JSONObject> l,String st,String lc,String c,String s)
    {
        super(con, R.layout.row_products);

        context             = con;
        list                = l;
        storeid             = st;
        locid               = lc;
        catid               = c;
        subcatid            = s;
        function            = new Functions(con);
        sp                  = new SharePrefsEntry(con);
        cc                  = new ConnectionClass(con);
    }

    @Override
    public int getCount() {
        return list.size();
    }




    public class ViewHolderItem {
        FotTextView name , price , catsub;
        CircularImageView image;
        ImageView increase,decrease;
        EditText qty;
        RelativeLayout imglr;
        ImageView favimg;

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
                convertView 				        = inflater.inflate(R.layout.row_products, parent, false);
                viewHolder 					        = new ViewHolderItem();
                viewHolder.image			        = (CircularImageView)convertView.findViewById(R.id.img);
                viewHolder.name			            = (FotTextView)convertView.findViewById(R.id.name);
                viewHolder.price			        = (FotTextView)convertView.findViewById(R.id.nameloc);
                viewHolder.catsub			        = (FotTextView)convertView.findViewById(R.id.distance);
                viewHolder.increase			        = (ImageView)convertView.findViewById(R.id.increase);
                viewHolder.decrease			        = (ImageView)convertView.findViewById(R.id.decrease);
                viewHolder.qty			            = (EditText) convertView.findViewById(R.id.qty);
                viewHolder.imglr			        = (RelativeLayout) convertView.findViewById(R.id.imglr);
                viewHolder.favimg			        = (ImageView)convertView.findViewById(R.id.favimg);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder 					        = (ViewHolderItem) convertView.getTag();
            }





            final JSONObject jsonObject		        = list.get(paramInt);
            final CircularImageView imageView       = viewHolder.image;
            final EditText qtyBox                   = viewHolder.qty;
            int productId                           = Integer.parseInt(jsonObject.getString("id"));


            String key                              = function.createProductKey(storeid,locid,productId,catid,subcatid);
            if(Constants.itemqty.get(key) == null)
                Constants.itemqty.put(key,0);

            jsonObject.put("key",key);


            final ImageView favImg                  = viewHolder.favimg;
            if(jsonObject.getInt("currentfav") == 1)
                favImg.setImageResource(R.drawable.fav_added);
            else
                favImg.setImageResource(R.drawable.fav_not_added_yet);

            final int currentQty                    = Constants.itemqty.get(key);

            qtyBox.setText(String.valueOf(currentQty));
            if(currentQty > 0)
                Constants.itemMap.put(key,jsonObject);

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

            if(jsonObject.getString("s"+BaseActivity.getDefLang(context)) != null)
            {
                if(!jsonObject.getString("s"+BaseActivity.getDefLang(context)).equals("null"))
                    viewHolder.catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context))+" , "+jsonObject.getString("s"+BaseActivity.getDefLang(context)));
                else
                    viewHolder.catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context)));
            }
            else
                viewHolder.catsub.setText(jsonObject.getString("ca"+BaseActivity.getDefLang(context)));

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


            viewHolder.imglr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFav(favImg,jsonObject,paramInt);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


    void addToFav(final ImageView favImg,final JSONObject jsonObject,final int paramInt)
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
                    jobj.put("key",jsonObject.getString("key")+"");
                    jobj.put("myid",sp.getUid());
                    jobj.put("lang",BaseActivity.getDefLang(context));
                    jobj.put("caller","addToFav");
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

                        if(jsonObject.getInt("currentfav") == 1)
                            favImg.setImageResource(R.drawable.fav_added);
                        else
                            favImg.setImageResource(R.drawable.fav_not_added_yet);

                        JSONObject jsonObject1               = list.get(paramInt);
                        jsonObject1.put("currentfav",jsonObject.getInt("currentfav"));
                        list.set(paramInt,jsonObject1);
                        notifyDataSetChanged();
                    }
                    else
                        Toast.makeText(context, context.getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    Toast.makeText(context ,context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}