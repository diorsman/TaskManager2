package com.personal.taskmanager2.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;

public class AccountSettingsFragment extends android.app.Fragment implements View.OnClickListener {

    private static final String TAG = "AccountSettingsFragment";

    private EditText mName;
    private EditText mPhone;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mConfirmPassword;
    private final ParseUser mCurrentUser = ParseUser.getCurrentUser();

    public static AccountSettingsFragment newInstance() {

        AccountSettingsFragment frag = new AccountSettingsFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_settings,
                                         container,
                                         false);

        rootView.findViewById(R.id.account_settings_save_changes_btn)
                .setOnClickListener(this);

        mName = (EditText) rootView.findViewById(R.id.account_settings_edit_name);
        mPhone = (EditText) rootView.findViewById(R.id.account_settings_edit_phone);
        mOldPassword = (EditText) rootView.findViewById(R.id.account_settings_edit_old_password);
        mNewPassword = (EditText) rootView.findViewById(R.id.account_settings_edit_new_password);
        mConfirmPassword =
                (EditText) rootView.findViewById(R.id.account_settings_edit_confirm_password);

        //place current values in
        mName.setText(mCurrentUser.getString("Name"));
        mPhone.setText(mCurrentUser.getString("Phone"));

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.findViewById(R.id.actionbar_spinner).setVisibility(View.GONE);
        toolbar.getMenu().clear();
        ((ActionBarActivity) getActivity()).getSupportActionBar()
                                           .setDisplayShowTitleEnabled(true);
        toolbar.setTitle(getString(R.string.account_settings_title));
    }

    @Override
    public void onClick(View v) {

        clearErrors();

        final String name = mName.getText().toString();
        final String phone = mPhone.getText().toString();
        final String old_pass = mOldPassword.getText().toString();
        final String new_pass = mNewPassword.getText().toString();
        final String confirm_pass = mConfirmPassword.getText().toString();

        //make sure old password is typed in
        if (old_pass.isEmpty()) {
            mOldPassword.setError("Can't leave blank!");
            mOldPassword.requestFocus();
            return;
        }

        if (!confirm_pass.equals(new_pass)) {
            mConfirmPassword.setError("Does not match!");
            mConfirmPassword.requestFocus();
            return;
        }

        ParseUser.logInInBackground(mCurrentUser.getUsername(),
                                    old_pass,
                                    new LogInCallback() {
                                        public void done(ParseUser user,
                                                         ParseException e) {

                                            if (user != null) {
                                                //user is logged in, save new values
                                                if (!name.isEmpty()) {
                                                    user.put("Name", name);
                                                }

                                                if (!phone.isEmpty()) {
                                                    user.put("Phone", phone);
                                                }

                                                if (!new_pass.isEmpty()) {
                                                    user.setPassword(new_pass);
                                                }

                                                user.saveInBackground();
                                                Toast.makeText(getActivity().getApplicationContext(),
                                                               "Changes saved!",
                                                               Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(),
                                                                           HomeScreenActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }

                                            else {
                                                //login failed, output message
                                                Toast.makeText(getActivity().getApplicationContext(),
                                                               e.getMessage(),
                                                               Toast.LENGTH_LONG).show();
                                                mOldPassword.requestFocus();
                                                clearErrors();
                                            }
                                        }
                                    });
    }

    private void clearErrors() {

        mName.setError(null);
        mPhone.setError(null);
        mOldPassword.setError(null);
        mNewPassword.setError(null);
        mConfirmPassword.setError(null);
    }

}
