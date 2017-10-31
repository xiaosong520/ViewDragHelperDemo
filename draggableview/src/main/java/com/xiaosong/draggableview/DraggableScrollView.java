package com.xiaosong.draggableview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * Created by xiaosong on 2017/8/21.
 */
public class DraggableScrollView extends ScrollView {

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_HORIZONTAL_SCROLLING = 1;
    private static final int TOUCH_STATE_VERTICAL_SCROLLING = -1;
    private static final String TAG = "DragScrollView-->";

    private float mLastMotionX;
    private float mLastMotionY;

    private int mTouchState = TOUCH_STATE_REST;
    private ScrollListener listener;
    private int mTouchSlop = 0;
    private boolean isFullScreen = false;

    public DraggableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //初始化滑动距离阈值
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setOnScrollListener(ScrollListener listener) {
        this.listener = listener;
    }



    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        super.onInterceptTouchEvent(ev);//继承父类方法，让它可以实现Scrollview的滑动

        if (isFullScreen){//全屏状态，不滑动ScrollView
            return false;
        }

        final int action = ev.getAction();
        boolean intercept = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchState = TOUCH_STATE_REST;
                mLastMotionY = ev.getY();
                mLastMotionX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                    Log.i(TAG, "mTouchState：水平已判定");
                    intercept = false;
                } else if (mTouchState == TOUCH_STATE_VERTICAL_SCROLLING) {
                    Log.i(TAG, "mTouchState：竖直已判定");
                    intercept = true;
                } else {

                    final float x = ev.getX();
                    final float y = ev.getY();

                    final int xDiff = (int) Math.abs(x - mLastMotionX);
                    final int yDiff = (int) Math.abs(y - mLastMotionY);

                    /*水平方向距离大于竖直两倍，则认为它为水平滑动，否则认为是竖直滑动*/
                    boolean xMoved = (xDiff >= 2 * yDiff);
                    boolean yMoved = (xDiff < 2 * yDiff);

                    if (xMoved) {
                        Log.i(TAG, "mTouchState：水平");
                        if (mTouchSlop <yDiff){
                            mTouchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
                        }

                    }else if (yMoved) {
                        Log.i(TAG, "mTouchState：竖直");
                        if (mTouchSlop <yDiff){
                            mTouchState = TOUCH_STATE_VERTICAL_SCROLLING;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // Release the state.
                Log.i(TAG, "onInterceptTouchEvent：ACTION_UP");
                mTouchState = TOUCH_STATE_REST;
                intercept = false;
                break;
            default:
                break;
        }

        Log.i("MultiScroll-->", "intercept：" + intercept);
        return intercept && !isForbidden();
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t <= 0) {
            listener.isOnTop(true);
        } else {
            listener.isOnTop(false);
        }
        listener.onScrollChanged(t);
    }

    private boolean isForbidden() {
        return listener != null && listener.isForbidden();
    }


    public interface ScrollListener {
        void isOnTop(boolean isTop);

        void onScrollChanged(int tY);

        boolean isForbidden();
    }

    public void setFullScreen(boolean fullScreen) {
        this.isFullScreen = fullScreen;
    }
}