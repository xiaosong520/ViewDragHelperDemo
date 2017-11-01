package com.xiaosong.draggableview.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xiaosong.draggableview.DraggableScrollView;
import com.xiaosong.draggableview.HorizontalDraggableView;
import com.xiaosong.draggableview.VerticalDraggableView;
import com.xiaosong.draggableview.interfaces.DraggableListener;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 垂直方向利用DragHelper，水平方向自行处理触摸事件 的示例Activity。
 * 用于解决用一个DraggableView不方便分发给子控件触摸事件的问题。
 */
public class VideoPlayerActivity extends AppCompatActivity implements DraggableListener, DraggableScrollView.ScrollListener {

    private static final String TAG = "VideoPlayerActivity";

    @BindView(R.id.drag_view_hor)
    HorizontalDraggableView dragViewHor;
    @BindView(R.id.scroll_view)
    DraggableScrollView scrollView;
    @BindView(R.id.drag_view_vertical)
    VerticalDraggableView dragViewVertical;
    @BindView(R.id.lv_video)
    LinearLayout lvVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vieo_player);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        dragViewVertical.setDraggableListener(this);
        dragViewHor.setDraggableListener(this);
        scrollView.setOnScrollListener(this);
        //如果是第一项或者最后一项则不能左右切换了。设置false
        dragViewHor.setFirst(false);
        dragViewHor.setLast(false);
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
        Toast.makeText(this, "向左滑动", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                dragViewHor.show();
            }
        }, 500);
    }

    @Override
    public void onClosedToRight() {
        Toast.makeText(this, "向左滑动", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                dragViewHor.show();
            }
        }, 500);
    }

    @Override
    public void onBackgroundChanged(int top) {
        int newAlpha = 255 - (int) (255 * ((float) top / (float) lvVideo.getRootView().getHeight()));

        if (newAlpha == 255) {
            dragViewVertical.setBackgroundResource(R.mipmap.bg_gauss_blur);
        } else {
            dragViewVertical.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        dragViewVertical.getBackground().setAlpha(newAlpha);
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
        dragViewVertical.setScrollToTop(isTop);
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

