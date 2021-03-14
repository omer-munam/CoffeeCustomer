 package com.tam.winati.ksa;

 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.content.ContextCompat;
 import android.support.v4.content.LocalBroadcastManager;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;

 import com.fragment.Favorites;
 import com.fragment.Offers;
 import com.fragment.Orders;
 import com.fragment.Products;
 import com.fragment.Reviews;
 import com.fragment.Sales;

 import org.json.JSONObject;

 import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

 import static com.utils.Constants.QTY_CHANGED;

 public class SpecificStore extends BaseActivity implements ViewPager.OnPageChangeListener {

     ViewPager pager;
     FragmentAdapter adapter;
     ImageView menu[]       = new ImageView[4];
     TextView me[]          = new TextView[4];

     public  static JSONObject storeJsonObject;
     RelativeLayout etx_home;
     LinearLayout signinHeading;
     public static EditText products;
     TextView cart_num , store , loc;
     RelativeLayout cartlr_;
     TextView title;
     public static TextView totalPrice ;
     public static String STOREID , LOCID;
     LinearLayout totalPricelr;


     int def_product        = 0;
     public static int MIN_DEL            = 0;
     int titles[]           = new int[]{R.string.menu_products,R.string.menu_orders,R.string.menu_offer,R.string.menu_fav,R.string.annoucement};
     public static boolean needFavLoad , categoriesLoaded;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_store);


        pager           = (ViewPager)findViewById(R.id.grid);
        needFavLoad     = true;
        def_product     = 0;
        menu[0]         = (ImageView)findViewById(R.id.menu_1);
        menu[1]         = (ImageView)findViewById(R.id.menu_2);
        menu[2]         = (ImageView)findViewById(R.id.menu_3);
        menu[3]         = (ImageView)findViewById(R.id.menu_4);
        me[0]           = (TextView)findViewById(R.id.me_1);
        me[1]           = (TextView)findViewById(R.id.me_2);
        me[2]           = (TextView)findViewById(R.id.me_3);
        me[3]           = (TextView)findViewById(R.id.me_4);
        etx_home        = (RelativeLayout)findViewById(R.id.etx_home);
