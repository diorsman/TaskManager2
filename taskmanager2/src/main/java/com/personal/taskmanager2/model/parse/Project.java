package com.personal.taskmanager2.model.parse;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.EditProjectFragment;
import com.personal.taskmanager2.utilities.BCrypt;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;

@ParseClassName("Project")
public class Project extends ParseObject implements Parcelable {

    public static final String NAME_COL           = "Name";
    public static final String UID_COL            = "Uid";
    public static final String DESCRIPTION_COL    = "Description";
    public static final String PASSWORD_COL       = "Password";
    public static final String ADMIN_COL          = "Administrator";
    public static final String ADMIN_NAME_COL     = "AdminName";
    public static final String DUE_DATE_COL       = "DueDate";
    public static final String USERS_ID_COL       = "UserId";
    public static final String USERS_NAME_COL     = "UserName";
    public static final String TOTAL_TASKS_COL    = "TotalTasks";
    public static final String COMPLETE_TASKS_COL = "CompletedTasks";
    public static final String STATUS_COL         = "Status";
    public static final String COLOR_COL          = "Color";
    public static final String ARCHIVED_COL       = "Archived";
    public static final String TRASH_COL          = "Trash";

    private String mAdminUid;

    public Project() {
        //default constructor
    }

    // Use this constructor when creating new projects
    public Project(String name,
                   String uid,
                   String password,
                   Date dueDate,
                   ParseUser admin,
                   String adminName) {
        // init values
        setName(name);
        setUid(uid);
        setPassword(password);
        setDueDate(dueDate);
        setAdministrator(admin);
        setAdminName(adminName);

        //default values
        setDescription("");
        setNumTotalTasks(12);
        setNumCompletedTasks(0);
        setStatus(false);
        addEmptyUser();
        setColor("Blue");
        setArchive(false);
        setTrash(false);
    }

    public void setAdministrator(ParseUser admin) {

        put(ADMIN_COL, admin);
    }

    public void setNumTotalTasks(int numTasks) {

        put(TOTAL_TASKS_COL, numTasks);
    }

    public void addUser(ParseUser user) {

        addUserId(user);
        addUserName(user);
    }

    private void addUserId(ParseUser user) {

        JSONArray userIdArray = getUserId();
        if (userIdArray == null) {
            userIdArray = new JSONArray();
        }
        userIdArray.put(user.getObjectId());
        put(USERS_ID_COL, userIdArray);
    }

    private void setUserIdArray(JSONArray array) {

        put(USERS_ID_COL, array);
    }

    public JSONArray getUserId() {

        return getJSONArray(USERS_ID_COL);
    }

    private void addUserName(ParseUser user) {

        JSONArray userNameArray = getUserName();
        if (userNameArray == null) {
            userNameArray = new JSONArray();
        }
        userNameArray.put(user.getString("Name"));
        put(USERS_NAME_COL, userNameArray);
    }

    private void setUserNameArray(JSONArray array) {

        put(USERS_NAME_COL, array);
    }

    public JSONArray getUserName() {

        return getJSONArray(USERS_NAME_COL);
    }

    private void addEmptyUser() {

        JSONArray idArray = new JSONArray();
        JSONArray nameArray = new JSONArray();
        put(USERS_ID_COL, idArray);
        put(USERS_NAME_COL, nameArray);
    }

    public String getName() {

        return getString(NAME_COL);
    }

    public void setName(String name) {

        put(NAME_COL, name);
    }

    public String getDescription() {

        return getString(DESCRIPTION_COL);
    }

    public void setDescription(String description) {

        put(DESCRIPTION_COL, description);
    }

    public String getUid() {

        return getString(UID_COL);
    }

    public void setUid(String Uid) {

        put(UID_COL, Uid);
    }

    public String getPassword() {

        return getString(PASSWORD_COL);
    }

    public void setPassword(String password) {

        password = BCrypt.hashpw(password, BCrypt.gensalt());
        put(PASSWORD_COL, password);
    }

    public ParseUser getAdmin() {

        return getParseUser(ADMIN_COL);
    }

    public String getAdminName() {

        return getString(ADMIN_NAME_COL);
    }

    public void setAdminName(String name) {

        put(ADMIN_NAME_COL, name);
    }

    public String getAdminUid() {

        return mAdminUid;
    }

    public Date getDueDate() {

        return getDate(DUE_DATE_COL);
    }

    public void setDueDate(Date dueDate) {

        put(DUE_DATE_COL, dueDate);
    }

    public int getNumTotalTask() {

        return getInt(TOTAL_TASKS_COL);
    }

    public int getNumCompletedTasks() {

        return getInt(COMPLETE_TASKS_COL);
    }

    public void setNumCompletedTasks(int completedTasks) {

        put(COMPLETE_TASKS_COL, completedTasks);
    }

