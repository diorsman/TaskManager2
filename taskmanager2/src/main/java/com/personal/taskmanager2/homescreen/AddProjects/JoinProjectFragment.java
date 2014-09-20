package com.personal.taskmanager2.homescreen.AddProjects;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.parseObjects.Project;
import com.personal.taskmanager2.utilities.BCrypt;
import com.personal.taskmanager2.utilities.EmptyEditTextException;
import com.personal.taskmanager2.utilities.EditTextNoErrorMsg;
import com.personal.taskmanager2.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JoinProjectFragment extends DialogFragment
        implements View.OnClickListener {

    private static final String TAG = "JoinProjectFragment";

    private static final int BLANK_EDIT_TEXT        = 0;
    private static final int PROJECT_NOT_FOUND      = 1;
    private static final int JOIN_PROJECT           = 2;
    private static final int EXCEPTION_OCCURRED     = 3;
    private static final int ALREADY_JOINED_PROJECT = 4;

    private ViewSwitcher       mViewSwitcher;
    private EditTextNoErrorMsg mUidView;
    private EditTextNoErrorMsg mPasswordView;


    public static JoinProjectFragment newInstance() {

        return new JoinProjectFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.join_project_title);
        View rootView = inflater.inflate(R.layout.fragment_join_project, container, false);

        mViewSwitcher = (ViewSwitcher) rootView.findViewById(R.id.view_switcher);
        mUidView = (EditTextNoErrorMsg) rootView.findViewById(R.id.joinProjectUID);
        mPasswordView = (EditTextNoErrorMsg) rootView.findViewById(R.id.joinProjectPassword);
        Button join = (Button) rootView.findViewById(R.id.join_project);
        Button cancel = (Button) rootView.findViewById(R.id.cancel);

        join.setOnClickListener(this);
        cancel.setOnClickListener(this);

        mViewSwitcher.setDisplayedChild(0);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.join_project:
                joinProject();
                break;
        }
    }

    private void joinProject() {

        clearError();
        mViewSwitcher.setDisplayedChild(1);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    //Edit text is empty
                    case BLANK_EDIT_TEXT:
                        EditTextNoErrorMsg view = (EditTextNoErrorMsg) msg.obj;
                        mViewSwitcher.setDisplayedChild(0);
                        view.setError("");
                        break;

                    // project not found
                    case PROJECT_NOT_FOUND:
                        mViewSwitcher.setDisplayedChild(0);
                        mUidView.setError("");
                        Toast.makeText(getActivity(),
                                       "Project Not Found. Please Try Again.",
                                       Toast.LENGTH_LONG).show();
                        break;

                    // Join Project
                    case JOIN_PROJECT:
                        dismiss();
                        Toast.makeText(getActivity(), "Joined Project", Toast.LENGTH_LONG).show();
                        Utilities.refreshFragment(getFragmentManager());
                        break;

                    //Exception Occurred
                    case EXCEPTION_OCCURRED:
                        Exception e = (Exception) msg.obj;
                        mViewSwitcher.setDisplayedChild(0);
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        break;

                    //User has already joined project
                    case ALREADY_JOINED_PROJECT:
                        mViewSwitcher.setDisplayedChild(0);
                        Toast.makeText(getActivity(),
                                       "You have already joined this project",
                                       Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                joinProjectInBackground(handler);
            }
        };

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(runnable);
    }

    private void joinProjectInBackground(Handler handler) {

        try {
            String uid = Utilities.getValFromEditText(mUidView);
            String password = Utilities.getValFromEditText(mPasswordView);

            ParseQuery<Project> projectQuery = new ParseQuery<>(Project.class);
            projectQuery.whereEqualTo("Uid", uid);
            Project project = projectQuery.getFirst();

            if (checkIfUserHasJoinedProject(project)) {
                Message msg = handler.obtainMessage(ALREADY_JOINED_PROJECT);
                handler.sendMessage(msg);
                return;
            }

            if (BCrypt.checkpw(password, project.getPassword())) {
                project.addUser(ParseUser.getCurrentUser());
                project.save();
                Message msg = handler.obtainMessage(JOIN_PROJECT);
                handler.sendMessage(msg);
            }
            else {
                Message msg = handler.obtainMessage(PROJECT_NOT_FOUND);
                handler.sendMessage(msg);
            }
        }
        catch (EmptyEditTextException e) {
            Message msg = handler.obtainMessage(0, e.getEditText());
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                Message msg = handler.obtainMessage(PROJECT_NOT_FOUND);
                handler.sendMessage(msg);
            }
            else {
                Message msg = handler.obtainMessage(EXCEPTION_OCCURRED, e);
                handler.sendMessage(msg);
            }
        }
    }

    private boolean checkIfUserHasJoinedProject(Project project) {

        try {
            if (ParseUser.getCurrentUser()
                         .getObjectId()
                         .equals(project.getAdmin().getObjectId())) {
                return true;
            }
            JSONArray users = project.getUserId();
            for (int i = 0; i < users.length(); i++) {
                String objectId = users.getString(i);
                return ParseUser.getCurrentUser().getObjectId().equals(objectId);
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }
        return false;
    }

    private void clearError() {

        mUidView.setError(null);
        mPasswordView.setError(null);
    }
}
