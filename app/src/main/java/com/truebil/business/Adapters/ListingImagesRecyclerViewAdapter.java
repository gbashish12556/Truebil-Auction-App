package com.truebil.business.Adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.truebil.business.Models.CarListModel;
import com.truebil.business.R;

public class ListingImagesRecyclerViewAdapter extends RecyclerView.Adapter<ListingImagesRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private CarListModel carListModel;
    private Fragment fragment;
    private ListingImagesRecyclerViewInterface mCallback;

    public interface ListingImagesRecyclerViewInterface {
        void onListingRecyclerViewImageClick(CarListModel carListModel);
    }

    public ListingImagesRecyclerViewAdapter(Fragment fragment, CarListModel carListModel) {
        this.mInflater = LayoutInflater.from(fragment.getContext());
        this.carListModel = carListModel;
        this.fragment = fragment;

        try {
            mCallback = (ListingImagesRecyclerViewInterface) fragment;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity or Fragment must implement ListingImagesRecyclerViewInterface");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.carousel_image_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ImageView imageView = holder.listingImageView;
        final ImageView errorImageView = holder.errorImageView;
        errorImageView.setVisibility(View.GONE);

        // Get image URLs from the list when position is within array index limits
        if (position < carListModel.getShowcaseImageList().size()) {
            String imageURL = carListModel.getShowcaseImageList().get(position);
            Picasso.with(fragment.getContext()).load(imageURL).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    errorImageView.setVisibility(View.VISIBLE);
                }
            });
        }
        // Display error image (This happens when we set count to 2 but the array has no values)
        else {
            errorImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (carListModel.getShowcaseImageList().size() > 0) {
            return carListModel.getShowcaseImageList().size();
        }
        else {
            /*
             * If API returns an empty showcase URL array
             * Need to display 3 images showing image error while loading
             */
            return 3;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView listingImageView;
        ImageView errorImageView;

        ViewHolder(View itemView) {
            super(itemView);
            listingImageView = itemView.findViewById(R.id.car_image);
            errorImageView = itemView.findViewById(R.id.carousel_image_broke_image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //startListingActivity(carListModel);

            if (mCallback !=  null)
                mCallback.onListingRecyclerViewImageClick(carListModel);
        }
        //getAdapterPosition()
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return carListModel.getShowcaseImageList().get(id);
    }
}
