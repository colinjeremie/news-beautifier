package com.github.colinjeremie.newsbeautifier.activities;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.github.colinjeremie.newsbeautifier.BuildConfig;
import com.github.colinjeremie.newsbeautifier.R;
import com.github.colinjeremie.newsbeautifier.models.RSSItem;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowActivity;

import java.text.DateFormat;

/**
 * Test for {@link ArticleActivity}
 *
 * Created by jerem_000 on 5/1/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(
        constants = BuildConfig.class,
        sdk = Build.VERSION_CODES.LOLLIPOP
)
public class ArticleActivityTest extends BaseTest{

    public static RSSItem getDummyData(){
        RSSItem item = new RSSItem();
        item.setAuthor("Fabio");
        item.setCategory("Buzz");
        item.setDescription("Le Face Swapping ou l’échange de visages en bon français n’est pas nouveau. Cela fait près de 10 ans que […]");
        item.setFeedLink("http://www.journaldugeek.com");
        item.setGuid("http://www.journaldugeek.com/?p=384771");
        item.setImage("http://www.journaldugeek.com/wp-content/blogs.dir/1/files/2016/04/b-1-1-160x105.jpg");
        item.setLink("http://feedproxy.google.com/~r/LeJournalDuGeek/~3/cht4LdQstfQ/");
        item.setTitle("Top 20 des meilleurs Faceswap de l’histoire");
        item.setPubDate("Fri, 29 Apr 2016 15:07:43 +0000");

        return item;
    }

    @Test
    public void shouldDisplayTheGoodInformations() throws Exception{
        DateFormat format = DateFormat.getDateInstance(DateFormat.FULL);

        RSSItem article = getDummyData();
        Intent intent = new Intent();
        intent.putExtra(ArticleActivity.ARTICLE, article);

        ArticleActivity activity = Robolectric.buildActivity(ArticleActivity.class).withIntent(intent).create().get();
        TextView titleView = (TextView) activity.findViewById(R.id.article_title);
        TextView authorView = (TextView) activity.findViewById(R.id.article_published_author);
        TextView contentView = (TextView) activity.findViewById(R.id.article_content);
        TextView pubDate = (TextView) activity.findViewById(R.id.article_published_date);
        TextView linkView = (TextView) activity.findViewById(R.id.article_link);

        Assert.assertNotNull(titleView);
        Assert.assertEquals(article.getTitle(), titleView.getText().toString());
        Assert.assertNotNull(authorView);
        Assert.assertEquals(activity.getString(R.string.article_author, article.getAuthor()), authorView.getText());
        Assert.assertNotNull(contentView);
        Assert.assertEquals(article.getDescription(), contentView.getText());
        Assert.assertNotNull(pubDate);
        if (article.getPubDateFormat() != null) {
            Assert.assertEquals(activity.getString(R.string.article_pubDate, format.format(article.getPubDateFormat())), pubDate.getText());
        }
        Assert.assertNotNull(linkView);
        Assert.assertTrue(linkView.getVisibility() == View.VISIBLE);
    }

    @Test
    public void shouldRedirectToTheWebViewActivity() throws Exception{
        RSSItem article = getDummyData();
        Intent intent = new Intent();
        intent.putExtra(ArticleActivity.ARTICLE, article);

        ArticleActivity activity = Robolectric.buildActivity(ArticleActivity.class).withIntent(intent).create().resume().get();
        TextView linkView = (TextView) activity.findViewById(R.id.article_link);

        Assert.assertNotNull(linkView);
        Assert.assertTrue(linkView.getVisibility() == View.VISIBLE);
        linkView.performClick();
        ShadowActivity shadowActivity = (ShadowActivity) ShadowExtractor.extract(activity);

        Assert.assertEquals(shadowActivity.getNextStartedActivity().getComponent().getClassName(), WebViewActivity.class.getName());
    }

    @Test
    public void shouldHideTheReadMoreButton() throws Exception{
        RSSItem article = getDummyData();
        article.setLink("");
        Intent intent = new Intent();
        intent.putExtra(ArticleActivity.ARTICLE, article);

        ArticleActivity activity = Robolectric.buildActivity(ArticleActivity.class).withIntent(intent).create().resume().get();
        TextView linkView = (TextView) activity.findViewById(R.id.article_link);

        Assert.assertNotNull(linkView);
        Assert.assertFalse(linkView.getVisibility() == View.VISIBLE);
    }
}
