package com.xiaosong.draggableview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.xiaosong.draggableview.interfaces.DraggableListener;


/**
 * ViewGroup 自行监听处理拖拽动画
 *
 * @author xiaosong
 */

public class HorizontalDraggableView extends FrameLayout {

    private Context mContext;
    private static String TAG = "HorizontalDragView";
    private int currentScreen = 0;  //当前屏

    private Scroller mScroller = null;
    private DraggableListener listener;

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;

    public static int SNAP_VELOCITY = 3000;//Fling快滑速度阈值
    private int mTouchSlop = 0;
    private float mLastMotionX = 0;
    private float mLastMotionY = 0;
    //处理触摸的速率
    private VelocityTracker mVelocityTracker = null;
    private int mMoveDistance;
    private boolean isFirst = true;
    private boolean isLast = true;
    private boolean isFullScreen = false;

    public HorizontalDraggableView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public HorizontalDraggableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init() {
        mScroller = new Scroller(mContext);
        //获取系统移动距离阈值
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        //表示已经开始滑动了，不需要走该Action_MOVE方法了(第一次时可能调用)。
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return !isFullScreen;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "onInterceptTouchEvent down");
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG, "onInterceptTouchEvent move");
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                final int yDiff = (int) Math.abs(mLastMotionY - y);

                if (xDiff > yDiff && xDiff >= mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;


            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                Log.e(TAG, "onInterceptTouchEvent up or cancel");
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        Log.d(TAG, "mTouchState=" + mTouchState + ", isFullScreen=" + isFullScreen + ", TouchEvent=" + ev.getAction());
        //在滑动状态 && 非全屏 && 允许拦截（子控件不需要消费事件）
        return mTouchState != TOUCH_STATE_REST && !isFullScreen && super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (isFullScreen) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        //手指x轴位置
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //如果屏幕的动画还没结束就按下了，就结束该动画
                if (mScroller != null) {
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentScreen == 0) {
                    int diffX = (int) (mLastMotionX - x);
                    int diffY = (int) (mLastMotionY - y);
                    if (2 * Math.abs(diffY) < Math.abs(diffX)) {//X值大于两倍Y值，才认为是水平滑动
                        scrollBy(diffX, 0);
                    }
                    mLastMotionX = x;
                    mLastMotionY = y;
                    mMoveDistance = mMoveDistance + diffX;
                    Log.d(TAG, "onTouchEvent - ACTION_MOVE," + "diffX:" + diffX);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (currentScreen == 0) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000);

                    int velocityX = (int) velocityTracker.getXVelocity();
//
                    //快速向右滑屏，马上进行切屏处理
                    if (velocityX > SNAP_VELOCITY) {
                        // Fling enough to move left
                        Log.e(TAG, "close to right，velocityX：" + velocityX);
                        snapToScreen(currentScreen - 1);

                    }
                    //快速向左滑屏
                    else if (velocityX < -SNAP_VELOCITY) {
                        Log.e(TAG, "close to left，velocityX：" + velocityX);
                        snapToScreen(currentScreen + 1);
                    }
                    //缓慢滑动
                    else {
                        snapToDestination(mMoveDistance > 0);
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }

                mMoveDistance = 0;
                break;
            default:
                break;
        }

        return true;
    }


    private void snapToDestination(boolean isToLeft) {  //缓慢移动，超过切屏，否则还原

        int destScreen;
        if (isToLeft) {
            destScreen = (getScrollX() + getWidth() * 2 / 3) > getWidth() ? 1 : 0;
        } else {
            destScreen = (getScrollX() + getWidth() / 3) < 0 ? -1 : 0;
        }
        snapToScreen(destScreen);
    }


    /**
     * 还原状态
     */
    public void onRest() {
        Log.d(TAG, "onRest");
        currentScreen = 0;
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, Math.abs(getScrollX()));
    }

    /**
     * 显示
     */
    public void show() {
        currentScreen = 0;
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 0);

        //渐显动画
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0, 1.0f);
        alphaAnimator.setDuration(100);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAlpha((float) animation.getAnimatedValue());
            }
        });
        alphaAnimator.start();
        postInvalidate();
    }

    private void snapToScreen(final int whichScreen) {//直接切屏(-1, 0, 1)
        currentScreen = whichScreen;
        if (currentScreen > 1) {
            currentScreen = 1;
        } else if (currentScreen < -1) {
            currentScreen = -1;
        }

//        Log.d(TAG, "onTouchEvent  ACTION_UP--->  Scroll To Screen:" + currentScreen);
        if (isFirst && currentScreen == -1) {
            onRest();

        } else if (isLast && currentScreen == 1) {
            onRest();

        } else {
            int dx = currentScreen * getWidth() - getScrollX();
            mScroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx));

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (whichScreen == 1) {
                        notifyCloseToLeftListener();
                    } else if (whichScreen == -1) {
                        notifyCloseToRightListener();
                    }
                }
            }, Math.abs(dx));
        }
        invalidate();
    }


    /**
     * 设置拖拽结果监听器
     */
    public void setDraggableListener(DraggableListener listener) {
        this.listener = listener;
    }

    /**
     * 设置是否为边界值
     */
    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    private void notifyCloseToRightListener() {
        if (listener != null) {
            listener.onClosedToRight();
        }
    }

    private void notifyCloseToLeftListener() {
        if (listener != null) {
            listener.onClosedToLeft();
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int startLeft = -getWidth(); // 每个子视图的起始布局坐标
        int startTop = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            //可见的区域，才显示
            if (child.getVisibility() != View.GONE)
                child.layout(startLeft, startTop, startLeft + getWidth(), startTop + getHeight());

            startLeft = startLeft + getWidth(); //校准每个子View的起始布局位置
            //三个子视图的在屏幕中的分布如下 [ -Width , 0] / [0, Width] / [Width, 2*Width]
        }
    }

    public void setFullScreen(boolean fullScreen) {
        this.isFullScreen = fullScreen;
    }
}
