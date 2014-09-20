package com.personal.taskmanager2.signIn;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.homescreen.HomeScreenActivity;
import com.personal.taskmanager2.utilities.EditTextNoErrorMsg;

public class SignInFragment extends Fragment implements View.OnClickListener {

    private EditTextNoErrorMsg mEmailView;
    private EditTextNoErrorMsg mPasswordView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =
                inflater.inflate(R.layout.fragment_sign_in, container, false);

        /*int[] resources =
                {R.id.signIn, R.id.createAccount, R.id.forgotPassword};
        for (int i : resources) {
            rootView.findViewById(i).setOnClickListener(this);
        }*/
        rootView.findViewById(R.id.signIn).setOnClickListener(this);
        rootView.findViewById(R.id.createAccount).setOnClickListener(this);
        rootView.findViewById(R.id.forgotPassword).setOnClickListener(this);

        // get views
        mEmailView = (EditTextNoErrorMsg) rootView.findViewById(R.id.emailTextBox);
        mPasswordView = (EditTextNoErrorMsg) rootView.findViewById(R.id.passwordTextBox);

        return rootView;
    }

    @Override
    public void onStop() {

        super.onStop();
        mEmailView.requestFocus();
        mEmailView.setText(null);
        mPasswordView.setText(null);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.signIn:

                // show progress dialog
                final ProgressDialog progress = new ProgressDialog(view.getContext());
                progress.setTitle("Logging In");
                progress.setMessage("Please Wait...");
                progress.show();

                // set error to null
                mEmailView.setError(null);
                mPasswordView.setError(null);

                String emailAddress = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                emailAddress = emailAddress.toLowerCase();

                // error checking
                if (emailAddress.isEmpty()) {
                    progress.dismiss();
                    mEmailView.setError("");
                    return;
                }
                if (password.isEmpty()) {
                    progress.dismiss();
                    mPasswordView.setError("");
                    return;
                }

                //log in
                ParseUser.logInInBackground(emailAddress,
                                            password,
                                            new LogInCallback() {
                                                @Override
                                                public void done(ParseUser parseUser,
                                                                 ParseException e) {

                                                    if (parseUser != null) {
                                                        progress.dismiss();
                                                        Intent intent = new Intent(getActivity(),
                                                                                   HomeScreenActivity.class);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    }
                                                    else {
                                                        progress.dismiss();
                                                        Toast.makeText(getActivity(),
                                                                       e.getMessage(),
                                                                       Toast.LENGTH_LONG).show();
                                                        mEmailView.setError("");
                                                        mPasswordView.setError("");
                                                    }
                                                }
                                            });
                break;
            case R.id.createAccount:
                CreateAccountFragment cf = new CreateAccountFragment();
                cf.show(getFragmentManager(), "fragment_create_account");
                break;
            case R.id.forgotPassword:
                ForgotPasswordFragment fp = new ForgotPasswordFragment();
                fp.show(getFragmentManager(), "fragment_forgot_password");
                break;
        }
    }
}
