package com.truebil.business.Models;

import com.truebil.business.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ListingModel {

    private String variantName,
                manufacturingYear,
                owner,
                fuelType,
                rtoName,
                mileage,
                cityName,
                suggestedPrice,
                truebilScore,
                carStatus,
                dealerAuctionStatus,
                auctionStatus,
                rcInsuranceError,
                listingId;

    private int auctionId,
            myDealerBid = 0,
            walletBalance = 0,
            minBidAmount = 0,
            highestBid = 0,
            suggestedAutoBidPrice = 0,
            dealerAutoBidAmount = 0,
            minAutoBidAmount = 0;

    private long bidEndTime = 0;

    private JSONObject inspectionReport,
                overview,
                inspectionSummary,
                listingApiResponse;

    private JSONArray refurbDetails,
                featureJSONArray,
                cargoNegativeComments,
                instaveritasVerificationDetails = new JSONArray();

    private ArrayList<String> showcaseImageURLList;

    public ListingModel(JSONObject apiResponse) {

        try {
            listingApiResponse = apiResponse;
            JSONObject listingDetails = apiResponse.getJSONObject("listing_details");
            overview = listingDetails.getJSONObject("overview");
            inspectionReport = listingDetails.getJSONObject("inspection_report").getJSONObject("report");
            refurbDetails = listingDetails.getJSONObject("inspection_report").getJSONArray("refurb_details");
            JSONObject basicInfo = listingDetails.getJSONObject("basic_info");
            JSONObject sellerInfo = overview.getJSONObject("seller_info");
            JSONObject carInfo = overview.getJSONObject("car_info");
            String yearVariantName = basicInfo.getString("variant_name");
            manufacturingYear = yearVariantName.substring(0, yearVariantName.indexOf(' '));
            variantName = yearVariantName.substring(yearVariantName.indexOf(' ') + 1);
            owner = sellerInfo.getString("Owner");
            fuelType = carInfo.getString("Fuel");
            rtoName = carInfo.getString("RTO");
            mileage = basicInfo.getString("mileage");
            cityName = listingDetails.getJSONObject("city_details").getString("City Name");
            carStatus = "The car is in top condition. Security camera power folding ORVM is a plus";
            int suggestedPriceInt = apiResponse.getJSONObject("auction_details").getInt("suggested_price");
            listingId = String.valueOf(apiResponse.getJSONObject("auction_details").getInt("listing_id"));
            suggestedPrice = Helper.getIndianCurrencyFormat(suggestedPriceInt);
            truebilScore = basicInfo.getString("rating");
            JSONArray showcaseImageURLs = basicInfo.getJSONArray("showcase_image_urls");
            showcaseImageURLList = new ArrayList<>();
            for (int i=0; i<showcaseImageURLs.length(); i++) {
                showcaseImageURLList.add("https:" + showcaseImageURLs.getString(i));
            }
            featureJSONArray = listingDetails.getJSONArray("features");
            inspectionSummary = listingDetails.getJSONObject("inspection_report").getJSONObject("summary");
            cargoNegativeComments = new JSONArray();
            try {
                cargoNegativeComments = listingDetails.getJSONObject("inspection_report").getJSONArray("listing_negative_comments");
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject auctionDetails = apiResponse.getJSONObject("auction_details");
            auctionId = auctionDetails.getInt("id");
            if (!auctionDetails.isNull("min_bid_amount")) {
                minBidAmount = (int) auctionDetails.getDouble("min_bid_amount");
            }

            // Bid End Time
            if (!auctionDetails.isNull("end_time")) {
                String bidEndTimeString = auctionDetails.getString("end_time");
                bidEndTimeString = bidEndTimeString.replace("+00:00", "");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                try {
                    Date date = sdf.parse(bidEndTimeString);
                    bidEndTime = date.getTime();
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (!auctionDetails.isNull("highest_bid"))
                highestBid = auctionDetails.getInt("highest_bid");

            dealerAuctionStatus = auctionDetails.getString("dealer_auction_status");
            auctionStatus = auctionDetails.getString("auction_status");

            if (!listingDetails.isNull("rc_insurance_validity") && listingDetails.getJSONObject("rc_insurance_validity").has("showErrorRcInsurance")) {
                rcInsuranceError = listingDetails.getJSONObject("rc_insurance_validity").getString("showErrorRcInsurance");
            }
            if (!auctionDetails.isNull("dealer_bid")) {
                myDealerBid = (int) apiResponse.getJSONObject("auction_details").getDouble("dealer_bid");
            }
            if (!auctionDetails.isNull("suggested_auto_bid_price")) {
                suggestedAutoBidPrice = auctionDetails.getInt("suggested_auto_bid_price");
            }
            if (!auctionDetails.isNull("dealer_auto_bid_amount")) {
                dealerAutoBidAmount = auctionDetails.getInt("dealer_auto_bid_amount");
            }
            if (!auctionDetails.isNull("min_auto_bid_amount")) {
                minAutoBidAmount = auctionDetails.getInt("min_auto_bid_amount");
            }
            if (!listingDetails.isNull("instaveritas_verification_details"))
                instaveritasVerificationDetails = listingDetails.getJSONArray("instaveritas_verification_details");

            final JSONObject walletDetails = apiResponse.getJSONObject("wallet_details");
            if (!walletDetails.isNull("value")) {
                walletBalance = (int) walletDetails.getDouble("value");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getVariantName() {
        return variantName;
    }

    public String getManufacturingYear() {
        return manufacturingYear;
    }

    public String getOwner() {
        return owner;
    }

    public String getFuelType() {
        return fuelType;
    }

    public String getRtoName() {
        return rtoName;
    }

    public String getMileage() {
        return mileage;
    }

    public String getCityName() {
        return cityName;
    }

    public String getSuggestedPrice() {
        return suggestedPrice;
    }

    public String getTruebilScore() {
        return truebilScore;
    }

    public String getCarStatus() {
        return carStatus;
    }

    public JSONObject getInspectionReport() {
        return inspectionReport;
    }

    public JSONObject getOverview() {
        return overview;
    }

    public JSONObject getInspectionSummary() {
        return inspectionSummary;
    }

    public JSONArray getRefurbDetails() {
        return refurbDetails;
    }

    public JSONArray getFeatureJSONArray() {
        return featureJSONArray;
    }

    public JSONArray getCargoNegativeComments() {
        return cargoNegativeComments;
    }

    public ArrayList<String> getShowcaseImageURLList() {
        return showcaseImageURLList;
    }

    public String getDealerAuctionStatus() {
        return dealerAuctionStatus;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }

    public JSONObject getListingApiResponse() {
        return listingApiResponse;
    }

    public int getMyDealerBid() {
        return myDealerBid;
    }

    public String getRcInsuranceError() {
        return rcInsuranceError;
    }

    public String getListingId() { return listingId; }

    public JSONArray getInstaveritasVerificationDetails() {
        return instaveritasVerificationDetails;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public int getWalletBalance() {
        return walletBalance;
    }

    public int getMinBidAmount() {
        return minBidAmount;
    }

    public int getHighestBid() {
        return highestBid;
    }

    public long getBidEndTime() {
        return bidEndTime;
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
}
