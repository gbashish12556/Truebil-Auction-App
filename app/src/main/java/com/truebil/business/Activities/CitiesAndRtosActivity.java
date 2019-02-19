package com.truebil.business.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Fragments.RTOSelectionFragment;
import com.truebil.business.Utils.VolleyIdlingResource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class CitiesAndRtosActivity extends AppCompatActivity implements RTOSelectionFragment.CitiesFragmentInterface {

    private ViewPager viewPager;
    CustomPagerAdapter customPagerAdapter;
    public TabLayout  tabLayout;
    JSONArray rto_states, rto_cities;
    protected RequestQueue volleyRequestQueue;
    Button submitButton;
    public String TAG = "RTOActivity";
    @Nullable public static VolleyIdlingResource mIdlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities_and_rtos);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        ImageButton backButton = findViewById(R.id.activity_rto_back_image_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCityRto()) {
                    onBackPressed();
                }
            }
        });

        getIdlingResource();
        sendVolleyRequest();
    }

    public void sendVolleyRequest() {
        String state_key_pref = Helper.getPreference(this, Constants.Keys.STATES);
        String city_key_pref = Helper.getPreference(this,Constants.Keys.CITIES);

        final String url = Constants.Config.API_PATH + "/filters/?state_code=" + state_key_pref + "&city_id=" + city_key_pref;

        volleyRequestQueue = Volley.newRequestQueue(this.getApplicationContext());

        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseVolleyResult(response);

                    if (mIdlingResource != null) {
                        mIdlingResource.setIdleState(true);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), getApplicationContext());
                    viewPager.setAdapter(customPagerAdapter);
                    customPagerAdapter.notifyDataSetChanged();

                    JSONObject additionalLogJson = new JSONObject();
                    try {
                        additionalLogJson.put("API_URL", url);
                        VolleyService.handleVolleyError(error, additionalLogJson, true, getApplicationContext());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (mIdlingResource != null) {
                        mIdlingResource.setIdleState(true);
                    }
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                SharedPreferences sharedPref = getApplication().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);

                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        volleyRequestQueue.add(jsonRequest);
    }

    public void parseVolleyResult(JSONObject response) {
        try {
            if (response.has("success")) {
                if ((Boolean) response.get("success")) {
                    if (response.has("filters")) {
                        JSONObject filters = (JSONObject) response.get("filters");
                        rto_states = (JSONArray) filters.get("state_code");
                        rto_cities = (JSONArray) filters.get("city");
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        customPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(customPagerAdapter);
        customPagerAdapter.notifyDataSetChanged();
        Helper.wrapTabIndicatorToTitle(tabLayout, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN, Constants.Config.TAB_LAYOUT_EXTERNAL_MARGIN);
        checkCityRto();
    }

    Boolean checkCityRto(){
        String city_states = Helper.getPreference(this, Constants.Keys.CITIES);
        String rto_states = Helper.getPreference(this, Constants.Keys.STATES);

        if (rto_states.equalsIgnoreCase("") && city_states.equalsIgnoreCase("")) {
            Toast.makeText(this,"Please select RTO cities and states",Toast.LENGTH_LONG).show();
            return false;
        }
        else if (rto_states.equalsIgnoreCase("")) {
            viewPager.setCurrentItem(2);
            Toast.makeText(this,"Please select a state",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (city_states.equalsIgnoreCase("")){
            Toast.makeText(this,"Please select a city",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onVolleyRetryClick() {
        sendVolleyRequest();
    }

    @Override
    public void onVolleyError() {
        submitButton.setVisibility(View.GONE);
    }

    @Override
    public void onVolleySuccess() {
        submitButton.setVisibility(View.VISIBLE);
    }

    class CustomPagerAdapter extends FragmentStatePagerAdapter {

        private CustomPagerAdapter(FragmentManager fm, Context ctx) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle bundle = new Bundle();
            Fragment fragment = new RTOSelectionFragment();

            switch(position){
                case 0:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.CITIES);
                    bundle.putString("JSON", String.valueOf(rto_cities));
                    break;
                case 1:
                    bundle.putString(Constants.Keys.FRAGMENT, Constants.Keys.STATES);
                    bundle.putString("JSON", String.valueOf(rto_states));
                    break;
                default:
                    return null;
            }

            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch(position){
                case 0:
                    title = "Current City";
                    break;
                case 1:
                    title = "RTO State";
                    break;
            }
            return title;
        }
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new VolleyIdlingResource();
        }
        return mIdlingResource;
    }
}
