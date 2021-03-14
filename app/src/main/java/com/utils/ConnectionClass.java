package com.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import org.apache.http.entity.mime.MultipartEntity;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import static com.tam.winati.ksa.BaseActivity.appLog;


public class ConnectionClass {
	
	Context context;
//	public static String BASE_URL		= "http://tamwinati.touran.sa/";
	public static String BASE_URL		= "http://dalilalqahwa.com/app/";
	public static String SERVER_URL		= BASE_URL+"_customer2.php";
	public static String PLACEHOLDER	= BASE_URL+"placeholder.png";
	public static String THUMB_URL		= BASE_URL+"images/";
	public static String WEB_SIGNUP     = "signup";
	public static String WEB_AJAX       = "ajax";
	public static String WEB_AJAX_UID	= "ajaxUsername";
	public static String WEB_RESET      = "resetPassword";


	SharePrefsEntry sp;
	public ConnectionClass(Context ctx) {
		this.context=ctx;
		sp  = new SharePrefsEntry(context);
	}


	public String connectToServerFunc(String urlLink)
	{
		
		try 
		{
			urlLink=SERVER_URL+"?"+urlLink;
            locLog("request :: "+urlLink);
			HttpClient client = new DefaultHttpClient();
	        HttpPost http_get = new HttpPost(urlLink);
	        http_get.addHeader("app_d", "1");
			try{http_get.addHeader("m", sp.getUid()+"");}catch(Exception e){}
	        HttpResponse responses;
	        responses = client.execute(http_get);
	        if (responses != null) 
	        {
	            InputStream in = responses.getEntity().getContent(); 
	            String a = convertStreamToString(in);
                locLog("RESPONSES :: "+a);
				return a;
	        }
	        
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}


    public String connectToServerFuncLink(String urlLink)
    {

        try
        {
            urlLink=BASE_URL+urlLink;
            locLog("request :: "+urlLink);
            HttpClient client = new DefaultHttpClient();
            HttpPost http_get = new HttpPost(urlLink);
            http_get.addHeader("app_d", "1");
            try{http_get.addHeader("m", sp.getUid());}catch(Exception e){}
            HttpResponse responses;
            responses = client.execute(http_get);
            if (responses != null)
            {
                InputStream in = responses.getEntity().getContent();
                String a = convertStreamToString(in);
                locLog("RESPONSES :: "+a);
                return a;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
	
	
	
	String convertStreamToString(InputStream is) {

	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try
	    {
	        while ((line = reader.readLine()) != null) 
	        {
	            sb.append(line);
	        }
	    } 
	    catch (Exception e)
	    {
	    	
	    	 //Toast.makeText(context, e.toString()+" io2", Toast.LENGTH_LONG).show();
	    } 
	    finally 
	    {
	        try 
	        {
	            is.close();
	        } 
	        catch (Exception e)
	        {
	        	
	        	//Toast.makeText(context, e.toString()+" io3", Toast.LENGTH_LONG).show();
	        }
	    }
	    return sb.toString();
	}

	public String fetchUsingGet(String urlLink)
	{

		try
		{
			locLog("request :: "+urlLink);
			HttpClient client = new DefaultHttpClient();
			HttpPost http_get = new HttpPost(urlLink);
			http_get.addHeader("app_d", "1");
			try{http_get.addHeader("m", sp.getUid());}catch(Exception e){}
			HttpResponse responses;
			responses = client.execute(http_get);
			if (responses != null)
			{
				InputStream in = responses.getEntity().getContent();
				String a = convertStreamToString(in);
				locLog("RESPONSES :: "+a);
				return a;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}


	public String sendPHPInput(String path,JSONObject json)
	{
		if(!path.startsWith(BASE_URL))
			path = BASE_URL+path;
        locLog("sendPHPInput :: "+path);
		HttpClient client 		= new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
		HttpResponse response;
		try
		{
			HttpPost post = new HttpPost(path);
			post.setHeader("json", json.toString());
			StringEntity se = new StringEntity(json.toString());
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
			post.setEntity(se);
			response = client.execute(post);
			if (response != null)
			{
				InputStream in = response.getEntity().getContent(); // Get the
				String a = convertStreamToString(in);
                locLog("RESPONSES :: "+a);
				return a;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

    public String sendFilePHP(String path, File file)
    {
        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(path);

            InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(true);
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);
            if (response != null)
            {
                InputStream in = response.getEntity().getContent(); // Get the
                String a = convertStreamToString(in);
                locLog("RESPONSES :: "+a);
                return a;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

	public String connectToServerFunc_NameValue(List<NameValuePair> params) {
		try 
		{
			HttpClient client = new DefaultHttpClient();
	        HttpPost http_get = new HttpPost(SERVER_URL);
	        http_get.addHeader("app_d", "1");
			try{http_get.addHeader("m", sp.getUid());}catch(Exception e){}
	        http_get.setEntity(new UrlEncodedFormEntity(params));
	        HttpResponse responses;
	        responses = client.execute(http_get);
	        
	        
	        if (responses != null) 
	        {
	            InputStream in = responses.getEntity().getContent(); 
	            String a = convertStreamToString(in);
                locLog("RESPONSES :: "+a);
	            return a;
	        }
	        
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}

    void locLog(String log)
    {
            Log.e("AndroidRuntime",log+"");
    }


    public String sendPostData(String para , File file)
    {
        String result = "";
        try
        {
			SimpleDateFormat fmt 			= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString 				= fmt.format(new Date());
            HttpClient httpClient           = new DefaultHttpClient();
            HttpContext localContext        = new BasicHttpContext();
            HttpPost httpPost               = new HttpPost(SERVER_URL);
			httpPost.addHeader("currentdate",dateString);
			httpPost.addHeader("language",sp.getLanguage());
			MultipartEntity entity          = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("txt",           new StringBody(para));
                if(file != null)
                entity.addPart("file",          new FileBody(file));

            httpPost.setEntity(entity);
            HttpResponse response           = httpClient.execute(httpPost, localContext);
            HttpEntity httpEntity           = response.getEntity();
            result                          = EntityUtils.toString(httpEntity);

           locLog("RESPONSES :: "+result);


        }catch (Exception e){
            e.printStackTrace();
        }

        return result;

    }


	public String getEncryptedString(String obj)
	{

        for(int i=0;i<10;i++)
        {
            try
            {
                byte[] data     = obj.getBytes("UTF-8");
                obj             = Base64.encodeToString(data, Base64.DEFAULT);
            }catch (Exception e){}

        }
		return obj;
	}

}
