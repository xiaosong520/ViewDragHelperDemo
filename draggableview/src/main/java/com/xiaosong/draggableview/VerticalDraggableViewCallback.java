package com.xiaosong.draggableview;

import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.View;


/**
 * ViewDragHelper.Callback 拖拽事件监听回调
 *
 * @author xaosong
 */
class VerticalDraggableViewCallback extends ViewDragHelper.Callback {

    private static final String TAG = "VerticalCallback";

    private static float Y_MIN_DISTANCE = 200;//竖直方向关闭最小值 px

    private VerticalDraggableView verticalDraggableView;


    public VerticalDraggableViewCallback(VerticalDraggableView verticalDraggableView) {
        this.verticalDraggableView = verticalDraggableView;
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
        verticalDraggableView.onViewPositionChanged(top);
    }

    /**
     * 子控件竖直方向位置改变时触发
     */
    @Override
    public int clampViewPositionVertical(View child, int top, int dy) {
        //水平滑动时触发了竖直方向，屏蔽掉
        if (verticalDraggableView.Move_Way.equals(verticalDraggableView.MOVE_LEFT)
                || verticalDraggableView.Move_Way.equals(verticalDraggableView.MOVE_RIGHT)) {
            return 0;
        }
        return Math.max(top, 0);
    }

    /**
     * 子控件水平方向位置改变时触发
     */
    @Override
    public int clampViewPositionHorizontal(View child, int left, int dx) {
        //屏蔽掉水平方向
        return 0;
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
        Log.d(TAG,"onViewReleased");
        int top = releasedChild.getTop(); //获取子控件Y值
        int left = releasedChild.getLeft(); //获取子控件X值

        if (Math.abs(left) <= Math.abs(top)) {//竖直滑动
            triggerOnReleaseActionsWhileVerticalDrag(top);
        }
    }

    @Override
    public boolean tryCaptureView(View view, int pointerId) {
        return true;
    }

    /**
     * 计算竖直方向的滑动
     */
    private void triggerOnReleaseActionsWhileVerticalDrag(float yVel) {

        if (yVel > 0 && yVel >= Y_MIN_DISTANCE) {
            verticalDraggableView.closeToBottom();
            Log.d(TAG, "ReleaseVerticalDrag"+", closeToBottom" );
        } else {
            verticalDraggableView.onReset();
            Log.d(TAG, "ReleaseVerticalDrag"+", onReset" );
        }
    }
}
