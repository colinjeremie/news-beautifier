package com.github.newsbeautifier.activities;

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
import com.github.newsbeautifier.R;
import com.github.newsbeautifier.models.RSSItem;
import com.github.newsbeautifier.utils.RSSItemHelper;

import org.jsoup.Jsoup;

public class ArticleActivity extends AppCompatActivity implements View.OnClickListener {

    public static String ARTICLE = "ARTICLE";

    private RSSItem mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        findViewById(R.id.share_fab).setOnClickListener(this);

        mModel = getIntent().getParcelableExtra(ARTICLE);
        collapsingToolbarLayout.setTitle(mModel.getCategory().isEmpty() ? "Article" : mModel.getCategory());
        initViews();
    }

    private void initViews(){
        TextView title = (TextView) findViewById(R.id.article_title);
        TextView author = (TextView) findViewById(R.id.article_published_author);
        TextView pubDate = (TextView) findViewById(R.id.article_published_date);
        TextView content = (TextView) findViewById(R.id.article_content);
        TextView link = (TextView) findViewById(R.id.article_link);

        ImageView cover = (ImageView) findViewById(R.id.article_cover);
        ImageView image = (ImageView) findViewById(R.id.article_image);

        title.setText(Html.fromHtml(mModel.getTitle() != null ? mModel.getTitle() : ""));
        if (!mModel.getAuthor().isEmpty()) {
            author.setText(getString(R.string.article_author, mModel.getAuthor()));
        } else {
            author.setVisibility(View.GONE);
        }
        pubDate.setText(getString(R.string.article_pubDate, mModel.getPubDate()));
        if (mModel.getLink().isEmpty()){
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

        String text = Jsoup.parse(mModel.getContent().isEmpty() ? mModel.getDescription() : mModel.getContent()).text();
        content.setText(text);

        String urlImgDescription = RSSItemHelper.getUrlImgFromXmlString(mModel.getDescription());
        String urlImage = mModel.getImage();

        if (urlImage.isEmpty()){
            urlImage = urlImgDescription;
        }
        if (urlImage != null) {
            Glide
                    .with(this)
                    .load(urlImage)
                    .centerCrop()
                    .into(cover);
            Glide
                    .with(this)
                    .load(urlImage)
                    .asBitmap()
                    .centerCrop()
                    .into(image);
        } else {
            image.setVisibility(View.GONE);
        }
    }

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

