package com.github.colinjeremie.newsbeautifier.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.colinjeremie.newsbeautifier.R;

public class WebViewActivity extends AppCompatActivity {

    public static String TITLE = "TITLE";
    public static String URL = "URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_view);
        setProgressBarVisibility(true);

        String title = getIntent().getStringExtra(TITLE);
        if (title != null && !title.isEmpty()){
            setTitle(title);
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        WebView webview = (WebView) findViewById(R.id.web_view);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        webview.loadUrl(getIntent().getStringExtra(URL));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
