package com.github.colinjeremie.newsbeautifier.activities;

import android.content.Intent;
import android.os.Build;
import android.webkit.WebView;

import com.github.colinjeremie.newsbeautifier.BuildConfig;
import com.github.colinjeremie.newsbeautifier.R;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowWebView;

/**
 * Test for {@link WebViewActivity}
 *
 * Created by jerem_000 on 5/1/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
)
public class WebViewActivityTest extends BaseTest{

    private static final String CORRECT_TITLE = "My title";
    private static final String CORRECT_URL = "https://google.com";

    @Test
    public void shouldDisplayTheRightURL() throws Exception{
        Intent intent = new Intent();
        intent.putExtra(WebViewActivity.TITLE, CORRECT_TITLE);
        intent.putExtra(WebViewActivity.URL, CORRECT_URL);
        WebViewActivity activity = Robolectric.buildActivity(WebViewActivity.class).withIntent(intent).create().start().get();

        WebView webview = (WebView) activity.findViewById(R.id.web_view);
        Assert.assertNotNull(webview);

        ShadowWebView shadowwebview = (ShadowWebView) ShadowExtractor.extract(webview);
        Assert.assertEquals(CORRECT_TITLE, activity.getTitle());
        Assert.assertEquals(CORRECT_URL, shadowwebview.getLastLoadedUrl());
    }
}
