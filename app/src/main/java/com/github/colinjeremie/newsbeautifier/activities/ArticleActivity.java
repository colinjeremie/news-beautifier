package com.github.colinjeremie.newsbeautifier.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;
import com.github.colinjeremie.newsbeautifier.utils.RSSItemHelper;

import org.jsoup.Jsoup;

import java.text.DateFormat;

/**
 * Display a blog article
 */
public class ArticleActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The key used to transfert the Article through the intent
     */
    public static String ARTICLE = "com.github.colinjeremie.newsbeautifier.activities.ARTICLE";

    /**
     * The Article to display
     */
    private RSSItem mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        View shareBtn = findViewById(R.id.share_fab);

        if (shareBtn != null){
            shareBtn.setOnClickListener(this);
        }

        mModel = getIntent().getParcelableExtra(ARTICLE);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(mModel.getCategory().isEmpty() ? getString(R.string.toolbar_title_article) : mModel.getCategory());
        }
        initViews();
    }

    /**
     * Get and bind the views
     */
    private void initViews(){
        TextView title = (TextView) findViewById(R.id.article_title);
        TextView author = (TextView) findViewById(R.id.article_published_author);
        TextView pubDate = (TextView) findViewById(R.id.article_published_date);
        TextView content = (TextView) findViewById(R.id.article_content);
        TextView link = (TextView) findViewById(R.id.article_link);

        ImageView cover = (ImageView) findViewById(R.id.article_cover);

        if (title != null) {
            title.setText(Html.fromHtml(mModel.getTitle() != null ? mModel.getTitle() : ""));
        }
        if (author != null) {
            if (!mModel.getAuthor().isEmpty()) {
                author.setText(getString(R.string.article_author, mModel.getAuthor()));
            } else {
                author.setVisibility(View.GONE);
            }
        }
        if (pubDate != null) {
            if (mModel.getPubDateFormat() != null){
                DateFormat format = DateFormat.getDateInstance(DateFormat.FULL);
                pubDate.setText(getString(R.string.article_pubDate, format.format(mModel.getPubDateFormat())));
            } else {
                pubDate.setText(getString(R.string.article_pubDate, mModel.getPubDate()));
            }
        }
        if (link != null) {
            if (mModel.getLink().isEmpty()) {
                link.setVisibility(View.GONE);
            } else {
                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ArticleActivity.this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.TITLE, mModel.getTitle());
                        intent.putExtra(WebViewActivity.URL, mModel.getLink());

                        startActivity(intent);
                    }
                });
            }
        }
        if (content != null) {
            String text = Jsoup.parse(mModel.getContent().isEmpty() ? mModel.getDescription() : mModel.getContent()).text();
            content.setText(text);
        }

        String urlImgDescription = RSSItemHelper.getUrlImgFromXmlString(mModel.getDescription());
        String urlImage = mModel.getImage();

        if (urlImage.isEmpty()){
            urlImage = urlImgDescription;
        }
        if (urlImage != null && cover != null) {
            Glide
                    .with(this)
                    .load(urlImage)
                    .centerCrop()
                    .into(cover);
        }
    }

    /**
     * Launch an intent to share the article
     */
    private void shareArticle(){
        if (mModel != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mModel.getTitle() + "\n" + mModel.getLink());
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.send_to)));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.share_fab){
            shareArticle();
        }
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

