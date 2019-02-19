package com.truebil.business;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.DecimalFormat;

public class Helper {

    public static void addRTOCityKeyPref(Context ctx, String city_key){
        String cities_shared_pref = Helper.getPreference(ctx, Constants.Keys.CITIES);
        if(cities_shared_pref.equalsIgnoreCase("")){
            cities_shared_pref  = city_key;
        }else{
            cities_shared_pref  = cities_shared_pref+","+city_key;
        }
        Helper.putPreference(ctx,Constants.Keys.CITIES,cities_shared_pref);
    }

    public static void removeRTOCityKeyPref(Context ctx, String city_key){
        String city_shared_pref = Helper.getPreference(ctx,Constants.Keys.CITIES);
        boolean isFound1 = city_shared_pref.indexOf(","+city_key) !=-1? true: false;
        boolean isFound2 = city_shared_pref.indexOf(city_key+",") !=-1? true: false;
        if(isFound1) {
            city_shared_pref = city_shared_pref.replace(","+city_key, "");
        }else if(isFound2) {
            city_shared_pref = city_shared_pref.replace(city_key+",", "");
        }
        else{
            city_shared_pref = city_shared_pref.replace(city_key, "");
        }
        Helper.putPreference(ctx,Constants.Keys.CITIES,city_shared_pref);
    }

    public static void addRTOStateKeyPref(Context ctx, String rto_key){

        String rto_shared_pref = Helper.getPreference(ctx, Constants.Keys.STATES);
        if(rto_shared_pref.equalsIgnoreCase("")){
            rto_shared_pref  = rto_key;
        }else{
            rto_shared_pref  = rto_shared_pref+","+rto_key;
        }
        Helper.putPreference(ctx,Constants.Keys.STATES,rto_shared_pref);
    }

    public static void removeRTOStateKeyPref(Context ctx, String rto_key){
        String rto_shared_pref = Helper.getPreference(ctx,Constants.Keys.STATES);
        boolean isFound1 = rto_shared_pref.indexOf(","+rto_key) !=-1? true: false;
        boolean isFound2 = rto_shared_pref.indexOf(rto_key+",") !=-1? true: false;
        if(isFound1) {
            rto_shared_pref = rto_shared_pref.replace(","+rto_key, "");
        }else if(isFound2) {
            rto_shared_pref = rto_shared_pref.replace(rto_key+",", "");
        }
        else{
            rto_shared_pref = rto_shared_pref.replace(rto_key, "");
        }
        Helper.putPreference(ctx,Constants.Keys.STATES,rto_shared_pref);
    }

    public static void clearRTOPref(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(Constants.Keys.CITIES).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(Constants.Keys.STATES).apply();
    }

    public static void putPreference(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, String.valueOf(value)).apply();
    }

    public static String getPreference(Context context, String key) {
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
        return value;
    }

    public static String convertToLakhs(int amount) {
        // Display compact format if amount greater than or equal to 1 Lakh
        if (amount >= 100000) {
            Float amountFloat = (float) amount;
            return "\u20B9" + new DecimalFormat("0.00").format(amountFloat / 100000) + "L";
        }

        // Display expanded format in case amount less than 1 Lakh
        return getIndianCurrencyFormat(amount);
    }

    public static String getIndianCurrencyFormat(int money) {
        String amount = String.valueOf(money);
        StringBuilder stringBuilder = new StringBuilder();
        char amountArray[] = amount.toCharArray();
        int a = 0, b = 0;
        for (int i = amountArray.length - 1; i >= 0; i--) {
            if (a < 3) {
                stringBuilder.append(amountArray[i]);
                a++;
            }
            else if (b < 2) {
                if (b == 0) {
                    stringBuilder.append(",");
                    stringBuilder.append(amountArray[i]);
                    b++;
                }
                else {
                    stringBuilder.append(amountArray[i]);
                    b = 0;
                }
            }
        }
        return "\u20B9" + stringBuilder.reverse().toString();
    }

    public static int convertDpToPixel(int dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static void wrapTabIndicatorToTitle(TabLayout tabLayout, int externalMargin, int internalMargin) {
        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            int childCount = ((ViewGroup) tabStrip).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View tabView = tabStripGroup.getChildAt(i);
                //set minimum width to 0 for instead for small texts, indicator is not wrapped as expected
                tabView.setMinimumWidth(0);
                // set padding to 0 for wrapping indicator as title
                tabView.setPadding(0, tabView.getPaddingTop(), 0, tabView.getPaddingBottom());
                // setting custom margin between tabs
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
                    if (i == 0) { // left
                        setTabLayoutMargin(layoutParams, externalMargin, internalMargin);
                    }
                    else if (i == childCount - 1) { // right
                        setTabLayoutMargin(layoutParams, internalMargin, externalMargin);
                    }
                    else { // internal
                        setTabLayoutMargin(layoutParams, internalMargin, internalMargin);
                    }
                }
            }

            tabLayout.requestLayout();
        }
    }

    private static void setTabLayoutMargin(ViewGroup.MarginLayoutParams layoutParams, int start, int end) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.setMarginStart(start);
            layoutParams.setMarginEnd(end);
        } else {
            layoutParams.leftMargin = start;
            layoutParams.rightMargin = end;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static String removeSlashes(String string) {
        String temp = string.replace('\\', ' ');
        return temp.replace('"', ' ');
    }

    public static void setupKeyboardHidingUI(View view, final Activity activity) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupKeyboardHidingUI(innerView, activity);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null && activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}