package com.truebil.business.Firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.truebil.business.Activities.ListingActivity;
import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessageService extends FirebaseMessagingService {

    final private static String TAG = "FirebaseMessageService";
    private RequestQueue requestQueue;
    private SharedPreferences sharedPref;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        sharedPref = this.getSharedPreferences("APP_PREFS", 0);
        String previousSavedToken = sharedPref.getString(Constants.SharedPref.FIREBASE_TOKEN, "");
        String loginJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");

        if (!previousSavedToken.equals(token) && !loginJWTToken.isEmpty())
            postFCMDeviceToken(token);
    }

    public void postFCMDeviceToken(final String deviceToken) {
        String dealerMobile = sharedPref.getString(Constants.SharedPref.DEALER_MOBILE, "");

        final JSONObject params = new JSONObject();
        try {
            params.put("token", deviceToken);
            params.put("device_source", "auction_app");
            params.put("type", "android");
            params.put("mobile", dealerMobile);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Constants.Config.API_PATH + "/register_device/";

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Save the token in SharedPreferences upon API success
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.SharedPref.FIREBASE_TOKEN, deviceToken);
                    editor.apply();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyService.handleVolleyError(error, params, false, getApplicationContext());
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        requestQueue.add(jsonRequest);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotification(remoteMessage.getData());
    }

    private void createNotification(Map<String,String> body) {

        int notificationId = createId();
        Boolean showNotification = false;
        String type = body.get("type");
        String auctionId = body.get("auction_id");
        String message = body.get("body");
        String title = body.get("title");

        /*
         * Handle case when a new auction is added using the type = "auction_app_new_car_notification"
         */
        if (type.equalsIgnoreCase("auction_app_new_car_notification")) {
            String stateId = body.get("state_code").trim();
            String cityId = String.valueOf(body.get("city_id")).trim();

            String userRtoCityPreferences = Helper.getPreference(this, Constants.Keys.CITIES);
            String userRtoStatePreferences = Helper.getPreference(this, Constants.Keys.STATES);

            String[] cityArray = userRtoCityPreferences.split(",");
            String[] stateArray = userRtoStatePreferences.split(",");

            for (String state : stateArray) {
                if (state.equalsIgnoreCase(stateId)) {
                    for (String city : cityArray) {
                        if (city.equalsIgnoreCase(cityId)) {
                            showNotification = true;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (showNotification) {
            Bundle bundle = new Bundle();
            bundle.putString("LISTING_ID", auctionId);
            bundle.putString("SCREEN_SOURCE", "PushNotification");
            Intent i = new Intent(this, ListingActivity.class);
            i.putExtras(bundle);

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getApplicationContext());
            taskStackBuilder.addNextIntentWithParentStack(i);

            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(Integer.parseInt(auctionId), PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.auction_channel_ID))
                    .setSmallIcon(R.drawable.ic_launcher_notification)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }
            else {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            if (notificationManager != null) {
                notificationManager.notify(notificationId, builder.build());
            }
        }
        else {
            Log.d(TAG, "createNotification: ERROR");
        }
    }

    public int createId() {
        return (int) (Math.random() * 1000);
    }
}
