package com.xiaosong.draggableview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by xiaosong on 2017/8/21.
 */

public class MyScrollView extends ScrollView {
    private ScrollListener listener;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t + getHeight() - getPaddingBottom() -getPaddingTop() >=computeVerticalScrollRange()) {
            listener.onBottom();
        }else if (t<=8) {
            listener.onTop();
        }else {
            listener.onScrolling();
        }
        listener.onScrollChanged(t);
    }

    public void setOnScrollListener (ScrollListener listener) {
        this.listener = listener;
    }

    public interface ScrollListener {
        void onBottom();
        void onTop();
        void onScrolling();
        void onScrollChanged(int tY);
    }
}