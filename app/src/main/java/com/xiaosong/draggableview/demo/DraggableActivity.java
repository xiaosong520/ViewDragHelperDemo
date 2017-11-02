package com.xiaosong.draggableview.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xiaosong.draggableview.DraggableScrollView;
import com.xiaosong.draggableview.DraggableView;
import com.xiaosong.draggableview.interfaces.DraggableListener;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 利用ViewDragHelper实现（下拉拖拽关闭） 以及 （水平拖拽切换） 的功能。
 */
public class DraggableActivity extends AppCompatActivity implements DraggableListener, DraggableScrollView.ScrollListener {

    private static final String TAG = "DraggableActivity";
    @BindView(R.id.fl_video)
    RelativeLayout flVideo;
    @BindView(R.id.scroll_view)
    DraggableScrollView scrollView;
    @BindView(R.id.drag_view)
    DraggableView dragView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draggable);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        dragView.setDraggableListener(this);
        scrollView.setOnScrollListener(this);
    }


    @Override
    public void onClosedToBottom() {
        Toast.makeText(this, "关闭页面", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                finish();
//                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.bottom_transparent_exit);
            }
        }, 500);

    }

    @Override
    public void onClosedToLeft() {

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(DraggableActivity.this, "向左滑动", Toast.LENGTH_SHORT).show();
                dragView.show();
            }
        }, 500);
    }

    @Override
    public void onClosedToRight() {

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(DraggableActivity.this, "向右滑动", Toast.LENGTH_SHORT).show();
                dragView.show();
            }
        }, 500);
    }

    @Override
    public void onBackgroundChanged(int top) {
        int newAlpha = 255 - (int) (255 * ((float) top / (float) dragView.getRootView().getHeight()));

        if (newAlpha == 255) {
            dragView.setBackgroundResource(R.mipmap.bg_gauss_blur);
        } else {
            dragView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        dragView.getBackground().setAlpha(newAlpha);
        if (newAlpha < 216) { //达到子控件缩放最小值，原大小的0.85倍
            scrollView.setScaleX(0.85f);
            scrollView.setScaleY(0.85f);
        } else {// newAlpha >= 204 平滑缩放
            scrollView.setScaleX(1 - (255.0f - (float) newAlpha) / 255);
            scrollView.setScaleY(1 - (255.0f - (float) newAlpha) / 255);
        }
    }

    @Override
    public void isOnTop(boolean isTop) {
        dragView.setScrollToTop(isTop);
    }

    @Override
    public void onScrollChanged(int tY) {

    }

    /**
     * 是否禁止下拉
     *
     * @return
     */
    @Override
    public boolean isForbidden() {
        return false;
    }
}
