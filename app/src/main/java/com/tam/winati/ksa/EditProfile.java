package com.tam.winati.ksa;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balram.library.FotTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.utils.ConnectionClass;
import com.utils.ExifUtil;
import com.utils.ImagePicker;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.utils.ConnectionClass.WEB_AJAX;
import static com.utils.Constants.FROM_IMAGE;


public class EditProfile extends BaseActivity {
	private EditText name , addDetails , phone;
	private EditText email;
	private EditText pwd , oldpassword;

	private String nameStr;
	private String emailStr;
	private String newPwdStr;
	private String oldpasswordStr,phoneStr;
	TextView email_error;

	private int edittextColor;
	LinearLayout password_lr;
	String addDetailsTxt;
    private LocationManager mLocationManager;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_editprofile);

		name 				= ((EditText)findViewById(R.id.name));
		addDetails 			= ((EditText)findViewById(R.id.uname));
		email 				= ((EditText)findViewById(R.id.email));
		pwd  				= ((EditText)findViewById(R.id.password));
		oldpassword  		= ((EditText)findViewById(R.id.oldpasword));
        phone               = (EditText)findViewById(R.id.contact);
		email_error			= (TextView)findViewById(R.id.email_error);
        map_address_txt	    = (FotTextView) findViewById(R.id.location_detailsetx);
		password_lr			= (LinearLayout) findViewById(R.id.password_lr);

        edittextColor       = getResources().getColor(R.color.edit_txt_black);

   	 	this.pwd.setInputType(129);
   	 	this.oldpassword.setInputType(129);
   	 	Typeface font = Typeface.createFromAsset(getAssets(), "Exo2-Regular.otf");
		this.pwd.setTypeface(font);
		this.oldpassword.setTypeface(font);

        name.setText(getSharePrefs().getUserName());
		email.setText(getSharePrefs().getEmail());

		addDetails.setText(Uri.decode(getSharePrefs().getAddressName()));
        phone.setText(getSharePrefs().getPhone());
        map_address_txt.setText(Uri.decode(getSharePrefs().getAddress()));


        latitude 		= getSharePref().getLatitude();
        longitude		= getSharePref().getLongitude();

        map_address_txt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openLocation();
			}
		});
   	 	emailBox();

        mLocationManager                = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0, mLocationListener);
	}

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            latitude           = location.getLatitude();
            longitude          = location.getLongitude();
            locationLoaded     = true;
            globalLog("XLOCATION :: "+location+" -- "+locationLoaded);
            try{mLocationManager.removeUpdates(mLocationListener);}catch (Exception e){}
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{mLocationManager.removeUpdates(mLocationListener);}catch (Exception e){}

    }

    @Override
    protected void onResume() {
        super.onResume();
        try
        {
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals(""))
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }catch (Exception e){}
        try{mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);}catch (Exception e){}
    }



    public void submitvalues(View v)
	{

		if (verify())
       {

			try
            {
					nameStr = Uri.encode(nameStr,"UTF-8");
					addDetailsTxt	= addDetails.getText().toString();

					try
					{
                        if(addressJSON == null)
                        {
                            addressJSON  = new JSONObject();
                            addressJSON.put("name",Uri.decode(getSharePrefs().getAddressName()));
                            addressJSON.put("latitude",latitude);
                            addressJSON.put("longitude",longitude);
                            addressJSON.put("address",Uri.decode(getSharePrefs().getAddress()));
                        }
						if(!addDetailsTxt.equals(""))
							addressJSON.put("name",addDetailsTxt);
					}catch (Exception e){}

					JSONObject jobj         = new JSONObject();
					jobj.put("email",emailStr);
					jobj.put("name",nameStr);
					jobj.put("phone",phoneStr);
					jobj.put("address",addressJSON+"");
					jobj.put("password",newPwdStr);
					jobj.put("currentpwd",oldpasswordStr);
					jobj.put("userthumb","");
					jobj.put("uid",getSharePrefs().getUid());
					jobj.put("caller","updateProfile");

					String encrypted        = cc.getEncryptedString(jobj.toString());
	            	AsyncHttpFunction http 	= new AsyncHttpFunction();
	                http.execute(encrypted);
            }
            catch (Exception localException)
            {
          	  localException.printStackTrace();
          	  Toast.makeText(EditProfile.this, localException.toString(), Toast.LENGTH_SHORT).show();
            }
       }
	}

	boolean verify()
	{

		resetErrorWarning();
	    nameStr 		= this.name.getText().toString();
	    emailStr 		= this.email.getText().toString();
	    newPwdStr 		= this.pwd.getText().toString();
		oldpasswordStr	= this.oldpassword.getText().toString();
        phoneStr        = phone.getText().toString();

	    if (nameStr.trim().equals(""))
	    {
			  name.requestFocus();
		      Toast.makeText(getApplicationContext(), getString(R.string.name_blank), Toast.LENGTH_SHORT).show();
		      return false;
	    }
	    else if (this.emailStr.equals(""))
	    {

	    	  email.requestFocus();
		      Toast.makeText(getApplicationContext(), getString(R.string.email_blank), Toast.LENGTH_SHORT).show();
		      return false;
	    }
		else if(oldpasswordStr.length() < 6)
		{
			oldpassword.requestFocus();
			Toast.makeText(getBaseActivity(), getString(R.string.old_password_cannot_b_blank), Toast.LENGTH_SHORT).show();
			return false;
		}

		else if (!validateEmail(this.emailStr))
		{
			email.requestFocus();
			Toast.makeText(getApplicationContext(), getString(R.string.email_invalid), Toast.LENGTH_SHORT).show();
			return false;
		}
	    else if (!newPwdStr.equals("") && this.newPwdStr.length() < 6)
	    {
	    	  pwd.requestFocus();
		      Toast.makeText(getApplicationContext(), getString(R.string.password_min_char), Toast.LENGTH_SHORT).show();
		      return false;
	    }

		if(!PhoneNumberUtils.isGlobalPhoneNumber(phoneStr) || (phoneStr.length() < 10))
		{
			phone.requestFocus();
			Toast.makeText(getBaseActivity(), getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
			return false;
		}


	    return true;
	}


	  public static boolean validateEmail(String paramString)
	  {
		  	Pattern pattern = Patterns.EMAIL_ADDRESS;
		  	return pattern.matcher(paramString).matches();
	  }

	  class AsyncHttpFunction extends AsyncTask<String, Void, String>
 	  {

			@Override
			protected String doInBackground(String... paramVarArgs) {
				return getConnection().sendPostData(paramVarArgs[0],null);
			}

	 	    @Override
	 	    protected void onPostExecute(String paramString)
	 	    {
	 	      super.onPostExecute(paramString);

		 	      try
		 	      {
		 	    	  if (paramString != null)
					  {
						  JSONObject result = new JSONObject(paramString);
						  if(result.getString("status").equals("1"))
						  {
							  if(result.getString("success").equals("1"))
								  onFinish(paramString);
							  else
							  {
								  int err                     = result.getInt("err");
								  if(err == 1)
								  {
									  email.setTextColor(Color.parseColor("#ff5555"));
									  email_error.setTextColor(Color.parseColor("#ff5555"));
									  email_error.setVisibility(View.VISIBLE);
									  email_error.setText(result.getString("message"));
								  }
							  }
						  }
						  else
							  Toast.makeText(EditProfile.this.getApplicationContext(), getString(R.string.error_response), Toast.LENGTH_SHORT).show();

					  }

		 	    	  else
		 	    		  Toast.makeText(EditProfile.this.getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
		 	      }
		 	      catch (Exception localException){
					  Toast.makeText(EditProfile.this.getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
				  }
		 	      finally
		 	      {
		 	    	 getFunction().dismissDialog();
		 	      }
	 	    }
	 	    @Override
	 	    protected void onPreExecute() {
	 	    	super.onPreExecute();
				getFunction().showDialog();
	 	    }


     }


 	 private void onFinish(String paramString)
 	 {
 	    String str = paramString.trim();
 	    try
 	    {
	 	    JSONObject jobj	=new JSONObject(str);
	 	    String status	= jobj.getString("status");
	 	    if(status.equals("1"))
	 	    {
	 	    	String success	= jobj.getString("success");
	 	    	String msg		= jobj.getString("message");
	 	    	if(success.equals("1"))
	 	    	{
	 	    		  boolean b = getSharePrefs().onFinish(new JSONObject(msg));
	 	    		  if(b)
	 	    		  {
						  Toast.makeText(EditProfile.this.getApplicationContext(), getString(R.string.updated_success), Toast.LENGTH_LONG).show();
	 			 	      Intent localIntent = new Intent(this, EditProfile.class);
	 			 	      localIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 			 	      startActivity(localIntent);
	 	    		  }

	 	    	}

	 	    }
	 	    else
	 	    	Toast.makeText(getApplicationContext(), getString(R.string.error_updating_profile), Toast.LENGTH_SHORT).show();
 	    }
 	    catch(Exception e)
 	    {
 	    		e.printStackTrace();
 	    		Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
 	    }

 	 }



 	class AsyncHttpFunctionAjax extends AsyncTask<String, Void, String>
	{
	    protected String doInBackground(String[] paramArrayOfString)
	    {
	      return getConnection().sendPostData(paramArrayOfString[0],null);
	    }

	    protected void onPostExecute(String paramString)
	    {
	      super.onPostExecute(paramString);
	      if (paramString != null)
	      {
	    	  try
	    	  {
		    	  	JSONObject jobj	=new JSONObject(paramString);
			 	    String status	= jobj.getString("status");
			 	    if(status.equals("1"))
			 	    {
			 	    	String success	= jobj.getString("success");
			 	    	String msg		= jobj.getString("message");
			 	    	if(success.equals("0"))
			 	    	{
							int err                     = jobj.getInt("err");
							if(err == 1)
							{
								email.setTextColor(Color.parseColor("#ff5555"));
								email_error.setTextColor(Color.parseColor("#ff5555"));
								email_error.setVisibility(View.VISIBLE);
								email_error.setText(msg);
							}

			 	    	}
			 	    	else if(success.equals("1"))
			 	    	{
			 	    		email.setTextColor(Color.parseColor("#19845e"));
			 	    	}
			 	    }
	    	  }catch(Exception e){
	    		  e.printStackTrace();
	    	  }
	      }

	    }
	 }


	 void emailBox()
 	 {
	 		this.email.setOnFocusChangeListener(new OnFocusChangeListener()
	 	    {
	 	      public void onFocusChange(View paramAnonymousView, boolean paramAnonymousBoolean)
	 	      {
	 	        if ((!EditProfile.this.email.hasFocus()) && (!EditProfile.this.email.getText().toString().equals("")) && (EditProfile.this.validateEmail(EditProfile.this.email.getText().toString())))
	 	        {
					try
					{
						JSONObject jobj         = new JSONObject();
						jobj.put("email",Uri.encode(email.getText().toString(),"UTF-8"));
						jobj.put("myid",getSharePrefs().getUid());
						jobj.put("caller",WEB_AJAX);

						String encrypted        = cc.getEncryptedString(jobj.toString());

						AsyncHttpFunctionAjax localAsyncHttpFunctionAjax = new AsyncHttpFunctionAjax();
						localAsyncHttpFunctionAjax.execute(encrypted);
					}catch (Exception e){
						e.printStackTrace();
					}

	 	        }
	 	        else if (!validateEmail(email.getText().toString())&&(!EditProfile.this.email.hasFocus()) && (!EditProfile.this.email.getText().toString().equals("")))
	 	        {
	 	        	Toast.makeText(EditProfile.this.getApplicationContext(), getString(R.string.email_invalid), Toast.LENGTH_SHORT).show();
	 	        }
	 	        
	 	      }
	 	    });
	 	    this.email.addTextChangedListener(new TextWatcher()
	 	    {
	 	      public void afterTextChanged(Editable paramAnonymousEditable)
	 	      {
	 	      }
	
	 	      public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
	 	      {
	 	      }
	
	 	      public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
	 	      {
				  resetErrorWarning();
	 	      }
	 	    });
 	 }


	void resetErrorWarning()
	{
		email.setTextColor(edittextColor);
		email_error.setVisibility(View.GONE);
		email_error.setText("");

	}
 	

}
