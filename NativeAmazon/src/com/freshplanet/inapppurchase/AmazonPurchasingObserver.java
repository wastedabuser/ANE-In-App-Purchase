//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

package com.freshplanet.inapppurchase;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.GetUserIdResponse;
import com.amazon.inapp.purchasing.ItemDataResponse;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.Receipt;

/**
 * Purchasing Observer will be called on by the Purchasing Manager
 * asynchronously. Since the methods on the UI thread of the application, all
 * fulfillment logic is done via an AsyncTask. This way, any intensive processes
 * will not hang the UI thread and cause the application to become unresponsive.
 */
public class AmazonPurchasingObserver extends BasePurchasingObserver {

	private static final String TAG = "AmazonPurchasingObserver";

	/**
	 * Creates new instance of the ButtonClickerObserver class.
	 * 
	 * @param activity
	 *            Activity context
	 */
	public AmazonPurchasingObserver(final Activity activity) {
		super(activity);
	}

	/**
	 * Invoked once the observer is registered with the Puchasing Manager If the
	 * boolean is false, the application is receiving responses from the SDK
	 * Tester. If the boolean is true, the application is live in production.
	 * 
	 * @param isSandboxMode
	 *            Boolean value that shows if the app is live or not.
	 */
	@Override
	public void onSdkAvailable(final boolean isSandboxMode) {
		Log.v(TAG, "onSdkAvailable recieved: Response -" + isSandboxMode);
		PurchasingManager.initiateGetUserIdRequest();
	}

	/**
	 * Invoked once the call from initiateGetUserIdRequest is completed. On a
	 * successful response, a response object is passed which contains the
	 * request id, request status, and the userid generated for your
	 * application.
	 * 
	 * @param getUserIdResponse
	 *            Response object containing the UserID
	 */
	@Override
	public void onGetUserIdResponse(final GetUserIdResponse getUserIdResponse) {
		Log.v(TAG, "onGetUserIdResponse recieved: Response -" + getUserIdResponse);
		Log.v(TAG, "RequestId:" + getUserIdResponse.getRequestId());
		Log.v(TAG, "IdRequestStatus:" + getUserIdResponse.getUserIdRequestStatus());
		// new GetUserIdAsyncTask().execute(getUserIdResponse);
	}

	/**
	 * Invoked once the call from initiateItemDataRequest is completed. On a
	 * successful response, a response object is passed which contains the
	 * request id, request status, and a set of item data for the requested
	 * skus. Items that have been suppressed or are unavailable will be returned
	 * in a set of unavailable skus.
	 * 
	 * @param itemDataResponse
	 *            Response object containing a set of
	 *            purchasable/non-purchasable items
	 */
	@Override
	public void onItemDataResponse(final ItemDataResponse itemDataResponse) {
		Log.v(TAG, "onItemDataResponse recieved");
		Log.v(TAG, "ItemDataRequestStatus" + itemDataResponse.getItemDataRequestStatus());
		Log.v(TAG, "ItemDataRequestId" + itemDataResponse.getRequestId());
		// new ItemDataAsyncTask().execute(itemDataResponse);
	}

