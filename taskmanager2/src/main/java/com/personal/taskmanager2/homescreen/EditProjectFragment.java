package com.personal.taskmanager2.homescreen;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.parse.ParseException;
import com.personal.taskmanager2.DatePickerFragment;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.TimePickerFragment;
import com.personal.taskmanager2.adapters.CategoryAdapter;
import com.personal.taskmanager2.parseObjects.Project;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EditProjectFragment extends android.app.Fragment
        implements View.OnClickListener,
                   DatePickerDialog.OnDateSetListener,
                   TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "EditProjectFragment";

    private final static List<String> colorValues =
            Arrays.asList("Blue", "Orange", "Yellow", "Green", "Red", "Purple");

    private Project mProject;

    private ViewSwitcher mMainViewSwitcher;
    private TextView     mProjectNameLabel;
    private TextView     mProjectDescriptionLabel;
    private EditText     mProjectName;
    private EditText     mProjectPassword;
    private EditText     mProjectDescription;
    private Button       mDateButton;
    private Button       mTimeButton;
    private Spinner      mSpinner;
    private Calendar     mCalendar;

    public static EditProjectFragment newInstance(Project project) {

        Bundle args = new Bundle();
        args.putParcelable("project", project);

        EditProjectFragment frag = new EditProjectFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mProject = getArguments().getParcelable("project");
        setUpActionBar();
        setHasOptionsMenu(true);
    }

    private void setUpActionBar() {
        // set nav mode
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // set title
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle("Editing Project");
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(mProject.getColorRsrc())));
    }

    private ActionBar getActionBar() {

        return getActivity().getActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_edit_project, container, false);

        mMainViewSwitcher = (ViewSwitcher) rootView.findViewById(R.id.main_view_switcher);
        mProjectNameLabel = (TextView) rootView.findViewById(R.id.edit_project_name_actual);
        mProjectDescriptionLabel =
                (TextView) rootView.findViewById(R.id.edit_project_description_actual);
        mDateButton = (Button) rootView.findViewById(R.id.editProjectDueDateButton);
        mTimeButton = (Button) rootView.findViewById(R.id.editProjectTimeButton);
        mProjectName = (EditText) rootView.findViewById(R.id.edit_project_name);
        mProjectDescription = (EditText) rootView.findViewById(R.id.edit_project_description);
        mProjectPassword = (EditText) rootView.findViewById(R.id.edit_project_password);
        mSpinner = (Spinner) rootView.findViewById(R.id.edit_project_category);

        mMainViewSwitcher.setDisplayedChild(0);

        //set click listeners
        mProjectNameLabel.setOnClickListener(this);
        mProjectDescriptionLabel.setOnClickListener(this);
        rootView.findViewById(R.id.edit_project_password_label).setOnClickListener(this);
        mDateButton.setOnClickListener(this);
        mTimeButton.setOnClickListener(this);

        DateFormat date = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        mDateButton.setText("Due on " + date.format(mProject.getDueDate()));
        mTimeButton.setText("Due at " + time.format(mProject.getDueDate()));

        mProjectNameLabel.setText(mProject.getName());

        String description = mProject.getDescription();
        if (!description.isEmpty()) {
            mProjectDescriptionLabel.setText(mProject.getDescription());
        }
        else {
            mProjectDescriptionLabel.setText("There is no description for this project!");
        }

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(mProject.getDueDate());

        // set up spinner
        ArrayAdapter<String> adapter = new CategoryAdapter(getActivity());
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(colorValues.indexOf(mProject.getColor()));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.edit_project, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            case R.id.action_accept:
                acceptChanges();
                return true;
            case R.id.action_cancel:
                resetViews();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void acceptChanges() {

        mMainViewSwitcher.setDisplayedChild(1);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 1:
                        Toast.makeText(getActivity(), "Project Updated!", Toast.LENGTH_LONG).show();
                        getFragmentManager().popBackStack();
                        break;
                    case 0:
                        Toast.makeText(getActivity(),
                                       "Error Occurred. Please Try Again",
                                       Toast.LENGTH_LONG).show();
                        resetViews();
                }
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    String name = mProjectName.getText().toString();
                    String description = mProjectDescription.getText().toString();
                    String password = mProjectPassword.getText().toString();
                    String category = mSpinner.getSelectedItem().toString();
                    Date newDate = mCalendar.getTime();

                    if (!name.isEmpty()) {
                        mProject.setName(name);
                    }
                    if (!description.isEmpty()) {
                        mProject.setDescription(description);
                    }
                    if (!password.isEmpty()) {
                        mProject.setPassword(password);
                    }
                    if (!newDate.equals(mProject.getDueDate())) {
                        mProject.setDueDate(newDate);
                    }

                    mProject.setColor(category);
                    mProject.save();
                    handler.sendEmptyMessage(1);
                }
                catch (ParseException e) {
                    Log.e(TAG, "ParseException", e);
                    handler.sendEmptyMessage(0);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void resetViews() {

        ViewSwitcher switcher;
        switcher = (ViewSwitcher) getView().findViewById(R.id.edit_project_name_view);
        switcher.setDisplayedChild(0);
        switcher = (ViewSwitcher) getView().findViewById(R.id.edit_project_password_view);
        switcher.setDisplayedChild(0);
        switcher = (ViewSwitcher) getView().findViewById(R.id.edit_project_description_view);
        switcher.setDisplayedChild(0);
        DateFormat date = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);
        mDateButton.setText("Due on " + date.format(mProject.getDueDate()));
        mTimeButton.setText("Due at " + time.format(mProject.getDueDate()));
        mSpinner.setSelection(colorValues.indexOf(mProject.getColor()));
    }

    @Override
    public void onClick(View v) {

        ViewSwitcher switcher;
        DialogFragment dialogFragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.edit_project_name_actual:
                switcher = (ViewSwitcher) getView().findViewById(R.id.edit_project_name_view);
                switcher.showNext();
                break;
            case R.id.edit_project_password_label:
                switcher = (ViewSwitcher) getView().findViewById(R.id.edit_project_password_view);
                switcher.showNext();
                break;
            case R.id.edit_project_description_actual:
                switcher =
                        (ViewSwitcher) getView().findViewById(R.id.edit_project_description_view);
                switcher.showNext();
                break;
            case R.id.editProjectDueDateButton:
                dialogFragment = new DatePickerFragment();
                dialogFragment.setTargetFragment(this, 1);
                dialogFragment.show(ft, "date picker");
                break;
            case R.id.editProjectTimeButton:
                dialogFragment = new TimePickerFragment();
                dialogFragment.setTargetFragment(this, 2);
                dialogFragment.show(ft, "time picker");
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

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
}
