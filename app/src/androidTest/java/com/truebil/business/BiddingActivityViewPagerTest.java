package com.truebil.business;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.truebil.business.Activities.BiddingActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class BiddingActivityViewPagerTest {

    @Rule
    public ActivityTestRule<BiddingActivity> mActivityTestRule = new ActivityTestRule<BiddingActivity>(BiddingActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            TestHelper.setUserDummyData(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };

    @Test
    public void clickMyBids_opensMyBidsFragment() {
        // Step-1: Click MyBids
        onView(withId(R.id.my_bid)).perform(click());

        // Step-2: Check TabLayout contains "Losing", "Winning" and "History"
        Matcher<View> matcher = allOf(withText("Losing"), isDescendantOfA(withId(R.id.tab_layout)));
        onView(matcher).perform(click());

        matcher = allOf(withText("Winning"), isDescendantOfA(withId(R.id.tab_layout)));
        onView(matcher).perform(click());

        matcher = allOf(withText("History"), isDescendantOfA(withId(R.id.tab_layout)));
        onView(matcher).perform(click());

        // Step-3: Check "My Bids" is displayed on header
        onView(withId(R.id.activity_bidding_header_text_view)).check(matches(withText("MY BIDS")));
    }

    @Test
    public void clickProcured_opensProcuredFragment() {
        // Step-1: Click Procured
        onView(withId(R.id.procured)).perform(click());

        // Step-2: Check TabLayout contains "Losing", "Winning" and "History"
        Matcher<View> matcher = allOf(withText("Negotiating"), isDescendantOfA(withId(R.id.tab_layout)));
        onView(matcher).perform(click());

        matcher = allOf(withText("Procured"), isDescendantOfA(withId(R.id.tab_layout)));
        onView(matcher).perform(click());

        matcher = allOf(withText("Deal Cancelled"), isDescendantOfA(withId(R.id.tab_layout)));
        onView(matcher).perform(click());

        // Step-2: Check "Procured" is displayed on header
        onView(withId(R.id.activity_bidding_header_text_view)).check(matches(withText("PROCURED")));
    }

    @Test
    public void clickInAuction_opensInAuctionFragment() {
        // Step-1: Click InAuction
        onView(withId(R.id.in_auction)).perform(click());

        // Step-2: Check "In Auction" is displayed on header
        onView(withId(R.id.activity_bidding_header_text_view)).check(matches(withText("IN AUCTION")));

        // TODO: How would you check if the fragment displays the in-auction cars?
    }
}
