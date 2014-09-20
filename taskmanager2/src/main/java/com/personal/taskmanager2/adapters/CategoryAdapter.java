package com.personal.taskmanager2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.personal.taskmanager2.R;

public class CategoryAdapter extends ArrayAdapter<String> {

    private static final String TAG = "CategoryAdapter";

    private final static String[] sColorValues =
            {"Blue", "Orange", "Yellow", "Green", "Red", "Purple"};

    public CategoryAdapter(Context context) {

        super(context, R.layout.spinner_category_row, sColorValues);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        return getCustomView(position, convertView, parent, R.layout.spinner_category_drop_down);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getCustomView(position, convertView, parent, R.layout.spinner_category_row);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent, int resource) {

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);
        }

        View colorSlice = convertView.findViewById(R.id.spinner_icon);
        TextView spinnerText = (TextView) convertView.findViewById(R.id.spinner_text);

        colorSlice.setBackgroundResource(getResourceFromPosition(position));
        spinnerText.setText(getItem(position));
        spinnerText.setTextColor(convertView.getResources().getColor(getResourceFromPosition(position)));

        return convertView;
    }

    private int getResourceFromPosition(int position) {

        switch (position) {
            case 0:
                return android.R.color.holo_blue_dark;
            case 1:
                return android.R.color.holo_orange_dark;
            case 2:
                return android.R.color.holo_orange_light;
            case 3:
                return android.R.color.holo_green_dark;
            case 4:
                return android.R.color.holo_red_dark;
            case 5:
                return android.R.color.holo_purple;
            default:
                return -1;
        }
    }
}
