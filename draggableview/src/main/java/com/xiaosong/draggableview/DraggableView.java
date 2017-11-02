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
import com.xiaosong.draggableview.interfaces.DraggableViewCallback;


/**
 * 包含水平和垂直方向的拖拽控件。
 *
 * @author xiaosong
 */
public class DraggableView extends LinearLayout {


    private static final int INVALID_POINTER = -1;

    private static final int SLIDE_TOP = 0; //DragHelper滑动到屏幕四个方向
    private static final int SLIDE_BOTTOM = 1;
    private static final int SLIDE_LEFT = 2;
    private static final int SLIDE_RIGHT = 3;

    private int mDragState = 0;//当前拖拽状态

    private static final String TAG = "DraggableView";
    private int activePointerId = INVALID_POINTER;

    private ViewDragHelper viewDragHelper;
    private DraggableListener listener;

    private View mDragView; //拖动的控件

    private int mDragViewId;

    private boolean isFullScreen = false;// 是否为全屏状态
    private boolean isScrollToTop = true;// 是否已滑动到顶部
    private boolean isNeedInterceptHorizontal = false; //是否需要拦截左右滑动
    private double mDownX = 0;
    private double mDownY = 0;
    private double mMoveDistance = 0;
    private boolean isJudgeWay = false; //是否已判定滑动方向

    public final String MOVE_TOP = "TOP";
    public final String MOVE_BOTTOM = "BOTTOM";
    public final String MOVE_LEFT = "LEFT";
    public final String MOVE_RIGHT = "RIGHT";
    public String Move_Way = "";


    public DraggableView(Context context) {
        super(context);
        this.setOrientation(VERTICAL);
        initializeViewDragHelper(context);
    }

