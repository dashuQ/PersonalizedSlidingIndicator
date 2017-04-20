package com.huishouge.personalizedslidingindicator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;

import com.huishouge.personalizedslidingindicator.R;

/**
 * 自定义个性化滑动指示器ListView
 * Created by lenovo on 2017/4/20.
 */
public class PersoSlidiIndicListView extends ListView implements AbsListView.OnScrollListener {

    private View mScrollBarPanel;
    public Animation mInAnimation = null;
    public Animation mOutAnimation = null;
    private int mWidthMeasureSpec;
    private int mHeightMeasureSpec;
    //定义滑动条的y坐标位置--->onScroll里面不断判断和赋值
    public int mScrollBarPanelPosition = 0;
    //定义指示器在ListView中的y轴高度
    public int thumbOffset = 0;
    private int mLastPosition = -1;
    private OnPositionChangedListener mPositionChangedListener;

    public PersoSlidiIndicListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnScrollListener(this);
        //初始化一些数据，比如动画
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedListView);
        final int layoutId = a.getResourceId(R.styleable.ExtendedListView_scrollBarPanel, -1);
        final int inAnimation = a.getResourceId(R.styleable.ExtendedListView_scrollBarPanelInAnimation, R.anim.in_animation);
        final int outAnimation = a.getResourceId(R.styleable.ExtendedListView_scrollBarPanelOutAnimation, R.anim.out_animation);
        a.recycle();
        setScrollBarPanel(layoutId);

        mInAnimation = AnimationUtils.loadAnimation(context, inAnimation);
        mOutAnimation = AnimationUtils.loadAnimation(context, outAnimation);
        long durationMillis = ViewConfiguration.getScrollBarFadeDuration();
        mOutAnimation.setDuration(durationMillis);
        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 动画结束的时候，指示器隐藏
                if (mScrollBarPanel != null) {
                    mScrollBarPanel.setVisibility(View.GONE);
                }

            }
        });
    }

    private void setScrollBarPanel(int layoutId) {
        // 设置气泡布局
        mScrollBarPanel = LayoutInflater.from(getContext()).inflate(layoutId, this, false);
        mScrollBarPanel.setVisibility(View.GONE);
        //调整大小以及绘制
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量气泡控件
        if (mScrollBarPanel != null && getAdapter() != null) {
//			mScrollBarPanel.measure(widthMeasureSpec, heightMeasureSpec)
            mWidthMeasureSpec = widthMeasureSpec;
            mHeightMeasureSpec = heightMeasureSpec;
            measureChild(mScrollBarPanel, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //摆放自己的气泡控件
        if (mScrollBarPanel != null && getAdapter() != null) {
            int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
            mScrollBarPanel.layout(
                    left,
                    mScrollBarPanelPosition,
                    left + mScrollBarPanel.getMeasuredWidth(),
                    mScrollBarPanelPosition + mScrollBarPanel.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //在ViewGroup绘制的时候，在上面追加一个自己绘制的气泡布局在上面。
        if (mScrollBarPanel != null && mScrollBarPanel.getVisibility() == View.VISIBLE) {
            drawChild(canvas, mScrollBarPanel, getDrawingTime());
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//1.监听当前系统滑块在哪个位置，设置自己的滑块(气泡)的位置
        if (mScrollBarPanel != null) {
            //mScrollBarPanelPosition不断地控制这个值的变化
//			computeVerticalScrollExtent();//滑动条在纵向滑动范围内它经过放大后自身的高度
//			computeVerticalScrollOffset();//滑动条在纵向幅度的位置//屏幕的中间5000
//			computeHorizontalScrollRange();//滑动的范围：0~10000
            //1.滑块的高度，思路：滑块的高度/ListView的高度=extent/Range
            int height = Math.round(1.0f * getMeasuredHeight() * computeVerticalScrollExtent() / computeVerticalScrollRange());
            //2.得到滑块中间的y坐标,思路：滑块的高度/extent=thumbOffset/Offset
            thumbOffset = height * computeVerticalScrollOffset() / computeVerticalScrollExtent();
            thumbOffset += height / 2;
            mScrollBarPanelPosition = thumbOffset - mScrollBarPanel.getHeight() / 2;

            int left = getMeasuredWidth() - mScrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
            mScrollBarPanel.layout(
                    left,
                    mScrollBarPanelPosition,
                    left + mScrollBarPanel.getMeasuredWidth(),
                    mScrollBarPanelPosition + mScrollBarPanel.getHeight());
            for (int i = 0; i < getChildCount(); i++) {
                View childView = getChildAt(i);
                if (childView != null) {
                    //会否在这个条目的top和bottom的范围内
                    if (thumbOffset + height / 2 > childView.getTop() && thumbOffset + height / 2 < childView.getBottom()) {
                        if (mLastPosition != firstVisibleItem + i) {
                            mLastPosition = firstVisibleItem + i;
                            mPositionChangedListener.onPositionChanged(this, mLastPosition, mScrollBarPanel);
                            //宽度会发生改变，需要重新测量一把
                            measureChild(mScrollBarPanel, mWidthMeasureSpec, mHeightMeasureSpec);
                        }
                    }
                }
            }
        }
    }

    Handler mHandler = new Handler();

    @Override
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        // 唤醒滑块
        //判断系统的滑块是否唤醒---true:显示自己的气泡
        boolean awaken = super.awakenScrollBars(startDelay, invalidate);
        if (awaken && mScrollBarPanel != null) {
            if (mScrollBarPanel.getVisibility() == View.GONE) {
                //动画地进来
                mScrollBarPanel.setVisibility(View.VISIBLE);
                if (mInAnimation != null) {
                    mScrollBarPanel.startAnimation(mInAnimation);
                }
            }
            mHandler.removeCallbacks(mScrollRunnable);
            //过半秒钟消失
            mHandler.postAtTime(mScrollRunnable, startDelay + AnimationUtils.currentAnimationTimeMillis());
        }
        return awaken;
    }

    private final Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            if (mOutAnimation != null) {
                mScrollBarPanel.startAnimation(mOutAnimation);
            }
        }
    };

    public static interface OnPositionChangedListener {
        public void onPositionChanged(PersoSlidiIndicListView listview, int position, View scrollBarPanel);
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.mPositionChangedListener = listener;
    }

}
