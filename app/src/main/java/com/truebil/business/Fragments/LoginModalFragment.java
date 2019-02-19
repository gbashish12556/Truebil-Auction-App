package com.truebil.business.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginModalFragment extends Fragment {

    private final static String TAG = "LoginModalFragment";
    LoginModalFragmentListener mCallback;
    TextView verifyMobileTextView;
    private RequestQueue queue;
    ProgressBar loaderProgressBar;

    public interface LoginModalFragmentListener {
        void onSendMobileButtonClicked(String mobileNumber, String name);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (LoginModalFragmentListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LoginModalFragmentListener");
        }
    }

    public LoginModalFragment() {
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_login_modal, container, false);
        Helper.setupKeyboardHidingUI(rootView, getActivity());

        loaderProgressBar = rootView.findViewById(R.id.fragment_login_modal_progress_bar);
        final EditText mobileEditText = rootView.findViewById(R.id.fragment_login_modal_mobile_number_edittext);
        final EditText nameEditText = rootView.findViewById(R.id.fragment_login_modal_name_edittext);
        verifyMobileTextView = rootView.findViewById(R.id.fragment_login_modal_verify_mobile_text_view);
        verifyMobileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileNo = mobileEditText.getText().toString();
                String name = nameEditText.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
                }
                else if (mobileNo.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.enter_mobile), Toast.LENGTH_SHORT).show();
                }
                else {
                    EventAnalytics.getInstance(getActivity()).logEvent("Login", "clicked_submit_mobileno", mobileNo, 0);

                    requestMobileOTP(mobileNo, name);
                    verifyMobileTextView.setEnabled(false);
                    loaderProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        // Call button
        ImageButton callSalesPersonButton = rootView.findViewById(R.id.fragment_login_modal_call_truebil_image_button);
        callSalesPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:02262459799";
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        return rootView;
    }

    void requestMobileOTP(final String mobileNumber, final String name) {

        if (getActivity() == null || getContext() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        String url = Constants.Config.API_PATH + "/generate_otp/";

        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("name", name);
            apiParams.put("mobile", mobileNumber);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, apiParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseResponse(mobileNumber, name, response);
                    loaderProgressBar.setVisibility(View.GONE);

                    //Enable the button
                    verifyMobileTextView.setEnabled(true);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Enable the button
                    verifyMobileTextView.setEnabled(true);
                    loaderProgressBar.setVisibility(View.GONE);

                    try {
                        apiParams.put("API", "/generate_otp");
                        VolleyService.handleVolleyError(error, apiParams, true, getContext());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseResponse(String mobileNumber, String name, JSONObject apiResponse) {

        try {
            boolean status = apiResponse.getBoolean("status");
            String message = apiResponse.getString("message");

            if (status) {
                mCallback.onSendMobileButtonClicked(mobileNumber, name);
            }
            else {
                Toast.makeText(getContext(), "Please retry. Error: " + message, Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}