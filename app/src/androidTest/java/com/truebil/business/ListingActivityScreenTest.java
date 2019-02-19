package com.truebil.business;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.truebil.business.Activities.ListingActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ListingActivityScreenTest {

    private String auctionId = "927";
    private String listingId = "46251";
    private String auctionDetails = "2014" + " \u2022 " + "Maruti Suzuki Swift DZire ZXI" + " \u2022 " + "Petrol" + " \u2022 " + "Delhi";
    private IdlingResource mIdlingResource;
    private static final String TAG = "ListingActivityScreenTest";

    @Rule
    public ActivityTestRule<ListingActivity> mActivityTestRule = new ActivityTestRule<ListingActivity>(ListingActivity.class) {

        @Override
        protected void beforeActivityLaunched() {
            TestHelper.setUserDummyData(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }

        @Override
        protected Intent getActivityIntent() {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("LISTING_ID", auctionId);
            bundle.putString("CAR_ID", listingId);
            intent.putExtras(bundle);
            return intent;
        }
    };

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void clickSeePhotosButton_opensGalleryActivity() {
        // Step-1: Click 'See Photos' button
        onView(withId(R.id.activity_listing_see_photos_button)).perform(click());

        // Checks that the GalleryActivity opens with the correct listing name displayed
        onView(withId(R.id.item_gallery_slider_primary_description_text_view)).check(matches(withText(auctionDetails)));
    }

    @Test
    public void clickDealerBidButton_updatesTopOfferText() {

        /* TODO:
         * 1) Check if listing is active. Else fail the test and show error
         * 2) Check if current user is either losing or has not placed a bid yet. Else fail test and show error
         */

        // Step-1: Click "Bid for Rs .. " linear layout
        onView(withId(R.id.fragment_in_auction_make_bid_linear_layout)).perform(click());

        // Check if "Great! You are winning this bid" is displayed
        onView(withId(R.id.fragment_in_auction_bid_status_text_view)).check(matches(withText("Great! You are winning this bid.")));

        // Wrong: Checks that Top Offer is updated with the correct value (bid amount + 2000)
        // onView(withId(R.id.top_offer)).check(matches(withText("abc")));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}