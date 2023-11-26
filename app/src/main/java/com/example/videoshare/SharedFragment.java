package com.example.videoshare;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.videoshare.AES.Encryptor;
import com.example.videoshare.ListAdapters.FileListAdapter;
import com.example.videoshare.ListItems.FileListItem;
import com.example.videoshare.RSA.KeyStoreHelper;
import com.example.videoshare.RSA.VideoKeyManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SharedFragment extends Fragment {

    private View mView;
    private SwipeRefreshLayout pullToRefresh;

    private FirebaseUser currentUser;
    private FirebaseAuth auth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static Context thisContext;

    private ListView listView;
    private FileListAdapter adapter;

    private List<FileListItem> dataList = new ArrayList<>();

    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    final ShowSharedVideoFragment showVideoFragment = new ShowSharedVideoFragment();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        adapter = new FileListAdapter(getContext(), dataList);

        fetchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisContext = container.getContext();
        mFragmentManager = getActivity().getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        mView = inflater.inflate(R.layout.fragment_shared, container, false);

        pullToRefresh = mView.findViewById(R.id.pullToRefresh);
        if (pullToRefresh != null) {
            pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    fetchData();
                    pullToRefresh.setRefreshing(false);
                }
            });
        }



        listView = mView.findViewById(R.id.sharedFilesListView);
        if (listView != null) {
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FileListItem selectedItem = dataList.get(position);
                    String name = selectedItem.getName();
                    String uuid = selectedItem.getUuid();

                    if (uuid != "") {
                        Bundle mBundle = new Bundle();
                        mBundle.putString("uuid", uuid);
                        showVideoFragment.setArguments(mBundle);

                        mFragmentTransaction
                                .replace(R.id.frameLayout, showVideoFragment)
                                .addToBackStack(uuid)
                                .commit();
                    }
                }
            });
        }

        return mView;
    }



    private void fetchData() {
        dataList.clear();

        db.collection("shared")
                .whereEqualTo("email", currentUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (task.getResult().isEmpty()) {
                                dataList.add(new FileListItem("No file : (", ""));
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String itemUuid = document.getString("fileUuid");

                                System.out.println(itemUuid);

                                db.collection("files")
                                        .document(itemUuid)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();

                                                    String itemName = document.getString("name");
                                                    String itemUuid = document.getString("uuid");

                                                    dataList.add(new FileListItem(itemName, itemUuid));

                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });

                            }

                            // Notify the adapter that the data has changed
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Error while getting documents", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}