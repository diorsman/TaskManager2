package com.personal.taskmanager2.signIn;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.personal.taskmanager2.R;

public class ForgotPasswordFragment extends DialogFragment {

    public ForgotPasswordFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.forgot_password);
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container);
        return rootView;
    }
}
