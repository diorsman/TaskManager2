package com.personal.taskmanager2;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.personal.taskmanager2.parseObjects.Chat;
import com.personal.taskmanager2.parseObjects.Project;
import com.personal.taskmanager2.parseObjects.Task;

public class App extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        //init parse
        ParseObject.registerSubclass(Project.class);
        ParseObject.registerSubclass(Chat.class);
        ParseObject.registerSubclass(Task.class);
        Parse.initialize(this,
                         getString(R.string.parse_application_id),
                         getString(R.string.parse_client_key));
    }
}
