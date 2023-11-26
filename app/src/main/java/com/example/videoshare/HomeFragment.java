package com.example.videoshare;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeFragment extends Fragment implements View.OnClickListener {

  private View mView;
  private FloatingActionButton addVideoToDb;
  private SwipeRefreshLayout pullToRefresh;

  private FirebaseUser currentUser;
  private FirebaseAuth auth;

  FirebaseFirestore db = FirebaseFirestore.getInstance();

  private static Context thisContext;
  ProgressDialog progressDialog;

  private ListView listView;
  private FileListAdapter adapter;

  private List<FileListItem> dataList = new ArrayList<>();

  FragmentManager mFragmentManager;
  FragmentTransaction mFragmentTransaction;
  final ShowVideoFragment showVideoFragment = new ShowVideoFragment();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    auth = FirebaseAuth.getInstance();
    currentUser = auth.getCurrentUser();

    progressDialog = new ProgressDialog(getContext());
    progressDialog.setTitle("Uploading");
    progressDialog.setMessage("Uploading your video...");

    adapter = new FileListAdapter(getContext(), dataList);

    fetchData();
  }

  @Override
  public View onCreateView(
    LayoutInflater inflater,
    ViewGroup container,
    Bundle savedInstanceState
  ) {
    thisContext = container.getContext();
    mFragmentManager = getActivity().getSupportFragmentManager();
    mFragmentTransaction = mFragmentManager.beginTransaction();

    mView = inflater.inflate(R.layout.fragment_home, container, false);

    pullToRefresh = mView.findViewById(R.id.pullToRefresh);
    if (pullToRefresh != null) {
      pullToRefresh.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
            fetchData();
            pullToRefresh.setRefreshing(false);
          }
        }
      );
    }

    addVideoToDb = (FloatingActionButton) mView.findViewById(R.id.addVideoToDb);
    if (addVideoToDb != null) {
      addVideoToDb.setOnClickListener(this);
    }

    listView = mView.findViewById(R.id.filesListView);
    if (listView != null) {
      listView.setAdapter(adapter);

      listView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(
            AdapterView<?> parent,
            View view,
            int position,
            long id
          ) {
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
        }
      );
    }

    return mView;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.addVideoToDb:
        showFileChooser();
        break;
    }
  }

  private void fetchData() {
    dataList.clear();

    db
      .collection("files")
      .whereEqualTo("userId", currentUser.getUid())
      .get()
      .addOnCompleteListener(
        new OnCompleteListener<QuerySnapshot>() {
          @Override
          public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
              if (task.getResult().isEmpty()) {
                dataList.add(new FileListItem("No file added : (", ""));
              }

              for (QueryDocumentSnapshot document : task.getResult()) {
                String itemName = document.getString("name");
                String itemUuid = document.getString("uuid");

                dataList.add(new FileListItem(itemName, itemUuid));
              }

              // Notify the adapter that the data has changed
              adapter.notifyDataSetChanged();
            } else {
              Toast
                .makeText(
                  getContext(),
                  "Error while getting documents",
                  Toast.LENGTH_SHORT
                )
                .show();
            }
          }
        }
      );
  }

  public void showFileChooser() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("video/*");
    intent.addCategory(Intent.CATEGORY_OPENABLE);

    try {
      startActivityForResult(
        Intent.createChooser(intent, "Select a file"),
        100
      );
    } catch (Exception e) {
      Toast
        .makeText(
          getActivity(),
          "Please install a fle manager",
          Toast.LENGTH_SHORT
        )
        .show();
    }
  }

  @Override
  public void onActivityResult(
    int requestCode,
    int resultCode,
    @Nullable Intent data
  ) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 100 && data != null) {
      progressDialog.show();

      Uri uri = data.getData();

      File myFile = null;
      try {
        myFile = FileUtil.from(getContext(), uri);
      } catch (IOException e) {
        e.printStackTrace();
      }

      String uuid = UUID.randomUUID().toString();

      File filesDir = getActivity().getFilesDir();

      String mySecretKey = Encryptor.generateKey();

      Encryptor.encryption(filesDir, myFile, uuid, mySecretKey);

      FirebaseStorage storage = FirebaseStorage.getInstance();
      StorageReference uploader = storage
        .getReference()
        .child(currentUser.getUid() + "/" + uuid);

      File finalMyFile = myFile;

      uploader
        .putFile(Uri.fromFile(new File(filesDir + "/" + uuid + ".txt")))
        .addOnSuccessListener(
          new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
              SharedPreferences settings = thisContext.getSharedPreferences(
                KeyStoreHelper.PREFS_NAME,
                0
              );
              String myPublicKey = settings.getString(
                KeyStoreHelper.PREFS_KEY_PUBLIC,
                ""
              );

              String encryptedKey = VideoKeyManager.encrypt(
                mySecretKey,
                myPublicKey
              );

              Map<String, Object> fileDetails = new HashMap<>();
              fileDetails.put("uuid", uuid);
              fileDetails.put("name", finalMyFile.getName());
              fileDetails.put("key", encryptedKey);
              fileDetails.put("userId", currentUser.getUid());

              db
                .collection("files")
                .document(uuid)
                .set(fileDetails)
                .addOnSuccessListener(
                  new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      fetchData();

                      progressDialog.cancel();

                      Toast
                        .makeText(
                          getActivity(),
                          "File Uploaded",
                          Toast.LENGTH_LONG
                        )
                        .show();
                    }
                  }
                )
                .addOnFailureListener(
                  new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      Toast
                        .makeText(
                          getActivity(),
                          "Something went wrong 1",
                          Toast.LENGTH_SHORT
                        )
                        .show();
                    }
                  }
                );
            }
          }
        );
    }
  }
}
