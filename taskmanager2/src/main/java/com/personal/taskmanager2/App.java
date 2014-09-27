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
                         "GxIcNKHwyZWMT7vSNe74lLbeNpEcQlTwo4Zjd33y",
                         "Vo2llHgiGZAMMymFGJZr08cX1ma4WR6LdTcpcs9V");
    }
}
