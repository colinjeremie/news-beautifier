package com.github.colinjeremie.newsbeautifier.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.colinjeremie.newsbeautifier.R;

/**
 * An activity embedded a {@link WebView}
 */
public class WebViewActivity extends AppCompatActivity {

    /**
     * The activity title
     */
    public static String TITLE = "com.github.colinjeremie.newsbeautifier.activities.TITLE";

    /**
     * The url to load
     */
    public static String URL = "com.github.colinjeremie.newsbeautifier.activities.URL";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);
        setProgressBarVisibility(true);

        String title = getIntent().getStringExtra(TITLE);
        if (title != null && !title.isEmpty()) {
            setTitle(title);
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        WebView webview = (WebView) findViewById(R.id.web_view);

        if (webview != null) {
            if (webview.getSettings() != null) {
                webview.getSettings().setJavaScriptEnabled(true);
            }
            webview.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                        if (progress == 100) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }
            });
            webview.loadUrl(getIntent().getStringExtra(URL));
        }
    }

    /**
     * Display the back button navigation
     *
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Bind the back button
     *
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
