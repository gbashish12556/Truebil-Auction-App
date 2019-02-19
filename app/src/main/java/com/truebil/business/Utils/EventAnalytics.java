package com.truebil.business.Utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class EventAnalytics {

    private static FirebaseAnalytics firebaseAnalytics;
    private static final EventAnalytics INSTANCE = new EventAnalytics();

    public static EventAnalytics getInstance(Context context) {
        if (firebaseAnalytics == null)
            initFirebaseAnalytics(context);

        return INSTANCE;
    }

    private static void initFirebaseAnalytics(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void logEvent(String category, String action, String label, long value) {
        Bundle bundle = new Bundle();
        bundle.putString("category", category);
        bundle.putString("action", action);
        bundle.putString("label", label);
        bundle.putLong("value", value);
        firebaseAnalytics.logEvent("User_Event", bundle);
    }
}
