package com.truebil.business.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.truebil.business.R;

import java.util.ArrayList;

public class ListingImagesViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mImageList;
    private ListingImagesViewPagerInterface mCallback;

    public interface ListingImagesViewPagerInterface {
        void onListingViewPagerImageClicked();
    }

    public ListingImagesViewPagerAdapter(Context context, ArrayList<String> imageList) {
        mContext = context;
        mImageList = imageList;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        try {
            mCallback = ((ListingImagesViewPagerInterface) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement ListingImagesViewPagerInterface");
        }
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        View itemView = mLayoutInflater.inflate(R.layout.carousel_image_view, container, false);
        final String imageURL = mImageList.get(position);

        if (imageURL != null) {
            final ImageView imageView = itemView.findViewById(R.id.car_image);
            final ImageView errorImageView = itemView.findViewById(R.id.carousel_image_broke_image_view);
            errorImageView.setVisibility(View.GONE);

            Picasso.with(mContext).load(imageURL).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startGalleryActivity();
                    mCallback.onListingViewPagerImageClicked();
                }
            });
        }

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
