package com.truebil.business;

public class Constants {

    public static final class Config{
        public static final String API_PATH = BuildConfig.API_PATH;
        public static final String WEBSITE_LINK = BuildConfig.WEBSITE_LINK;
        public static final String UTM_PARAMS = "{\"utm_source\":\"AuctionApp\",\"utm_medium\":\"buyer\",\"utm_campaign\":\"\",\"utm_content\":\"\",\"gclid\":\"\"}";
        public static final int MIN_LOW_BALANCE = 4999;
        public static final int MAX_TIMEOUT_TIME = 15000;
        public static final int LISTING_ACTIVITY_RELOAD_DELAY = 2000;
        public static final String TRUEBIL_DEFAULT_NO = "02262459799";
        public static final int TAB_LAYOUT_EXTERNAL_MARGIN = 20;
    }

    public static final class Message {
        public static final String NETWORK_ERROR = "Network Error";
    }

    public static final class SharedPref {
        public static final String HAS_LOGGED_IN_BEFORE = "HAS_LOGGED_IN_BEFORE";
        public static final String IS_VERIFIED_FROM_ADMIN = "IS_VERIFIED_FROM_ADMIN";
        public static final String JWT_TOKEN = "JWT_TOKEN";
        public static final String DEALER_ID = "DEALER_ID";
        public static final String DEALER_MOBILE = "DEALER_MOBILE";
        public static final String SALES_PERSON_NAME = "SALES_PERSON_NAME";
        public static final String SALES_PERSON_MOBILE = "SALES_PERSON_MOBILE";
        public static final String FIREBASE_TOKEN = "FIREBASE_TOKEN";
    }

    public static final class Keys{
        public static final String LISTING_ID = "listing_id";
        public static final String BidInfo = "BidInfo";
        public static final String USER_ID = "user_id";
        public static final String HIGHEST_BID = "highest_bid";
        public static final String BID_END_TIME = "bid_end_time";
        public static final String FRAGMENT = "fragment";
        public static final String InAuction = "InAuction";
        public static final String Procured = "Procured";
        public static final String Negotiating = "Negotiating";
        public static final String DealCancelled = "DealCancelled";
        public static final String MyBidsWinning = "MyBidsWinning";
        public static final String MyBidsLosing = "MyBidsLosing";
        public static final String MyBidsHistory = "MyBidsHistory";
        public static final String LOSING_BID = "losing_bid";
        public static final String NOTIFICATION_TYPE = "notification_type";
        public static final String MESSAGE = "message";
        public static final String TITLE = "title";
        public static final String BIDDING_ACTIVITY = "BiddingActivity";
        public static final String CITIES = "CITIES";
        public static final String STATES = "STATES";
        public static final String TRUE = "true";
        public static final String FALSE = "false";
        public static final String AUTHORIZATION = "authorization";
        public static final String LISTING_AUCTION_ID = "listing_auction_id";
        public static final String AMOUNT = "amount";
        public static final String MOBILE = "MOBILE";
        public static final String SALES_PERSON_NAME = "SALES_PERSON_NAME";
        public static final String SALES_PERSON_MOBILE = "SALES_PERSON_MOBILE";
        public static final String USERNAME = "USERNAME";
    }
}
