package net.sitecore.android.mediauploader.provider;

import android.test.ActivityInstrumentationTestCase2;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.ui.WelcomeActivity;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

public class WelcomeActivityTest extends ActivityInstrumentationTestCase2<WelcomeActivity>{

    public WelcomeActivityTest() {
        super(WelcomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testCaseLogInAsExtranetUser() {

        String urlString = "http://mobiledev1ua1.dk.sitecore.net:722";
        String loginString = "extranet\\creatorex";
        String passString = "creatorex";
        //clear fields
        onView(withId(R.id.edit_url)).perform(scrollTo(), clearText());
        onView(withId(R.id.edit_login)).perform(scrollTo(), clearText());
        onView(withId(R.id.edit_password)).perform(scrollTo(), clearText());
        //input data
        onView(withId(R.id.edit_url)).perform(scrollTo(), typeText(urlString));
        onView(withId(R.id.edit_login)).perform(scrollTo(), typeText(loginString));
        onView(withId(R.id.edit_password)).perform(scrollTo(), typeText(passString));
        //check
        onView(withId(R.id.edit_url)).perform(scrollTo()).check(matches(withText(urlString)));
        onView(withId(R.id.edit_login)).perform(scrollTo()).check(matches(withText(loginString)));
        onView(withId(R.id.edit_password)).perform(scrollTo()).check(matches(withText(passString)));

//      //press Login
        onView(withId(R.id.button_ok)).perform(scrollTo()).perform(click());

       //assert user logged in
        sleep(5000);
        onView(withId(R.id.action_upload_here)).check(matches(isDisplayed()));
    }

    private void sleep(int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
/*
    private static ViewAction actionOpenDrawer() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DrawerLayout.class);
            }

            @Override
            public String getDescription() {
                return "open drawer";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((DrawerLayout) view).openDrawer(GravityCompat.START);
            }
        };
    }
    private static ViewAction actionCloseDrawer() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(DrawerLayout.class);
            }

            @Override
            public String getDescription() {
                return "close drawer";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((DrawerLayout) view).closeDrawer(GravityCompat.START);
            }
        };
    }
    */
}
