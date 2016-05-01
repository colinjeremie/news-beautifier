package com.github.colinjeremie.newsbeautifier.activities;

import android.os.Build;
import android.widget.ListView;

import com.github.colinjeremie.newsbeautifier.BuildConfig;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.adapters.AboutAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Test for {@link AboutActivity}
 *
 * Created by jerem_000 on 5/1/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
)
public class AboutActivityTest extends BaseTest{

    @Test
    public void shouldDisplayTheVersionName() throws Exception{
        AboutActivity activity = Robolectric.buildActivity(AboutActivity.class).create().start().get();
        ListView listView = (ListView) activity.findViewById(R.id.list_view);
        Assert.assertNotNull(listView);

        AboutAdapter adapter = (AboutAdapter) listView.getAdapter();
        Assert.assertEquals(activity.getString(R.string.about_version), adapter.getItem(0).name);
        Assert.assertEquals(activity.getVersionName(), adapter.getItem(0).value);
    }
}
