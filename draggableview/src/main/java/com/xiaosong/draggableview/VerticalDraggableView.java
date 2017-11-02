package com.xiaosong.draggableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.example.xiaosong.draggableview.R;
import com.xiaosong.draggableview.interfaces.DraggableListener;
import com.xiaosong.draggableview.interfaces.VerticalDraggableViewCallback;


/**
 * ViewGroup 使用ViewDragHelper 帮助类进行拖拽动画处理
 *
 * @author zengsong
 */
public class VerticalDraggableView extends LinearLayout {


    private static final int INVALID_POINTER = -1;

    private static final int SLIDE_TOP = 0; //DragHelper滑动到屏幕方向
    private static final int SLIDE_BOTTOM = 1;
    private static final String TAG = "VerticalDragView";
    private int activePointerId = INVALID_POINTER;

    private ViewDragHelper viewDragHelper;
    private DraggableListener listener;

    private View mDragView; //拖动的子控件
    private int mDragViewId;

    private boolean isFullScreen = false;// 是否为全屏状态

    private boolean isScrollToTop = true;// 是否已滑动到顶部
    private double mDownX = 0;
    private double mDownY = 0;
    private double mMoveY;
    private double mMoveX;

    private double mMoveDistance = 0;
    public boolean isJudgeWay = false; //是否已判定滑动方向

    public final String MOVE_IDLE = "IDLE";
    public final String MOVE_TOP = "TOP";
    public final String MOVE_BOTTOM = "BOTTOM";
    public final String MOVE_LEFT = "LEFT";
    public final String MOVE_RIGHT = "RIGHT";
    public String Move_Way = MOVE_TOP;
    private boolean canDoublePointer = false;


