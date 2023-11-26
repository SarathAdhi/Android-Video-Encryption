package com.example.videoshare;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoshare.AES.Decryptor;
import com.example.videoshare.RSA.KeyStoreHelper;
import com.example.videoshare.RSA.VideoKeyManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ShowVideoFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = auth.getCurrentUser();

    private TextView uuidTextView;
    private TextView nameTextView;

    private Button downloadEncryptedTextButton;
    private Button downloadDecryptedVideoButton;
    private Button showDecryptKey;
    private Button shareVideo;


    private String uuid;
    private String name;
    private String key;

    final private String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

    ProgressDialog progressDialog;

    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    final ShareVideoFragment shareVideoFragment = new ShareVideoFragment();


    Timer timer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getContext());

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkDecryptBtnStatus();
                        }
                    });
                }
            }
        }, 0, 2000);


        Bundle bundle = getArguments();
        uuid = bundle.getString("uuid");

        DocumentReference docRef = db.collection("files").document(uuid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        name = document.getString("name");
                        nameTextView.setText(name);
//                        extension = name.substring(name.lastIndexOf('.') + 1);
                        key = document.getString("key");

                        SharedPreferences settings = getActivity().getSharedPreferences(KeyStoreHelper.PREFS_NAME, 0);
                        String myPrivateKey = settings.getString(KeyStoreHelper.PREFS_KEY_PRIVATE, "");

                        String decryptedKey = VideoKeyManager.decrypt(key, myPrivateKey);
System.out.println("--> " + key);
                        key = decryptedKey;
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_video, container, false);

        mFragmentManager = getActivity().getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        uuidTextView = (TextView) view.findViewById(R.id.videoUuid);
        uuidTextView.setText("UUID: " + uuid);

        nameTextView = (TextView) view.findViewById(R.id.videoName);

        shareVideo = (Button) view.findViewById(R.id.shareButton);
        if (shareVideo != null) {
            shareVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle mBundle = new Bundle();
                    mBundle.putString("uuid", uuid);
                    shareVideoFragment.setArguments(mBundle);

                    mFragmentTransaction
                            .replace(R.id.frameLayout, shareVideoFragment)
                            .addToBackStack(shareVideoFragment.getClass().getSimpleName() + uuid)
                            .commit();
                }
            });
        }

        showDecryptKey = (Button) view.findViewById(R.id.showDecryptKey);
        if (showDecryptKey != null) {
            showDecryptKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle("Decryption Key");
                    dialog.setMessage("KEY: " + key);

                    dialog.setPositiveButton("Okay",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                }
            });
        }

        downloadEncryptedTextButton = (Button) view.findViewById(R.id.downloadEncryptedTextButton);
        if (downloadEncryptedTextButton != null) {
            downloadEncryptedTextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    downloadEncryptedFile();

                    checkDecryptBtnStatus();
                }
            });
        }

        downloadDecryptedVideoButton = (Button) view.findViewById(R.id.downloadDecryptedVideoButton);
        if (downloadDecryptedVideoButton != null) {

            checkDecryptBtnStatus();

            downloadDecryptedVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    File myEncryptedFile = new File(downloadPath, uuid);

                    if (myEncryptedFile.exists()) {
                        Decryptor.decryption(myEncryptedFile, key, downloadPath, name);

                        Toast.makeText(getActivity(), "File decrypted and downloaded", Toast.LENGTH_SHORT).show();

                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle("Alert");
                        dialog.setMessage("Download the Encrypted file to Decrypt the video.");

                        dialog.setPositiveButton("Okay",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });


                        AlertDialog alertDialog = dialog.create();
                        alertDialog.show();

                    }

                }
            });
        }


        return view;
    }

    private void checkDecryptBtnStatus() {
        if (uuid != "" && downloadDecryptedVideoButton != null) {
            File myEncryptedFile = new File(downloadPath, uuid);
            downloadDecryptedVideoButton.setEnabled(myEncryptedFile.exists());

            if (myEncryptedFile.exists()) timer.cancel();

        }
    }

    private void downloadEncryptedFile() {
        StorageReference encryptedFileRef = storageRef.child(currentUser.getUid() + "/" + uuid);

        encryptedFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                System.out.println(uri.toString());
                downloadFile(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getActivity(), "Something went wrong while downloading", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void downloadFile(String downloadUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

        // Set the destination directory and file name for the downloaded file
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uuid);

        // Optionally, you can set other properties of the download request here
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(getContext().DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            // Enqueue the download request
            long downloadId = downloadManager.enqueue(request);

            Toast.makeText(getActivity(), "Download started", Toast.LENGTH_SHORT).show();
        } else {
            // Handle the case where DownloadManager is not available on the device
            Toast.makeText(getActivity(), "Download Manager is not available", Toast.LENGTH_SHORT).show();
        }
    }
}