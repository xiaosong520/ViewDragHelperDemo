package com.xiaosong.draggableview;

import android.app.Activity;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.View;




/**
 * ViewDragHelper.Callback 拖拽事件监听回调
 *
 * @author zengsong
 */
class DraggableViewCallback extends ViewDragHelper.Callback {

    private static final String TAG = "DraggableViewCallback";

    private static float X_MIN_DISTANCE = 200;//水平方向关闭最小值 px
    private static float Y_MIN_DISTANCE = 200;//竖直方向关闭最小值 px

    private DraggableView draggableView;


    public DraggableViewCallback(DraggableView draggableView) {
        this.draggableView = draggableView;
//        X_MIN_DISTANCE = DisplayMetricsUtils.getScreenWidthPixels((Activity) draggableView.getContext())/3;
//        Y_MIN_DISTANCE = DisplayMetricsUtils.getScreenHeightPixels((Activity) draggableView.getContext())/3;
    }

    /**
     * 子控件位置改变时触发（包括X和Y轴方向）
     *
     * @param left position.
     * @param top  position.
     * @param dx   change in X position from the last call.
     * @param dy   change in Y position from the last call.
     */
    @Override
    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        // Log.d(TAG, "onViewPositionChanged"+". left:"+left+". top:"+top+". dx:"+dx+". dy:"+dy);
        draggableView.onViewPositionChanged(top);
    }

    /**
     * 子控件竖直方向位置改变时触发
     */
    @Override
    public int clampViewPositionVertical(View child, int top, int dy) {
        Log.d(TAG, "clampViewPositionVertical" + ", top" + top + ", dy:" + dy);

        //水平滑动时触发了竖直方向，屏蔽掉
        if (draggableView.Move_Way.equals(draggableView.MOVE_LEFT)
                || draggableView.Move_Way.equals(draggableView.MOVE_RIGHT)) {
            return 0;
        }
        return Math.max(top, 0);
    }

    /**
     * 子控件水平方向位置改变时触发
     */
    @Override
    public int clampViewPositionHorizontal(View child, int left, int dx) {
        // Log.d(TAG, "clampViewPositionHorizontal" + ", left" + left + ", dx:" + dx);

        //竖直滑动时触发了水平方向，屏蔽掉
        if (draggableView.Move_Way.equals(draggableView.MOVE_TOP)
                || draggableView.Move_Way.equals(draggableView.MOVE_BOTTOM)) {
            return 0;
        }

        return left;
    }


    /**
     * 手指松开时触发
     *
     * @param releasedChild the captured child view now being released.
     * @param xVel          X velocity of the pointer as it left the screen in pixels per second.
     * @param yVel          Y velocity of the pointer as it left the screen in pixels per second.
     */
    @Override
    public void onViewReleased(View releasedChild, float xVel, float yVel) {
        super.onViewReleased(releasedChild, xVel, yVel);
        //Log.d(TAG, "onViewReleased" + "xVel:" + xVel + ", yVel:" + yVel);

        int top = releasedChild.getTop(); //获取子控件Y值
        int left = releasedChild.getLeft(); //获取子控件X值

        if (Math.abs(left) <= Math.abs(top)) {//竖直滑动
            triggerOnReleaseActionsWhileVerticalDrag(top);
        } else if (Math.abs(top) < Math.abs(left)) {//水平滑动
            triggerOnReleaseActionsWhileHorizontalDrag(left);
        }
    }

    /**
     * 告知被拖拽的View是哪个，返回true 代表允许被拖拽。
     *
     * @param view      child the user is attempting to capture.
     * @param pointerId ID of the pointer attempting the capture,
     * @return true if capture should be allowed, false otherwise.
     */
    @Override
    public boolean tryCaptureView(View view, int pointerId) {
        Log.d(TAG, "tryCaptureView");
        return true;
    }


    /**
     * 计算竖直方向的滑动
     */
    private void triggerOnReleaseActionsWhileVerticalDrag(float yVel) {
        //Log.d(TAG, "ReleaseVerticalDrag"+", yVel：" + yVel);
        if (yVel < 0 && yVel <= -Y_MIN_DISTANCE) {
            draggableView.onReset();
        } else if (yVel > 0 && yVel >= Y_MIN_DISTANCE) {
            draggableView.closeToBottom();
        } else {
            draggableView.onReset();
        }
    }

    /**
     * 计算水平方向的滑动
     */
    private void triggerOnReleaseActionsWhileHorizontalDrag(float xVel) {
//        Log.d(TAG, "ReleaseHorizontalDrag"+", xVel：" + xVel);
        if (xVel < 0 && xVel <= -X_MIN_DISTANCE) {
            draggableView.closeToLeft();
        } else if (xVel > 0 && xVel >= X_MIN_DISTANCE) {
            draggableView.closeToRight();
        } else {
            draggableView.onReset();
        }
    }


    @Override
    public void onViewDragStateChanged(int state) {
        draggableView.onViewDragStateChanged(state);
    }
}
