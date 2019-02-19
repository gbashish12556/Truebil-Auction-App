package com.truebil.business.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.truebil.business.Constants;
import com.truebil.business.Helper;
import com.truebil.business.R;
import com.truebil.business.Utils.EventAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class RTOSelectionFragment extends Fragment {

    private static final String TAG = "RTOSelectionFragment";
    private ArrayList<RTOPlaceModel> rtoModelsList = new ArrayList<>();
    private RTOListAdapter rtoListAdapter;
    private String fragmentType;

    CitiesFragmentInterface mCallBack;

    public interface CitiesFragmentInterface{
        void onVolleyRetryClick();
        void onVolleyError();
        void onVolleySuccess();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallBack = (CitiesFragmentInterface) context;
        }
        catch(ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement CitiesFragmentInterface");
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rto_selection, container, false);

        EditText searchEditText = rootView.findViewById(R.id.rto_selection_fragment_search_edit_text);
        final ListView rtoListView = rootView.findViewById(R.id.listview);
        rtoListAdapter = new RTOListAdapter(rtoModelsList, getContext());
        rtoListView.setAdapter(rtoListAdapter);

        final LinearLayout volley_error_container = rootView.findViewById(R.id.item_volley_error_linear_layout);
        final LinearLayout rto_linear_layout = rootView.findViewById(R.id.cities_rto_container);

        Button retryButton = rootView.findViewById(R.id.retry_volley_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.onVolleyRetryClick();
            }
        });

        if (getArguments() != null) {
            fragmentType = getArguments().getString(Constants.Keys.FRAGMENT);
            String jsonString = getArguments().getString("JSON");

            if (jsonString == null || jsonString.equals("")  || jsonString.equals("null")) {
                volley_error_container.setVisibility(View.VISIBLE);
                rto_linear_layout.setVisibility(View.GONE);
                mCallBack.onVolleyError();
                return rootView;
            }
            else {
                volley_error_container.setVisibility(View.GONE);
                rto_linear_layout.setVisibility(View.VISIBLE);
                mCallBack.onVolleySuccess();
            }

            try {
                JSONArray rtoInfoJsonArray = new JSONArray(jsonString);

                for (int i=0; i<rtoInfoJsonArray.length(); i++) {
                    JSONObject rtoItemJsonObject = rtoInfoJsonArray.getJSONObject(i);

                    RTOPlaceModel rtoPlaceModel = new RTOPlaceModel(rtoItemJsonObject);
                    rtoModelsList.add(rtoPlaceModel);
                }

                rtoListAdapter.notifyDataSetChanged();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<RTOPlaceModel> resultRTOModelList = new ArrayList<>();

                for (int i=0; i<rtoModelsList.size(); i++) {

                    RTOPlaceModel rtoPlaceModel = rtoModelsList.get(i);

                    if (s.length() > rtoPlaceModel.getName().length())
                        continue;

                    if (rtoPlaceModel.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                        resultRTOModelList.add(rtoPlaceModel);
                    }
                }

                rtoListAdapter = new RTOListAdapter(resultRTOModelList, getContext());
                rtoListView.setAdapter(rtoListAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return rootView;
    }

    class RTOPlaceModel {

        String name;
        String id;
        Boolean isChecked = false;

        RTOPlaceModel(JSONObject response) {

            try {
                name = response.getString("name");
                isChecked = response.getBoolean("is_checked");
                id = response.getString("id");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public Boolean getIsChecked() {
            return isChecked;
        }

        public void setIsChecked(Boolean status) {
            isChecked = status;
        }
    }

    public class RTOListAdapter extends ArrayAdapter<RTOPlaceModel> {

        Context context;
        ArrayList<RTOPlaceModel> data;

        RTOListAdapter(ArrayList<RTOPlaceModel> data, Context context) {
            super(context, R.layout.item_rto_place, data);
            this.context = context;
            this.data = data;
        }

        @NonNull @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final RTOPlaceModel rtoPlaceModel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_rto_place, parent, false);
            }

            TextView placeNameTextView = convertView.findViewById(R.id.item_multiselect_view_place_name_text_view);
            final CheckBox placeCheckBox = convertView.findViewById(R.id.item_multiselect_view_place_name_check_box);

            if (rtoPlaceModel != null) {

                placeCheckBox.setChecked(rtoPlaceModel.getIsChecked());
                placeNameTextView.setText(rtoPlaceModel.getName());
                SharedPreferences sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                final String dealerId = String.valueOf(sharedPref.getInt(Constants.SharedPref.DEALER_ID,-1));

                placeCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final boolean isChecked = placeCheckBox.isChecked();

                        rtoPlaceModel.setIsChecked(isChecked);
                        String placeId = rtoPlaceModel.getId();

                        // Iterate through all the models and set true/false with id == placeId
                        // TODO: Create a hashtable {id: Model}
                        for (int i=0; i<rtoModelsList.size(); i++) {
                            if (rtoModelsList.get(i).getId().equals(placeId)) {
                                rtoModelsList.set(i, rtoPlaceModel);
                                break;
                            }
                        }

                        String rtoName = rtoPlaceModel.getName();
                        String label = dealerId+","+rtoName;
                        String action = "";
                        if (isChecked) {
                            if (fragmentType.equals("CITIES")) {
                                Helper.addRTOCityKeyPref(getActivity(), rtoPlaceModel.getId());
                                action = "clicked_selected_city";
                            }
                            else if (fragmentType.equals("STATES")) {
                                Helper.addRTOStateKeyPref(getActivity(), rtoPlaceModel.getId());
                                action = "clicked_selected_state";
                            }
                        }
                        else {
                            if (fragmentType.equals("CITIES")) {
                                Helper.removeRTOCityKeyPref(getActivity(), rtoPlaceModel.getId());
                                action = "clicked_unselected_city";
                            }
                            else if (fragmentType.equals("STATES")) {
                                Helper.removeRTOStateKeyPref(getActivity(), rtoPlaceModel.getId());
                                action = "clicked_unselected_state";
                            }
                        }
                        EventAnalytics.getInstance(getActivity()).logEvent("More", action, label, 0);

                    }
                });
            }

            return convertView;
        }
    }
}
