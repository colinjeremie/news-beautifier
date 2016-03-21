package com.github.colinjeremie.newsbeautifier.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by james_000 on 3/10/2016.
 */
public class GridItemRelativeLayout extends RelativeLayout {
    public GridItemRelativeLayout(Context context) {
        super(context);
    }


    public GridItemRelativeLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public GridItemRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
