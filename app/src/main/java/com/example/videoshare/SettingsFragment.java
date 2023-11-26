package com.example.videoshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoshare.RSA.KeyStoreHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private View mView;
    private Button logoutButton;
    private TextView publicKeyTextView;
    private TextView privateKeyTextView;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        logoutButton = (Button) mView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);

        String myPublicKey = KeyStoreHelper.getPublicKey(getContext());
        String myPrivateKey = KeyStoreHelper.getPrivateKey(getContext());

        publicKeyTextView = (TextView) mView.findViewById(R.id.publicKey);
        privateKeyTextView = (TextView) mView.findViewById(R.id.privateKey);


        if(publicKeyTextView != null)
            publicKeyTextView.setText("Public Key: " + myPublicKey);

        if(privateKeyTextView != null)
            privateKeyTextView.setText("Private Key: " + myPrivateKey);

        return mView;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutButton:
                progressDialog.setTitle("Logout");
                progressDialog.setMessage("We are logging out your account");

                progressDialog.show();

                final Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        FirebaseAuth.getInstance().signOut();
                        progressDialog.cancel();

                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);

                        t.cancel();
                    }
                }, 2000);

                break;
        }
    }
}