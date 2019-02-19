package com.truebil.business;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.truebil.business.Activities.CitiesAndRtosActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RTOActivityScreenTest {

    private IdlingResource mIdlingResource;
    private static final String TAG = "RTOActivityScreenTest";

    @Rule
    public ActivityTestRule<CitiesAndRtosActivity> mActivityTestRule = new ActivityTestRule<CitiesAndRtosActivity>(CitiesAndRtosActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            // Set Dummy SharedPreferences data
            TestHelper.setUserDummyData(InstrumentationRegistry.getTargetContext());

            //Clear RTO Cache
            Helper.clearRTOPref(getInstrumentation().getTargetContext());

            super.beforeActivityLaunched();
        }
    };

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void clickSelectRTOButton_OpensRTOActivity_selectRTOOptionCheckbox() {

        int POSITION = 0;

        // Step-1: Get the name of the first option
        String rtoText = TestHelper.getText(TestHelper.withIndex(withId(R.id.item_multiselect_view_place_name_text_view), POSITION));

        /*
         * Step-2: Select first option from listview in RTOSelectionFragment (inside CitiesAndRtosActivity)
         * Source: https://stackoverflow.com/questions/39021133/espresso-click-first-item-in-listview-inside-a-viewpager
         */
        onData(anything())
                .inAdapterView(allOf(withId(R.id.listview), isCompletelyDisplayed()))
                .atPosition(POSITION)
                .onChildView(withId(R.id.item_multiselect_view_place_name_check_box))
                .perform(click());

        // Step-3: Check if SharedPreferences are updated
        if (rtoText.toLowerCase().contains("mumbai")) {
            String rtoCitiesPref = Helper.getPreference(getInstrumentation().getTargetContext(), Constants.Keys.CITIES);
            assertTrue(rtoCitiesPref.contains("1"));
        }
        else
            assertTrue(false);
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}
