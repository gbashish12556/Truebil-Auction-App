package com.truebil.business.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Fragments.LoginModalFragment;
import com.truebil.business.Fragments.OTPVerificationFragment;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Fragments.VerificationSuccessFragment;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements
        LoginModalFragment.LoginModalFragmentListener,
        OTPVerificationFragment.OTPVerificationFragmentListener {

    FragmentManager fragmentManager;
    private static final String TAG = "LoginActivity";
    SharedPreferences sharedPref;
    private RequestQueue queue;

    @Override
    protected void onStart() { // Need to check dealer status when user opens the app after minimizing
        super.onStart();

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        boolean hasLoggedInBefore = sharedPref.getBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, false);
        if (hasLoggedInBefore) {
            checkDealerStatus();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        boolean hasLoggedInBefore = sharedPref.getBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, false);
        if (hasLoggedInBefore) {
            checkDealerStatus();
        }
        else {
            Helper.putPreference(this,Constants.Keys.CITIES,"");
            Helper.putPreference(this,Constants.Keys.STATES,"");
            LoginModalFragment loginModalFragment = new LoginModalFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.activity_login_frame_layout, loginModalFragment)
                    .commit();
        }
    }

    @Override
    public void onSendMobileButtonClicked(String mobileNumber, String name) {

        OTPVerificationFragment otpVerificationFragment = new OTPVerificationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Keys.MOBILE, mobileNumber);
        bundle.putString(Constants.Keys.USERNAME, name);
        otpVerificationFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .replace(R.id.activity_login_frame_layout, otpVerificationFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onVerifyOTPSuccess(boolean isVerifiedFromAdmin, String salesPersonName, String salesPersonMobile) {

        if (isVerifiedFromAdmin) {
            Intent intent = new Intent(this, BiddingActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            VerificationSuccessFragment verificationSuccessFragment = new VerificationSuccessFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Keys.SALES_PERSON_NAME, salesPersonName);
            bundle.putString(Constants.Keys.SALES_PERSON_MOBILE, salesPersonMobile);
            verificationSuccessFragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.activity_login_frame_layout, verificationSuccessFragment)
                    .commit();
        }
    }

    @Override
    public void onEditMobileButtonClicked() {
        LoginModalFragment loginModalFragment = new LoginModalFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.activity_login_frame_layout, loginModalFragment)
                .commit();
    }

    void checkDealerStatus() {

        if (queue == null)
            queue = Volley.newRequestQueue(getApplicationContext());

        String url = Constants.Config.API_PATH + "/dealer_status/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject apiResponse) {

                        try {
                            JSONObject details = apiResponse.getJSONObject("details");
                            boolean isVerifiedFromAdmin = details.getBoolean("is_verified_from_admin");
                            String salesPersonName = details.getJSONObject("sales_rep_info").getString("Name");
                            String salesPersonMobile = details.getJSONObject("sales_rep_info").getString("Mobile");
                            salesPersonMobile = "02262459799";

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(Constants.SharedPref.SALES_PERSON_NAME,salesPersonName);
                            editor.putString(Constants.SharedPref.SALES_PERSON_MOBILE,salesPersonMobile);

                            if (isVerifiedFromAdmin) { // Go to Bidding Activity

                                editor.putBoolean(Constants.SharedPref.IS_VERIFIED_FROM_ADMIN, true);
                                Intent intent = new Intent(getApplicationContext(), BiddingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else { // Verification Pending
                                VerificationSuccessFragment verificationSuccessFragment = new VerificationSuccessFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.Keys.SALES_PERSON_NAME, salesPersonName);
                                bundle.putString(Constants.Keys.SALES_PERSON_MOBILE, salesPersonMobile);
                                verificationSuccessFragment.setArguments(bundle);

                                fragmentManager.beginTransaction()
                                        .replace(R.id.activity_login_frame_layout, verificationSuccessFragment)
                                        .commitAllowingStateLoss(); // .commit() should not be called inside async task.
                            }

                            editor.apply();
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
                            additionalLogJson.put("JAVA_FILE", "LoginActivity.java");
                            VolleyService.handleVolleyError(error, additionalLogJson, true, getApplicationContext());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
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

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}
