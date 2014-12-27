package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.widget.CharCircleIcon;
import com.personal.taskmanager2.utilities.IconKey;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public abstract class BaseProjectAdapter<E extends BaseProjectAdapter.ViewHolder>
        extends RecyclerView.Adapter<E> {

    private static final String TAG = "BaseProjectAdapter";

    public interface OnItemClickListener {

        public void onAvatarClick(View v, int position);

        public void onItemClick(View v);

        public void onItemLongClick(View v);
    }

    public interface ApplyAction {
        public void modifyProject(Project project);
    }

    private static final int CHECK_ANIM = 1;
    private static final int ORIG_ANIM  = 2;

    private Context            mContext;
    private List<Project>      mProjectList;
    private SparseBooleanArray mSelectedItems;
    private SparseIntArray     mAnimItems;
    private SectionedRecycleViewAdapter mSectionAdapter;

    private static HashMap<IconKey, CharCircleIcon> sIconMap = new HashMap<>();
    Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

    protected OnItemClickListener mClickListener;

    public BaseProjectAdapter(Context context,
                              List<Project> projectList,
                              OnItemClickListener listener) {
        mProjectList = projectList;
        mContext = context;
        mSelectedItems = new SparseBooleanArray();
        mAnimItems = new SparseIntArray();
        mClickListener = listener;
    }

    public void setSectionAdapter(SectionedRecycleViewAdapter adapter) {
        mSectionAdapter = adapter;
    }

    @Override
    public int getItemCount() {
        return mProjectList.size();
    }

    public Project getItem(int position) {
        return mProjectList.get(position);
    }

    protected Context getContext() {
        return mContext;
    }

    public void selectItem(int position) {
        mSelectedItems.put(position, true);
        mAnimItems.put(position, CHECK_ANIM);
        notifyItemChanged(mSectionAdapter.positionToSectionedPosition(position));
    }

    public void unselectedItem(int position) {
        mSelectedItems.delete(position);
        mAnimItems.put(position, ORIG_ANIM);
        notifyItemChanged(mSectionAdapter.positionToSectionedPosition(position));
    }

    public boolean isItemSelected(int position) {
        return mSelectedItems.get(position);
    }

    public int getNumSelected() {
        return mSelectedItems.size();
    }

    public void removeItem(Project project) {
        mProjectList.remove(project);
    }

    public void clearSelection(int firstVisPos, int lastVisPos) {
        int size = mSelectedItems.size();
        for (int i = 0; i < size; ++i) {
            int key = mSelectedItems.keyAt(i);
            notifyItemChanged(mSectionAdapter.positionToSectionedPosition(key));
            if (key >= firstVisPos && key <= lastVisPos) {
                mAnimItems.put(key, ORIG_ANIM);
            }
            else {
                mAnimItems.delete(key);
            }
        }
        mSelectedItems.clear();
    }

    public void forEachSelectedItemModifyInPlace(ApplyAction func) {
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            int projectPos = mSelectedItems.keyAt(i);
            Project project = mProjectList.get(projectPos);
            func.modifyProject(project);
        }
    }

    public void forEachSelectedItemRemove(ApplyAction func) {
        for (int i = mSelectedItems.size() - 1; i >= 0; --i) {
            int pos = mSelectedItems.keyAt(i);
            Project project = getItem(pos);
            func.modifyProject(project);
            removeItem(project);
            notifyItemRemoved(mSectionAdapter.positionToSectionedPosition(pos));
        }
        mSelectedItems.clear();
        mAnimItems.clear();
    }

    protected View initView(ViewGroup parent, int layout) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout,
                                                                     parent,
                                                                     false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(v);
                return true;
            }
        });

        return view;
    }

    protected void setTitleAppearance(TextView textView,
                                      Project project,
                                      int styleComplete,
                                      int styleNotComplete) {

        if (project.getStatus()) {
            textView.setTextAppearance(mContext, styleComplete);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else if (project.getDueDate().getTime() < System.currentTimeMillis()) {
            textView.setTextAppearance(mContext, styleNotComplete);
            textView.setPaintFlags(textView.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(Color.RED);
        }
        else {
            textView.setTextAppearance(mContext, styleNotComplete);
            textView.setPaintFlags(textView.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    protected void initAvatar(View avatar, Project project, final int position) {
        if (isItemSelected(position)) {
            showAnim(position, CHECK_ANIM, R.animator.card_flip_left_in, avatar);
            avatar.setBackground(getContext().getResources().getDrawable(R.drawable.checked_item));
        }
        else {
            char initLet = project.getAdminName().charAt(0);
            int colorRsrc = Utilities.getColorRsrcFromColor(project.getColor());

            //check if icon already exists
            IconKey key = new IconKey(initLet, colorRsrc);
            CharCircleIcon icon = sIconMap.get(key);

            //create new icon if it does not exist
            if (icon == null) {
                icon = new CharCircleIcon(initLet,
                                          mContext.getResources().getColor(colorRsrc),
                                          typeface);
                sIconMap.put(key, icon);
            }

            showAnim(position,ORIG_ANIM, R.animator.card_flip_right_in, avatar);
            avatar.setBackground(icon);
        }
    }

    private void showAnim(int pos, int animType, int animId, View view) {
        if (mAnimItems.get(pos) == animType) {
            AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), animId);
            anim.setTarget(view);
            anim.start();
            mAnimItems.delete(pos);
        }
    }

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {

        public View     projectAvatar;
        public TextView projectName;
        public TextView projectDueDate;

        public ViewHolder(final View itemView) {
            super(itemView);

            projectAvatar = itemView.findViewById(R.id.project_list_color_slice);
            projectName = (TextView) itemView.findViewById(R.id.project_list_name);
            projectDueDate = (TextView) itemView.findViewById(R.id.project_list_due_date);
        }
    }
}
