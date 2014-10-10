package com.personal.taskmanager2.model.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Task")
public class Task extends ParseObject {

    public static final String NAME_COL        = "name";
    public static final String ASSIGNED_TO_COL = "assignedTo";
    public static final String DESCRIPTION_COL = "description";
    public static final String STATUS_COL      = "status";
    public static final String DUE_DATE_COL    = "dueDate";
    public static final String COLOR_COL       = "color";
    public static final String COLOR_RSRC_COL  = "ColorRsrc";

    public void setName(String name) {

        put(NAME_COL, name);
    }

    public String getName() {

        return getString(NAME_COL);
    }

    public void setDescription(String description) {

        put(DESCRIPTION_COL, description);
    }

    public String getDescription() {

        return getString(ASSIGNED_TO_COL);
    }

    public void assignTo(ParseUser user) {

        put(ASSIGNED_TO_COL, user);
    }

    public ParseUser getAssignedTo() {

        return getParseUser(ASSIGNED_TO_COL);
    }

    public void setDueDate(Date date) {

        put(DUE_DATE_COL, date);
    }

    public Date getDueDate() {

        return getDate(DUE_DATE_COL);
    }

    public void setStatus(String status) {

        put(STATUS_COL, status);
    }

    public String getStatus() {

        return getString(STATUS_COL);
    }

    public String getColor() {

        return getString(COLOR_COL);
    }

    public void setColor(String color) {

        put(COLOR_COL, color);

        switch (color) {
            case "Blue":
                setColorRsrc(android.R.color.holo_blue_dark);
                break;
            case "Orange":
                setColorRsrc(android.R.color.holo_orange_dark);
                break;
            case "Yellow":
                setColorRsrc(android.R.color.holo_orange_light);
                break;
            case "Green":
                setColorRsrc(android.R.color.holo_green_dark);
                break;
            case "Red":
                setColorRsrc(android.R.color.holo_red_dark);
                break;
            case "Purple":
                setColorRsrc(android.R.color.holo_purple);
                break;
        }
    }

    public int getColorRsrc() {

        return getInt(COLOR_RSRC_COL);
    }

    private void setColorRsrc(int colorRsrc) {

        put(COLOR_RSRC_COL, colorRsrc);
    }

}

