package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.personal.taskmanager2.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/26/14.
 */
public class SectionedRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private static final int SECTION_TYPE = 0;

    private boolean mValid = true;
    private LayoutInflater                                    mLayoutInflater;
    private BaseProjectAdapter<BaseProjectAdapter.ViewHolder> mBaseAdapter;
    private SparseArray<Section> mSections = new SparseArray<>();


    public SectionedRecycleViewAdapter(Context context,
                                       BaseProjectAdapter<BaseProjectAdapter.ViewHolder> baseAdapter) {

        mLayoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBaseAdapter = baseAdapter;
        mContext = context;

        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            final View view = mLayoutInflater.inflate(R.layout.list_item_section, parent, false);
            return new SectionViewHolder(view,
                                         R.id.list_item_section_title,
                                         R.id.list_item_section_num_items);
        }
        else {
            return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            Section section = mSections.get(position);
            ((SectionViewHolder) sectionViewHolder).title.setText(section.title);
            if (section.numItems > 0) {
                ((SectionViewHolder) sectionViewHolder).numItems.setText(Integer.toString(section.numItems));
            }
            else {
                ((SectionViewHolder) sectionViewHolder).numItems.setText("");
            }
        }
        else {
            BaseProjectAdapter.ViewHolder viewHolder =
                    (BaseProjectAdapter.ViewHolder) sectionViewHolder;
            mBaseAdapter.onBindViewHolder(viewHolder, sectionedPositionToPosition(position));
            if (isSectionHeaderPosition(position + 1)) {
                viewHolder.divider.setVisibility(View.INVISIBLE);
            }
            else {
                viewHolder.divider.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView numItems;

        public SectionViewHolder(View view, int mTextResourceid, int numItemsResourceId) {
            super(view);
            title = (TextView) view.findViewById(mTextResourceid);
            numItems = (TextView) view.findViewById(numItemsResourceId);
        }
    }


    public static class Section {

        int          firstPosition;
        int          sectionedPosition;
        CharSequence title;
        int          numItems;

        public Section(int firstPosition, CharSequence title, int numItems) {
            this.firstPosition = firstPosition;
            this.title = title;
            this.numItems = numItems;
        }

        public CharSequence getTitle() {
            return title;
        }
    }


    public void setSections(List<Section> sections) {
        Collections.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }


    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }

}
