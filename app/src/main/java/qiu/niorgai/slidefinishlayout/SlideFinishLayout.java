package qiu.niorgai.slidefinishlayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 滑动返回的Layout
 * Created by qiu on 10/30/15.
 */
public class SlideFinishLayout extends LinearLayout {
    //parent
    private View mParentView;

    //边界距离
    private int mEdgeSlop;

    //最小滑动距离
    private int mTouchSlop;

    //初始坐标
    private int mInitX;

    private int mInitY;

    //用于计算x的偏移值
    private int mTempX;

    private Scroller mScroller;

    private boolean isFinish = false;

    private boolean isSliding = false;

    private int mViewWidth;

    //背景色
    private ColorDrawable backDrawable;

    private Window window;

    private onSlideFinishListener finishListener;

    public SlideFinishLayout(Context context) {
        this(context, null);
    }

    public SlideFinishLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mEdgeSlop = ViewConfiguration.get(context).getScaledEdgeSlop();

        mScroller = new Scroller(context);

        if (context instanceof Activity) {
            window = ((Activity) context).getWindow();
            //初始化时以40%的黑色透明作为Activity背景色
            backDrawable = new ColorDrawable(getResources().getColor(R.color.common_black));
            backDrawable.setAlpha((int) (255 * 0.4));
            window.setBackgroundDrawable(backDrawable);
        }

        //设置Layout背景色
        this.setBackgroundResource(R.color.common_white);
    }

    public void setFinishListener(onSlideFinishListener finishListener) {
        this.finishListener = finishListener;
    }

    public interface onSlideFinishListener {
        public void onSlideFinish();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            mParentView = (View) getParent();
            mViewWidth = getWidth();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInitX = mTempX = (int) event.getRawX();
                mInitY = (int) event.getRawY();
                //如果从边缘滑动,那么拦截这个Touch事件
                if (mInitX <= mEdgeSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getRawX();
                //判断是x方向的滑动则开始view的滑动
                if (!isSliding && moveX - mInitX > mTouchSlop && Math.abs((int) event.getRawY() - mInitY) < mTouchSlop) {
                    isSliding = true;
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getRawX();
                if (moveX > mInitX && isSliding) {
                    mParentView.scrollBy(mTempX - moveX, 0);
                    //计算百分比设置40% - 100%的透明
                    if (window != null) {
                        int pre = (int) ((((float) moveX / (float) mViewWidth) * 153f) + 102f);
                        backDrawable.setAlpha(255 - pre);
                        window.setBackgroundDrawable(backDrawable);
                    }
                } else if (moveX < mInitX) {
                    //解决连续滑动Activity无法闭合的问题
                    mParentView.scrollTo(0, 0);
                }
                mTempX = moveX;
                break;
            case MotionEvent.ACTION_UP:
                isSliding = false;
                if (mParentView.getScrollX() <= -mViewWidth / 2) {
                    isFinish = true;
                    scrollToRight();
                } else {
                    isFinish = false;
                    scrollToOrigin();
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            if (mScroller.isFinished() && isFinish && finishListener != null) {
                finishListener.onSlideFinish();
            }
        }
    }

    //滑动至原点
    private void scrollToOrigin() {
        final int delta = mParentView.getScrollX();
        mScroller.startScroll(mParentView.getScrollX(), 0, -delta, 0, Math.abs(delta));
        postInvalidate();
    }

    //滑动到右侧
    private void scrollToRight() {
        backDrawable.setAlpha(0);
        window.setBackgroundDrawable(backDrawable);
        final int delta = mViewWidth + mParentView.getScrollX();
        mScroller.startScroll(mParentView.getScrollX(), 0, -delta + 1, 0, Math.abs(delta));
        postInvalidate();
    }
}
