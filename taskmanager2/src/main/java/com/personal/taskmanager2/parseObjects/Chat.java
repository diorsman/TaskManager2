package com.personal.taskmanager2.parseObjects;


import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

@ParseClassName("Chat")
public class Chat extends ParseObject {

    public static final String PROJECT_COL = "project";
    public static final String USER_NAME_COL = "UserName";
    public static final String USER_ID_COL = "UserId";

    public Project getProject() {
        return (Project) getParseObject(PROJECT_COL);
    }

    public void setProject(Project project) {
        put(PROJECT_COL, project);
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
        put(USER_ID_COL, userIdArray);
    }

    private void setUserIdArray(JSONArray array) {
        put(USER_ID_COL, array);
    }

    public JSONArray getUserId() {
        return getJSONArray(USER_ID_COL);
    }

    private void addUserName(ParseUser user) {
        JSONArray userNameArray = getUserName();
        if (userNameArray == null) {
            userNameArray = new JSONArray();
        }
        userNameArray.put(user.getString("Name"));
        put(USER_NAME_COL, userNameArray);
    }

    private void setUserNameArray(JSONArray array) {
        put(USER_NAME_COL, array);
    }

    public JSONArray getUserName() {
        return getJSONArray(USER_NAME_COL);
    }

    private void addEmptyUser() {
        JSONArray idArray = new JSONArray();
        JSONArray nameArray = new JSONArray();
        put(USER_ID_COL, idArray);
        put(USER_NAME_COL, nameArray);
    }
}
