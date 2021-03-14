package com.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by mac on 1/04/2017.
 */

public class GridRelativeLayout extends RelativeLayout {

    public GridRelativeLayout(Context context) {
        super(context);
    }

    public GridRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // This is the key that will make the height equivalent to its width
    }
}