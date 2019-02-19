package com.truebil.business.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.truebil.business.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class InspectionReportFragment extends Fragment {

    public InspectionReportFragment() {
    }

    public interface InspectionReportFragmentInterface {
        void onInspectionErrorImageClick();
    }

    InspectionReportFragmentInterface mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (InspectionReportFragmentInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement InspectionReportFragmentInterface");
        }
    }

    private static final String TAG = "InspectionReport";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_inspection_report, container, false);
        LinearLayout reportLinearLayout = rootView.findViewById(R.id.fragment_inspection_report_linear_layout);

        if (getArguments() != null) {
            String reportString = getArguments().getString("ReportInfo");

            try {
                JSONObject report = new JSONObject(reportString);

                for (int i=0; i<report.names().length(); i++) {

                    String reportHeader = report.names().getString(i);
                    JSONObject correspondingReport = (JSONObject) report.get(report.names().getString(i));

                    String reportRating = correspondingReport.getString("rating");
                    JSONArray reportDetailsArray = correspondingReport.getJSONArray("details");

                    final View headerView = inflater.inflate(R.layout.item_inspection_report, reportLinearLayout, false);
                    TextView headerTextView = headerView.findViewById(R.id.item_inspection_report_title_text_view);
                    headerTextView.setText(reportHeader);
                    TextView headerRatingTextView = headerView.findViewById(R.id.item_inspection_report_score_text_view);
                    headerRatingTextView.setText(reportRating);
                    final ImageButton animationImageButton = headerView.findViewById(R.id.item_inspection_report_animation_image_button);
                    final LinearLayout detailsLinearLayout = headerView.findViewById(R.id.item_inspection_report_details_linear_layout);

                    for (int j=0; j<reportDetailsArray.length(); j++) {
                        JSONObject reportDetail = reportDetailsArray.getJSONObject(j);

                        for (int k=0; k<reportDetail.names().length(); k++) {
                            String subReportHeading = reportDetail.names().getString(k);
                            JSONObject subReportDetails = (JSONObject) reportDetail.get(reportDetail.names().getString(k));

                            JSONArray images = subReportDetails.getJSONArray("images");
                            JSONArray positiveComments = subReportDetails.getJSONArray("positive_comments");
                            JSONArray negativeComments = subReportDetails.getJSONArray("negative_comments");
                            JSONArray neutralComments = subReportDetails.getJSONArray("neutral_comments");

                            View subheaderLinearLayout = inflater.inflate(R.layout.item_inspection_report_subheader, reportLinearLayout, false);
                            TextView subheaderTextView = subheaderLinearLayout.findViewById(R.id.item_inspection_report_sub_header_text_view);
                            subheaderTextView.setText(subReportHeading);

                            detailsLinearLayout.addView(subheaderLinearLayout);

                            // Add Tyre Groove Percentage View if data returned in api
                            if (subReportDetails.has("groove_percentage")) {
                                /*
                                 * BUG: Make sure that groove percentage string contains an integer value.
                                 * It should not be empty and should not be just "%" string.
                                 */
                                String groovePercentage = subReportDetails.getString("groove_percentage");
                                if (groovePercentage == null || groovePercentage.isEmpty() || groovePercentage.equals("%")) {
                                    groovePercentage = "0%";
                                }

                                View tyrePercentageLayout = inflater.inflate(R.layout.item_tyre_percentage, reportLinearLayout, false);
                                ProgressBar tyrePercentageProgressBar = tyrePercentageLayout.findViewById(R.id.item_tyre_percentage_progress_bar);
                                tyrePercentageProgressBar.setProgress(Integer.parseInt(groovePercentage.substring(0, groovePercentage.length() - 1)));
                                TextView tyrePercentageTextView = tyrePercentageLayout.findViewById(R.id.item_tyre_percentage_text_view);
                                tyrePercentageTextView.setText(groovePercentage + " left");
                                detailsLinearLayout.addView(tyrePercentageLayout);
                            }

                            for (int l=0; l<positiveComments.length(); l++) {
                                View commentView = inflater.inflate(R.layout.item_inspection_report_comment, reportLinearLayout, false);

                                TextView commentTextView = commentView.findViewById(R.id.item_inspection_report_comment_text_view);
                                commentTextView.setText(positiveComments.getString(l));

                                ImageView commentTickImageView = commentView.findViewById(R.id.item_inspection_report_comment_image_view);
                                commentTickImageView.setImageResource(R.drawable.tick);

                                detailsLinearLayout.addView(commentView);
                            }

                            for (int l=0; l<neutralComments.length(); l++) {
                                View commentView = inflater.inflate(R.layout.item_inspection_report_comment, reportLinearLayout, false);

                                TextView commentTextView = commentView.findViewById(R.id.item_inspection_report_comment_text_view);
                                commentTextView.setText(neutralComments.getString(l));

                                ImageView commentTickImageView = commentView.findViewById(R.id.item_inspection_report_comment_image_view);
                                commentTickImageView.setImageResource(R.drawable.ic_tick_neutral);

                                detailsLinearLayout.addView(commentView);
                            }

                            for (int l=0; l<negativeComments.length(); l++) {
                                View commentView = inflater.inflate(R.layout.item_inspection_report_comment, reportLinearLayout, false);

                                TextView commentTextView = commentView.findViewById(R.id.item_inspection_report_comment_text_view);
                                commentTextView.setText(negativeComments.getString(l));

                                ImageView commentTickImageView = commentView.findViewById(R.id.item_inspection_report_comment_image_view);
                                commentTickImageView.setImageResource(R.drawable.close_red);

                                detailsLinearLayout.addView(commentView);
                            }


                            View errorImagesLinearLayout = inflater.inflate(R.layout.item_inspection_report_error_images, detailsLinearLayout, false);
                            errorImagesLinearLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mCallback.onInspectionErrorImageClick();
                                }
                            });

                            for (int l=0; l<images.length(); l++) {
                                String imageUrl = "https:" + images.getJSONObject(l).getString("url");

                                if (l == 0) {
                                    ImageView firstErrorImageView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_1_error_image_view);
                                    Picasso.with(getContext()).load(imageUrl).into(firstErrorImageView);
                                }
                                else if (l == 1) {
                                    ImageView secondErrorImageView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_2_error_image_view);
                                    Picasso.with(getContext()).load(imageUrl).into(secondErrorImageView);
                                }
                                else if (l == 2) {
                                    ImageView thirdErrorImageView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_3_error_image_view);
                                    Picasso.with(getContext()).load(imageUrl).into(thirdErrorImageView);
                                }
                                else {
                                    TextView extraCountTextView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_extra_count_image_view);
                                    extraCountTextView.setText("+" + String.valueOf(l - 2));
                                }
                            }

                            if (images.length() != 0)
                                detailsLinearLayout.addView(errorImagesLinearLayout);
                        }
                    }

                    reportLinearLayout.addView(headerView);

                    // Assign expand-contract button
                    detailsLinearLayout.setVisibility(View.GONE);
                    headerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (detailsLinearLayout.getVisibility() == View.GONE) {
                                detailsLinearLayout.setVisibility(View.VISIBLE);
                                animationImageButton.setImageResource(R.drawable.chevron_up);
                            }
                            else {
                                detailsLinearLayout.setVisibility(View.GONE);
                                animationImageButton.setImageResource(R.drawable.chevron_down);
                            }
                        }
                    });
                    animationImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            headerView.performClick();
                        }
                    });

                    if (i==0) {
                        detailsLinearLayout.setVisibility(View.VISIBLE);
                        animationImageButton.setImageResource(R.drawable.chevron_up);
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return rootView;
    }
}
