package com.truebil.business.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.truebil.business.Helper;
import com.truebil.business.R;
import org.json.JSONException;
import org.json.JSONObject;

public class WonBottomSheetFragment extends Fragment {

    View contentView;

    public WonBottomSheetFragment() {
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_won_bottom_sheet, container, false);
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView dealStatus = contentView.findViewById(R.id.deal_status);
        View secondDot = contentView.findViewById(R.id.second_dot);
        View thirdDot = contentView.findViewById(R.id.third_dot);
        TextView secondStatus = contentView.findViewById(R.id.second_status);
        TextView thirdStatus = contentView.findViewById(R.id.third_status);
        View firstLine = contentView.findViewById(R.id.first_line);
        View secondLine = contentView.findViewById(R.id.second_line);
        LinearLayout paymentInvoiceDetail = contentView.findViewById(R.id.payment_invoice_detail);
        paymentInvoiceDetail.setVisibility(View.GONE);

        if (getArguments() != null) {
            try {
                JSONObject jo = new JSONObject((String) getArguments().get("JSON"));
                JSONObject auctionDetails = jo.getJSONObject("auction_details");
                String auctionStatus = (String) auctionDetails.get("auction_status");

                if (auctionStatus.equalsIgnoreCase("negotiation") ||
                        auctionStatus.equalsIgnoreCase("waiting_for_procurement")) {

                    secondLine.setBackgroundColor(getResources().getColor(R.color.border_grey));
                    secondDot.setBackground(getResources().getDrawable(R.drawable.background_circle_white_stroke_purple));
                    thirdDot.setBackground(getResources().getDrawable(R.drawable.background_circle_gray));
                    thirdStatus.setTextColor(getResources().getColor(R.color.border_grey));
                    dealStatus.setBackgroundResource(R.drawable.background_green);

                    if (auctionStatus.equalsIgnoreCase("negotiation")) {
                        dealStatus.setText(getResources().getString(R.string.negotiating_status));
                    }
                    else if (auctionStatus.equalsIgnoreCase("waiting_for_procurement")) {
                        dealStatus.setText(getResources().getString(R.string.waiting_for_procurement_status));
                        secondStatus.setText(R.string.waiting_for_procurement);
                    }
                }
                else if (auctionStatus.equalsIgnoreCase("payment_waiting")) {

                    paymentInvoiceDetail.setVisibility(View.VISIBLE);
                    thirdDot.setBackground(getResources().getDrawable(R.drawable.background_circle_white_stroke_purple));
                    dealStatus.setBackgroundResource(R.drawable.background_green);
                    dealStatus.setText(getResources().getString(R.string.waiting_payment_status));

                    JSONObject invoice_details = auctionDetails.getJSONObject("invoice_details");

                    if (invoice_details.getJSONObject("car_price").get("value") != null) {
                        int car_price =  (int) invoice_details.getJSONObject("car_price").get("value");
                        TextView car_price_view = contentView.findViewById(R.id.car_price);
                        car_price_view.setText(Helper.getIndianCurrencyFormat(car_price));
                    }

                    if (invoice_details.getJSONObject("parking_charge").get("value") != null) {
                        int parking_charge = (int) invoice_details.getJSONObject("parking_charge").get("value");
                        TextView parking_charge_view = contentView.findViewById(R.id.parking_charge);
                        parking_charge_view.setText(Helper.getIndianCurrencyFormat(parking_charge));
                    }

                    if (invoice_details.getJSONObject("delivery_charge").get("value") != null) {
                        int delivery_charge =  (int) invoice_details.getJSONObject("delivery_charge").get("value");
                        TextView delivery_charge_view = contentView.findViewById(R.id.delivery_charge);
                        delivery_charge_view.setText(Helper.getIndianCurrencyFormat(delivery_charge));
                    }

                    if (invoice_details.getJSONObject("ref_sec_deposit").get("value") != null) {
                        int ref_sec_deposit =  (int) invoice_details.getJSONObject("ref_sec_deposit").get("value");
                        TextView ref_sec_deposit_view = contentView.findViewById(R.id.ref_sec_deposit);
                        ref_sec_deposit_view.setText(Helper.getIndianCurrencyFormat(ref_sec_deposit));
                    }

                    if (invoice_details.getJSONObject("total_amount").get("value") != null) {
                        int total_amount =  (int) invoice_details.getJSONObject("total_amount").get("value");
                        TextView total_amount_view = contentView.findViewById(R.id.total_amount);
                        total_amount_view.setText("Total : " + Helper.getIndianCurrencyFormat(total_amount));
                    }
                }
                else if (auctionStatus.equalsIgnoreCase("dealer_cancelled") ||
                        auctionStatus.equalsIgnoreCase("seller_cancelled") ||
                        auctionStatus.equalsIgnoreCase("dealer_rejected") ||
                        auctionStatus.equalsIgnoreCase("seller_rejected")) {

                    thirdDot.setBackground(getResources().getDrawable(R.drawable.background_circle_orange));
                    thirdStatus.setTextColor(getResources().getColor(R.color.orange_background));
                    dealStatus.setBackgroundResource(R.drawable.background_orange);

                    switch (auctionStatus.trim()) {

                        case "dealer_cancelled":
                            thirdStatus.setText(getResources().getString(R.string.dealer_cancelled));
                            dealStatus.setText(getResources().getString(R.string.dealer_cancelled_status));
                            break;
                        case "seller_cancelled":
                            thirdStatus.setText(getResources().getString(R.string.seller_cancelled));
                            dealStatus.setText(getResources().getString(R.string.seller_cancelled_status));
                            break;
                        case "dealer_rejected":
                            thirdStatus.setText(getResources().getString(R.string.dealer_rejected));
                            dealStatus.setText(getResources().getString(R.string.dealer_rejected_status));
                            break;
                        case "seller_rejected":
                            thirdStatus.setText(getResources().getString(R.string.seller_rejected));
                            dealStatus.setText(getResources().getString(R.string.seller_rejected_status));
                            break;
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
