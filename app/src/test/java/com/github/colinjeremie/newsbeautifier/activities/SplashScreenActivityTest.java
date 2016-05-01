package com.github.colinjeremie.newsbeautifier.activities;

import android.os.Build;

import com.github.colinjeremie.newsbeautifier.BuildConfig;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLooper;

/**
 * Test for the {@link SplashScreenActivity}
 *
 * Created by jerem_000 on 5/1/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
)
public class SplashScreenActivityTest extends BaseTest{

    @Test
    public void shouldLaunchNextActivityAfterXMs() throws Exception{
        SplashScreenActivity activity = Robolectric.buildActivity(SplashScreenActivity.class).create().start().get();
        ShadowActivity shadowActivity = (ShadowActivity) ShadowExtractor.extract(activity);

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Assert.assertNotNull(shadowActivity.getNextStartedActivity());
    }
}
