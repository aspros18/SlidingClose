package com.aspros.slidingclose;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SlideLayout extends FrameLayout {

    private Activity mActivity;
    private Scroller mScroller;

    // 页面边缘的阴影图
    private Drawable mLeftShadow;

    // 页面边缘阴影的宽度默认值
    private static final int SHADOW_WIDTH = 16;

    // 页面边缘阴影的宽度
    private int mShadowWidth;

    private int mInterceptDownX;
    private int mLastInterceptX;
    private int mLastInterceptY;
    private int mTouchDownX;
    private int mLastTouchX;
    private int mLastTouchY;
    private boolean isConsumed = false;

    public SlideLayout(@NonNull Activity activity) {
        this(activity, null);
    }

    public SlideLayout(@NonNull Activity activity, @Nullable AttributeSet attrs) {
        this(activity, attrs, 0);
    }

    public SlideLayout(@NonNull Activity activity, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(activity, attrs, defStyleAttr);

        initView(activity);
    }

    private void initView(Activity activity) {
        mActivity = activity;
        mScroller = new Scroller(mActivity);
        mLeftShadow = getResources().getDrawable(R.drawable.left_shadow);
        int density = (int) activity.getResources().getDisplayMetrics().density;
        mShadowWidth = SHADOW_WIDTH * density;
    }

    /**
     * 绑定Activity
     */
    public void bindActivity(Activity activity) {
        mActivity = activity;
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        View child = decorView.getChildAt(0);
        decorView.removeView(child);
        addView(child);
        decorView.addView(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                mInterceptDownX = x;
                mLastInterceptX = x;
                mLastInterceptY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastInterceptX;
                int deltaY = y - mLastInterceptY;
                // 手指处于屏幕边缘，且横向滑动距离大于纵向滑动距离时，拦截事件
                if (mInterceptDownX < (getWidth() / 10) && Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                mLastInterceptX = x;
                mLastInterceptY = y;
                break;

            case MotionEvent.ACTION_UP:
                intercept = false;
                mInterceptDownX = mLastInterceptX = mLastInterceptY = 0;
                break;
        }

        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = x;
                mLastTouchX = x;
                mLastTouchY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastTouchX;
                int deltaY = y - mLastTouchY;

                if (!isConsumed && mTouchDownX < (getWidth() / 10) && Math.abs(deltaX) > Math.abs(deltaY)) {
                    isConsumed = true;
                }

                if (isConsumed) {
                    int rightMovedX = mLastTouchX - (int) event.getX();
                    // 左侧即将滑出屏幕
                    if (getScrollX() + rightMovedX >= 0) {
                        scrollTo(0, 0);
                    } else {
                        scrollBy(rightMovedX, 0);
                    }
                }
                mLastTouchX = x;
                mLastTouchY = y;
                break;

            case MotionEvent.ACTION_UP:
                isConsumed = false;
                mTouchDownX = mLastTouchX = mLastTouchY = 0;
                // 根据手指释放时的位置决定回弹还是关闭
                if (-getScrollX() < getWidth() / 2) {
                    scrollBack();
                } else {
                    scrollClose();
                }
                break;
        }


        return true;
    }

    /**
     * 滑动返回
     */
    private void scrollBack() {
        int startX = getScrollX();
        int dx = -getScrollX();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    /**
     * 滑动关闭
     */
    private void scrollClose() {
        int startX = getScrollX();
        int dx = -getScrollX() - getWidth();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (-getScrollX() >= getWidth()) {
            mActivity.finish();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShadow(canvas);
    }

    /**
     * 绘制边缘的阴影
     */
    private void drawShadow(Canvas canvas) {
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        canvas.save();
        canvas.translate(-mShadowWidth, 0);
        mLeftShadow.draw(canvas);
        canvas.restore();
    }
}
