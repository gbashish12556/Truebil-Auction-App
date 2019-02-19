package com.truebil.business.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.truebil.business.Adapters.GridViewAdapter;
import com.truebil.business.CustomLayouts.StaticExpandedGridView;
import com.truebil.business.Models.Features;
import com.truebil.business.R;

import org.json.JSONArray;
import org.json.JSONException;

public class ListingFeaturesDialogFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full Screen Dialog Fragment
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_listing_features, container, false);

        StaticExpandedGridView safetyGridView = rootView.findViewById(R.id.dialog_fragment_listing_features_safety_grid_view);
        StaticExpandedGridView comfortGridView = rootView.findViewById(R.id.dialog_fragment_listing_features_comfort_grid_view);
        StaticExpandedGridView accessoriesGridView = rootView.findViewById(R.id.dialog_fragment_listing_features_accessories_grid_view);
        ImageButton backImageButton = rootView.findViewById(R.id.dialog_fragment_listing_back_image_button);

        if (getArguments() == null)
            return rootView;

        String featureArray = getArguments().getString("FEATURE_STRING_ARRAY");

        try {
            JSONArray featureJSONArray = new JSONArray(featureArray);

            Features features = new Features(getContext(), featureJSONArray);

            GridViewAdapter gridViewAdapter = new GridViewAdapter(getActivity(), features.getSafetyFeatures());
            safetyGridView.setAdapter(gridViewAdapter);

            gridViewAdapter = new GridViewAdapter(getActivity(), features.getComfortFeatures());
            comfortGridView.setAdapter(gridViewAdapter);

            gridViewAdapter = new GridViewAdapter(getActivity(), features.getAccessoriesFeatures());
            accessoriesGridView.setAdapter(gridViewAdapter);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return rootView;
    }
}
