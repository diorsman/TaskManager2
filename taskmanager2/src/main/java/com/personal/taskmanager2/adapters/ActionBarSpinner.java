package com.personal.taskmanager2.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.personal.taskmanager2.R;

public class ActionBarSpinner extends ArrayAdapter<String> {

    private static final String TAG = "ActionBarSpinner";

    private String mTitle;

    public ActionBarSpinner(Context context, String[] values, String title) {

        super(context,
              R.layout.action_bar_spinner_item,
              values);

        mTitle = title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.action_bar_spinner_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.action_bar_title);
        TextView subtitle = (TextView) convertView.findViewById(R.id.action_bar_subtitle);
        subtitle.setText(getItem(position));
        title.setText(mTitle);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        TextView textVew = (TextView) view.findViewById(android.R.id.text1);
        textVew.setTextColor(Color.BLACK);

        int[] attrs = new int[] {android.R.attr.selectableItemBackground};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0);
        ta.recycle();
        textVew.setBackground(drawableFromTheme);

        return view;
    }
}
