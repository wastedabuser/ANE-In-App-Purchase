package com.freshplanet.inapppurchase;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BillingActivity extends Activity {

	private static String TAG = "BillingActivity";
	
	public static String MAKE_PURCHASE = "MakePurchase";
	public static String MAKE_SUBSCRIPTION = "MakeSubscription";
	
    static final int RC_REQUEST = 10001;

    public static BillingActivity instance = null;
    
    
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
            	Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
            	finish();
            	if (BillingActivity.instance != null)
            	{
            		BillingActivity.instance.finish();
            	}
            	return;
            }


           JSONObject resultObject = new JSONObject();
           try {
        	   JSONObject resultInfoObject = new JSONObject();
        	   resultInfoObject.put("signedData", purchase.getOriginalJson());
        	   resultInfoObject.put("signature", purchase.getSignature());
        	   resultObject.put("receipt", resultInfoObject);
               resultObject.put("receiptType", "GooglePlay");
           } catch (JSONException e) {
        	   e.printStackTrace();
           }
            
            Extension.context.dispatchStatusEventAsync("PURCHASE_SUCCESSFUL", resultObject.toString());
            Log.d(TAG, "Purchase successful.");
        	if (BillingActivity.instance != null)
        	{
        		BillingActivity.instance.finish();
        	}
        }
    };

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "create activity");
		super.onCreate(savedInstanceState);

		Bundle values = this.getIntent().getExtras();
		String mtype = values.getString("type");

		Log.d(TAG, "type : "+mtype);
		
		IabHelper mHIabHelper = ExtensionContext.mHelper;
		
		if (mtype == null)
		{
			Log.e(TAG, "unsupported type: "+mtype);
		} else if (mtype.equals(MAKE_PURCHASE)) {
			Log.d(TAG, "starting "+mtype);
			String purchaseId = values.getString("purchaseId");
			try
			{
				mHIabHelper.launchPurchaseFlow(this, purchaseId, RC_REQUEST, 
		                mPurchaseFinishedListener, null);
			} catch( IllegalStateException e)
			{
				finish();
            	Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
				return;
			}

		} else if (mtype.equals(MAKE_SUBSCRIPTION)) {
			Log.d(TAG, "starting "+mtype);
			String purchaseId = values.getString("purchaseId");
			try
			{
				mHIabHelper.launchSubscriptionPurchaseFlow(this, purchaseId, RC_REQUEST, 
	                mPurchaseFinishedListener);
			} catch( IllegalStateException e)
			{
				finish();
            	Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "ERROR");
				return;
			}

		} else {
			Log.e(TAG, "unsupported type: "+mtype);
		}
		Log.d(TAG, "creation done");
		BillingActivity.instance = this;
	}
	
	@Override
	protected void onStart()
	{
		Log.d(TAG, "start activity");
		super.onStart();
	}
    
	@Override
    protected void onRestart()
	{
		Log.d(TAG, "restart activity");
		super.onRestart();
		finish();
	}

	@Override
    protected void onResume(){
		Log.d(TAG, "resume activity");
		super.onResume();
	}

	@Override
    protected void onPause(){
		Log.d(TAG, "pause activity");
		super.onPause();
	}

	@Override
    protected void onStop(){
		Log.d(TAG, "stop activity");
		super.onStop();
	}

	@Override
    protected void onDestroy(){
		Log.d(TAG, "destroy activity");
		super.onDestroy();
		BillingActivity.instance = null;
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (ExtensionContext.mHelper != null)
        {
        	Log.e(TAG, "IabHelper is null");
        	ExtensionContext.mHelper.handleActivityResult(requestCode, resultCode, data);
        } else
        {
        	Log.e(TAG, "IabHelper is null");
        }

//		finish();
	}

	
}
