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

package com.personal.taskmanager2.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public class ItemTouchListener implements RecyclerView.OnItemTouchListener {

    private static final String TAG = "ItemTouchListener";

    // Cached ViewConfiguration and system-wide constant values
    private int  mSlop;
    private int  mMinFlingVelocity;
    private int  mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private RecyclerView       mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private DismissCallbacks   mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private List<PendingDismissData> mPendingDismisses         = new ArrayList<>();
    private int                      mDismissAnimationRefCount = 0;
    private float           mDownX;
    private float           mDownY;
    private float           mDeltaX;
    private boolean         mSwiping;
    private int             mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int             mDownPosition;
    private View            mDownView;
    private boolean         mPaused;
    private View            mCompletionView;
    private View            mArchiveView;
    private View            mMainView;


    private static final int NON_SWIPE_STATE = 0;
    private static final int SWIPE_STATE     = 1;
    private              int mCurState       = NON_SWIPE_STATE;

    /**
     * The callback interface used by {@link ItemTouchListener} to inform its client
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
         * @param listView               The originating {@link android.support.v7.widget.RecyclerView}.
         * @param reverseSortedPositions An array of positions to dismiss, sorted in descending
         *                               order for convenience.
         */
        void onDismiss(RecyclerView listView, int[] reverseSortedPositions);
    }

    public ItemTouchListener(RecyclerView recyclerView,
                             SwipeRefreshLayout refreshLayout,
                             DismissCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = recyclerView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mRecyclerView = recyclerView;
        mRefreshLayout = refreshLayout;
        mCallbacks = callbacks;

    }

    public RecyclerView.OnScrollListener makeScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        };
    }

    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        mViewWidth = mRecyclerView.getWidth();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_DOWN: {
                // Find the child view that was touched (perform a hit test)
                mDownView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());

                if (mDownView != null) {
                    mDownX = e.getRawX();
                    mDownY = e.getRawY();
                    mDownPosition = mRecyclerView.getChildPosition(mDownView);
                    if (mCallbacks.canDismiss(mDownPosition)) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(e);

                        BaseProjectAdapter.ViewHolder viewHolder =
                                (BaseProjectAdapter.ViewHolder) mRecyclerView.getChildViewHolder(mDownView);
                        mCompletionView = viewHolder.completionView;
                        mMainView = viewHolder.mainView;
                        mArchiveView = viewHolder.archiveView;
                    }
                    else {
                        mDownView = null;
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }
                actionUp(e);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(e);
                float deltaX = e.getRawX() - mDownX;
                float deltaY = e.getRawY() - mDownY;

                if ((mCurState == SWIPE_STATE) ||
                    (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2)) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mDeltaX = deltaX;
                    mCurState = SWIPE_STATE;
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
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker == null) {
                    break;
                }
                actionUp(e);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDeltaX > 0) {
                    mCompletionView.setVisibility(View.VISIBLE);
                    mArchiveView.setVisibility(View.GONE);
                }
                else {
                    mArchiveView.setVisibility(View.VISIBLE);
                    mCompletionView.setVisibility(View.GONE);
                }
                mMainView.setTranslationX(mDeltaX);
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
                break;
        }
    }

    private void actionUp(MotionEvent e) {

        float deltaX = e.getRawX() - mDownX;
        mVelocityTracker.addMovement(e);
        mVelocityTracker.computeCurrentVelocity(1000);
        float velocityX = mVelocityTracker.getXVelocity();
        float absVelocityX = Math.abs(velocityX);
        float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
        boolean dismiss = false;
        boolean dismissRight = false;

        if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
            && absVelocityY < absVelocityX && mSwiping) {
            // dismiss only if flinging in the same direction as dragging
            dismiss = (velocityX < 0) == (deltaX < 0);
            dismissRight = mVelocityTracker.getXVelocity() > 0;
        }
        if (dismiss && mDownPosition != RecyclerView.NO_POSITION) {
            // dismiss
            final View downView = mDownView; // mDownView gets null'd before animation ends
            final int downPosition = mDownPosition;
            ++mDismissAnimationRefCount;
            mMainView.animate()
                     .translationX(dismissRight ? mViewWidth : -mViewWidth)
                     .alpha(0)
                     .setDuration(mAnimationTime)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             performDismiss(downView, downPosition);
                         }
                     });
        }
        else {
            // cancel
            mMainView.animate()
                     .translationX(0)
                     .alpha(1)
                     .setDuration(mAnimationTime)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             if (mCompletionView != null &&
                                 mCompletionView.getVisibility() == View.VISIBLE) {
                                 mCompletionView.setVisibility(View.GONE);
                             }
                             else if (mArchiveView != null &&
                                      mArchiveView.getVisibility() == View.VISIBLE) {
                                 mArchiveView.setVisibility(View.GONE);
                             }
                         }
                     });
        }

        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mDownX = 0;
        mDownY = 0;
        mDownView = null;
        mDownPosition = RecyclerView.NO_POSITION;
        mSwiping = false;
        mCurState = NON_SWIPE_STATE;
    }

    class PendingDismissData implements Comparable<PendingDismissData> {

        public int  position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --mDismissAnimationRefCount;
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(mPendingDismisses);

                    int[] dismissPositions = new int[mPendingDismisses.size()];
                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                        dismissPositions[i] = mPendingDismisses.get(i).position;
                    }
                    mCallbacks.onDismiss(mRecyclerView, dismissPositions);

                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                    // animation with a stale position
                    mDownPosition = RecyclerView.NO_POSITION;

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        // Reset view presentation
                        pendingDismiss.view.setAlpha(1f);
                        pendingDismiss.view.setTranslationX(0);
                        lp = pendingDismiss.view.getLayoutParams();
                        lp.height = originalHeight;
                        pendingDismiss.view.setLayoutParams(lp);
                    }

                    // Send a cancel event
                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(time,
                                                                 time,
                                                                 MotionEvent.ACTION_CANCEL,
                                                                 0,
                                                                 0,
                                                                 0);
                    mRecyclerView.dispatchTouchEvent(cancelEvent);

                    mPendingDismisses.clear();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }
}