package com.truebil.business.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.truebil.business.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class VolleyService {

    private static final String TAG = "VolleyService";

    public static String handleVolleyError(VolleyError error, JSONObject additionalLogData, boolean displayToast, @NonNull Context context) {

        String toastMessage;

        /*
         * Two dealer bid simultaneously
         */
        if (isDealerBidError(error)) {
            toastMessage = "Someone placed the bid earlier! Please bid again!";
        }

        /*
         * If auto bid amount placed is less than acceptable
         */
        else if (isAutoBidAmountLow(error)) {
            toastMessage = "Your auto bid amount is too low. Please try again.";
        }

        /*
         * 1) Either time out or
         * 2) there is no connection or
         * 3) there was network error while performing the request
         */
        else if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
            toastMessage = "Unstable internet Connection! Please check your connection.";
        }

        /*
         * 1) server responded with a error response or
         * 2) there was an Authentication Failure while performing the request or
         * 3) the server response could not be parsed
         */
        else if (error instanceof ServerError || error instanceof ParseError) {
            toastMessage = "Internal Server Error";
        }
        else if (error instanceof AuthFailureError) {
            toastMessage = "Please Login Again";
        }
        else {
            toastMessage = "Please retry";
        }

        logVolleyError(error, additionalLogData, context);

        if (displayToast)
            displayToast(toastMessage, context);

        return getVolleyErrorMessage(error);
    }

    private static String getVolleyErrorMessage(VolleyError error) {
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {
            try {
                JSONObject obj = new JSONObject(new String(response.data));
                return obj.getString("message");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "NULL";
    }

    private static Boolean isDealerBidError(VolleyError error){
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null && response.statusCode == 400) {
            try {
                JSONObject obj = new JSONObject(new String(response.data));
                if (obj.has("code")) {
                    int errorCode = obj.getInt("code");
                    if (errorCode == 100) {
                        return true;
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean isAutoBidAmountLow(VolleyError error) {
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null && response.statusCode == 400) {
            try {
                JSONObject obj = new JSONObject(new String(response.data));
                if (obj.has("code")) {
                    int errorCode = obj.getInt("code");
                    if (errorCode == 101) {
                        return true;
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static int getVolleyErrorStatus(VolleyError error) {
        NetworkResponse response = error.networkResponse;
        if (response != null)
            return response.statusCode;
        return -1;
    }

    // Send info to Crashlytics along with user id, bid value, car id, etc.
    private static void logVolleyError(VolleyError error, JSONObject additionalLogData, Context context ) {

        // Find status code, network response from VolleyError object
        int statusCode = getVolleyErrorStatus(error);
        String networkResponseMessage = getVolleyErrorMessage(error);

        // Get Dealer Id
        int dealerId = -1;

        /*
         * Crashlytics Bug Report: context is sometimes null. We have also
         * a @Nonnull annotation for the context argument in the handleVolleyError() function
         */
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            dealerId = sharedPref.getInt(Constants.SharedPref.DEALER_ID, -1);
        }

        StringBuilder logString = new StringBuilder();
        logString.append("Status Code: ").append(statusCode);
        logString.append(". Network Response: ").append(networkResponseMessage);
        logString.append(". DEALER_ID: ").append(dealerId);

        // Fill remaining information passed in function
        if (additionalLogData != null) {
            try {
                for (int i = 0; i < additionalLogData.names().length(); i++) {
                    String key = additionalLogData.names().getString(i);
                    String value = additionalLogData.getString(key);
                    logString.append(". ").append(key).append(": ").append(value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Log error and dispatch to Crashlytics
        Log.d(TAG, "Volley Error: " + logString.toString());

        // Add the dealer to crashlytics
        if (dealerId != -1)
            Crashlytics.setUserIdentifier(String.valueOf(dealerId));

        Crashlytics.logException(new Exception(logString.toString()));
    }

    private static void displayToast(String toastString, Context context) {
        if (context == null)
            return;
        
        Toast.makeText(context, toastString, Toast.LENGTH_LONG).show();
    }
}