    public VerticalDraggableView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        initializeViewDragHelper(context);
    }

    public VerticalDraggableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        initializeViewDragHelper(context);
        initializeAttributes(attrs);
    }

    public VerticalDraggableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
        initializeViewDragHelper(context);
        initializeAttributes(attrs);
    }


    private void initializeAttributes(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.draggable_view);
        this.mDragViewId = attributes.getResourceId(R.styleable.draggable_view_drag_view_id, 0);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            mapGUI();
        }
    }

    /**
     * 绑定子控件
     */
    private void mapGUI() {
        mDragView = findViewById(mDragViewId);

        if (getBackground() == null) {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }


    private void initializeViewDragHelper(Context context) {

        viewDragHelper = ViewDragHelper.create(this, 1.0f, new VerticalDraggableViewCallback(this));
    }


    /**
     * Override method to intercept only touch events over the drag view and to cancel the drag when
     * the action associated to the MotionEvent is equals to ACTION_CANCEL or ACTION_UP.
     *
     * @param ev captured.
     * @return true if the view is going to process the touch event or false if not.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(ev);
        if (!isEnabled()) {
            Log.e(TAG, "onInterceptTouchEvent, do not Enabled");
            return false;
        }
        int index = MotionEventCompat.getActionIndex(ev);
        activePointerId = ev.getPointerId(index);
        if (activePointerId == INVALID_POINTER) {
            Log.e(TAG, "onInterceptTouchEvent,INVALID_POINTER");
            return false;
        }

        switch (ev.getAction()) {

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                viewDragHelper.cancel();
                Log.e(TAG, "onInterceptTouchEvent, viewDragHelper.cancel()");
                return false;
            default:
                break;
        }

        //捕获点击到当前控件范围的事件
        boolean interceptTap = viewDragHelper.isViewUnder(mDragView, (int) ev.getX(), (int) ev.getY());
        //自动判断是否拦截（子控件无事件需要消费，则父控件执行）
        boolean shouldInterceptTouchEvent = viewDragHelper.shouldInterceptTouchEvent(ev);

        Log.d(TAG, "onInterceptTouchEvent,return " + (shouldInterceptTouchEvent || interceptTap));
        return shouldInterceptTouchEvent || interceptTap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        int index = MotionEventCompat.getActionIndex(ev);
        activePointerId = ev.getPointerId(index);
        if (activePointerId == INVALID_POINTER) {
            Log.e(TAG, "onTouchEvent:" + ev + ",INVALID_POINTER" + "（无效手势）");
            return false;
        }

        if (!canDoublePointer && ev.getPointerCount() > 1) { //屏蔽多指操作
            Log.e(TAG, "onInterceptTouchEvent, getPointerCount > 1 （多点触控，屏蔽掉）");
            mDragView.dispatchTouchEvent(cloneMotionEventWithAction(ev, MotionEvent.ACTION_CANCEL));
            viewDragHelper.cancel();
            return false;
        }
        int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mDownX = ev.getX();
                mDownY = ev.getY();

                if (!isFullScreen && !isForbidden()) {//非全屏状态并且可以下拉
                    viewDragHelper.processTouchEvent(ev);
                    Log.d(TAG, "onTouchEvent-DOWN:" + " viewDragHelper.processTouchEvent()");
                }
                mDragView.dispatchTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:

                mMoveX = ev.getX() - mDownX;
                mMoveY = ev.getY() - mDownY;

                if (!isJudgeWay) {//还未判定滑动方向

                    //滑动方向大致判断，注意： 水平方向距离要有两倍才会判定为左右滑动
                    if (mMoveY > 0 && Math.abs(mMoveY) > 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_BOTTOM;
                    } else if (mMoveY <= 0 && Math.abs(mMoveY) > 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_TOP;
                    } else if (mMoveX <= 0 && Math.abs(mMoveY) <= 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_LEFT;
                    } else if (mMoveX > 0 && Math.abs(mMoveY) <= 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_RIGHT;
                    } else {
                        Move_Way = MOVE_IDLE;
                    }

                    //从按下 ，到手指当前位置的移动距离 = 根号(X^2 + Y^2)
                    mMoveDistance = Math.sqrt(Math.pow((Math.abs(ev.getX()) - Math.abs(mDownX)), 2) + Math.pow(((Math.abs(ev.getY()) - Math.abs(mDownY))), 2));
                    if (mMoveDistance >= viewDragHelper.getTouchSlop()) {
                        isJudgeWay = true;
                    }
                }

                Log.d(TAG, "onTouchEvent,mMoveDistance:" + mMoveDistance);
                Log.d(TAG, "onTouchEvent,Move_Way:" + Move_Way);

                /**
                 * 将事件分发给子控件的条件：
                 * 1.没有被关闭
                 * 2.非顶部下滑动
                 * 3.向上滑动
                 */
                if (isForbidden() || isFullScreen) {//loading状态 或 全屏状态，不分发DragHelper的事件
                    mDragView.dispatchTouchEvent(ev);
                } else {//非全屏状态下，可下拉
                    Log.d(TAG, "onTouchEvent,isScrollToTop:" + isScrollToTop);
                    if (isScrollToTop && Move_Way.equals(MOVE_BOTTOM)) { //顶部下拉拖拽
                        Log.d(TAG, "onTouchEvent, 顶部下拉拖拽，分发事件给VerticalDragHelper");
                        viewDragHelper.processTouchEvent(ev);
                        mDragView.dispatchTouchEvent(cloneMotionEventWithAction(ev, MotionEvent.ACTION_CANCEL));
                    } else if (Math.abs(mMoveY) < viewDragHelper.getTouchSlop()) {
                        Log.d(TAG, "onTouchEvent, 点击事件");
                        Move_Way = MOVE_IDLE;
                        mDragView.dispatchTouchEvent(ev);
                    } else {
                        Log.d(TAG, "onTouchEvent, 滑动事件，分发事件给子控件");
                        viewDragHelper.cancel();
                        mDragView.dispatchTouchEvent(ev);
                    }
                }
                break;
            default:
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "ACTION_UP");
                }
                mMoveDistance = 0;
                isJudgeWay = false;//还原方向判定状态

                mDragView.dispatchTouchEvent(ev);
                boolean isDragDown = mDragView.getTop() > 0;
                if (!isFullScreen && (!isForbidden() || isDragDown)) {
                    viewDragHelper.processTouchEvent(ev);
                    Log.d(TAG, "processTouchEvent: "+ev.getAction());
                }
                break;
        }
        return !isClosedAtBottom(); //可见时消费掉触摸事件，避免底层其他控件触发
    }


    /**
     * Clone given motion event and set specified action. This method is useful, when we want to
     * cancel event propagation in child views by sending event with {@link
     * MotionEvent#ACTION_CANCEL}
     * action.
     *
     * @param event  event to clone
     * @param action new action
     * @return cloned motion event
     */
    private MotionEvent cloneMotionEventWithAction(MotionEvent event, int action) {
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), action, event.getX(),
                event.getY(), event.getMetaState());
    }


    /**
     * 设置拖拽结果监听器
     */
    public void setDraggableListener(DraggableListener listener) {
        this.listener = listener;
    }


    /**
     * 辅助自动处理滑动
     */
    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 从底部打开控件
     */
    public void open() {
        smoothSlideTo(SLIDE_TOP);
    }

    /**
     * 重新显示控件
     */
    public void show() {

        //重置控件位置到顶部
        mDragView.setVisibility(View.INVISIBLE);
        if (viewDragHelper.smoothSlideViewTo(mDragView, 0, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        //等动画结束再显示出来
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mDragView.setVisibility(VISIBLE);
            }
        }, 400);

    }


    /**
     * 滑向底部关闭
     */
    public void closeToBottom() {
        Log.d(TAG, "closeToBottom");
        smoothSlideTo(SLIDE_BOTTOM);
    }


    /**
     * 平顺滑动(屏幕上下左右)
     */
    private void smoothSlideTo(int SLIDE) {

        if (SLIDE == SLIDE_TOP) { //从底部弹出控件，渐显
            setVisibility(VISIBLE);

            AlphaAnimation showAnimation = new AlphaAnimation(0.0f, 1.0f);
            showAnimation.setDuration(500);
            showAnimation.setFillAfter(true);
            startAnimation(showAnimation);

            mDragView.setVisibility(VISIBLE);
            if (viewDragHelper.smoothSlideViewTo(mDragView, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }

        } else if (SLIDE == SLIDE_BOTTOM) {
            if (viewDragHelper.smoothSlideViewTo(mDragView, 0, getRootView().getHeight())) {
                ViewCompat.postInvalidateOnAnimation(this);
                notifyCloseToBottomToListener();
            }

        }
    }

    /**
     * 是否已打开
     */
    public boolean isOpened() {
        Log.d(TAG, "isOpened" + ",getTop():" + mDragView.getTop() + ",getLeft():" + mDragView.getTop());
        return mDragView.getTop() < 10 && Math.abs(mDragView.getLeft()) < 10;
    }


    /**
     * 是否从底部边界关闭
     */
    public boolean isClosedAtBottom() {
        return mDragView.getTop() >= getHeight();
    }

    public boolean isClosed() {
        return isClosedAtBottom();
    }

    /**
     * 子控件被拖拽,移动了位置
     *
     * @param top
     */
    public void onViewPositionChanged(int top) {
        notifyBackgroundChangedListener(top);
    }

    /**
     * 判断某个View是否被点击了
     *
     * @param view to analyze.
     * @param x    position.
     * @param y    position.
     * @return true if x and y positions are below the view.
     */
    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0]
                && screenX < viewLocation[0] + view.getWidth()
                && screenY >= viewLocation[1]
                && screenY < viewLocation[1] + view.getHeight();
    }


    /**
     * Notify te view is minimized to the DraggableListener
     */
    private void notifyCloseToBottomToListener() {
        if (listener != null) {
            listener.onClosedToBottom();
        }
    }

    /**
     * Y 轴位置改动 改变背景
     *
     * @param top
     */
    private void notifyBackgroundChangedListener(final int top) {
        if (listener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    listener.onBackgroundChanged(top);
                }
            });

        }
    }


    private boolean isForbidden() {
        return listener != null && listener.isForbidden();
    }

    /**
     * 设置子控件是否滑动到顶部，用于判断是否需要拦截滑动事件
     */
    public void setScrollToTop(boolean scrollToTop) {
        isScrollToTop = scrollToTop;
    }


    /**
     * 重置回弹到顶部
     */
    public void onReset() {
        Log.d(TAG, "onReset");
        mDragView.setVisibility(VISIBLE);
        viewDragHelper.settleCapturedViewAt(0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setFullScreen(boolean fullScreen) {
        this.isFullScreen = fullScreen;
    }

    public void setCanDoublePointer(boolean canDoublePointer) {
        this.canDoublePointer = canDoublePointer;
    }
}
