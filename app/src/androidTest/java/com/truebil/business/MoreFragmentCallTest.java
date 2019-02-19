package com.truebil.business;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.truebil.business.Activities.BiddingActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MoreFragmentCallTest {

    private IdlingResource mIdlingResource;
    private static final String TRUEBIL_PHONE_NUMBER = "02262459799";
    private static final Uri INTENT_DATA_PHONE_NUMBER = Uri.parse("tel:" + TRUEBIL_PHONE_NUMBER);

    @Rule
    public IntentsTestRule<BiddingActivity> mActivityTestRule = new IntentsTestRule<BiddingActivity>(BiddingActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            TestHelper.setUserDummyData(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };

    @Before
    public void stubAllExternalIntents() {
        // Stubbing needs to be setup before every test run
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void clickMore_ClickCallTruebil_TestsCallIntent() {

        // Step-1: Click More LinearLayout
        onView(withId(R.id.more)).perform(click());

        // Step-2: Click "Call Truebil" button
        onView(withId(R.id.fragment_more_menu_call_truebil_relative_layout)).perform(click());

        // Step-3: Check if intent is correct
        intended(allOf(
                hasAction(Intent.ACTION_DIAL),
                hasData(INTENT_DATA_PHONE_NUMBER)));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

}
