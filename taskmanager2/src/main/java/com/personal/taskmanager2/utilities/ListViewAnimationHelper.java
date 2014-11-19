package com.personal.taskmanager2.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.ListView;

import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.model.parse.Project;

/**
 * Created by Omid Ghomeshi on 11/14/14.
 */
public class ListViewAnimationHelper<T> {

    private ListView                mListView;
    private AnimatorListenerAdapter mRemoveListener;
    private BaseProjectAdapter      mAdapter;


    public ListViewAnimationHelper(AnimatorListenerAdapter removeListener) {
        mRemoveListener = removeListener;
    }

    public void setAdapter(BaseProjectAdapter adapter) {
        mAdapter = adapter;
    }

    public void setListView(ListView listView) {
        mListView = listView;
    }

    public void showRemoveAnimation(T item) {
        int pos = mAdapter.getPosition((Project) item);
        showRemoveAnimation(pos);
    }

    public void showRemoveAnimation(int pos) {
        mAdapter.remove(pos);
        int visiblePos = mListView.getFirstVisiblePosition();
        final View item = mListView.getChildAt(pos - visiblePos);
        item.animate()
            .alpha(0)
            .translationX(mListView.getWidth() / 2)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRemoveListener.onAnimationEnd(animation);
                    item.setAlpha(1f);
                    item.setTranslationX(0);
                }
            });
    }

}
