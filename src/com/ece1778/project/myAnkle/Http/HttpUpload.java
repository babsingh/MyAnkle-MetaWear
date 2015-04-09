package com.ece1778.project.myAnkle.Http;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.ece1778.project.myAnkleUser.BuildConfig;

public class HttpUpload {
	
	public static final String TAG = HttpUpload.class.getSimpleName();
	
	private httpUploadListener mListener = null; 
		
    public HttpUpload(httpUploadListener listener){        
    	mListener = listener;
    }
    
    public interface httpUploadListener{
    	public void returnResponse(String response);     	
    }
	
	
	public void execute(String urlString,  String fileName, List<BasicNameValuePair> params, FileInputStream fileInputStream){
		
		String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        
        StringBuffer response =new StringBuffer();
			
		try
        {
        
        	if(BuildConfig.DEBUG) Log.i(TAG,"Starting Http File Sending to URL");

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection)(new URL(urlString)).openConnection();

            // Allow Inputs
            conn.setDoInput(true);

            // Allow Outputs
            conn.setDoOutput(true);

            // Don't use a cached copy.
            conn.setUseCaches(false);

            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());                
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"filename\""+ lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(fileName);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);               
                
            for (Iterator<BasicNameValuePair> itr = params.iterator(); itr.hasNext();){
            	BasicNameValuePair bnvp = (BasicNameValuePair) itr.next();
            	if(BuildConfig.DEBUG) Log.d(TAG,bnvp.getName().toString() + " : " + bnvp.getValue().toString());
                                	
            	dos.writeBytes("Content-Disposition: form-data; name=\"" + bnvp.getName().toString() + "\""+ lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(bnvp.getValue().toString());
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                
            }
                 
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
            dos.writeBytes(lineEnd);

            if(BuildConfig.DEBUG) Log.i(TAG,"Headers are written");

            // create a buffer of maximum size
            int bytesAvailable = fileInputStream.available();
                
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[ ] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0,bufferSize);
            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            fileInputStream.close();
                
            dos.flush();
                
            if(BuildConfig.DEBUG) Log.i(TAG,"File Sent, Response: "+String.valueOf(conn.getResponseCode()));
                 
            InputStream is = conn.getInputStream();
                
            // retrieve the response from server
            int ch;

            
            while( ( ch = is.read() ) != -1 ){ response.append( (char)ch ); }
            final String s=response.toString();
            if(BuildConfig.DEBUG) Log.d(TAG,s);
            dos.close();         

        }
		catch (MalformedURLException e)
        {
            if(BuildConfig.DEBUG) Log.e(TAG, "URL error: " + e.getMessage(), e);
        }
		catch (IOException e)
        {
        	if(BuildConfig.DEBUG) Log.e(TAG, "IO error: " + e.getMessage(), e);
        }
		
		if(mListener != null) mListener.returnResponse(response.toString());
	}
	
}
