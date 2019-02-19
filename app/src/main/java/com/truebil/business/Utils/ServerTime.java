package com.truebil.business.Utils;

import android.os.SystemClock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*
 * Class for finding the current server time
 * Source: https://julien-millau.fr/articles/Secure-Android-time-based-application.html
 */
public class ServerTime {

    private Date serverDate;
    private long elapsedTimeDuringSetup;
    private boolean isInitialised = false;
    private static final ServerTime INSTANCE = new ServerTime();

    public static ServerTime getInstance() {
        return INSTANCE;
    }

    public long getTime() {
        if (serverDate == null) {
            serverDate = Calendar.getInstance().getTime();
        }
        else {
            //serverDate.setTime(serverDate.getTime() + SystemClock.elapsedRealtime() - elapsedRealtime);
            //elapsedRealtime = SystemClock.elapsedRealtime(); // Reset elapsed time again

            // 1. Find time elapsed since server time was set last time
            long timeElapsedSinceSetup = SystemClock.elapsedRealtime() - elapsedTimeDuringSetup;

            // 2. Append this time to the server time since current server time has gone ahead
            long currentServerTime = serverDate.getTime() + timeElapsedSinceSetup;

            // 3. No need to update any other variable. Whenever we need current server time,
            // we will call getTime() method. It will add the preset serverDate with the time elapsed since setup.

            // 4. If we need to resync the actual server time, we can directly call initServerDate() function
            // and again call the getTime() method.

            // 3. Return this time
            return currentServerTime;
        }
        return serverDate.getTime();
    }

    public void initServerDate(String serverTimeString, long delay) {

        serverTimeString = serverTimeString.replace("+00:00", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Set to null and check later if value is not null
        serverDate = null;

        try {
            serverDate = sdf.parse(serverTimeString);
            serverDate.setTime(serverDate.getTime() + delay);
            elapsedTimeDuringSetup = SystemClock.elapsedRealtime();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        isInitialised = serverDate != null;
    }

    public boolean isInitialised() {
        return isInitialised;
    }
}
