package com.truebil.business.Models;

import android.os.CountDownTimer;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class CarListModel {

    //Car Details
    private String variantName,
            modelName,
            fuelType,
            manufactutingYear,
            truebilScore,
            cityName,
            rto,
            ownerDetail,
            mileage,
            auctionId,
            carId;

    private ArrayList<String> showcaseImageList = new ArrayList<>();

    //Auction Details
    private String bidEndTime;
    private int highestBid = 0;
    private int myBid = 0;
    private int securityDepositAmount = 0;
    private int suggestedAutoBidPrice = 0;
    private int dealerAutoBidAmount = 0;
    private int minAutoBidAmount = 0;
    private int minBidAmount = 0;
    private String dealerAuctionStatus = null;
    private String auctionStatus;
    private CountDownTimer countDownTimer;
    private ValueEventListener valueEventListener;

    public CarListModel(JSONObject carInfoJson) {
        try {
            JSONObject carInfo = carInfoJson.getJSONObject("listing_details").getJSONObject("car_info");
            JSONArray listingImages = carInfoJson.getJSONObject("listing_details").getJSONArray("image_urls");
            JSONObject auctionDetails = carInfoJson.getJSONObject("auction_details");

            truebilScore = carInfo.getString("rating");
            mileage = carInfo.getString("mileage");
            String variantDetail = carInfo.getString("variant_name");
            variantName = variantDetail.substring(variantDetail.indexOf(' ') + 1);
            ownerDetail = carInfo.getString("owner");
            fuelType = carInfo.getString("primary_fuel");
            auctionId = String.valueOf(auctionDetails.getInt("id"));
            carId = String.valueOf(carInfo.getInt("id"));
            manufactutingYear = variantDetail.substring(0, variantDetail.indexOf(' '));
            cityName = carInfo.getString("city_name");
            rto = carInfo.getString("rto");
            modelName = carInfo.getString("model_name");

            // Create array list from JSONArray
            for (int j=0; j<listingImages.length(); j++) {
                showcaseImageList.add("https:" + listingImages.getString(j));
            }
            if (!auctionDetails.isNull("dealer_auction_status")) {
                dealerAuctionStatus = auctionDetails.getString("dealer_auction_status");
            }
            String bidStartTime = auctionDetails.getString("start_time");
            bidEndTime = auctionDetails.getString("end_time");
            bidEndTime = bidEndTime.replace("+00:00", "");

            auctionStatus = auctionDetails.getString("auction_status");

            if (!auctionDetails.isNull("dealer_bid")) {
                myBid = (int) auctionDetails.getDouble("dealer_bid");
            }
            if (!auctionDetails.isNull("highest_bid")) {
                highestBid = (int) auctionDetails.getDouble("highest_bid");
            }
            if (!auctionDetails.isNull("dealer_auto_bid_amount")) {
                dealerAutoBidAmount = auctionDetails.getInt("dealer_auto_bid_amount");
            }
            if (!auctionDetails.isNull("suggested_auto_bid_price")) {
                suggestedAutoBidPrice = auctionDetails.getInt("suggested_auto_bid_price");
            }
            if (!auctionDetails.isNull("min_auto_bid_amount")) {
                minAutoBidAmount = auctionDetails.getInt("min_auto_bid_amount");
            }
            if (!auctionDetails.isNull("security_deposit_amount")) {
                securityDepositAmount = auctionDetails.getInt("security_deposit_amount");
            }
            if (!auctionDetails.isNull("min_bid_amount")) {
                minBidAmount = auctionDetails.getInt("min_bid_amount");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setHighestBid(int highestBid) {
        this.highestBid = highestBid;
    }

    public void setBidEndTime(String bidEndTime) {
        this.bidEndTime = bidEndTime;
    }

    public void setMyBid(int myBid) {
        this.myBid = myBid;
    }

    public void setCountDownTimer(CountDownTimer countDownTimer) {
        this.countDownTimer = countDownTimer;
    }

    public void setValueEventListener(ValueEventListener valueEventListener) {
        this.valueEventListener = valueEventListener;
    }

    public ValueEventListener getValueEventListener() {
        return valueEventListener;
    }

    public CountDownTimer getCountDowntimer() {
        return countDownTimer;
    }

    public String getVariantName() {
        return variantName;
    }

    public String getFuelType() {
        return fuelType;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getCarId() {
        return carId;
    }

    public String getManufactutingYear() {
        return manufactutingYear;
    }

    public String getCityName() {
        return cityName;
    }

    public String getRto() {
        return rto;
    }

    public String getOwnerDetail() {
        return ownerDetail;
    }

    public String getMileage() {
        return mileage;
    }

    public String getBidEndTime()  {
        return bidEndTime;
    }

    public int getHighestBid() {
        return highestBid;
    }

    public int getMyBid() {
        return myBid;
    }

    public String getDealerAuctionStatus() {
        return dealerAuctionStatus;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }

    public ArrayList<String> getShowcaseImageList() {
        return showcaseImageList;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTruebilScore() {
        return truebilScore;
    }

    public int getSecurityDepositAmount() {
        return securityDepositAmount;
    }

    public int getSuggestedAutoBidPrice() {
        return suggestedAutoBidPrice;
    }

    public int getDealerAutoBidAmount() {
        return dealerAutoBidAmount;
    }

    public int getMinAutoBidAmount() {
        return minAutoBidAmount;
    }

    public int getMinBidAmount() {
        return minBidAmount;
    }
}
