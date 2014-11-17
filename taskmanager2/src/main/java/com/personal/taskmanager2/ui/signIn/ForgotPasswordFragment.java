package com.personal.taskmanager2.ui.signIn;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.BaseDialogFragment;

public class ForgotPasswordFragment extends BaseDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle(R.string.forgot_password);
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container);
        return rootView;
    }
}
