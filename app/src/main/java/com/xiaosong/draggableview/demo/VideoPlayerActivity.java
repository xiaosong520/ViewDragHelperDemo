package com.xiaosong.draggableview.demo;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xiaosong.draggableview.DraggableListener;
import com.xiaosong.draggableview.HorizontalDraggableView;
import com.xiaosong.draggableview.MyScrollView;
import com.xiaosong.draggableview.VerticalDraggableView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class VideoPlayerActivity extends AppCompatActivity implements DraggableListener {

    private static final String TAG = "VideoPlayerActivity";
    @BindView(R.id.fl_video)
    RelativeLayout flVideo;
    @BindView(R.id.drag_view_hor)
    HorizontalDraggableView dragViewHor;
    @BindView(R.id.scroll_view)
    MyScrollView scrollView;
    @BindView(R.id.drag_view_vertical)
    VerticalDraggableView dragViewVertical;

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
        //如果是第一项或者最后一项则不能左右切换了，因此demo演示就设置false
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
        int newAlpha = 255 - (int) (255 * ((float) top / (float) flVideo.getRootView().getHeight()));

        int topFlVideo = flVideo.getTop();
        int rootView = flVideo.getRootView().getHeight();

        Log.d(TAG, "flVideo.top:" + topFlVideo);
        Log.d(TAG, "rootView:" + rootView);
        Log.d(TAG, "newAlpha:" + newAlpha);

        if (newAlpha == 255) {
            dragViewVertical.setBackgroundResource(R.mipmap.bg_gauss_blur);
        } else {
            dragViewVertical.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        dragViewVertical.getBackground().setAlpha(newAlpha);
        if (newAlpha < 216) { //达到子控件缩放最小值，原大小的0.85倍
            flVideo.setScaleX(0.85f);
            flVideo.setScaleY(0.85f);
        } else {// newAlpha >= 204 平滑缩放
            flVideo.setScaleX(1 - (255.0f - (float) newAlpha) / 255);
            flVideo.setScaleY(1 - (255.0f - (float) newAlpha) / 255);
        }
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

