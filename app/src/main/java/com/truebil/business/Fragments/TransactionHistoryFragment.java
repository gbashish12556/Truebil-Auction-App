package com.truebil.business.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.truebil.business.Helper;
import com.truebil.business.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryFragment extends Fragment {

    public TransactionHistoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        List<TransactionItem> listTransactionItems = new ArrayList<>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            String response = bundle.getString("response");
            try {
                JSONArray transactionResponse = new JSONArray(response);

                for (int i=0; i<transactionResponse.length(); i++) {
                    TransactionItem item = new TransactionItem(transactionResponse.getJSONObject(i));
                    listTransactionItems.add(item);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ListAdapter transactionItemListAdapter = new ListAdapter(getContext(), R.layout.item_transaction_history, listTransactionItems);
        ListView rootListView = (ListView) inflater.inflate(R.layout.fragment_transaction_history, container, false);
        rootListView.setAdapter(transactionItemListAdapter);

        return rootListView;
    }

    public class ListAdapter extends ArrayAdapter<TransactionItem> {

        ListAdapter(Context context, int resource, List<TransactionItem> items) {
            super(context, resource, items);
        }

        @NonNull @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_transaction_history, parent, false);
            }

            TransactionItem transactionItem = getItem(position);

            if (transactionItem != null) {
                TextView transactionSummaryTextView = convertView.findViewById(R.id.item_transaction_summary_text_view);
                TextView transactionDateTextView = convertView.findViewById(R.id.item_transaction_date_text_view);
                TextView transactionCarDetailsTextView = convertView.findViewById(R.id.item_transaction_car_info_text_view);
                TextView transactionAmountTextView = convertView.findViewById(R.id.item_transaction_amount_text_view);

                if (!transactionItem.getPurpose().isEmpty())
                    transactionSummaryTextView.setText(transactionItem.getPurpose());
                else
                    transactionSummaryTextView.setVisibility(View.GONE);

                transactionDateTextView.setText(transactionItem.getDate());

                if (!transactionItem.getCarVariantName().isEmpty())
                    transactionCarDetailsTextView.setText(String.format("%s (%s)", transactionItem.getCarVariantName(), transactionItem.getListingId()));
                else
                    transactionCarDetailsTextView.setVisibility(View.GONE);

                transactionAmountTextView.setText(Helper.getIndianCurrencyFormat(transactionItem.getAmount().intValue()));

                String transactionType = transactionItem.getTransactionType();
                if (transactionType.equals("CR")) {
                    transactionAmountTextView.setTextColor(Color.parseColor("#00B289"));
                }
                else if (transactionType.equals("DB")) {
                    transactionAmountTextView.setTextColor(Color.parseColor("#FF7143"));
                }
            }

            return convertView;
        }
    }

    class TransactionItem {

        String date;
        String purpose;
        String carVariantName;
        String transactionType;
        int listingId = -1;
        Double amount;

        TransactionItem(JSONObject item) {
            try {
                amount = item.getDouble("amount");
                date = item.getString("transaction_date");
                purpose = item.getString("purpose");
                carVariantName = item.getString("variant_name");

                if (!item.isNull("listing_id"))
                    listingId = item.getInt("listing_id");

                transactionType = item.getString("type");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getDate() {
            return date;
        }

        public String getPurpose() {
            return purpose;
        }

        public String getCarVariantName() {
            return carVariantName;
        }

        public int getListingId() {
            return listingId;
        }

        public Double getAmount() {
            return amount;

        }
        public String getTransactionType() {
            return transactionType;
        }
    }
}
