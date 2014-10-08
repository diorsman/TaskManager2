package com.personal.taskmanager2.homescreen.AddProjects;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.DatePickerFragment;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.TimePickerFragment;
import com.personal.taskmanager2.parseObjects.Project;
import com.personal.taskmanager2.utilities.EmptyEditTextException;
import com.personal.taskmanager2.utilities.EditTextNoErrorMsg;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class CreateProjectFragment extends DialogFragment
        implements View.OnClickListener,
                   DatePickerDialog.OnDateSetListener,
                   TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "CreateProjectFragment";

    private static final int BLANK_EDIT_TEXT    = 0;
    private static final int UID_EXISTS         = 1;
    private static final int CREATE_PROJECT     = 2;
    private static final int EXCEPTION_OCCURRED = 3;

    private EditTextNoErrorMsg mNameView;
    private EditTextNoErrorMsg mUidView;
    private EditTextNoErrorMsg mPasswordView;
    private Calendar           mCalendar;
    private Button             mTimeButton;
    private Button             mDateButton;
    private ViewSwitcher       mViewSwitcher;

    public static CreateProjectFragment newInstance() {

        CreateProjectFragment frag = new CreateProjectFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.create_project_title);
        View rootView = inflater.inflate(R.layout.fragment_create_project, container, false);

        mNameView = (EditTextNoErrorMsg) rootView.findViewById(R.id.project_name);
        mUidView = (EditTextNoErrorMsg) rootView.findViewById(R.id.project_uid);
        mPasswordView = (EditTextNoErrorMsg) rootView.findViewById(R.id.project_password);
        mDateButton = (Button) rootView.findViewById(R.id.project_due_date);
        mTimeButton = (Button) rootView.findViewById(R.id.project_due_time);
        Button submitButton = (Button) rootView.findViewById(R.id.create_project);
        Button cancelButton = (Button) rootView.findViewById(R.id.cancel);
        mViewSwitcher = (ViewSwitcher) rootView.findViewById(R.id.view_switcher);

        mDateButton.setOnClickListener(this);
        mTimeButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        mCalendar = Calendar.getInstance();

        DateFormat date = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        mDateButton.setText("Due on " + date.format(mCalendar.getTime()));
        mTimeButton.setText("Due at " + time.format(mCalendar.getTime()));

        mViewSwitcher.setDisplayedChild(0);

        return rootView;
    }

    @Override
    public void onDateSet(DatePicker view,
                          int year,
                          int monthOfYear,
                          int dayOfMonth) {

        mCalendar.set(year, monthOfYear, dayOfMonth);
        DateFormat date = DateFormat.getDateInstance(DateFormat.SHORT);
        mDateButton.setText("Due on " + date.format(mCalendar.getTime()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        mTimeButton.setText("Due at " + time.format(mCalendar.getTime()));
    }

    @Override
    public void onClick(View v) {

        DialogFragment dialogFragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.project_due_date:
                dialogFragment = new DatePickerFragment();
                dialogFragment.setTargetFragment(this, 1);
                dialogFragment.show(ft, "date picker");
                break;

            case R.id.project_due_time:
                dialogFragment = new TimePickerFragment();
                dialogFragment.setTargetFragment(this, 2);
                dialogFragment.show(ft, "time picker");
                break;

            case R.id.create_project:
                createProject();
                break;
            case R.id.cancel:
                dismiss();
                break;
        }
    }

    private void createProject() {

        clearError();
        mViewSwitcher.setDisplayedChild(1);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case BLANK_EDIT_TEXT:
                        EditTextNoErrorMsg view = (EditTextNoErrorMsg) msg.obj;
                        mViewSwitcher.setDisplayedChild(0);
                        view.setError("");
                        break;

                    case UID_EXISTS:
                        mViewSwitcher.setDisplayedChild(0);
                        mUidView.setError("");
                        Toast.makeText(getActivity(),
                                       "Error. UID already exists.",
                                       Toast.LENGTH_LONG).show();
                        break;

                    case CREATE_PROJECT:
                        Toast.makeText(getActivity(), "Project Created!", Toast.LENGTH_SHORT)
                             .show();
                        getDialog().dismiss();
                        Utilities.refreshFragment(getFragmentManager());
                        break;

                    case EXCEPTION_OCCURRED:
                        Exception e = (Exception) msg.obj;
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        mViewSwitcher.setDisplayedChild(0);
                        break;
                }
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                createProjectInBackground(handler);
            }
        };

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(runnable);
    }

    private void createProjectInBackground(Handler handler) {

        try {
            final String name = Utilities.getValFromEditText(mNameView);
            final String uid = Utilities.getValFromEditText(mUidView);
            final String password = Utilities.getValFromEditText(mPasswordView);

            final Date date = mCalendar.getTime();

            ParseQuery<Project> query = ParseQuery.getQuery(Project.class);
            query.whereEqualTo("Uid", uid);
            int numObject = query.count();
            switch (numObject) {
                case 0:
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    String adminName = (String) currentUser.get("Name");
                    Project newProject =
                            new Project(name, uid, password, date, currentUser, adminName);
                    newProject.save();
                    Message message = handler.obtainMessage(CREATE_PROJECT);
                    handler.sendMessage(message);
                    break;
                default:
                    Message msg = handler.obtainMessage(UID_EXISTS);
                    handler.sendMessage(msg);
            }
        }
        catch (EmptyEditTextException e) {
            Message msg = handler.obtainMessage(BLANK_EDIT_TEXT, e.getEditText());
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            Message msg = handler.obtainMessage(EXCEPTION_OCCURRED, e);
            handler.sendMessage(msg);
        }
    }

    private void clearError() {

        mNameView.setError(null);
        mUidView.setError(null);
        mPasswordView.setError(null);
    }
}
