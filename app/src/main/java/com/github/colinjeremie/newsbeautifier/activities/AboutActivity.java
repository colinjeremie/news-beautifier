package com.github.colinjeremie.newsbeautifier.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.colinjeremie.newsbeautifier.BuildConfig;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.adapters.AboutAdapter;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AboutAdapter aboutAdapter = new AboutAdapter(this);
        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            listView.setAdapter(aboutAdapter);
        }

        // add version
        aboutAdapter.add(new AboutAdapter.Model(getString(R.string.about_version), getVersionName()));
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

    public String getVersionName() {
        return "v" + BuildConfig.VERSION_NAME + "-r" + BuildConfig.VERSION_CODE;
    }
}
