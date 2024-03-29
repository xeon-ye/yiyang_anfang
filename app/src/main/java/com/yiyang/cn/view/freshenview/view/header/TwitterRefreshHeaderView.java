package com.yiyang.cn.view.freshenview.view.header;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yiyang.cn.R;
import com.yiyang.cn.view.freshenview.SwipeRefreshHeaderLayout;


/**
 * Created by Aspsine on 2015/9/9.
 */
public class TwitterRefreshHeaderView extends SwipeRefreshHeaderLayout {

    private ImageView ivArrow;

    private ImageView ivSuccess;

    private ImageView ivImage;

    private TextView tvRefresh;

    private ProgressBar progressBar;

    private int mHeaderHeight;

    private Animation rotateUp;

    private Animation rotateDown;

    private boolean rotated = false;

    private AnimationDrawable animation;

    public TwitterRefreshHeaderView(Context context) {
        this(context, null);
    }

    public TwitterRefreshHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwitterRefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHeaderHeight = getResources().getDimensionPixelOffset(R.dimen.refresh_header_height_twitter);
        rotateUp = AnimationUtils.loadAnimation(context, R.anim.rotate_up);
        rotateDown = AnimationUtils.loadAnimation(context, R.anim.rotate_down);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tvRefresh = (TextView) findViewById(R.id.tvRefresh);
        ivArrow = (ImageView) findViewById(R.id.ivArrow);
        ivSuccess = (ImageView) findViewById(R.id.ivSuccess);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    public void onRefresh() {
        ivSuccess.setVisibility(GONE);
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
        ivImage.setVisibility(VISIBLE);
//        progressBar.setVisibility(VISIBLE);
        tvRefresh.setText("正在刷新");

        animation = (AnimationDrawable) ivImage.getDrawable();
        ivImage.post(new Runnable() {
            @Override
            public void run() {
                animation.start();
            }
        });
    }

    @Override
    public void onPrepare() {
        Log.d("TwitterRefreshHeader", "onPrepare()");
    }

    @Override
    public void onMove(int y, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            ivArrow.setVisibility(VISIBLE);
            ivImage.setVisibility(GONE);
//            progressBar.setVisibility(GONE);
            ivSuccess.setVisibility(GONE);
            if (y > mHeaderHeight) {
                tvRefresh.setText("释放立即刷新");
                if (!rotated) {
                    ivArrow.clearAnimation();
//                    ivArrow.startAnimation(rotateUp);
                    rotated = true;
                }
            } else if (y < mHeaderHeight) {
                if (rotated) {
                    ivArrow.clearAnimation();
//                    ivArrow.startAnimation(rotateDown);
                    rotated = false;
                }

                tvRefresh.setText("下拉刷新");
            }
        }
    }

    @Override
    public void onRelease() {
        Log.d("TwitterRefreshHeader", "onRelease()");
    }

    @Override
    public void onComplete() {
        rotated = false;
        ivSuccess.setVisibility(VISIBLE);
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
        ivImage.setVisibility(GONE);
//        progressBar.setVisibility(GONE);
        tvRefresh.setText("刷新成功");
    }

    @Override
    public void onReset() {
        rotated = false;
        ivSuccess.setVisibility(GONE);
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
        ivImage.setVisibility(GONE);
//        progressBar.setVisibility(GONE);
    }

}
