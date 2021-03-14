package com.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.tam.winati.ksa.services.MyFirebaseInstanceIDService;

import org.json.JSONObject;

import java.util.Locale;

import static android.R.id.edit;
import static android.provider.Telephony.Carriers.PASSWORD;

/**
 * Created by mac on 23/09/2017.
 */

public class SharePrefsEntry {

    Context context;
    SharedPreferences prefs , pushPref;
    SharedPreferences.Editor editor;
    public static String LOC_LATITUDE       = "latitude";
    public static String LOC_LONGITUDE      = "longitude";
    public static String LOC_ACCURACY       = "accuracy";
    public static String LOC_ALTITUDE       = "altitude";
    public static String LOC_BEARING        = "bearing";
    public static String LOC_TIMING         = "timing";
    public static String LOC_PROVIDER       = "provider";
    public static String LOC_SPEED          = "speed";
    public static String LOC_DATA           = "locdata";
    public static String VIDEO_SHOWN        = "de";
    public static String LAST_LOCATION      = "lastloc_add";

    public static final String LAST_LOADED	= "last_laoded_at";
    public static final String FAILED_COUNT	= "failed_counter";

    public static final String UID          = "id";
    public static final String NAME         = "name";
    public static final String THUMB        = "profilepic";
    public static final String EMAIL        = "email";
    public static final String PASSWORD     = "password";
    public static final String ADDRESS      = "address";
    public static final String PROVINCE     = "province";
    public static final String GCMREG       = "gcmreg";
    public static final String TYPE         = "type";
    public static final String IS_CELEB     = "isceleb";
    public static final String DOB 			= "dob";
    public static final String PHONE 		= "contact";
    public static final String GENDER       = "gender";
    public static final String UNAME        = "username";
    public static final String DATA         = "data";
    public static final String LANG         = "language";
    public static final String ADD_NAME     = "addname";


    public SharePrefsEntry(Context cont)
    {
        context = cont;
        prefs   = context.getSharedPreferences("applicationtksa",Context.MODE_PRIVATE);
        editor  = prefs.edit();
        pushPref= context.getSharedPreferences(MyFirebaseInstanceIDService.SHARED_PREF, 0);
    }


    public String getDeviceId()
    {
        return "";
    }

    public String getUid()
    {
        return prefs.getString(UID,"");
    }

    public void loadLocalLanguage()
    {
        String lang             = getLanguage();
        Locale locale           = new Locale(lang);
        Locale.setDefault(locale);

        Resources res           = context.getResources();
        Configuration config    = new Configuration(res.getConfiguration());
        config.locale           = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    public void setLanguage(String value)
    {
        editor.putString(LANG,value);
        editor.commit();
    }

    public String getLanguage()
    {
        String lang = prefs.getString(LANG,"");
        if(lang.equals(""))
        {
            lang    = Locale.getDefault().getLanguage();
        }

        return lang;
    }

    public void setGeneralString(String key,String value)
    {
        editor.putString(key,value);
        editor.commit();
    }

    public String getGeneralString(String key)
    {
        return prefs.getString(key,"");
    }

    public String getProfilePic() {
        String pp = prefs.getString(THUMB,"");
        return getImageUrl(pp);
    }

    public String getImageUrl(String s)
    {
        String pp = s;
        try
        {
            if(pp.equals(""))
                pp = ConnectionClass.PLACEHOLDER;
            else
                pp = ConnectionClass.THUMB_URL+pp;
        }catch(Exception e){
            pp = ConnectionClass.PLACEHOLDER;
        }
        return pp;
    }


    public String getUserName() {
        return prefs.getString(NAME,"");
    }

    public void setKeyValInt(String key, int val)
    {
        editor.putInt(key, val);
        editor.commit();
    }

    public String getName() {
        return prefs.getString(NAME,"");
    }
    public String getUseridTag() {
        return prefs.getString(UNAME,null);
    }

    public String getPassword() {
        return prefs.getString(PASSWORD,null);
    }

    public String getPhone() {
        return prefs.getString(PHONE,"");
    }
    public String getEmail() {
        return prefs.getString(EMAIL,"");
    }

    public int getKeyValInt(String key)
    {
        return prefs.getInt(key,-1);
    }


    public void setKeyValLong(String key, long val)
    {
        editor.putLong(key, val);
        editor.commit();
    }


    public String getAddressName() {
        return prefs.getString(ADD_NAME,"");
    }
    public String getAddress() {
        return prefs.getString(ADDRESS,"");
    }
    public double getLatitude() {
        if(Double.isNaN(prefs.getFloat(LOC_LATITUDE,24.774265f))){
            return 24.774265f;
        }
        return prefs.getFloat(LOC_LATITUDE,24.774265f);

    }
    public double getLongitude() {
        if(Double.isNaN(prefs.getFloat(LOC_LONGITUDE,46.738586f))){
            return 46.738586f;
        }
        return prefs.getFloat(LOC_LONGITUDE,46.738586f);
    }

    public void setBooleanValue(String key , boolean b)
    {
        editor.putBoolean(key, b);
        editor.commit();
    }


    public boolean getBooleanValue(String key)
    {
        return prefs.getBoolean(key,false);
    }

    public long getKeyValLong(String key)
    {
        return prefs.getLong(key,-1l);
    }

    public void setKeyValString(String key, String val)
    {
        editor.putString(key, val);
        editor.commit();
    }



    public String getPushID()
    {
        return pushPref.getString("regId", null);
    }

    public boolean updateDefAddress(JSONObject jobj)
    {
        try
        {
            editor.putString(ADDRESS,jobj.optString(ADDRESS));
            editor.putFloat(LOC_LATITUDE,(float)jobj.optDouble(LOC_LATITUDE));
            editor.putFloat(LOC_LONGITUDE,(float)jobj.optDouble(LOC_LONGITUDE));
            editor.putString(ADD_NAME,jobj.optString(NAME));
            editor.commit();
            return true;
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
            return true;
        }



    }

    public boolean onFinish(JSONObject jobj)
    {
        try
        {
            editor.putString(DATA, jobj + "");
            editor.commit();
        }catch(Exception e){
            e.printStackTrace();
        }

        try
        {
            editor.putString(THUMB, jobj.optString(THUMB));
            editor.putString(UID, jobj.optString(UID));
            editor.putString(PHONE, jobj.optString(PHONE));
            editor.putString(NAME, jobj.optString(NAME));
            editor.putString(ADDRESS,jobj.optString(ADDRESS));
            editor.putFloat(LOC_LATITUDE,(float)jobj.optDouble(LOC_LATITUDE));
            editor.putFloat(LOC_LONGITUDE,(float)jobj.optDouble(LOC_LONGITUDE));
            editor.putString(ADD_NAME,jobj.optString(ADD_NAME));
            editor.putString(PASSWORD, jobj.optString(PASSWORD));
            editor.putString(EMAIL, jobj.optString(EMAIL));
            editor.commit();

            return true;
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
            return true;
        }



    }

    public boolean isLoggedin()
    {
        try
        {
            String s=prefs.getString(UID,null);
            int i=Integer.parseInt(s);
            if(i>0)
                return true;
            else
                return false;
        } catch (NumberFormatException e) {
            return false;
        }


    }

    public void logout()
    {
        editor.clear();
        editor.commit();
    }

    public void updatePassword(String s) {
        editor.putString(PASSWORD, s);
        editor.commit();
    }

    public String getData() {
        return prefs.getString(DATA,"");
    }
}
