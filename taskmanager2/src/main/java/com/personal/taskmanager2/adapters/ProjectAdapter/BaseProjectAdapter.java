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

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public abstract class BaseProjectAdapter<E extends BaseProjectAdapter.ViewHolder>
        extends RecyclerView.Adapter<E> {

    private static final String TAG = "BaseProjectAdapter";

    public interface OnItemClickListener {

        void onAvatarClick(View v, int position);

        void onItemClick(View v);

        void onItemLongClick(View v);
    }

    public interface ApplyAction {

        void modifyProject(Project project);
    }

    private static final int CHECK_ANIM = 1;
    private static final int ORIG_ANIM  = 2;

    private Context                     mContext;
    private List<Project>               mProjectList;
    private SparseBooleanArray          mSelectedItems;
    private SparseIntArray              mAnimItems;
    private SectionedRecycleViewAdapter mSectionAdapter;

    private int                         mStyleIdCompleted;
    private int                         mStyleIdNotCompleted;
    private static DateFormat sDateFormat;

    private static HashMap<IconKey, CharCircleIcon> sIconMap = new HashMap<>();
    Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

    protected OnItemClickListener mClickListener;

    public BaseProjectAdapter(Context context,
                              int styleIdCompleted,
                              int styleIdNotCompleted,
                              List<Project> projectList,
                              OnItemClickListener listener,
                              DateFormat dateFormat) {
        mProjectList = projectList;
        mContext = context;
        mSelectedItems = new SparseBooleanArray();
        mAnimItems = new SparseIntArray();
        mClickListener = listener;
        mStyleIdCompleted = styleIdCompleted;
        mStyleIdNotCompleted = styleIdNotCompleted;
        sDateFormat = dateFormat;
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
            int posKey = mSectionAdapter.positionToSectionedPosition(mSelectedItems.keyAt(i));
            notifyItemChanged(posKey);
            if (posKey >= firstVisPos && posKey <= lastVisPos) {
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

    protected void initAvatarClick(final ViewHolder viewHolder) {
        viewHolder.projectAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onAvatarClick(v, viewHolder.getPosition());
            }
        });
    }

    @Override
    public void onBindViewHolder(E holder, int position) {
        Project project = getItem(position);

        if (isItemSelected(position)) {
            holder.itemView.setBackgroundColor(getContext().getResources().getColor(R.color.item_selected_background));
        }
        else {
            holder.itemView.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
        }
        //holder.itemView.setActivated(isItemSelected(position));
        initAvatar(holder.projectAvatar, project, position);

        holder.projectName.setText(project.getName());
        setTitleAppearance(holder.projectName,
                           project,
                           mStyleIdCompleted,
                           mStyleIdNotCompleted);

        holder.projectDueDate.setText(sDateFormat.format(project.getDueDate()));
    }

    private void initAvatar(View avatar, Project project, final int position) {
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

            showAnim(position, ORIG_ANIM, R.animator.card_flip_right_in, avatar);
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

    private void setTitleAppearance(TextView textView,
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View     projectAvatar;
        public TextView projectName;
        public TextView projectDueDate;
        public View divider;

        public ViewHolder(final View itemView) {
            super(itemView);

            projectAvatar = itemView.findViewById(R.id.project_list_color_slice);
            projectName = (TextView) itemView.findViewById(R.id.project_list_name);
            projectDueDate = (TextView) itemView.findViewById(R.id.project_list_due_date);
            divider = (View) itemView.findViewById(R.id.divider);
        }
    }
}
