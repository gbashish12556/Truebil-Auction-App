package com.truebil.business.Activities;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.truebil.business.Constants;
import com.truebil.business.Network.VolleyService;
import com.truebil.business.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    private static String TAG = "GalleryActivity";
    List<GalleryImageInfo> galleryImageInfoList = new ArrayList<>();
    ListAdapter galleryImageListAdapter;
    LayoutPagerAdapter galleryImagePagerAdapter;
    RequestQueue volleyRequestQueue;
    ListView galleryImageListView;
    ViewPager galleryImageViewPager;
    HorizontalScrollView headerTabScrollView;
    PhotoView zoomPhotoView;
    String listingID;
    RelativeLayout zoomRelativeLayout;
    ImageButton zoomOutImageButton;
    Map<String,Integer> imageCategoryCountDict = new HashMap<>();
    Map<Integer,String> imagePositionCategoryDict = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        galleryImageListView = findViewById(R.id.activity_gallery_image_list_view);
        galleryImageListAdapter = new ListAdapter(getApplicationContext(), R.layout.item_gallery_image, galleryImageInfoList);
        galleryImageListView.setAdapter(galleryImageListAdapter);

        galleryImageViewPager = findViewById(R.id.activity_gallery_image_view_pager);
        galleryImagePagerAdapter = new LayoutPagerAdapter(this);
        galleryImageViewPager.setAdapter(galleryImagePagerAdapter);

        headerTabScrollView = findViewById(R.id.activity_gallery_header_scroll_view);

        zoomRelativeLayout = findViewById(R.id.activity_gallery_zoom_relative_layout);
        zoomRelativeLayout.setVisibility(View.GONE);

        zoomPhotoView = findViewById(R.id.activity_gallery_photo_view);
        zoomPhotoView.setZoomable(true);

        zoomOutImageButton = findViewById(R.id.activity_gallery_zoom_out_image_button);
        zoomOutImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomRelativeLayout.setVisibility(View.GONE);
            }
        });

        galleryImageListView.setVisibility(View.VISIBLE);
        galleryImageViewPager.setVisibility(View.GONE);

        listingID = getIntent().getStringExtra("LISTING_ID");

        fetchImageInfo();

        ImageButton backButton = findViewById(R.id.activity_gallery_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String manufacturingYear = getIntent().getStringExtra("LISTING_YEAR");
        String listingName = getIntent().getStringExtra("LISTING_NAME");
        String mileage = getIntent().getStringExtra("LISTING_MILEAGE");
        String listingCityName = getIntent().getStringExtra("LISTING_CITY_NAME");
        String fuelType = getIntent().getStringExtra("LISTING_FUEL_TYPE");
        String ownerDetails = getIntent().getStringExtra("LISTING_OWNER");
        String rtoInfo = getIntent().getStringExtra("LISTING_RTO_INFO");

        TextView primaryDescTextView = findViewById(R.id.item_gallery_slider_primary_description_text_view);
        primaryDescTextView.setText(manufacturingYear + " \u2022 " + listingName + " \u2022 " + fuelType + " \u2022 " + listingCityName);

        TextView secondaryDescTextView = findViewById(R.id.item_gallery_slider_secondary_description_text_view);
        secondaryDescTextView.setText(mileage + " km \u2022 " + ownerDetails + " Owner" + " \u2022 " + rtoInfo);
    }

    public class ListAdapter extends ArrayAdapter<GalleryImageInfo> {

        ListAdapter(Context context, int resource, List<GalleryImageInfo> items) {
            super(context, resource, items);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_gallery_image, parent, false);
            }

            final GalleryImageInfo galleryImageInfo = getItem(position);

            if (galleryImageInfo != null) {
                final ImageView galleryImageView = convertView.findViewById(R.id.item_gallery_image_view);
                final TextView galleryImageDescriptionTextView = convertView.findViewById(R.id.item_gallery_image_description);
                final ImageView galleryImageErrorIcon = convertView.findViewById(R.id.item_gallery_image_error_icon);

                Picasso.with(getContext())
                    .load(galleryImageInfo.getImageURL())
                    .into(galleryImageView);

                galleryImageDescriptionTextView.setText(galleryImageInfo.getImageDescription());

                if (galleryImageInfo.getIsErrorImage()) {
                    galleryImageErrorIcon.setVisibility(View.VISIBLE);
                    galleryImageErrorIcon.setImageResource(R.drawable.close_red);
                }
                else
                    galleryImageErrorIcon.setVisibility(View.GONE);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        galleryImageViewPager.setVisibility(View.VISIBLE);
                        galleryImageListView.setVisibility(View.GONE);
                        galleryImageViewPager.setCurrentItem(position, true);
                    }
                });
            }

            return convertView;
        }
    }

    void fetchImageInfo() {
        volleyRequestQueue = Volley.newRequestQueue(this);
        String url = Constants.Config.API_PATH + "/album_images/?listing_id=" + listingID;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseImageGalleryInfo(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    JSONObject additionalLogJson = new JSONObject();
                    try {
                        additionalLogJson.put("API", "/album_images");
                        additionalLogJson.put("AUCTION_ID", String.valueOf(listingID));
                        VolleyService.handleVolleyError(error, additionalLogJson, true, getApplicationContext());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        });
        jsonRequest.setTag(TAG);
        volleyRequestQueue.add(jsonRequest);
    }

    void parseImageGalleryInfo(JSONObject response) {

        ArrayList<String> headerCategoryList = new ArrayList<>();

        try {
            JSONArray ja = response.getJSONArray("album_images");
            boolean isSampleData = response.getBoolean("sample_data");

            for(int i=0; i<ja.length(); i++) {
                JSONObject object   = ja.getJSONObject(i);
                JSONArray images    = object.getJSONArray("images");
                int categoryId      = object.getInt("category_id");
                String categoryName = object.getString("category_name");

                if (imageCategoryCountDict.get(categoryName) == null)
                    imageCategoryCountDict.put(categoryName, galleryImageInfoList.size());

                for (int j=0; j<images.length(); j++) {
                    JSONObject imageObject  = images.getJSONObject(j);
                    String imageURL         = "https:" + imageObject.getString("url");
                    String imageCaption     = imageObject.getString("caption");

                    GalleryImageInfo galleryImageInfo = new GalleryImageInfo();
                    galleryImageInfo.setImageCategory(categoryName);
                    galleryImageInfo.setImageDescription(imageCaption);
                    galleryImageInfo.setImageURL(imageURL);

                    if (categoryId == 6) // Error Photos
                        galleryImageInfo.setIsErrorImage(true);
                    else
                        galleryImageInfo.setIsErrorImage(false);

                    imagePositionCategoryDict.put(galleryImageInfoList.size(), categoryName);

                    galleryImageInfoList.add(galleryImageInfo);
                }
                headerCategoryList.add(categoryName);
            }

            galleryImageListAdapter.notifyDataSetChanged();
            galleryImagePagerAdapter.notifyDataSetChanged();
            populateHeaderLayout(headerCategoryList);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void populateHeaderLayout(ArrayList<String> headerCategoryList) {

        final LinearLayout headerLinearLayout = findViewById(R.id.activity_gallery_header_linear_layout);

        for (int i=0; i<headerCategoryList.size(); i++) {
            View headerButtonLayout = getLayoutInflater().inflate(R.layout.item_gallery_header_tab, headerLinearLayout, false);
            TextView headerButtonTextView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_text_view);
            View headerButtonHighlightView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_highlight_view);
            headerButtonTextView.setText(headerCategoryList.get(i));

            if (i == 0) {
                headerButtonHighlightView.setVisibility(View.VISIBLE);
                headerButtonTextView.setTextColor(Color.parseColor("#03A9F4"));
            }
            else {
                headerButtonHighlightView.setVisibility(View.INVISIBLE);
                headerButtonTextView.setTextColor(Color.WHITE);
            }

            headerButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Unhighlight all tabs
                    for (int pos=0; pos<headerLinearLayout.getChildCount(); pos++ ) {
                        View headerButtonLayout = headerLinearLayout.getChildAt(pos);
                        TextView headerButtonTextView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_text_view);
                        View headerButtonHighlightView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_highlight_view);
                        headerButtonHighlightView.setVisibility(View.INVISIBLE);
                        headerButtonTextView.setTextColor(Color.WHITE);
                    }

                    //Highlight the selected tab
                    View headerButtonHighlightView = v.findViewById(R.id.item_gallery_header_tab_highlight_view);
                    headerButtonHighlightView.setVisibility(View.VISIBLE);
                    TextView headerButtonTextView = v.findViewById(R.id.item_gallery_header_tab_text_view);
                    headerButtonTextView.setTextColor(Color.parseColor("#03A9F4"));

                    //Scroll listview or viewpager to the category
                    String headerButtonText = headerButtonTextView.getText().toString();

                    if (galleryImageListView.getVisibility() == View.VISIBLE)
                        galleryImageListView.smoothScrollToPosition(imageCategoryCountDict.get(headerButtonText));
                    else if (galleryImageViewPager.getVisibility() == View.VISIBLE)
                        galleryImageViewPager.setCurrentItem(imageCategoryCountDict.get(headerButtonText), true);
                }
            });

            headerLinearLayout.addView(headerButtonLayout);
        }

        galleryImageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //Iterate all tabs
                for (int pos=0; pos<headerLinearLayout.getChildCount(); pos++ ) {
                    View headerButtonLayout = headerLinearLayout.getChildAt(pos);
                    TextView headerButtonTextView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_text_view);
                    View headerButtonHighlightView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_highlight_view);

                    if (headerButtonTextView.getText().toString().equals(imagePositionCategoryDict.get(firstVisibleItem + visibleItemCount - 1))) {
                        headerButtonHighlightView.setVisibility(View.VISIBLE);
                        headerButtonTextView.setTextColor(Color.parseColor("#03A9F4"));

                        // Slide horizontal layout when on extremes
                        if (pos == 0){
                            headerTabScrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                        }
                        else if (pos == headerLinearLayout.getChildCount()-1) {
                            headerTabScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                        }
                    }
                    else {
                        headerButtonHighlightView.setVisibility(View.INVISIBLE);
                        headerButtonTextView.setTextColor(Color.WHITE);
                    }
                }
            }
        });

        galleryImageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                //Iterate all tabs
                for (int pos=0; pos<headerLinearLayout.getChildCount(); pos++ ) {
                    View headerButtonLayout = headerLinearLayout.getChildAt(pos);
                    TextView headerButtonTextView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_text_view);
                    View headerButtonHighlightView = headerButtonLayout.findViewById(R.id.item_gallery_header_tab_highlight_view);

                    if (headerButtonTextView.getText().toString().equals(imagePositionCategoryDict.get(position))) {
                        headerButtonHighlightView.setVisibility(View.VISIBLE);
                        headerButtonTextView.setTextColor(Color.parseColor("#03A9F4"));

                        // Slide horizontal layout when on extremes
                        if (pos == 0){
                            headerTabScrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                        }
                        else if (pos == headerLinearLayout.getChildCount()-1) {
                            headerTabScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                        }
                    }
                    else {
                        headerButtonHighlightView.setVisibility(View.INVISIBLE);
                        headerButtonTextView.setTextColor(Color.WHITE);
                    }
                }
            }
        });
    }

    class GalleryImageInfo {
        private String imageURL;
        private String imageDescription;
        private boolean isErrorImage;
        private String imageCategory;

        void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        void setImageDescription(String imageDescription) {
            this.imageDescription = imageDescription;
        }

        void setIsErrorImage(boolean isErrorImage) {
            this.isErrorImage = isErrorImage;
        }

        void setImageCategory(String imageCategory) {
            this.imageCategory = imageCategory;
        }

        public String getImageURL() {
            return imageURL;
        }

        public String getImageDescription() {
            return imageDescription;
        }

        public boolean getIsErrorImage() {
            return isErrorImage;
        }

        public String getImageCategory() {
            return imageCategory;
        }
    }

    class LayoutPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        LayoutPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return galleryImageInfoList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            View itemView = mLayoutInflater.inflate(R.layout.item_gallery_slider_image, container, false);
            final GalleryImageInfo galleryImageInfo = galleryImageInfoList.get(position);

            if (galleryImageInfo != null) {
                final ImageView galleryImageView = itemView.findViewById(R.id.item_gallery_slider_image_view);
                final TextView galleryImageDescriptionTextView = itemView.findViewById(R.id.item_gallery_slider_image_description);
                final ImageView galleryImageErrorIcon = itemView.findViewById(R.id.item_gallery_slider_image_error_icon);
                ImageButton zoomImageButton = itemView.findViewById(R.id.item_gallery_slider_zoom_in_image_button);

                Picasso.with(mContext)
                    .load(galleryImageInfo.getImageURL())
                    .into(galleryImageView);

                galleryImageDescriptionTextView.setText( String.format(Locale.US, "%d of %d %s", position+1, getCount(), galleryImageInfo.getImageDescription()));

                if (galleryImageInfo.getIsErrorImage()) {
                    galleryImageErrorIcon.setVisibility(View.VISIBLE);
                    galleryImageErrorIcon.setImageResource(R.drawable.close_red);
                }
                else {
                    galleryImageErrorIcon.setVisibility(View.GONE);
                }

                zoomImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Picasso.with(mContext)
                                .load(galleryImageInfo.getImageURL())
                                .into(zoomPhotoView);
                        zoomRelativeLayout.setVisibility(View.VISIBLE);
                        zoomPhotoView.setScale(2.0f);
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

    @Override
    protected void onStop() {
        super.onStop();
        if (volleyRequestQueue != null) {
            volleyRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    public void onBackPressed() {

        if (zoomRelativeLayout.getVisibility() == View.VISIBLE) {
            zoomRelativeLayout.setVisibility(View.GONE);
        }
        else if (galleryImageViewPager.getVisibility() == View.VISIBLE) {
            galleryImageViewPager.setVisibility(View.GONE);
            galleryImageListView.setVisibility(View.VISIBLE);
        }
        else if (galleryImageViewPager.getVisibility() == View.GONE && galleryImageListView.getVisibility() == View.VISIBLE) {
            finish();
        }
        else {
            finish();
        }
    }
}

//Replace category name to category id in dict
//Separate function for highlighting a specific header
//Horizontal scroll view should automatically slide on extreme headers.
//Loading gif while API call result not retrieved