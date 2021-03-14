package com.tam.winati.ksa;

/**
 * Created by mac on 11/10/2017.
 */

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.adapter.StoresAdapter;
import com.utils.ConnectionClass;
import com.utils.EndlessAdapter;
import com.utils.FetchDataTask;
import com.utils.IItemsReadyListener;
import com.utils.SharePrefsEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

import static com.utils.Constants.DEF_CURRENCY;
import static com.utils.Constants.MIN_STORE_LIST;
import static com.utils.SharePrefsEntry.LOC_DATA;
import static com.utils.SharePrefsEntry.LOC_LATITUDE;
import static com.utils.SharePrefsEntry.LOC_LONGITUDE;

public class ShowStores extends BaseActivity {
    ListView lv;
    public static ArrayList<JSONObject> list    = new ArrayList<JSONObject>();
    ArrayList<JSONObject> lastSelectedItem      = new ArrayList<>();
    ArrayList<JSONObject> searching             = new ArrayList<>();
    ArrayList<JSONObject> listSelectedItem      = new ArrayList<>();
    public static ArrayList<String> idvalues    = new ArrayList<String>();
    boolean executing;
    boolean completed;
    SharePrefsEntry sp;
    ConnectionClass cc;
    double lastDistance;
    EndlessLoading adapter;
    View no_nearby;
    RelativeLayout rl;
    public static EditText searchStoreText;
    String searchTxt;
    ImageView filterPopup;
    AlertDialog alertDialog1;
    CharSequence[] SortValues = {" Rating "," Min Order Value "," Fastest Delivery ", " Distance ", " Available "};
    int checked = -1;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showstores);
        checked=-1;

        searchStoreText     = (EditText) findViewById(R.id.searchStoreText);
        lv                  = (ListView)findViewById(R.id.lv);
        no_nearby           = (View)findViewById(R.id.no_nearby);
        cc                  = new ConnectionClass(getBaseActivity());
        sp                  = new SharePrefsEntry(getBaseActivity());
        adapter             = new EndlessLoading();
        lastDistance        = -1;
        executing           = false;
        rl                  = findViewById(R.id.etx_home);
        filterPopup         = findViewById(R.id.filterPopup);
        fab                 = findViewById(R.id.fab_subscribe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ShowStores.this);
                final LinearLayout layout = new LinearLayout(ShowStores.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText email = new EditText(getApplicationContext());
                final EditText city = new EditText(getApplicationContext());
                email.setHint("Enter E-mail");
                city.setHint("Enter City");
                layout.addView(email); // Notice this is an add method
                layout.addView(city); // Another add method
                layout.setPadding(30,30,30,20);
                alert.setTitle("Subscribe to Our Newsletter");
                alert.setView(layout);

                alert.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final String mail = email.getText().toString();
                        final String City = city.getText().toString();
                        if (!mail.equals("") && !City.equals("")){
                            new AsyncTask<Void,Void,String>()
                            {
                                @Override
                                protected String doInBackground(Void... params) {
                                    String res = null;
                                    try
                                    {
                                        JSONObject jobj         = new JSONObject();
                                        jobj.put("email", mail);
                                        jobj.put("city", City);
                                        jobj.put("myid", getSharePref().getUid());
                                        jobj.put("caller","setsubscriber");
                                        appLog("jobj :: "+jobj);
                                        String encrypted        = cc.getEncryptedString(jobj.toString());
                                        res                     = getConnection().sendPostData(encrypted,null);
                                        Log.d("orsers", res);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return res;
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    super.onPostExecute(s);
                                    try
                                    {
                                        if(s != null)
                                        {
                                            JSONObject jsonObject                 = new JSONObject(s);

                                            if(jsonObject.getString("success").equals("1"))
                                            {
                                                Toast.makeText(ShowStores.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                            }
                                            else
                                                Toast.makeText(getBaseActivity(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                            Toast.makeText(getBaseActivity(), getString(R.string.check_your_internet), Toast.LENGTH_SHORT).show();


                                    }catch (Exception e){
                                        e.printStackTrace();
                                        Toast.makeText(getBaseActivity(), getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        else {
                            Toast.makeText(ShowStores.this, "All fields are Mandatory", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }
        });

        OverScrollDecoratorHelper.setUpOverScroll(lv);

        adapter.cacheInBackground();
    }

    public void searchStores(View v){
        if (rl.getVisibility() == View.VISIBLE){
            filterPopup.setVisibility(View.VISIBLE);
            rl.setVisibility(View.INVISIBLE);
        }
        else{
            filterPopup.setVisibility(View.INVISIBLE);
            rl.setVisibility(View.VISIBLE);
        }
    }

    public void filterPopup(View v){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Sort By: ");

        builder.setSingleChoiceItems(SortValues, checked, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                checked=item;
                sort(item);
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();

    }

    private void sort(int i) {
        switch (i){
            case 0:
                Collections.sort( listSelectedItem, new Comparator<JSONObject>() {
                    private static final String KEY_NAME = "rating";
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        String valA = new String();
                        String valB = new String();
                        try {
                            valA = (String) a.get(KEY_NAME);
                            valB = (String) b.get(KEY_NAME);
                        }
                        catch (JSONException e) {}
                        return -valA.compareTo(valB);
                        //if you want to change the sort order, simply use the following:
                        //return -valA.compareTo(valB);
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 1:
                Collections.sort( listSelectedItem, new Comparator<JSONObject>() {
                    private static final String KEY_NAME = "minimum_del";
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        String valA = new String();
                        String valB = new String();
                        try {
                            valA = (String) a.get(KEY_NAME);
                            valB = (String) b.get(KEY_NAME);
                        }
                        catch (JSONException e) {}
                        return valA.compareTo(valB);
                        //if you want to change the sort order, simply use the following:
                        //return -valA.compareTo(valB);
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 2:
                Collections.sort( listSelectedItem, new Comparator<JSONObject>() {
                    private static final String KEY_NAME = "next";
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        String valA = new String();
                        String valB = new String();
                        try {
                            valA = (String) a.get(KEY_NAME);
                            valB = (String) b.get(KEY_NAME);
                        }
                        catch (JSONException e) {}
                        return -valA.compareTo(valB);
                        //if you want to change the sort order, simply use the following:
                        //return -valA.compareTo(valB);
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 3:
                Collections.sort( listSelectedItem, new Comparator<JSONObject>() {
                    private static final String KEY_NAME = "distance";
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        Double valA=0.0;
                        Double valB=0.0;
                        try {
                            valA = (Double) a.get(KEY_NAME);
                            valB = (Double) b.get(KEY_NAME);
                        }
                        catch (JSONException e) {}
                        return valA.compareTo(valB);
                        //if you want to change the sort order, simply use the following:
                        //return -valA.compareTo(valB);
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 4:
                Collections.sort( listSelectedItem, new Comparator<JSONObject>() {
                    private static final String KEY_NAME = "status";
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        String valA = new String();
                        String valB = new String();
                        try {
                            valA = (String) a.get(KEY_NAME);
                            valB = (String) b.get(KEY_NAME);
                        }
                        catch (JSONException e) {}
                        return -valA.compareTo(valB);
                        //if you want to change the sort order, simply use the following:
                        //return -valA.compareTo(valB);
                    }
                });
                adapter.notifyDataSetChanged();
                break;
        }
    }


    class EndlessLoading extends EndlessAdapter implements IItemsReadyListener, TextWatcher {
        JSONObject jobjGlobal;
        EndlessLoading()
        {
            super(getBaseActivity(),new StoresAdapter(getBaseActivity(),listSelectedItem), R.layout.row_zero);
            ShowStores.searchStoreText.addTextChangedListener(this);
        }



        @Override
        protected boolean cacheInBackground(){
            if(!executing && !completed) {
                executing   = true;

                try
                {
                    JSONObject jsonObject   =   new JSONObject(sp.getGeneralString(LOC_DATA));
                    JSONObject jobj         = new JSONObject();
                    jobj.put("lastdistance",lastDistance);
                    jobj.put("latitude",jsonObject.getDouble(LOC_LATITUDE));
                    jobj.put("longitude",jsonObject.getDouble(LOC_LONGITUDE));
                    jobj.put("myid",sp.getUid());
                    jobj.put("caller","getStores");

//                    Log.d("orsers", jobj.toString());
                    String encrypted        = cc.getEncryptedString(jobj.toString());
                    FetchDataTask fetchs = new FetchDataTask(getBaseActivity(),this,list.size(),encrypted);
                    fetchs.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return !completed;
        }

        @Override
        public void onItemsReady(ArrayList<JSONObject> d) {
           // d = new ArrayList<>();
            for(int i=0;i<d.size();i++)
            {
                try
                {
                    jobjGlobal 						= d.get(i);
                    appLog("jobjGlobal :: "+jobjGlobal);
                    lastDistance 	                = jobjGlobal.getDouble("distance");
                    if(!idvalues.contains(jobjGlobal.getString("slid")))
                    {
                        if(d.get(i).getDouble("radius") >= d.get(i).getDouble("distance")){
                            list.add(d.get(i));
                        }
                        idvalues.add(jobjGlobal.getString("slid"));
                    }

                }catch (Exception e){}

            }
            listSelectedItem.addAll(list);
            if(lv.getAdapter() == null)
                lv.setAdapter(adapter);

            if(d.size() < MIN_STORE_LIST)
                completed = true;
            else
            {
                adapter.onDataReady();
                adapter.notifyDataSetChanged();
            }

            if(list.size() == 0)
            {
                no_nearby.setVisibility(View.VISIBLE);
                lv.setVisibility(View.GONE);

            }
            else
            {
                no_nearby.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
            }


            executing   = false;
        }

        @Override
        protected void appendCachedData() {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            searchTxt               = ShowStores.searchStoreText.getText().toString();
            appLog("before :: "+searchTxt);
            if(searchTxt.equals(""))
            {
                lastSelectedItem.clear();
                lastSelectedItem.addAll(listSelectedItem);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            onSearch();
        }

        private void onSearch() {
            searchTxt               = ShowStores.searchStoreText.getText().toString();
            listSelectedItem.clear();
            if(!searchTxt.equals(""))
            {

//                globalLog("catselected :: "+currentCatid);
                searching.clear();
                for(int i=0;i<list.size();i++)
                {
                    try
                    {
                        Log.d("search", list.get(i).toString());
                        boolean nameComp     = (list.get(i).getString("title_ar").startsWith(searchTxt) || list.get(i).getString("title_en").toLowerCase().startsWith(searchTxt.toLowerCase()));
//                    if(currentCatid > 0 && currentSubcatid == 0)
//                    {
//                        if(nameComp && listItem.get(i).getString("catid").equals(String.valueOf(currentCatid)))
//                            searching.add(listItem.get(i));
//                    }
//                    else if(currentCatid > 0 && currentSubcatid > 0)
//                    {
//                        if(nameComp && listItem.get(i).getString("catid").equals(String.valueOf(currentCatid)) && listItem.get(i).getString("subcatid").equals(String.valueOf(currentSubcatid)))
//                            searching.add(listItem.get(i));
//                    }
//                    else if(currentCatid == -1)
//                    {
//                        nameComp     = (favListBuffer.get(i).getString("title_ar").startsWith(searchTxt) || favListBuffer.get(i).getString("title_en").toLowerCase().startsWith(searchTxt.toLowerCase()));
//                        if(nameComp)
//                            searching.add(favListBuffer.get(i));
//                    }
//                    else

                        if(nameComp)
                            searching.add(list.get(i));


                    }catch (Exception e){e.printStackTrace();}

                }


                listSelectedItem.addAll(searching);

            }
            else
                listSelectedItem.addAll(lastSelectedItem);

            adapter.notifyDataSetChanged();
        }
    }

}
