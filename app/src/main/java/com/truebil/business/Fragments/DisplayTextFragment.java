package com.truebil.business.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;

public class DisplayTextFragment extends Fragment {

    protected RequestQueue volleyRequestQueue;
    TextView plainTextView;
    static final private String TAG = "DisplayTextFragment";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_plain_text, container, false);
        plainTextView = rootView.findViewById(R.id.fragment_plain_text_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.getArguments() != null && getActivity() != null) {
            String url = this.getArguments().getString("url");

            volleyRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseURLResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley Error: " + error.toString());
                        VolleyService.handleVolleyError(error, null, true, getContext());
                    }
                }
            );
        }
    }

    void parseURLResponse(String urlResponse) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            plainTextView.setText(Html.fromHtml(urlResponse));
        else
            plainTextView.setText(Html.fromHtml(urlResponse, Html.FROM_HTML_MODE_COMPACT));
    }
}
