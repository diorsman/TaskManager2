package com.personal.taskmanager2.ui.signIn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.BaseDialogFragment;
import com.personal.taskmanager2.ui.homescreen.HomeScreenActivity;

public class CreateAccountFragment extends BaseDialogFragment
        implements View.OnClickListener {

    private EditText    mName;
    private EditText    mEmail;
    private EditText    mPassword;
    private ProgressBar mProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_create_account, container);

        rootView.findViewById(R.id.createNewAccount).setOnClickListener(this);

        mName = (EditText) rootView.findViewById(R.id.accountCreateName);
        mEmail = (EditText) rootView.findViewById(R.id.accountCreateEmail);
        mPassword = (EditText) rootView.findViewById(R.id.accountCreatePassword);
        mProgress = (ProgressBar) rootView.findViewById(R.id.accountCreateProgress);

        mProgress.setVisibility(View.INVISIBLE);

        return rootView;
    }

    public void onClick(View v) {
        // reset errors
        mName.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);

        //get values in fields
        String name = mName.getText().toString();
        String emailAddress = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        emailAddress = emailAddress.toLowerCase();

        // check if fields have valid values
        if (name.isEmpty()) {
            mName.setError("");
            return;
        }
        if (emailAddress.isEmpty()) {
            mEmail.setError("");
            return;
        }
        if (password.isEmpty()) {
            mPassword.setError("");
        }

        // create parse user
        ParseUser newUser = new ParseUser();
        newUser.setUsername(emailAddress);
        newUser.setPassword(password);
        newUser.setEmail(emailAddress);
        newUser.put("Name", name);

        //show progress bar
        mProgress.setVisibility(View.VISIBLE);
        mName.setVisibility(View.INVISIBLE);
        mPassword.setVisibility(View.INVISIBLE);
        mEmail.setVisibility(View.INVISIBLE);

        // sign user up
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {
                    mProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "Account Created!", Toast.LENGTH_SHORT).show();
                    getDialog().dismiss();
                    Intent intent = new Intent(getActivity(), HomeScreenActivity.class);
                    startActivity(intent);
                    //getActivity().finish();
                }
                else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    mProgress.setVisibility(View.INVISIBLE);
                    mName.setVisibility(View.VISIBLE);
                    mPassword.setVisibility(View.VISIBLE);
                    mEmail.setVisibility(View.VISIBLE);
                    mName.setError("");
                    mPassword.setError("");
                    mEmail.setError("");
                }
            }
        });
    }
}
