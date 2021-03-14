package com.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.adapter.ReviewAdapter;
import com.tam.winati.ksa.R;
import com.utils.ConnectionClass;
import com.utils.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.tam.winati.ksa.SpecificStore.storeJsonObject;


public class Reviews extends Fragment {

    ConnectionClass cc;
    public ArrayList<JSONObject> list           = new ArrayList<JSONObject>();
    RecyclerView rv;
    ReviewAdapter adapter;

    public static Reviews newInstance(String fragmentName) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FRAGMENT_NAME, fragmentName);
        Reviews fragment = new Reviews();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_reviews, container, false);
        cc                  = new ConnectionClass(getActivity());
        rv                  = v.findViewById(R.id.reviewRecycler);
        adapter             = new ReviewAdapter(getContext(), list);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        getData();
        return v;
    }

    private void getData() {
        new AsyncTask<Void,Void,String>()
        {
            @Override
            protected String doInBackground(Void... params) {
                try
                {
                    JSONObject jobj         = new JSONObject();
                    jobj.put("caller","getReviews");
                    jobj.put("data",storeJsonObject+"");
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
                    JSONObject jobj = new JSONObject(s);
                    if (jobj.getString("success").equals("1")){
                        JSONArray jarray = jobj.getJSONArray("data");
                        list.clear();
                        for(int i=0;i<jarray.length();i++)
                        {
                            try
                            {
                                list.add(jarray.getJSONObject(i));
                            }catch (Exception e){}
                        }
                    }
                }catch (Exception e){e.printStackTrace();}
                adapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


}
