package com.truebil.business.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.truebil.business.Adapters.GridViewAdapter;
import com.truebil.business.CustomLayouts.StaticExpandedGridView;
import com.truebil.business.Helper;
import com.truebil.business.Models.Features;
import com.truebil.business.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListingOverviewFragment extends Fragment {

    public ListingOverviewFragment() {
    }

    private final static String TAG = "ListingOverview";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_listing_overview, container, false);
        LinearLayout listingLinearLayout = rootView.findViewById(R.id.fragment_listing_overview_listing_linear_layout);
        LinearLayout sellerLinearLayout = rootView.findViewById(R.id.fragment_listing_overview_seller_linear_layout);

        if (getArguments() != null) {
            String overviewString = getArguments().getString("OverviewInfo");
            String summaryString = getArguments().getString("SummaryInfo");
            String featuresString = getArguments().getString("FeaturesInfo");
            String cargoNegativeComments = getArguments().getString("CargoNegativeComments");
            String instaveritasVerificationDetailsString = getArguments().getString("InstaveritasVerificationDetails");

            try {
                JSONObject overview = new JSONObject(overviewString);
                JSONObject sellerInfo = overview.getJSONObject("seller_info");
                JSONObject carInfo = overview.getJSONObject("car_info");

                for(int i = 0; i < sellerInfo.names().length(); i++){

                    View listingOverviewItem = getLayoutInflater().inflate(R.layout.item_listing_overview, sellerLinearLayout, false);
                    TextView keyTextView = listingOverviewItem.findViewById(R.id.item_listing_overview_key_text_view);
                    TextView valueTextView = listingOverviewItem.findViewById(R.id.item_listing_overview_value_text_view);

                    keyTextView.setText(sellerInfo.names().getString(i));
                    valueTextView.setText(sellerInfo.get(sellerInfo.names().getString(i)).toString());

                    if (keyTextView.getText().toString().equals("Seller")) // Product Demand: Do not display "Seller (Individual)"
                        continue;
                    if (keyTextView.getText().toString().equals("Employment"))
                        continue;

                    sellerLinearLayout.addView(listingOverviewItem);
                }

                for(int i = 0; i < carInfo.names().length(); i++){

                    View listingOverviewItem = getLayoutInflater().inflate(R.layout.item_listing_overview, sellerLinearLayout, false);
                    TextView keyTextView = listingOverviewItem.findViewById(R.id.item_listing_overview_key_text_view);
                    TextView valueTextView = listingOverviewItem.findViewById(R.id.item_listing_overview_value_text_view);

                    keyTextView.setText(carInfo.names().getString(i));
                    valueTextView.setText(carInfo.get(carInfo.names().getString(i)).toString());

                    if (keyTextView.getText().toString().equals("RC Type"))
                        continue;

                    if (carInfo.get(carInfo.names().getString(i)) instanceof String) //Only if value is not a JSON, but a string
                        listingLinearLayout.addView(listingOverviewItem);
                }

                /// RTO Info Below ///

                LinearLayout rtoVerifiedLinearLayout = rootView.findViewById(R.id.fragment_listing_overview_rto_verified_linear_layout);
                JSONArray instaveritasVerificationDetailsJsonArray = new JSONArray(instaveritasVerificationDetailsString);

                if (instaveritasVerificationDetailsJsonArray.length() == 0) {
                    rtoVerifiedLinearLayout.setVisibility(View.GONE);
                }
                else {
                    for (int i = 0; i < instaveritasVerificationDetailsJsonArray.length(); i++) {
                        View rtoVerfiedView = inflater.inflate(R.layout.item_rto_verified, rtoVerifiedLinearLayout, false);
                        TextView rtoVerifiedKeyTextView = rtoVerfiedView.findViewById(R.id.item_rto_verified_key_text_view);
                        TextView rtoVerfiedValueTextView = rtoVerfiedView.findViewById(R.id.item_rto_verified_value_text_view);

                        JSONObject rtoVerifiedJson = instaveritasVerificationDetailsJsonArray.getJSONObject(i);
                        String key = rtoVerifiedJson.names().get(0).toString();
                        rtoVerifiedKeyTextView.setText(key);
                        rtoVerfiedValueTextView.setText(rtoVerifiedJson.getString(key));

                        rtoVerifiedLinearLayout.addView(rtoVerfiedView);
                    }
                }

                ////// Summary Filling Below //////

                JSONObject inspectionSummary = new JSONObject(summaryString);
                JSONArray positiveInspectionComments = inspectionSummary.getJSONArray("positive_comments");
                JSONArray negativeInspectionComments = inspectionSummary.getJSONArray("negative_comments");

                // Set summary
                LinearLayout summaryLinearLayout = rootView.findViewById(R.id.fragment_listing_overview_summary_linear_layout);

                // Set positive summary comments
                for (int i=0; i<positiveInspectionComments.length(); i++) {
                    View summaryItemView = getLayoutInflater().inflate(R.layout.item_inspection_report_comment, summaryLinearLayout, false);
                    ImageView summaryItemImageView = summaryItemView.findViewById(R.id.item_inspection_report_comment_image_view);
                    TextView summaryItemTextView = summaryItemView.findViewById(R.id.item_inspection_report_comment_text_view);
                    summaryItemImageView.setImageResource(R.drawable.tick);
                    summaryItemTextView.setText(positiveInspectionComments.getString(i));
                    summaryLinearLayout.addView(summaryItemView);
                }

                // Set negative summary comments
                for (int i=0; i<negativeInspectionComments.length(); i++) {
                    View summaryItemView = getLayoutInflater().inflate(R.layout.item_inspection_report_comment, summaryLinearLayout, false);
                    ImageView summaryItemImageView = summaryItemView.findViewById(R.id.item_inspection_report_comment_image_view);
                    TextView summaryItemTextView = summaryItemView.findViewById(R.id.item_inspection_report_comment_text_view);
                    summaryItemImageView.setImageResource(R.drawable.close_red);
                    summaryItemTextView.setText(negativeInspectionComments.getString(i));
                    summaryLinearLayout.addView(summaryItemView);
                }

                ////// Cargo Negative Comments Below //////

                JSONArray cargoNegativeCommentsJsonArray = new JSONArray(cargoNegativeComments);
                LinearLayout additionalCommentsLinearLayout = rootView.findViewById(R.id.fragment_listing_overview_additional_comments_linear_layout);
                LinearLayout cargoCommentsLinearLayout = rootView.findViewById(R.id.fragment_listing_overview_cargo_comments_linear_layout);
                for (int i=0; i<cargoNegativeCommentsJsonArray.length(); i++) {
                    View cargoCommentItemView = getLayoutInflater().inflate(R.layout.item_inspection_report_comment, cargoCommentsLinearLayout, false);
                    ImageView cargoCommentItemImageView = cargoCommentItemView.findViewById(R.id.item_inspection_report_comment_image_view);
                    TextView cargoCommentItemTextView = cargoCommentItemView.findViewById(R.id.item_inspection_report_comment_text_view);

                    // Set a bullet with 8dp height instead of cross or tick
                    cargoCommentItemImageView.setImageResource(R.drawable.ic_bullet);
                    cargoCommentItemImageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.text_dark_grey));
                    cargoCommentItemImageView.getLayoutParams().height = Helper.convertDpToPixel(8, getActivity());
                    cargoCommentItemImageView.requestLayout();
                    cargoCommentItemTextView.setText(cargoNegativeCommentsJsonArray.getString(i));
                    cargoCommentsLinearLayout.addView(cargoCommentItemView);
                }
                if (cargoNegativeCommentsJsonArray.length() == 0)
                    additionalCommentsLinearLayout.setVisibility(View.GONE);
                else
                    additionalCommentsLinearLayout.setVisibility(View.VISIBLE);

                ////// Feature Filling Below //////

                final JSONArray featureJSONArray = new JSONArray(featuresString);
                Features features = new Features(getContext(), featureJSONArray);

                GridViewAdapter gridViewAdapter = new GridViewAdapter(getContext(), features.getActiveFeatures());
                final StaticExpandedGridView featuresGridView = rootView.findViewById(R.id.fragment_listing_overview_features_grid_view);
                featuresGridView.setAdapter(gridViewAdapter);

                featuresGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FragmentTransaction ft = getChildFragmentManager().beginTransaction().addToBackStack(null);
                        DialogFragment dialogFragment = new ListingFeaturesDialogFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString("FEATURE_STRING_ARRAY", featureJSONArray.toString());
                        dialogFragment.setArguments(bundle);

                        dialogFragment.show(ft, "dialog");
                    }
                });

                TextView seeAllFeaturesTextView = rootView.findViewById(R.id.fragment_listing_overview_see_all_features_text_view);
                seeAllFeaturesTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        featuresGridView.performItemClick(featuresGridView.getChildAt(0), 0, featuresGridView.getItemIdAtPosition(0));
                    }
                });
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return rootView;
    }

}
