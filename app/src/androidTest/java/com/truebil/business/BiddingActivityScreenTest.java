package com.truebil.business;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.truebil.business.Activities.BiddingActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class BiddingActivityScreenTest {

    private IdlingResource mIdlingResource;
    private static final String TAG = "BiddingActivityScreenTest";

    @Rule
    public ActivityTestRule<BiddingActivity> mActivityTestRule = new ActivityTestRule<BiddingActivity>(BiddingActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            TestHelper.setUserDummyData(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void clickListViewItem_OpenListingActivity() {
        int POSITION = 0;

        // Step-1: Get the variant name from the adapter item at index POSITION
        String listingText = TestHelper.getText(TestHelper.withIndex(withId(R.id.item_car_listing_variant_name_text_view), POSITION));

        // Step-2: Open the car in InAuction listview at POSITION position
        onData(anything())
                .inAdapterView(withId(R.id.fragment_car_listings_list_view))
                .atPosition(POSITION)
                .perform(click());

        // Step-3: Check if name passed correctly in the ListingActivity
        onView(withId(R.id.activity_listing_year_make_name_text_view)).check(matches(withText(containsString(listingText))));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}
