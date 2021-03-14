package com.utils;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.jaychang.sa.AuthCallback;
import com.jaychang.sa.SocialUser;
import com.jaychang.sa.facebook.SimpleAuth;
import com.tam.winati.ksa.R;
import com.tam.winati.ksa.ViewCart;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static android.bluetooth.BluetoothAssignedNumbers.GOOGLE;
import static com.facebook.FacebookSdk.getApplicationContext;
import static com.google.android.gms.auth.api.credentials.IdentityProviders.TWITTER;
import static com.tam.winati.ksa.BaseActivity.appLog;
import static com.utils.Constants.ANDROID_DEVICE;


public class SocialLogins {

    ConnectionClass cc;
    Functions functions;
    SharePrefsEntry sp;
    Activity activity;
    public SocialLogins(Activity act)
    {
        activity        = act;
        sp              = new SharePrefsEntry(act);
        functions       = new Functions(act);
        cc              = new ConnectionClass(act);

    }





    public void connectFacebook() {
        List<String> scopes = Arrays.asList("public_profile", "email");

        SimpleAuth.connectFacebook(scopes, new AuthCallback() {
            @Override
            public void onSuccess(SocialUser socialUser) {
                broadCastSocial(Constants.FACEBOOK , socialUser);
            }

            @Override
            public void onError(Throwable error) {
                appLog( error.getMessage());
            }

            @Override
            public void onCancel() {
                appLog( "Canceled");
            }
        });
    }


    public void connectGoogle() {
        List<String> scopes = Arrays.asList(
                "https://www.googleapis.com/auth/plus.login"
        );

        com.jaychang.sa.google.SimpleAuth.connectGoogle(scopes, new AuthCallback() {
            @Override
            public void onSuccess(SocialUser socialUser) {
                broadCastSocial(Constants.GOOGLE , socialUser);
            }

            @Override
            public void onError(Throwable error) {
                appLog(error.getMessage());
            }

            @Override
            public void onCancel() {
                appLog("Canceled");
            }
        });
    }


    public void connectTwitter() {
        com.jaychang.sa.twitter.SimpleAuth.connectTwitter(new AuthCallback() {
            @Override
            public void onSuccess(SocialUser socialUser) {
                broadCastSocial(Constants.TWITTER , socialUser);
            }

            @Override
            public void onError(Throwable error) {
                appLog(error.getMessage());
            }

            @Override
            public void onCancel() {
                appLog("Canceled");
            }
        });
    }


    void broadCastSocial(final int soc ,final  SocialUser socialUser)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try
                {

                    String email            = "";
                    String name             = "";
                    String mobile           = "";
                    String password         = "";
                    String device_type      = "";
                    String username         = "";

                    String push_noti        = "";
                    String social           = soc+"";
                    String accesstoken      = "";
                    String profilepic       = "";
                    String socialid         = "";
                    String deviceid         = "";

                    try{email               = socialUser.email;}catch (Exception e){}
                    try{name                = socialUser.fullName;}catch (Exception e){}
                    try{username            = socialUser.username;}catch (Exception e){}
                    try{profilepic          = socialUser.profilePictureUrl;}catch (Exception e){}
                    try{accesstoken         = socialUser.accessToken;}catch (Exception e){}
                    try{socialid            = socialUser.userId;}catch (Exception e){}
                    try{device_type         = ANDROID_DEVICE;}catch (Exception e){}
                    try{push_noti           = sp.getPushID()+"";}catch (Exception e){}
                    try{deviceid            = sp.getDeviceId() + "";}catch (Exception e){}

                    final JSONObject jobj = new JSONObject();
                    jobj.put("email", email);
                    jobj.put("name", name);
                    jobj.put("mobile", mobile);
                    jobj.put("password", password);
                    jobj.put("device_type", device_type);
                    jobj.put("username", username);
                    jobj.put("device_type", device_type);
                    jobj.put("push_noti",push_noti );
                    jobj.put("social", social);
                    jobj.put("accesstoken", accesstoken);
                    jobj.put("profilepic", profilepic);
                    jobj.put("socialid", socialid);
                    jobj.put("deviceid", deviceid);
                    jobj.put("caller", "loginSocial");

                    withinUI(jobj);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    void withinUI(final JSONObject jobj)
    {
        try
        {
            new AsyncTask<Void,Void,String>()
            {
                @Override
                protected String doInBackground(Void... voids) {

                    String res  = null;
                    try
                    {
                        String encrypted        = cc.getEncryptedString(jobj.toString());
                        res                     = cc.sendPostData(encrypted,null);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    return res;
                }


                @Override
                protected void onPostExecute(String paramString) {
                    super.onPostExecute(paramString);
                    try
                    {
                        functions.dismissDialog();
                        if (paramString != null)
                        {

                            try
                            {
                                JSONObject jsonObject           = new JSONObject(paramString);
                                if(jsonObject.getString("success").equals("0") && jsonObject.getString("status").equals("1"))
                                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                                else if(jsonObject.getString("success").equals("1") && jsonObject.getString("status").equals("1"))
                                {
                                    sp.setBooleanValue(SharePrefsEntry.VIDEO_SHOWN,true);
                                    onFinishSignin(paramString);
                                }

                            }catch (Exception e){
                                Toast.makeText(activity, activity.getString(R.string.error_network), Toast.LENGTH_SHORT).show();

                            }
                        }
                        else
                        {
                            Toast.makeText(activity, activity.getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception localException){}
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    functions.showDialog();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    void onFinishSignin(String result)
    {
        try
        {
            JSONObject jobj	=new JSONObject(result);
            String status	= jobj.getString("status");
            if(status.equals("1"))
            {
                String success	= jobj.getString("success");
                if(success.equals("1"))
                {
                    String data		    = jobj.getString("data");
                    boolean b			= sp.onFinish(new JSONObject(data));
                    if(b)
                    {
                        Intent localIntent = new Intent(activity, ViewCart.class);
                        localIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(localIntent);
                        activity.finish();

                    }
                }
                else
                {
                    String msg		    = jobj.getString("message");
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }

            }
            else
                Toast.makeText(activity, jobj.getString("message"), Toast.LENGTH_SHORT).show();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(activity, activity.getString(R.string.error_network),Toast.LENGTH_SHORT).show();
        }
    }


}