	/**
	 * Is invoked once the call from initiatePurchaseRequest is completed. On a
	 * successful response, a response object is passed which contains the
	 * request id, request status, and the receipt of the purchase.
	 * 
	 * @param purchaseResponse
	 *            Response object containing a receipt of a purchase
	 */
	@Override
	public void onPurchaseResponse(final PurchaseResponse purchaseResponse) {
		Log.d(TAG, "onPurchaseResponse recieved");
		Log.d(TAG, "PurchaseRequestStatus:" + purchaseResponse.getPurchaseRequestStatus());
		switch (purchaseResponse.getPurchaseRequestStatus()) {
		case SUCCESSFUL:
			final Receipt receipt = purchaseResponse.getReceipt();
			printReceipt(purchaseResponse.getReceipt());
			switch (receipt.getItemType()) {
			case CONSUMABLE:
				JSONObject jsonObject = new JSONObject();
				try {
					JSONObject obj = new JSONObject();
					obj.put("sky", receipt.getSku());
					obj.put("type", receipt.getItemType());
					obj.put("userId", purchaseResponse.getUserId());
					obj.put("requestId", purchaseResponse.getRequestId());
					obj.put("purchaseToken", receipt.getPurchaseToken());
					jsonObject.put("receipt", obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					jsonObject.put("receiptType", "Amazon");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Log.i(TAG, jsonObject.toString());
				if (Extension.context != null) {
					Extension.context.dispatchStatusEventAsync("PURCHASE_SUCCESSFUL",
						jsonObject.toString());
				} else {
					Log.e(TAG, "context is null");
				}
				Log.i(TAG, "Successful purchase for request" + receipt.getSku());
				return;
			case ENTITLED:
				Log.e(TAG, "ENTITLED not supported" + receipt.getSku());
				break;
			case SUBSCRIPTION:
				Log.e(TAG, "SUBSCRIPTION not supported" + receipt.getSku());
				break;
			}
			if (Extension.context != null) {
				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR",
					"unsupported type of purchase");
			} else {
				Log.e(TAG, "context is null");
			}
			return;
		case ALREADY_ENTITLED:
			/*
			 * If the customer has already been entitled to the item, a receipt
			 * is not returned. Fulfillment is done unconditionally, we
			 * determine which item should be fulfilled by matching the request
			 * id returned from the initial request with the request id stored
			 * in the response.
			 */
			Log.e(TAG, "ENTITLED not supported");

			if (Extension.context != null) {
				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR",
					"unsupported type of purchase");
			} else {
				Log.e(TAG, "context is null");
			}
		case FAILED:
			/*
			 * If the purchase failed for some reason, (The customer canceled
			 * the order, or some other extraneous circumstance happens) the
			 * application ignores the request and logs the failure.
			 */
			Log.e(TAG, "Failed purchase for request" + purchaseResponse.getRequestId());

			if (Extension.context != null) {
				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "purchase FAILED");
			} else {
				Log.e(TAG, "context is null");
			}
		case INVALID_SKU:
			/*
			 * If the sku that was purchased was invalid, the application
			 * ignores the request and logs the failure. This can happen when
			 * there is a sku mismatch between what is sent from the application
			 * and what currently exists on the dev portal.
			 */
			Log.e(TAG, "Invalid Sku for request " + purchaseResponse.getRequestId());

			if (Extension.context != null) {
				Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", "purchase FAILED");
			} else {
				Log.e(TAG, "context is null");
			}
			return;
		}
	}

	/**
	 * Is invoked once the call from initiatePurchaseUpdatesRequest is
	 * completed. On a successful response, a response object is passed which
	 * contains the request id, request status, a set of previously purchased
	 * receipts, a set of revoked skus, and the next offset if applicable. If a
	 * user downloads your application to another device, this call is used to
	 * sync up this device with all the user's purchases.
	 * 
	 * @param purchaseUpdatesResponse
	 *            Response object containing the user's recent purchases.
	 */
	@Override
	public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse purchaseUpdatesResponse) {
		Log.v(TAG, "onPurchaseUpdatesRecived recieved: Response -" + purchaseUpdatesResponse);
		Log.v(
			TAG,
			"PurchaseUpdatesRequestStatus:"
				+ purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus());
		Log.v(TAG, "RequestID:" + purchaseUpdatesResponse.getRequestId());
		// new PurchaseUpdatesAsyncTask().execute(purchaseUpdatesResponse);
	}

	/*
	 * Helper method to print out relevant receipt information to the log.
	 */
	private void printReceipt(final Receipt receipt) {
		Log.v(
			TAG,
			String.format("Receipt: ItemType: %s Sku: %s SubscriptionPeriod: %s",
				receipt.getItemType(), receipt.getSku(), receipt.getSubscriptionPeriod()));
	}
}
