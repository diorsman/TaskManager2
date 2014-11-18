package com.personal.taskmanager2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.toolbar_spinner_dropdown, parent, false);
        }

        CheckedTextView text = (CheckedTextView) convertView.findViewById(R.id.spinner_item);
        text.setText(getItem(position));
        return convertView;
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
}
