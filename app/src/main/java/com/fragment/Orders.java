package com.fragment;

/**
 * Created by mac on 11/10/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.adapter.OrderMainAdapter;
import com.adapter.OrdersAdapter;
import com.adapter.StoresAdapter;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.SpecificStore;
import com.utils.ConnectionClass;
import com.utils.Constants;
import com.utils.EndlessAdapter;
import com.utils.FetchDataTask;
import com.utils.IItemsReadyListener;
import com.utils.SharePrefsEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import pl.droidsonroids.gif.GifImageView;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.BaseActivity.base;
import static com.tam.winati.ksa.R.id.subcategories;
import static com.tam.winati.ksa.SpecificStore.storeJsonObject;
import static com.utils.Constants.MIN_STORE_LIST;
import static com.utils.Constants.ORDERS_RELOAD;
import static com.utils.Constants.SUBCAT_PRESENT;


public class Orders extends Fragment {
    private View view;
    ListView lv;
    public ArrayList<JSONObject> list           = new ArrayList<JSONObject>();
    public ArrayList<String> idvalues           = new ArrayList<String>();
    boolean executing;
    SharePrefsEntry sp;
    ConnectionClass cc;
    EndlessLoading adapter;
    String lastid;
    boolean completed;
    OrdersAdapter adapter2;
    JSONObject jobjGlobal;
    GifImageView loader_img;

    public static Orders newInstance(String fragmentName) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FRAGMENT_NAME, fragmentName);
        Orders fragment = new Orders();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view 				= inflater.inflate(R.layout.fragment_orders,container, false);
        lv                  = (ListView)view.findViewById(R.id.lv);
        loader_img          = (GifImageView) view.findViewById(R.id.loader_img);

        cc                  = new ConnectionClass(getActivity());
        sp                  = new SharePrefsEntry(getActivity());
        adapter             = new EndlessLoading();
        adapter2            = new OrdersAdapter(getActivity(),list);


        OverScrollDecoratorHelper.setUpOverScroll(lv);
        loadList();

        LocalBroadcastManager.getInstance(base).registerReceiver(loadList,new IntentFilter(ORDERS_RELOAD));


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{LocalBroadcastManager.getInstance(base).unregisterReceiver(loadList);}catch (Exception e){}

    }

    public BroadcastReceiver loadList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ORDERS_RELOAD))
            {
                loadList();
            }
        }
    };


    void loadList()
    {
        completed           = false;
        executing           = false;
        list.clear();
        lastid              = "";
        lv.setAdapter(null);
        adapter             = new EndlessLoading();
        idvalues.clear();
        adapter.cacheInBackground();
    }


    class EndlessLoading extends EndlessAdapter implements IItemsReadyListener {
        JSONObject jobjGlobal;
        EndlessLoading()
        {
            super(getActivity(),new OrderMainAdapter(getActivity(),list), R.layout.row_zero);

        }



        @Override
        protected boolean cacheInBackground(){
            appLog("calledcache :: "+executing+" -- "+completed);
            if(!executing && !completed) {
                executing   = true;

                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("myid",sp.getUid());
                    jobj.put("lastid",lastid);
                    jobj.put("data",storeJsonObject+"");
                    jobj.put("caller","getMyOrders");
                    appLog("ordrjobj :: "+jobj);
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    FetchDataTask fetchs = new FetchDataTask(getActivity(),this,list.size(),encrypted);
                    fetchs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            return !completed;
        }

        @Override
        public void onItemsReady(ArrayList<JSONObject> d) {
            loader_img.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
            for(int i=0;i<d.size();i++)
            {
                try
                {
                    jobjGlobal 						= d.get(i);
                    if(!idvalues.contains(jobjGlobal.getString("threadid")))
                    {
                        list.add(d.get(i));
                        lastid              = jobjGlobal.getString("id");
                        idvalues.add(jobjGlobal.getString("threadid"));
                    }

                }catch (Exception e){}

            }


            if(lv.getAdapter() == null)
                lv.setAdapter(adapter);

            if(d.size() < 10)
                completed = true;
            else
            {
                adapter.onDataReady();
                adapter.notifyDataSetChanged();
            }

            executing   = false;

        }

        @Override
        protected void appendCachedData() {

        }
    }

}
