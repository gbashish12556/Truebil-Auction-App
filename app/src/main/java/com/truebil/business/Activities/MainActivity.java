package com.truebil.business.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.BuildConfig;
import com.truebil.business.Constants;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.RemoteConfigUpdateCheck;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements RemoteConfigUpdateCheck.RemoteConfigUpdateCheckInterface{

    final static private String TAG = "MainActivity";
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("APP_PREFS",Context.MODE_PRIVATE);

        // Read Shared Preferences
        if (BuildConfig.DEBUG) {
            for (Map.Entry<String, ?> entry : sharedPref.getAll().entrySet()) {
                Log.d(TAG, "Map values " + entry.getKey() + ": " + entry.getValue().toString());
            }
        }

        /*
         * Perform playstore app version check using firebase remote config.
         * If force_update == true, then show alert dialog prompting app update.
         * Otherwise, now proceed to check "is user verified by admin" using dealer_status api
         * Depending on response, either direct user to LoginActivity or BiddingActivity.
         */
        new RemoteConfigUpdateCheck(this);
    }

    @Override
    public void onAppUpdateNotRequired() {
        // Always check dealer status before allowing BiddingActivity access
        boolean hasLoggedInBefore = sharedPref.getBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, false);
        if (hasLoggedInBefore) {
            checkDealerStatus();
        }
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onForcedAppUpdateRequired() {

        if (!MainActivity.this.isFinishing()) {
            /*
             * Display the AlertDialog only when the activity is NOT finishing.
             * This bug was detected through crashlytics
             */
            new AlertDialog.Builder(this)
                    .setTitle("Update Required")
                    .setMessage("Truebil for Business needs to be updated to continue. Update to the latest version?")
                    .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openPlaystoreListing();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create()
                    .show();
        }
    }

    void dealerStatusNavigation(boolean isVerifiedFromAdmin) {
        if (isVerifiedFromAdmin) { // Go to Bidding Activity
            Intent intent = new Intent(getApplicationContext(), BiddingActivity.class);
            startActivity(intent);
        }
        else { // Verification Pending
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        finish();
    }

    void checkDealerStatus() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.Config.API_PATH + "/dealer_status/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject apiResponse) {
                        try {
                            JSONObject details = apiResponse.getJSONObject("details");
                            boolean isVerifiedFromAdmin = details.getBoolean("is_verified_from_admin");

                            dealerStatusNavigation(isVerifiedFromAdmin);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/dealer_status");
                            additionalLogJson.put("JAVA_FILE", "MainActivity.java");
                            VolleyService.handleVolleyError(error, additionalLogJson, true, getApplicationContext());

                            //Check for JWT Token Expiration
                            handleJWTTokenExpiration(error);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.Config.MAX_TIMEOUT_TIME, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    private void openPlaystoreListing() {
        String playstoreUrl = "https://play.google.com/store/apps/details?id=com.truebil.business";
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playstoreUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void handleJWTTokenExpiration(VolleyError error) {
        if (error instanceof AuthFailureError) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            sharedPref.edit().putBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, false).apply();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
}