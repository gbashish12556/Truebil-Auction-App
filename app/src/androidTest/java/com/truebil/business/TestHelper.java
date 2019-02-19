package com.truebil.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

public class TestHelper {

    /**
     * Source: https://stackoverflow.com/questions/29378552/in-espresso-how-to-choose-one-the-view-who-have-same-id-to-avoid-ambiguousviewm
     */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    /**
     * Source: https://stackoverflow.com/questions/23381459/how-to-get-text-from-textview-using-espresso/23467629#23467629
     */
    public static String getText(final Matcher<View> matcher) {
        final String[] stringHolder = {null};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView) view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }

    public static void setUserDummyData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Clear all previous saved preferences
        editor.clear();

        // Add New Oldhonk Data
        editor.putString(Constants.SharedPref.JWT_TOKEN, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJtb2JpbGUiOjk2NjY3MDYwNjUsIm9yaWdfaWF0IjoxNTQ0MTY5MTM3LCJleHAiOjE1NTk3MjExMzd9.MPcJJOk0jAq96U1aAJt95nU0wpTxUR7Ow0Iut4B_waw");
        editor.putInt(Constants.SharedPref.DEALER_ID, 1);
        editor.putString(Constants.SharedPref.DEALER_MOBILE, "9666706065");
        editor.putBoolean(Constants.SharedPref.IS_VERIFIED_FROM_ADMIN, true); //Set this for later checks.
        editor.putBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, true); //Set this for later checks.
        editor.putString(Constants.SharedPref.SALES_PERSON_NAME, "Sonali Sinha");
        editor.putString(Constants.SharedPref.SALES_PERSON_MOBILE, "02262459799");
        editor.apply();

        // Clear RTO Preferences
        Helper.clearRTOPref(context);

        // Add new RTO Preferences
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.Keys.STATES, String.valueOf("MH,GA,DL,KA,HR,UP")).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(Constants.Keys.CITIES, String.valueOf("1,2,5")).apply();
    }
}
