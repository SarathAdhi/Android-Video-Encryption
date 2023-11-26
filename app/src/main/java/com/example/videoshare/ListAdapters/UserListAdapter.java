package com.example.videoshare.ListAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.videoshare.ListItems.FileListItem;
import com.example.videoshare.ListItems.UserListItem;
import com.example.videoshare.R;
import com.example.videoshare.RSA.KeyStoreHelper;
import com.example.videoshare.RSA.VideoKeyManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class UserListAdapter extends BaseAdapter {
    private Context context;
    private List<UserListItem> itemList;
    private String uuid;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public UserListAdapter(Context context, List<UserListItem> itemList, String uuid) {
        this.context = context;
        this.itemList = itemList;
        this.uuid = uuid;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.data_list_user, parent, false);
        }

        UserListItem listItem = itemList.get(position);

        // Bind data to the custom layout
//        CheckBox checkBoxTextView = convertView.findViewById(R.id.checkbox_is_user_assigned);
//
//        checkBoxTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(listItem.getIsAssigned()) {
//                    db.collection("shared")
//                                .whereEqualTo("email", listItem.getEmail())
//                                .whereEqualTo("fileUuid", uuid)
//                                .get()
//                                .addOnCompleteListener(task -> {
//                                    if (task.isSuccessful()) {
//
//
//                                        for (QueryDocumentSnapshot document : task.getResult()) {
//                                            String id = document.getId();
//
//                                            db.collection("shared").document(id)
//                                                    .delete()
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                        }
//                                                    })
//                                                    .addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                        }
//                                                    });
//
//                                        }
//
//                                    }
//                                });
//                }
//
//                else {
//                        Map<String, Object> fileDetails = new HashMap<>();
//                        fileDetails.put("fileUuid", uuid);
//                        fileDetails.put("email", listItem.getEmail());
//                        fileDetails.put("isAccessEnabled", listItem.getIsAssigned());
//
//                        db.collection("files").document(uuid)
//                                .get()
//                                .addOnCompleteListener(task -> {
//                                    if (task.isSuccessful()) {
//                                        DocumentSnapshot document = task.getResult();
//
//                                        String _encryptedKey = document.getString("key");
//
//                                        String decryptOrgKey = VideoKeyManager.decrypt(_encryptedKey, KeyStoreHelper.getPrivateKey(context));
//
//                                        db.collection("users").document(listItem.getId())
//                                                .get()
//                                                .addOnCompleteListener(task1 -> {
//                                                    if (task1.isSuccessful()) {
//                                                        DocumentSnapshot document1 = task1.getResult();
//
//                                                        String publicKey = document1.getString("publicKey");
//
//                                                        String encryptedKey = VideoKeyManager.encrypt(decryptOrgKey, publicKey);
//                                                        System.out.println("-->" + encryptedKey);
//
//
//                                                        fileDetails.put("key", encryptedKey);
//
//                                                        String newDocumentId = db.collection("shared").document().getId();
//
//                                                        db.collection("shared").document(newDocumentId)
//                                                                .set(fileDetails)
//                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                    @Override
//                                                                    public void onSuccess(Void aVoid) {
//                                                                        try {
//                                                                            fetchData.call();
//                                                                        } catch (Exception e) {
//                                                                            e.printStackTrace();
//                                                                        }
//
//                                                                        Toast.makeText(context, "User assigned", Toast.LENGTH_LONG).show();
//                                                                    }
//                                                                })
//                                                                .addOnFailureListener(new OnFailureListener() {
//                                                                    @Override
//                                                                    public void onFailure(@NonNull Exception e) {
//                                                                        Toast.makeText(context, "Something went wrong 1", Toast.LENGTH_SHORT).show();
//                                                                    }
//                                                                });
//
//
//                                                    } else {
//                                                        Toast.makeText(context, "Email doesnt exist", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });
//                                    } else {
//                                        Toast.makeText(context, "Error while getting documents", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//
//
//
//                    }
//            }
//        });

        TextView emailTextView = convertView.findViewById(R.id.list_user_email);
        CheckBox assignedCheckBox = convertView.findViewById(R.id.checkbox_is_user_assigned);

//        if(listItem.getIsAssigned()) {
//            convertView.setBackgroundColor(Color.parseColor("#72f793"));
//            assignedCheckBox.setChecked(true);
//        } else {
//        }

        assignedCheckBox.setChecked(listItem.getIsAssigned());

//        checkBoxTextView.setChecked(listItem.getIsAssigned());
        emailTextView.setText(listItem.getEmail());

        return convertView;
    }
}
