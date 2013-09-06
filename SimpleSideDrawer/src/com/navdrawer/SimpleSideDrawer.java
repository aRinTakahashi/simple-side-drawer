/*
 * Copyright (C) 2013 Masahiko Adachi(http://www.adamrocker.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navdrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * <p>This class enables to add a NavDrawer simply.</p>
 * <p>How to use:</p>
 * <p>After calling setContentView method in onCreate method, call 2 methods bellow.</p>
 * <ul>
 *     <li>- New SimpleNavDrawer instance</li>
 *     <li>- Set a layout file which will be set in the NavDrawer.</li>
 * </ul> 
 * <pre class="prettyprint">
 * public void onCreate(Bundle data) {
 *     super.onCreate(data);
 *     setContentView(R.layout.main);
 *     mNav = new SimpleNavDrawer(this);
 *     mNav.setLeftBehindContentView(R.layout.manu);
 * }
 * 
 * public boolean dispatchTouchEvent(MotionEvent ev) {
 *     if (mNav.dispatchActivityTouchEvent(ev)) {
 *         return true;
 *     } else {
 *         return super.dispatchTouchEvent(ev);
 *     }
 * }
 * </pre>
 * @author Masahiko Adachi
  */
public class SimpleSideDrawer extends FrameLayout {
    private final Window mWindow;
    private final ViewGroup mAboveView;
    private final BehindLinearLayout mBehindView;
    private final LinearLayout mLeftBehindBase;
    private final LinearLayout mRightBehindBase;
    private final View mOverlay;
    
    private Scroller mScroller;
    private View mLeftBehindView;//menu of left-behind will be set
    private View mRightBehindView;//menu of right-behind will be set
    private Rect mLeftPaddingRect;
    private Rect mRightPaddingRect;
    private int mDurationLeft;
    private int mDurationRight;
    private int mLeftBehindViewWidth;
    private int mRightBehindViewWidth;
    private boolean mDragOpenEnabled = true;

    private DragAction mDragAction = new DragAction();

    private enum DragState { INITIAL, SCROLLING, NOT_SCROLLING }; 
    
    private class DragAction {
        private float mLastMotionX = 0f;
        private float mLastMotionY = 0f;
        private float mLastMoveDistanceX = 0f;
        private DragState mState = DragState.INITIAL;

