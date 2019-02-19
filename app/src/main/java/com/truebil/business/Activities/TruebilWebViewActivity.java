package com.truebil.business.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.R;

import org.json.JSONException;
import org.json.JSONObject;


public class TruebilWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private boolean firstReload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_truebil_web_view);

        final SharedPreferences sharedPref = this.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        webView = findViewById(R.id.activity_truebil_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                String key = "_jwt";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("localStorage.setItem('" + key + "', '" + dealerJWTToken + "');", null);
                }
                else {
                    webView.loadUrl("javascript:localStorage.setItem('" + key + "', '" + dealerJWTToken + "');");
                }

                if (firstReload) {
                    webView.reload();
                    firstReload = false;
                }
            }
        });
        webView.loadUrl(Constants.Config.WEBSITE_LINK + "/?utm_source=AuctionApp&utm_medium=buyer");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void getResultsPage(String city) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("localStorage.getItem('" + city + "');", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    String res = Helper.removeSlashes(value);
                    try {
                        JSONObject object = new JSONObject(res);
                        String name = object.getString("nameInLower");
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
