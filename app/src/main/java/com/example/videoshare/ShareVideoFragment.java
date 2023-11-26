package com.example.videoshare;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videoshare.JWebToken.JWebToken;
import com.example.videoshare.ListAdapters.UserListAdapter;
import com.example.videoshare.ListItems.FileListItem;
import com.example.videoshare.ListItems.UserListItem;
import com.example.videoshare.RSA.KeyStoreHelper;
import com.example.videoshare.RSA.VideoKeyManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

interface AssignmentCallback {
    void onAssignmentChecked(boolean isAssigned);
}

public class ShareVideoFragment extends Fragment {

    private View mView;
    private SwipeRefreshLayout pullToRefresh;


    private FirebaseUser currentUser;
    private FirebaseAuth auth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static Context thisContext;

    private String uuid;

    private ListView listView;
    private UserListAdapter adapter;

    private List<UserListItem> dataList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        Bundle bundle = getArguments();
        uuid = bundle.getString("uuid");

        adapter = new UserListAdapter(getContext(), dataList, uuid);

        fetchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_share_video, container, false);

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


        listView = mView.findViewById(R.id.usersListView);

        if (listView != null) {
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listView.setEnabled(false);

                    UserListItem listItem = dataList.get(position);

                    if (listItem.getId() != null) {
                        Dialog dialog = new Dialog(getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(true);
                        dialog.setContentView(R.layout.custom_user_assign_dialog);


                        TextView userEmail = dialog.findViewById(R.id.userEmail);
                        userEmail.setText(listItem.getEmail());

                        EditText userAccessTime = dialog.findViewById(R.id.userAccessTime);

                        Button assignFileButton = dialog.findViewById(R.id.assignFileButton);
                        assignFileButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String timeString = userAccessTime.getText().toString();
                                System.out.println(timeString);


                                try {
                                    int time = Integer.parseInt(timeString);
                                    System.out.println(time);

                                    JSONObject jwtPayload = new JSONObject();
                                    jwtPayload.put("status", 0);

                                    JSONArray audArray = new JSONArray();
                                    audArray.put("admin");
                                    jwtPayload.put("sub", listItem.getEmail());

                                    jwtPayload.put("aud", audArray);
                                    LocalDateTime ldt = LocalDateTime.now().plusMinutes(time);
                                    jwtPayload.put("exp", ldt.toEpochSecond(ZoneOffset.UTC)); //this needs to be configured


                                    try {

                                        String token = new JWebToken(jwtPayload).toString();


                                        System.out.println(token);

                                        Map<String, Object> fileDetails = new HashMap<>();
                                        fileDetails.put("fileUuid", uuid);
                                        fileDetails.put("email", listItem.getEmail());
                                        fileDetails.put("isAccessEnabled", true);
                                        fileDetails.put("token", token);

                                        db.collection("files").document(uuid)
                                                .get()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();

                                                        String _encryptedKey = document.getString("key");

                                                        String decryptOrgKey = VideoKeyManager.decrypt(_encryptedKey, KeyStoreHelper.getPrivateKey(getContext()));

                                                        db.collection("users").document(listItem.getId())
                                                                .get()
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        DocumentSnapshot document1 = task1.getResult();

                                                                        String publicKey = document1.getString("publicKey");

                                                                        String encryptedKey = VideoKeyManager.encrypt(decryptOrgKey, publicKey);
                                                                        System.out.println("-->" + encryptedKey);


                                                                        fileDetails.put("key", encryptedKey);

                                                                        String newDocumentId = db.collection("shared").document().getId();

                                                                        db.collection("shared").document(newDocumentId)
                                                                                .set(fileDetails)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        try {
                                                                                            fetchData();
                                                                                        } catch (Exception e) {
                                                                                            e.printStackTrace();
                                                                                        }

                                                                                        Toast.makeText(getContext(), "User assigned", Toast.LENGTH_LONG).show();
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Toast.makeText(getContext(), "Something went wrong 1", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });


                                                                    } else {
                                                                        Toast.makeText(getContext(), "Email doesnt exist", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    } else {
                                                        Toast.makeText(getContext(), "Error while getting documents", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Fill the time limit", Toast.LENGTH_SHORT).show();

                                }


                            }
                        });


                        if (listItem.getIsAssigned()) {
                            db.collection("shared")
                                    .whereEqualTo("email", listItem.getEmail())
                                    .whereEqualTo("fileUuid", uuid)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {


                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String doc_id = document.getId();

                                                db.collection("shared").document(doc_id)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                fetchData();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                            }
                                                        });

                                            }

                                        }
                                    });
//
                        } else {

                            dialog.show();


                        }


                        listView.setEnabled(true);
                    }
                }

            });
        }


        return mView;
    }

    public Void fetchData() {
        dataList.clear();

        db.collection("users")
                .whereNotEqualTo("email", currentUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (task.getResult().isEmpty()) {
                                dataList.add(new UserListItem(null, "No Registered User", false));
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String email = document.getString("email");

                                getIsUserAssigned(email, new AssignmentCallback() {
                                    @Override
                                    public void onAssignmentChecked(boolean isAssigned) {
                                        dataList.add(new UserListItem(document.getId(), email, isAssigned));

                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                });
        return null;
    }


    private void getIsUserAssigned(String email, AssignmentCallback callback) {

        db.collection("shared")
                .whereEqualTo("fileUuid", uuid)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean isAssigned = !task.getResult().isEmpty();

//                     asynchronous
                        callback.onAssignmentChecked(isAssigned);
                    }
                });

    }
}