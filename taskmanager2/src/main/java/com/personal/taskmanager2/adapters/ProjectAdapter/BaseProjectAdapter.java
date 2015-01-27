package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
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

        void onItemClick(View v, int position);

        void onItemLongClick(View v, int position);
    }

    public interface ApplyAction {

        void modifyProject(Project project, int position);
    }

    private static final int CHECK_ANIM = 1;
    private static final int ORIG_ANIM  = 2;

    private Context                     mContext;
    private List<Project>               mProjectList;
    private SparseBooleanArray          mSelectedItems;
    private SparseIntArray              mAnimItems;
    private SectionedRecycleViewAdapter mSectionAdapter;

    private        int        mStyleIdCompleted;
    private        int        mStyleIdNotCompleted;
    private static DateFormat sDateFormat;

    private static HashMap<IconKey, CharCircleIcon> sIconMap = new HashMap<>();
    Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

    private RecyclerView mRecyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

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
            int position = mSelectedItems.keyAt(i);
            Project project = mProjectList.get(position);
            func.modifyProject(project, position);
        }
    }

    public void forEachSelectedItemRemove(ApplyAction func) {
        for (int i = mSelectedItems.size() - 1; i >= 0; --i) {
            int position = mSelectedItems.keyAt(i);
            Project project = getItem(position);
            func.modifyProject(project, position);
            removeItem(project);
            notifyItemRemoved(mSectionAdapter.positionToSectionedPosition(position));
        }
        mSelectedItems.clear();
        mAnimItems.clear();
    }

    protected void initClick(final ViewHolder viewHolder) {
        viewHolder.projectAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onAvatarClick(v, viewHolder.getPosition());
            }
        });

        viewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(viewHolder.itemView, viewHolder.getPosition());
            }
        });

        viewHolder.mainView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(viewHolder.itemView, viewHolder.getPosition());
                return true;
            }
        });
    }

    @Override
    public void onBindViewHolder(E holder, int position) {
        Project project = getItem(position);

        if (isItemSelected(position)) {
            holder.mainView.setCardBackgroundColor(getContext().getResources()
                                                               .getColor(R.color.item_selected_background));
        }
        else {
            holder.mainView.setCardBackgroundColor(getContext().getResources()
                                                               .getColor(android.R.color.white));
        }

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

        public CardView mainView;
        public View     completionView;
        public View     archiveView;
        public View     projectAvatar;
        public TextView projectName;
        public TextView projectDueDate;
        public View     divider;

        public ViewHolder(final View itemView) {
            super(itemView);

            mainView = (CardView) itemView.findViewById(R.id.main_view);
            completionView = itemView.findViewById(R.id.completion_view);
            archiveView = itemView.findViewById(R.id.archive_view);
            projectAvatar = itemView.findViewById(R.id.project_list_color_slice);
            projectName = (TextView) itemView.findViewById(R.id.project_list_name);
            projectDueDate = (TextView) itemView.findViewById(R.id.project_list_due_date);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}