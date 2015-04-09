package com.ece1778.project.myAnkle.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.ece1778.project.myAnkleUser.BuildConfig;

public class HttpPostRequest {

	public static final String TAG = HttpPostRequest.class.getSimpleName();

	private httpPostResponseListener mListener = null;    
	
    public HttpPostRequest(httpPostResponseListener listener){        
    	mListener = listener;
    }
    
    public interface httpPostResponseListener{
    	public void returnResponse(String response);     	
    }
		
	public void execute(String uriString, List<BasicNameValuePair> params){
		
		if(BuildConfig.DEBUG) Log.i(TAG, "Initiate and http put request");
	    final StringBuilder response = new StringBuilder();
		
		try{
			
	    	HttpPost post = new HttpPost();
	    	post.setURI(new URI(uriString));
	    	post.setEntity(new UrlEncodedFormEntity(params));
	    	DefaultHttpClient httpClient = new DefaultHttpClient();
	    	HttpResponse httpResponse = httpClient.execute(post);
      	        
	        if (httpResponse.getStatusLine().getStatusCode() == 200) {
	            
	        	if(BuildConfig.DEBUG) Log.i(TAG, "Httpp post succeeded");
	            
	        	HttpEntity messageEntity = httpResponse.getEntity();
	            InputStream is = messageEntity.getContent();
	            BufferedReader br = new BufferedReader(new InputStreamReader(is));
	            String line;
	            
	            while ((line = br.readLine()) != null) {
	                response.append(line + "\n");
	            }            
	            
	        } else {
	        	if(BuildConfig.DEBUG) Log.d(TAG, "Http post status code is not 200");
	        }
        }catch(URISyntaxException e){        	
        	if(BuildConfig.DEBUG) Log.e(TAG,"URI Malformatted : " + e.getMessage());
        	
	    } catch (IOException e) {	        
	    	if(BuildConfig.DEBUG) Log.e(TAG, "IO expection: " + e.getMessage());
	    	
	    }	    

		if(mListener != null) mListener.returnResponse(response.toString());				
		    
    }	
}
