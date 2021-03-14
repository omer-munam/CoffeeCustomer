package com.fragment;

/**
 * Created by mac on 11/10/2017.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.adapter.OffersAdapter;
import com.tam.winati.ksa.R;
import com.utils.ConnectionClass;
import com.utils.Constants;
import com.utils.SharePrefsEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.SpecificStore.storeJsonObject;
import static com.utils.SharePrefsEntry.LOC_DATA;
import static com.utils.SharePrefsEntry.LOC_LATITUDE;
import static com.utils.SharePrefsEntry.LOC_LONGITUDE;

public class Sales extends Fragment {
    private View view;
    ListView lv;
    public ArrayList<JSONObject> list           = new ArrayList<JSONObject>();
    public ArrayList<String> idvalues           = new ArrayList<String>();
    boolean executing;
    SharePrefsEntry sp;
    ConnectionClass cc;
    OffersAdapter adapter;
    JSONObject jobjGlobal;

    public static Sales newInstance(String fragmentName) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FRAGMENT_NAME, fragmentName);
        Sales fragment = new Sales();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view 				= inflater.inflate(R.layout.fragment_sales,container, false);
        lv                  = (ListView)view.findViewById(R.id.lv);

        cc                  = new ConnectionClass(getActivity());
        sp                  = new SharePrefsEntry(getActivity());
        adapter             = new OffersAdapter(getActivity(),list);
        executing           = false;

        OverScrollDecoratorHelper.setUpOverScroll(lv);

        lv.setAdapter(adapter);
        loadData();
        return view;
    }


    void loadData()
    {
        list.clear();
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                try
                {
                    JSONObject jsonObject   =   new JSONObject(sp.getGeneralString(LOC_DATA));
                    JSONObject jobj         = new JSONObject();
                    jobj.put("latitude",jsonObject.getDouble(LOC_LATITUDE));
                    jobj.put("longitude",jsonObject.getDouble(LOC_LONGITUDE));
                    jobj.put("myid",sp.getUid());
                    jobj.put("data",storeJsonObject+"");
                    jobj.put("caller","getOffers");
                    appLog("jobj :: "+jobj);
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    return cc.sendPostData(encrypted,null);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try
                {
                    JSONArray jarray = new JSONArray(s);
                    for(int i=0;i<jarray.length();i++)
                    {
                        try
                        {
                            jobjGlobal 						= jarray.getJSONObject(i);
                            list.add(jobjGlobal);

                        }catch (Exception e){}

                    }
                }catch (Exception e){e.printStackTrace();}

                adapter.notifyDataSetChanged();

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
