package com.github.colinjeremie.newsbeautifier.adapters;

import android.os.Build;
import android.view.View;

import com.github.colinjeremie.newsbeautifier.BuildConfig;
import com.github.colinjeremie.newsbeautifier.activities.BaseTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link AboutAdapter}
 *
 * Created by jerem_000 on 5/1/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
)
public class AboutAdapterTest extends BaseTest{

    static public List<AboutAdapter.Model> _data;

    static {
        _data = new ArrayList<>();
        _data.add(new AboutAdapter.Model("test", "test_value"));
        _data.add(new AboutAdapter.Model("test2", "test2_value"));
        _data.add(new AboutAdapter.Model("test3", "test3_value"));
    }

    @Test
    public void shouldDisplayTheseData() throws Exception{
        AboutAdapter adapter = new AboutAdapter(RuntimeEnvironment.application);

        adapter.addAll(_data);
        Assert.assertEquals(_data.size(), adapter.getCount());
        Assert.assertEquals(_data.get(1), adapter.getItem(1));
        View v = adapter.getView(0, null, null);

        Assert.assertTrue(v.getTag() instanceof AboutAdapter.ViewHolder);
        AboutAdapter.ViewHolder viewHolder = (AboutAdapter.ViewHolder) v.getTag();

        Assert.assertEquals(_data.get(0).name, viewHolder.title.getText());
        Assert.assertEquals(_data.get(0).value, viewHolder.subTitle.getText());

        v = adapter.getView(0, v, null);
        Assert.assertEquals(viewHolder, v.getTag());
    }
}
