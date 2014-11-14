package com.personal.taskmanager2.utilities;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Omid on 11/14/14.
 */
public class ListViewAnimationHelper<T> {

    private BaseAdapter               mAdapter;
    private ListView                  mListView;
    private Animation                 mRemoveAnimation;
    private int                       mRemoveAnimDuration;
    private Context                   mContext;
    private List<T>                   mDataSource;

    public interface ListViewAnimationListener {

        public void onAnimationStart();

        public void onAnimationEnd();

        public void onAnimationRepeat();
    }

    public ListViewAnimationHelper(int anim_resource_id,
                                   int animDuration,
                                   Context context,
                                   ListViewAnimationListener animListener) {
        mRemoveAnimDuration = animDuration;
        mContext = context;

        initAnim(context, anim_resource_id, 350, animListener);
    }

    public int getRemoveAnimDuration() {
        return mRemoveAnimDuration;
    }

    public void setRemoveAnimDuration(int removeAnimDuration) {
        mRemoveAnimDuration = removeAnimDuration;
        mRemoveAnimation.setDuration(removeAnimDuration);
    }

    public void setDataSource(List<T> dataSource) {
        mDataSource = dataSource;
    }

    public void setListView(ListView listView) {
        mListView = listView;
    }

    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
    }

    private void initAnim(Context context,
                          int anim_res_id,
                          int duration,
                          final ListViewAnimationListener listener) {
        mRemoveAnimation = AnimationUtils.loadAnimation(context, anim_res_id);
        mRemoveAnimation.setDuration(duration);
        mRemoveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listener.onAnimationEnd();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                listener.onAnimationRepeat();
            }
        });
    }

    public void showAnimation(T item) {
        int pos = mDataSource.indexOf(item);
        showAnimation(pos);
    }

    public void showAnimation(int pos) {
        mDataSource.remove(pos);
        int visiblePos = mListView.getFirstVisiblePosition();
        mListView.getChildAt(pos - visiblePos).startAnimation(mRemoveAnimation);
    }

}