    public DraggableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
        initializeViewDragHelper(context);
        initializeAttributes(attrs);
    }

    public DraggableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOrientation(VERTICAL);
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
        viewDragHelper = ViewDragHelper.create(this, 1.0f, new DraggableViewCallback(this));
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
        Log.d(TAG, "onInterceptTouchEvent, " + "isScrollToTop:" + isScrollToTop);

        if (!isEnabled()) {
            return false;
        }

        switch (MotionEventCompat.getActionMasked(ev) & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                viewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                activePointerId = MotionEventCompat.getPointerId(ev, index);
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                break;

            default:
                break;
        }

        //捕获点击到当前控件范围的事件
        boolean interceptTap = viewDragHelper.isViewUnder(mDragView, (int) ev.getX(), (int) ev.getY());
        //自动判断是否拦截（子控件无事件需要消费，则父控件执行）
        boolean shouldInterceptTouchEvent = viewDragHelper.shouldInterceptTouchEvent(ev);

        Log.d(TAG, "onInterceptTouchEvent,return " + (shouldInterceptTouchEvent /*|| interceptTap*/));
        return shouldInterceptTouchEvent || interceptTap;
    }

    /**
     * Override method to dispatch touch event to the dragged view.
     *
     * @param ev captured.
     * @return true if the touch event is realized over the drag or second view.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int index = MotionEventCompat.getActionIndex(ev);
        activePointerId = MotionEventCompat.getPointerId(ev, index);
//                Log.d("DraggableView", "TouchEvent-activePointerId:" + activePointerId);
        if (activePointerId == INVALID_POINTER) {
            return false;
        }

        switch (MotionEventCompat.getActionMasked(ev) & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                mDownX = ev.getX();
                mDownY = ev.getY();

                if (!isFullScreen) {//非全屏状态
                    viewDragHelper.processTouchEvent(ev);
                }
                mDragView.dispatchTouchEvent(ev);

                Log.d(TAG, "onTouchEvent-DOWN:" + ",mDownX:" + mDownX + ",mDownY:" + mDownY);
                break;

            case MotionEvent.ACTION_MOVE:
                double mMoveY;
                double mMoveX;

                mMoveX = ev.getX() - mDownX;
                mMoveY = ev.getY() - mDownY;
                Log.d(TAG, "onTouchEvent-MOVE:" + ",mMoveX:" + mMoveX + ",mMoveY:" + mMoveY);

                if (!isJudgeWay) {//还未判定滑动方向

                    //滑动方向大致判断，注意： 水平方向距离要有两倍才会判定为左右滑动
                    if (mMoveY > 0 && Math.abs(mMoveY) > 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_BOTTOM;
                    } else if (mMoveY < 0 && Math.abs(mMoveY) > 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_TOP;
                    } else if (mMoveX < 0 && Math.abs(mMoveY) <= 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_LEFT;
                    } else if (mMoveX > 0 && Math.abs(mMoveY) <= 0.5 * Math.abs(mMoveX)) {
                        Move_Way = MOVE_RIGHT;
                    } else {
                        Move_Way = MOVE_TOP;
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
                if (isFullScreen) {//全屏状态
                    mDragView.dispatchTouchEvent(ev);
                } else {//非全屏状态下
                    Log.d(TAG, "onTouchEvent,isScrollToTop:" + isScrollToTop);

                    //不在顶部下滑，或者上滑
                    if (!isClosed() && (!isScrollToTop && Move_Way.equals(MOVE_BOTTOM) || Move_Way.equals(MOVE_TOP))) {
                        Log.d(TAG, "onTouchEvent, 下滑，或者上滑，分发事件给子控件");
                        mDragView.dispatchTouchEvent(ev);
                    } else if (isScrollToTop && Move_Way.equals(MOVE_BOTTOM)) { //顶部下拉拖拽
                        Log.d(TAG, "onTouchEvent, 顶部下拉拖拽，分发事件给DragHelper");
                        viewDragHelper.processTouchEvent(ev);
                        mDragView.dispatchTouchEvent(cloneMotionEventWithAction(ev, MotionEvent.ACTION_CANCEL));
                    } else if (Move_Way.equals(MOVE_LEFT) || Move_Way.equals(MOVE_RIGHT)) {//左右滑动
                        if (isNeedInterceptHorizontal) {// 拦截拖拽，让SeekBar拖动
                            Log.d(TAG, "onTouchEvent, 拦截拖拽，让SeekBar拖动");
                            mDragView.dispatchTouchEvent(ev);
                        } else {
                            Log.d(TAG, "onTouchEvent, 左右滑动，分发事件给DragHelper");
                            viewDragHelper.processTouchEvent(ev);
                            mDragView.dispatchTouchEvent(cloneMotionEventWithAction(ev, MotionEvent.ACTION_CANCEL));
                        }
                    } else {
                        Log.d(TAG, "onTouchEvent, 都不符合条件，ACTION_CANCEL");
                        mDragView.dispatchTouchEvent(cloneMotionEventWithAction(ev, MotionEvent.ACTION_CANCEL));
                    }
                }

                break;
            default:
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "ACTION_UP");
                    //还原方向判定状态
                    mMoveDistance = 0;
                    isJudgeWay = false;
                }
                mDragView.dispatchTouchEvent(ev);
                if (!isFullScreen) {//非全屏状态
                    viewDragHelper.processTouchEvent(ev);
                }
                break;
        }

        //可见时消费掉触摸事件，避免底层其他控件触发
        return !isClosedAtBottom();
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
        mDragView.setAlpha(0.0f);
        if (viewDragHelper.smoothSlideViewTo(mDragView, 0, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        //等动画结束再显示出来
        new Handler().postDelayed(new Runnable() {
            public void run() {
                AlphaAnimation showAnimation = new AlphaAnimation(0.0f, 1.0f);
                showAnimation.setDuration(500);
                showAnimation.setFillAfter(true);
                mDragView.startAnimation(showAnimation);
                mDragView.setAlpha(1.0f);
            }
        }, 500);

    }


    /**
     * 滑向底部关闭
     */
    public void closeToBottom() {
        Log.d(TAG, "closeToBottom");
        smoothSlideTo(SLIDE_BOTTOM);
    }

    /**
     * 滑向右边界关闭
     */
    public void closeToRight() {
        Log.d(TAG, "closeToRight");
        smoothSlideTo(SLIDE_RIGHT);
    }

    /**
     * 滑向左边界关闭
     */
    public void closeToLeft() {
        Log.d(TAG, "closeToLeft");
        smoothSlideTo(SLIDE_LEFT);
    }


    /**
     * 平顺滑动(屏幕上下左右)
     */
    private void smoothSlideTo(int SLIDE) {
        int left;
        int top;

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

        } else if (SLIDE == SLIDE_LEFT) {
            left = -mDragView.getMeasuredWidth();
            top = 0;
            if (viewDragHelper.smoothSlideViewTo(mDragView, left, top)) {
                ViewCompat.postInvalidateOnAnimation(this);
                notifyCloseToLeftListener();
            }
        } else if (SLIDE == SLIDE_RIGHT) {
            left = mDragView.getMeasuredWidth();
            top = 0;
            if (viewDragHelper.smoothSlideViewTo(mDragView, left, top)) {
                ViewCompat.postInvalidateOnAnimation(this);
                notifyCloseToRightListener();
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
     * 是否从右边界关闭
     */
    public boolean isClosedAtRight() {
//        Log.d(TAG, "isClosedAtRight:");
        return mDragView.getLeft() >= getWidth();
    }

    /**
     * 是否从左边界关闭
     */
    public boolean isClosedAtLeft() {
//        Log.d(TAG, "isClosedAtLeft:");
        return mDragView.getRight() <= 0;
    }

    /**
     * 是否从底部边界关闭
     */
    public boolean isClosedAtBottom() {
        return mDragView.getTop() >= getRootView().getHeight();
    }

    public boolean isClosed() {
        return isClosedAtBottom() || isClosedAtRight() || isClosedAtLeft();
    }

    /**
     * 子控件被拖拽,移动了位置
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
     * Notify te view is closed to the right to the DraggableListener
     */
    private void notifyCloseToRightListener() {
        if (listener != null) {
            listener.onClosedToRight();
        }
    }

    /**
     * Notify te view is closed to the left to the DraggableListener
     */
    private void notifyCloseToLeftListener() {
        if (listener != null) {
            listener.onClosedToLeft();
        }
    }

    /**
     * Y 轴位置改动 改变背景
     */
    private void notifyBackgroundChangedListener(int top) {
        if (listener != null) {
            listener.onBackgroundChanged(top);
        }
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

    /**
     * Drag状态改变
     */
    public void onViewDragStateChanged(int state) {
//        Log.d(TAG, "mDragState：" + mDragState);
        mDragState = state;
    }

    /**
     * Drag状态获取
     */
    public int getDragState() {
        return mDragState;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public void setNeedInterceptHorizontal(boolean needInterceptHorizontal) {
        isNeedInterceptHorizontal = needInterceptHorizontal;
    }
}