        public boolean dispatchTouchEvent(MotionEvent event) {
            int actionMasked = event.getAction() & MotionEvent.ACTION_MASK;
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mState = DragState.INITIAL;
            } else if (actionMasked == MotionEvent.ACTION_UP) {
                if (mState == DragState.SCROLLING && mAboveView.getScrollX() != 0) {
                    int currentX = mAboveView.getScrollX();
                    int diffX = 0;
                    if (mLastMoveDistanceX < 0) {
                        if (isLeftSideOpened()) {
                            diffX = -(mLeftBehindViewWidth + currentX);
                        } else if (isRightSideOpened()) {
                            diffX = -currentX;
                        }
                    } else if (mLastMoveDistanceX > 0) {
                        if (isLeftSideOpened()) {
                            diffX = -currentX;
                        } else if (isRightSideOpened()) {
                            diffX = mRightBehindViewWidth - currentX;
                        }
                    }
                    if (diffX != 0) {
                        mScroller.startScroll(currentX, 0, diffX, 0, mDurationLeft);
                        invalidate();
                        event.setAction(MotionEvent.ACTION_CANCEL);
                    }
                }
            } else if (actionMasked == MotionEvent.ACTION_MOVE) {
                float distanceX = mLastMotionX - event.getX();
                float distanceY = mLastMotionY - event.getY();
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mLastMoveDistanceX = distanceX;

                float nextX = mAboveView.getScrollX() + distanceX;
                if (mLeftBehindView == null && nextX < 0) {
                    nextX = 0;
                } else if (mRightBehindView == null && nextX > 0) {
                    nextX = 0;
                } else if (nextX < -mLeftBehindViewWidth) {
                    nextX = -mLeftBehindViewWidth;
                } else if (nextX > mRightBehindViewWidth) {
                    nextX = mRightBehindViewWidth;
                }
                
                if ((mState == DragState.INITIAL && Math.abs(distanceX) > Math.abs(distanceY))
                        || mState == DragState.SCROLLING) {
                    if (mAboveView.getScrollX() >= 0 && nextX < 0) {
                        if (!mDragOpenEnabled) {
                            return false;
                        }
                        mLeftBehindBase.setVisibility(View.VISIBLE);
                        mRightBehindBase.setVisibility(View.GONE);
                    } else if (mAboveView.getScrollX() <= 0 && nextX > 0) {
                        if (!mDragOpenEnabled) {
                            return false;
                        }
                        mLeftBehindBase.setVisibility(View.GONE);
                        mRightBehindBase.setVisibility(View.VISIBLE);
                    }
                    mAboveView.scrollTo((int) nextX, 0);
                    mState = DragState.SCROLLING;
                    return true;
                } else {
                    mState = DragState.NOT_SCROLLING;
                    return false;
                }
            }
            return false;
        }
	}

    /**
     * <p>The default Interpolator of drawer animation is DecelerateInterpolator(9.9).</p>
     * <p>The default animation duration is 230msec.</p>
     * @see SimpleNavDrawer(Activity act, Interpolator ip, int duration);
     * @param act
     */
    public SimpleSideDrawer(Activity act) {
        this(act, new DecelerateInterpolator(0.9f), 180);
    }
    
    public SimpleSideDrawer(Activity act, Interpolator ip, int duration) {
        super(act.getApplicationContext());
        final Context context = act.getApplicationContext();
        mDurationLeft = duration;
        mDurationRight = duration;
        mWindow = act.getWindow();
        mScroller = new Scroller(context, ip);
        
        final int fp = LayoutParams.FILL_PARENT;
        final int wp = LayoutParams.WRAP_CONTENT;
        //behind
        mBehindView = new BehindLinearLayout(context);
        mBehindView.setLayoutParams(new LinearLayout.LayoutParams(fp, fp));
        mBehindView.setOrientation(LinearLayout.HORIZONTAL);
        //left-behind base
        mLeftBehindBase = new BehindLinearLayout(context);
        mBehindView.addView(mLeftBehindBase, new LinearLayout.LayoutParams(wp, fp));
        //behind adjusting view
        mBehindView.addView(new View(context), new LinearLayout.LayoutParams(0, fp, 1));
        //right-behind base
        mRightBehindBase = new BehindLinearLayout(context);
        mBehindView.addView(mRightBehindBase, new LinearLayout.LayoutParams(wp, fp));

        addView(mBehindView);
        
        //above
        mAboveView = new FrameLayout(context);
        mAboveView.setLayoutParams(new FrameLayout.LayoutParams(fp, fp));
        //overlay is used for controlling drag action, slid to close/open.
        mOverlay = new View(getContext());
        mOverlay.setLayoutParams(new FrameLayout.LayoutParams(fp, fp, Gravity.BOTTOM));
        mOverlay.setEnabled(true);
        mOverlay.setVisibility(View.GONE);
        mOverlay.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if ( mLeftBehindBase.getVisibility() != View.GONE ) {
                    closeLeftSide();
                } else if ( mRightBehindBase.getVisibility() != View.GONE ){
                    closeRightSide();
                }
            }
        });
        
        ViewGroup decor = (ViewGroup) mWindow.getDecorView();
        ViewGroup above = (ViewGroup) decor.getChildAt(0);//including actionbar
        decor.removeView(above);
        above.setBackgroundDrawable(decor.getBackground());
        mAboveView.addView(above);
        mAboveView.addView(mOverlay);
        decor.addView(this);
        
        addView(mAboveView);
    }
    
    /**
     * <p>Get the left behind view</p>
     * @return The view which you set. Return null if you did not set the view.
     */
    public View getLeftBehindView() {
        return mLeftBehindBase.getChildAt(0);
    }
    
    /**
     * <p>Get the right behind view</p>
     * @return The view which you set. Return null if you did not set the view.
     */
    public View getRightBehindView() {
        return mRightBehindBase.getChildAt(0);
    }
    
    /**
     * <p>Set the behind view layout.</p>
     * <p>Call this method after setting the main content view by calling setContentView().</p>
     * @param <p><b>leftBehindLayout</b>: The layout id, under the res/layout directory, which is displayed left side.</p>
     * @return The view which will be created from the layout id.
     * @deprecated You should use setLeftBehindContentView()
     */
    public View setBehindContentView(int leftBehindLayout) {
        return setLeftBehindContentView(leftBehindLayout);
    }
    
    /**
     * <p>Set the left behind view layout.</p>
     * <p>Call this method after setting the main content view by calling setContentView().</p>
     * @param <p><b>leftBehindLayout</b>: The layout id, under the res/layout directory, which is displayed left side.</p>
     * @return The view which will be created from the layout id.
     */
    public View setLeftBehindContentView(int leftBehindLayout) {
        final View content = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(leftBehindLayout, mLeftBehindBase);
        mLeftPaddingRect = new Rect(content.getPaddingLeft(), content.getPaddingTop(), content.getPaddingRight(), content.getPaddingBottom());
        mLeftBehindView = content;
        return content;
    }

    /**
     * <p>Set the right behind view layout.</p>
     * <p>Call this method after setting the main content view by calling setContentView().</p>
     * @param <p><b>leftBehindLayout</b>: The layout id, under the res/layout directory, which is displayed left side.</p>
     * @return The view which will be created from the layout id.
     */
    public View setRightBehindContentView(int rightBehindLayout) {
        final View content = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(rightBehindLayout, mRightBehindBase);
        mRightPaddingRect = new Rect(content.getPaddingLeft(), content.getPaddingTop(), content.getPaddingRight(), content.getPaddingBottom());
        mRightBehindView = content;
        return content;
    }

    /**
     * Change the side scroll interpolator
     * @param ip Interpolator object
     */
    public void setScrollInterpolator(Interpolator ip) {
        mScroller = new Scroller(getContext(), ip);
    }
    
    /**
     * Change the duration time of scrolling
     * @param msec The duration time should be milli-second
     * @deprecated You should use setAnimationDurationLeft()
     */
    public void setAnimationDuration(int msec) {
        setAnimationDurationLeft(msec);
    }
    
    /**
     * Set the duration time of left sliding animation. 
     * @param msec The duration time should be milli-second ( default = 180 )
     */
    public void setAnimationDurationLeft(int msec) {
        mDurationLeft = msec;
    }
    
    /**
     * Set the duration time of ringht sliding animation. 
     * @param msec The duration time should be milli-second ( default = 180 )
     */
    public void setAnimationDurationRight(int msec) {
        mDurationRight = msec;
    }
    
    /**
     * Close the behind view by swiping left the front view.
     * @deprecated You should use closeLeftSide()
     */
    public void close() {
        closeLeftSide();
    }
    
    /**
     * Close the left-side behind view
     */
    public void closeLeftSide() {
        int curX = mAboveView.getScrollX();
        mScroller.startScroll(curX, 0, -curX, 0, mDurationLeft);
        invalidate();
    }
    
    /**
     * Close the right-side behide view 
     */
    public void closeRightSide() {
        int curX = mAboveView.getScrollX();
        mScroller.startScroll(curX, 0, -curX, 0, mDurationRight);
        invalidate();
    }
    
    /**
     * Open the behind view by swiping the front view right
     * @deprecated You should use openLeftSide()
     */
    public void open() {
        openLeftSide();
    }
    
    /**
     * Open the left behind view by swiping the front view right
     */
    public void openLeftSide() {
        mLeftBehindBase.setVisibility( View.VISIBLE );
        mRightBehindBase.setVisibility( View.GONE );
            
        int curX = mAboveView.getScrollX();
        mScroller.startScroll(curX, 0, -mLeftBehindViewWidth, 0, mDurationLeft);
        invalidate();
    }
    
    public void openRightSide() {
        mRightBehindBase.setVisibility( View.VISIBLE );
        mLeftBehindBase.setVisibility( View.GONE );
        
        int curX = mAboveView.getScrollX();
        mScroller.startScroll(curX, 0, mRightBehindViewWidth, 0, mDurationRight);
        invalidate();
    }
    
    /**
     * If the behind view is opened, close it. If the behind view is closed, open it.
     * @deprecated You should use toggleLeftDrawer()
     */
    public void toggleDrawer() {
        toggleLeftDrawer();
    }
    
    /**
     * If the left behind view is opened, close it. If the left behind view is closed, open it.
     */
    public void toggleLeftDrawer() {
        if (isClosed()) {
            openLeftSide();
        } else {
            closeLeftSide();
        }
    }
    
    /**
     * If the right behind view is opened, close it. If the left behind view is closed, open it.
     */
    public void toggleRightDrawer() {
        if (isClosed()) {
            openRightSide();
        } else {
            closeRightSide();
        }
    }

    /**
     * Check the current status of the behind view
     * @return
     */
    public boolean isClosed() {
        return mAboveView != null && mAboveView.getScrollX() == 0;
    }
    
    /**
     * Set the enabled state of opening side drawer by dragging
     * @param enabled
     */
    public void setDragOpenEnabled(boolean enabled) {
        mDragOpenEnabled = enabled;
    }
    
    /**
     * call this method from Activity.dispatchTouchEvent
     * @param ev
     * @return
     */
    public boolean dispatchActivityTouchEvent(MotionEvent ev) {
        if (mDragAction.dispatchTouchEvent(ev)) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }
    
    private boolean isLeftSideOpened() {
        return mLeftBehindBase.getVisibility() == View.VISIBLE && mRightBehindBase.getVisibility() == View.GONE;
    }
    
    private boolean isRightSideOpened() {
        return mRightBehindBase.getVisibility() == View.VISIBLE && mLeftBehindBase.getVisibility() == View.GONE;
    }
    
    /**
     * Need to adjust the behind view height
     * {@hide}
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLeftBehindViewWidth = mLeftBehindBase.getMeasuredWidth();
        mRightBehindViewWidth = mRightBehindBase.getMeasuredWidth();

        //adjust the behind display area
        ViewGroup decor = (ViewGroup) mWindow.getDecorView();
        Rect rect = new Rect();
        decor.getWindowVisibleDisplayFrame(rect);
        mBehindView.fitDisplay(rect);
    }
    
    /**
     * Side scroll animation
     * {@hide}
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mAboveView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else {
            if (mAboveView.getScrollX() == 0) {
                mOverlay.setVisibility(View.GONE);
                mLeftBehindBase.setVisibility(View.GONE);
                mRightBehindBase.setVisibility(View.GONE);
            } else {
                mOverlay.setVisibility(View.VISIBLE);
            }
        }
    }

	private class BehindLinearLayout extends LinearLayout {

        public BehindLinearLayout(Context context) {
            super(context);
        }
        
        /**
         * Adjust the behind view
         * @param rect The display area
         */
        public void fitDisplay(Rect rect) {
            mBehindView.setPadding(rect.left, rect.top, 0, 0);
            requestLayout();
        }
    }
}