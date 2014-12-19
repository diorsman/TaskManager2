package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.CharCircleIcon;
import com.personal.taskmanager2.utilities.IconKey;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public abstract class BaseProjectAdapter<E extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<E> {

    public interface OnItemClickListener {

        public void onAvatarClick(int position);

        public void onItemClick(View v);

        public void onItemLongClick(View v);
    }

    private static final String TAG = "BaseProjectAdapter";

    private Context            mContext;
    private List<Project>      mProjectList;
    private SparseBooleanArray mSelectedItems;

    private static HashMap<IconKey, CharCircleIcon> sIconMap = new HashMap<>();
    Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

    protected OnItemClickListener mClickListener;

    public BaseProjectAdapter(Context context,
                              List<Project> projectList,
                              OnItemClickListener listener) {
        mProjectList = projectList;
        mContext = context;
        mSelectedItems = new SparseBooleanArray();
        mClickListener = listener;
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
        notifyItemChanged(position);
    }

    public void unselectedItem(int position) {
        mSelectedItems.delete(position);
        notifyItemChanged(position);
    }

    public boolean isItemSelected(int position) {
        return mSelectedItems.get(position);
    }

    public int getNumSelected() {
        return mSelectedItems.size();
    }

    public void clearSelection() {
        mSelectedItems.clear();
        notifyDataSetChanged();
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
        else {
            textView.setTextAppearance(mContext, styleNotComplete);
            textView.setPaintFlags(textView.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    protected void initAvatar(View avatar, Project project, final int position) {
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onAvatarClick(position);
            }
        });

        if (isItemSelected(position)) {
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
            avatar.setBackground(icon);
        }
    }
}
