/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.personal.taskmanager2.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public class RecyclerViewTouchListener implements RecyclerView.OnItemTouchListener {

    private static final String TAG = "RecyclerViewTouchListener";

    private VelocityTracker mVelocityTracker;
    private float           mDownX;
    private float           mDeltaX;
    private boolean         mSwiping;

    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private int mAnimationTime;
    private int mSlop;

    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    private View mChildView;
    private int  mChildPosition;

    private RecyclerView       mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;

    private DismissCallbacks mCallbacks;

    /**
     * The callback interface used by {@link RecyclerViewTouchListener} to inform its client
     * about a successful dismissal of one or more list item positions.
     */
    public interface DismissCallbacks {

        /**
         * Called to determine whether the given position can be dismissed.
         */
        boolean canDismiss(int position);

        /**
         * Called when the user has indicated they she would like to dismiss one or more list item
         * positions.
         *
         * @param listView               The originating {@link ListView}.
         * @param reverseSortedPositions An array of positions to dismiss, sorted in descending
         *                               order for convenience.
         */
        void onDismiss(RecyclerView listView, int[] reverseSortedPositions);
    }


    public RecyclerViewTouchListener(RecyclerView recyclerView, SwipeRefreshLayout refreshLayout, DismissCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = recyclerView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mSlop = vc.getScaledTouchSlop();
        mRecyclerView = recyclerView;
        mRefreshLayout = refreshLayout;
        mCallbacks = callbacks;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        boolean mPaused = mRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE;

        mViewWidth = mRecyclerView.getWidth();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }
                Log.d(TAG, "Cancel Intercept");

                if (mChildView != null) {
                    // cancel
                    mChildView.animate()
                              .translationX(0)
                              .alpha(1)
                              .setDuration(mAnimationTime)
                              .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDeltaX = 0;
                mChildView = null;
                mChildPosition = RecyclerView.NO_POSITION;
                mSwiping = false;

                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }
                Log.d(TAG, "Down Intercept");

                mChildView = mRecyclerView.findChildViewUnder(e.getRawX(), e.getY());
                if (mChildView == null) {
                    Log.d(TAG, "child view is null in action down");
                    break;
                }

                mChildPosition = mRecyclerView.getChildPosition(mChildView);
                mDownX = e.getRawX();
                if (mCallbacks.canDismiss(mChildPosition)) {
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(e);
                }
                else {
                    mChildView = null;
                }
                //rv.onTouchEvent(e);
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                Log.d(TAG, "Up Intercept");

                float deltaX = e.getRawX() - mDownX;
                float absDeltaX = Math.abs(deltaX);
                mVelocityTracker.addMovement(e);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (absDeltaX > mViewWidth / 2) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                }
                else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                         && absVelocityY < absVelocityX) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }

                if (dismiss) {
                    dismiss(mChildView, mChildPosition, dismissRight);
                }
                else {
                    mChildView.animate()
                              .alpha(1)
                              .translationX(0)
                              .setDuration(mAnimationTime)
                              .setListener(null);
                }

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDeltaX = 0;
                mChildView = null;
                mChildPosition = RecyclerView.NO_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                Log.d(TAG, "Move Intercept");

                mVelocityTracker.addMovement(e);
                float deltaX = e.getRawX() - mDownX;
                float absDeltaX = Math.abs(deltaX);
                if (absDeltaX > mSlop) {
                    mDeltaX = deltaX;
                    return true;
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "Cancel Event");
                break;
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "Press Event");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "Up Event");

                if (mVelocityTracker == null) {
                    Log.d(TAG, "velocity tracker is null in action up");
                    break;
                }

                Log.d(TAG, "Up Intercept");
                float deltaX = e.getRawX() - mDownX;
                float absDeltaX = Math.abs(deltaX);
                mVelocityTracker.addMovement(e);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (absDeltaX > mViewWidth / 2) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                }
                else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                         && absVelocityY < absVelocityX) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }

                if (dismiss) {
                    dismiss(mChildView, mChildPosition, dismissRight);
                }
                else {
                    mChildView.animate()
                              .alpha(1)
                              .translationX(0)
                              .setDuration(mAnimationTime)
                              .setListener(null);
                }

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDeltaX = 0;
                mChildView = null;
                mChildPosition = RecyclerView.NO_POSITION;
                mSwiping = false;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "Move Event");
                mRecyclerView.requestDisallowInterceptTouchEvent(true);
                mRefreshLayout.requestDisallowInterceptTouchEvent(true);

                // Cancel ListView's touch (un-highlighting the item)
                MotionEvent cancelEvent = MotionEvent.obtain(e);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                                      (e.getActionIndex()
                                       << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                mRecyclerView.onTouchEvent(cancelEvent);
                mRefreshLayout.onTouchEvent(cancelEvent);
                cancelEvent.recycle();

                mChildView.setTranslationX(mDeltaX);
                /*mChildView.setAlpha(Math.max(0.15f, Math.min(1f,
                                                             1f - 2f * Math.abs(mDeltaX) /
                                                                  mViewWidth)));*/
                break;
        }
    }

    private void dismiss(final View view, final int position, boolean dismissRight) {


        view.animate()
            .translationX(dismissRight ? mViewWidth : -mViewWidth)
            .alpha(0)
            .setDuration(mAnimationTime)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //performDismiss(view, position);
                }
            });
    }

}