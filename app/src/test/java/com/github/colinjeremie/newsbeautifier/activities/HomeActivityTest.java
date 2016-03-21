package com.github.newsbeautifier.activities;

import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import com.github.colinjeremie.newsbeautifier.activities.HomeActivity;
import com.github.newsbeautifier.BuildConfig;
import com.github.newsbeautifier.NewRobolectricGradleTestRunner;
import com.github.newsbeautifier.R;
import com.github.newsbeautifier.TestApplication;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertNotNull;

/**
 * *
 * Created by jerem_000 on 10/25/2015.
 */
@RunWith(NewRobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        application = TestApplication.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
)
public class HomeActivityTest {

    private ShadowActivity shadowActivity;

    @Before
    public void setup(){
        HomeActivity activity = Robolectric.setupActivity(HomeActivity.class);
        assertNotNull(activity);
        shadowActivity = Shadows.shadowOf(activity);

        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ListView headerList = (ListView) navigationView.getHeaderView(0).findViewById(R.id.left_drawer);
        Shadows.shadowOf(headerList).performItemClick(0);
        Shadows.shadowOf(headerList).performItemClick(1);
    }

    @Test
    public void exampleTest(){
        Assert.assertTrue(true);
    }
}