//        signinHeading   = (LinearLayout) findViewById(R.id.signinHeading);
        title           = (TextView)findViewById(R.id.title);
        products        = (EditText)findViewById(R.id.products) ;
        totalPrice      = (TextView)findViewById(R.id.totalPrice);
        totalPricelr    = (LinearLayout)findViewById(R.id.totalPricelr);
        cart_num        = (TextView) findViewById(R.id.cart_num_);
        cartlr_         = (RelativeLayout)findViewById(R.id.cartlr_);
        store           = (TextView) findViewById(R.id.store);
        loc             = (TextView) findViewById(R.id.loc);
        totalPricelr.setVisibility(View.GONE);

         try
         {
             storeJsonObject            = new JSONObject(getIntent().getExtras().getString("data"));
             store.setText(storeJsonObject.getString("title"+BaseActivity.getDefLang(this)));
             loc.setText(storeJsonObject.getString("loc"+BaseActivity.getDefLang(this)));
             STOREID                    = storeJsonObject.getString("id");
             LOCID                      = storeJsonObject.getString("slid");
             MIN_DEL                    = Integer.parseInt(storeJsonObject.getString("minimum_del"));
         }catch (Exception e){}



        LocalBroadcastManager.getInstance(base).registerReceiver(qtyChanged,new IntentFilter(QTY_CHANGED));

        setMenu(0);
        pager.setOnPageChangeListener(this);

        OverScrollDecoratorHelper.setUpOverScroll(pager);

        adapter         = new FragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(4);
        if(getSharePrefs().getLanguage().equals("ar"))
        {
            def_product      = 2;
            titles           = new int[]{R.string.menu_offer,R.string.menu_orders,R.string.menu_products,R.string.annoucement};
            pager.setCurrentItem(def_product);

            menu[0]         = (ImageView)findViewById(R.id.menu_3);
            menu[1]         = (ImageView)findViewById(R.id.menu_2);
            menu[2]         = (ImageView)findViewById(R.id.menu_1);
            menu[3]         = (ImageView)findViewById(R.id.menu_4);
            me[0]           = (TextView)findViewById(R.id.me_3);
            me[1]           = (TextView)findViewById(R.id.me_2);
            me[2]           = (TextView)findViewById(R.id.me_1);
            me[3]           = (TextView)findViewById(R.id.me_4);

        }


         pager.setCurrentItem(def_product);
         setMenu(def_product);

    }




    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        setMenu(position);
        //title.setText(getResources().getString(titles[position]));
        globalLog("deflanguage :: "+title.getText()+" -- "+def_product);

        if(position != def_product)
        {
            title.setVisibility(View.VISIBLE);
            etx_home.setVisibility(View.GONE);
//            signinHeading.setVisibility(View.GONE);
        }
        else
        {
            title.setVisibility(View.INVISIBLE);
            etx_home.setVisibility(View.VISIBLE);
//            signinHeading.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class FragmentAdapter extends FragmentPagerAdapter
    {
        public FragmentAdapter(FragmentManager fm)
        {
            super(fm);
            globalLog("specificstore");
        }
        @Override
        public int getCount()
        {
            return 4;
        }
        @Override
        public Fragment getItem(int position)
        {

            globalLog("specificstore :: "+getSharePrefs().getLanguage());
            if(getSharePrefs().getLanguage().equals("en"))
            {
                if(position == 0)
                    return Products.newInstance("Products"+System.currentTimeMillis());
                else if(position == 1)
                    return Orders.newInstance("Orders"+System.currentTimeMillis());
                else if(position == 2)
                    return Offers.newInstance("Offers"+System.currentTimeMillis());
                else if (position == 3)
                    return Reviews.newInstance("Reviews"+System.currentTimeMillis());
                else
                    return Sales.newInstance("Sales"+System.currentTimeMillis());
            }
            else
            {
                if(position == 0)
                    return Offers.newInstance("Offers"+System.currentTimeMillis());
                else if(position == 1)
                    return Orders.newInstance("Orders"+System.currentTimeMillis());
                else if(position == 2)
                    return Products.newInstance("Products"+System.currentTimeMillis());
                else if (position == 3)
                    return Reviews.newInstance("Reviews"+System.currentTimeMillis());
                else
                    return Sales.newInstance("Sales"+System.currentTimeMillis());
            }


        }
    }


//    public void viewOnMap(View v)
//    {
//            Intent intent = new Intent(this,MapActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//    }

    void setMenu(int index)
    {
        int color       = ContextCompat.getColor(this, R.color.header);
        menu[index].setColorFilter(color);
        me[index].setTextColor(color);
        for(int i=0;i<menu.length;i++)
        {
            if(i != index)
            {
                menu[i].setColorFilter(Color.parseColor("#888888"));
                me[i].setTextColor(Color.parseColor("#888888"));
            }
        }
    }

    public void selectMenu(View v)
    {
         int tag    = Integer.parseInt(v.getTag().toString());
         pager.setCurrentItem(tag);
    }

     public BroadcastReceiver qtyChanged = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if(intent.getAction().equals(QTY_CHANGED))
             {
                 try
                 {
                     final int qty          = intent.getExtras().getInt("qty");
                     appLog("qty : "+qty+" -- "+cart_num);
                     cart_num.setText(String.valueOf(qty));
                     cartlr_.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             if(qty > 0)

                                 base.viewCart(null);
                         }
                     });

                     if(qty > 0 )
                     {
                         cart_num.setVisibility(View.VISIBLE);
                         totalPricelr.setVisibility(View.VISIBLE);
                     }
                     else
                     {
                         totalPricelr.setVisibility(View.GONE);
                         cart_num.setVisibility(View.GONE);
                     }
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }
         }
     };

     @Override
     public void onDestroy() {
         super.onDestroy();
         try{
             LocalBroadcastManager.getInstance(base).unregisterReceiver(qtyChanged);}catch (Exception e){}

     }

     @Override
     protected void onResume() {
         super.onResume();
         getFunction().loadQty(STOREID,LOCID);
     }
 }