    public boolean getStatus() {

        return getBoolean(STATUS_COL);
    }

    public void setStatus(boolean status) {

        put(STATUS_COL, status);
    }

    public String getColor() {

        return getString(COLOR_COL);
    }

    public void setColor(String color) {

        put(COLOR_COL, color);
    }

    public boolean getArchive() {

        return getBoolean(ARCHIVED_COL);
    }

    public void setArchive(boolean archive) {

        put(ARCHIVED_COL, archive);
    }

    public boolean getTrash() {

        return getBoolean(TRASH_COL);
    }

    public void setTrash(boolean trash) {

        put(TRASH_COL, trash);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    public boolean safeEdit(final FragmentManager fragmentManager, Context context) {

        /*if (isProjectAdminCurUser(ParseUser.getCurrentUser())) {
            fragmentManager.beginTransaction()
                           .addToBackStack(null)
                           .add(R.id.container, EditProjectFragment.newInstance(this))
                           .commit();
        }
        else {
            Toast.makeText(context, "Only administrator can edit the project", Toast.LENGTH_LONG)
                 .show();
        }*/
        return safeModify(context, new ModifyProject() {
            @Override
            public void modify() {
                fragmentManager.beginTransaction()
                               .addToBackStack(null)
                               .replace(R.id.container,
                                        EditProjectFragment.newInstance(Project.this))
                               .commit();
            }
        });
    }

    public void share(Context context) {

        String uid = getUid();
        String password = getPassword();
        String projectName = getName();
        String body =
                "Hello,\n\n" + ParseUser.getCurrentUser().get("Name")
                + " has invited you to join their project, "
                + projectName + "."
                +
                "\nYou can join them on the TaskManager app with the following information."
                + "\nUnique Identifier: " + uid
                + "\nPassword: " + password
                + "\n\nSincerely,\nThe Task Manager App Team\n";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                             "You have been invited to join project " + projectName);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(emailIntent, "Share project..."));
    }

    public boolean safeChangeStatus(final boolean status, Context context) {

        return safeModify(context, new Project.ModifyProject() {
            @Override
            public void modify() {

                setStatus(status);
                saveInBackground();
            }
        });
    }

    public boolean safeArchive(final boolean archive, Context context) {

        return safeModify(context, new Project.ModifyProject() {
            @Override
            public void modify() {

                setArchive(archive);
                setTrash(false);
                saveInBackground();
            }
        });
    }

    public boolean safeTrash(final boolean trash, Context context) {

        return safeModify(context, new Project.ModifyProject() {
            @Override
            public void modify() {

                setArchive(false);
                setTrash(trash);
                saveInBackground();
            }
        });
    }

    private interface ModifyProject {

        void modify();
    }

    private boolean safeModify(Context context, ModifyProject command) {

        if (isProjectAdminCurUser(ParseUser.getCurrentUser())) {
            command.modify();
            return true;
        }
        else {
            Toast.makeText(context,
                           "Only the administrator can make changes to the project.",
                           Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean isProjectAdminCurUser(ParseUser currentUser) {

        return getAdmin().getObjectId().equals(currentUser.getObjectId());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(getObjectId());
        parcel.writeString(getName());
        parcel.writeString(getUid());
        parcel.writeValue(getDueDate());
        parcel.writeString(getAdmin().getObjectId());
        parcel.writeString(getAdminName());
        parcel.writeString(getDescription());
        parcel.writeInt(getNumTotalTask());
        parcel.writeInt(getNumCompletedTasks());
        parcel.writeByte((byte) (getStatus() ? 1 : 0));
        parcel.writeString(getUserId().toString());
        parcel.writeString(getUserName().toString());
        parcel.writeString(getColor());
        parcel.writeByte((byte) (getArchive() ? 1 : 0));
        parcel.writeByte((byte) (getTrash() ? 1 : 0));
    }


    public static final Creator<Project> CREATOR = new Creator<Project>() {

        @Override
        public Project createFromParcel(Parcel source) {

            return new Project(source);
        }

        @Override
        public Project[] newArray(int size) {

            return new Project[size];
        }
    };

    private Project(Parcel source) {

        try {
            setObjectId(source.readString());
            setName(source.readString());
            setUid(source.readString());
            setDueDate((Date) source.readValue(Date.class.getClassLoader()));
            mAdminUid = source.readString();
            setAdminName(source.readString());
            setDescription(source.readString());
            setNumTotalTasks(source.readInt());
            setNumCompletedTasks(source.readInt());
            setStatus(source.readByte() != 0);
            String idString = source.readString();
            setUserIdArray(new JSONArray(idString));
            String nameString = source.readString();
            setUserNameArray(new JSONArray(nameString));
            setColor(source.readString());
            setArchive(source.readByte() != 0);
            setTrash(source.readByte() != 0);
        }
        catch (JSONException e) {
            //wont happen
        }
    }
}
