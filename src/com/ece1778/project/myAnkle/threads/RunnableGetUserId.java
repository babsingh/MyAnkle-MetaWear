package com.ece1778.project.myAnkle.threads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.ece1778.project.myAnkle.Http.HttpGetRequest;
import com.ece1778.project.myAnkle.Http.HttpGetRequest.httpGetResponseListener;


public class RunnableGetUserId implements Runnable{

	private httpGetResponseListener mListener;
	private Context mContext;
	private String mWebAddess;
	
	public RunnableGetUserId(Context context, String webAddress, httpGetResponseListener listener) {
		mContext = context;
		mListener = listener;
		mWebAddess = webAddress;
	}
	
	public RunnableGetUserId(Context context, String webAddress) {
		this(context,webAddress, null);
	}
	
	@Override
	public void run() {
		
		// Check if the Internet is connected
		ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
		
		if(netInfo != null && netInfo.isConnected()) {
			// create the sever interaction object and execute
			HttpGetRequest httpGet = new HttpGetRequest( mListener );
			httpGet.execute(mWebAddess, null);
		}
		
	}

}
