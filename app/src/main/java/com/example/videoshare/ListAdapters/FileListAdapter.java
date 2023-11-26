package com.example.videoshare.ListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.videoshare.ListItems.FileListItem;
import com.example.videoshare.R;

import java.util.List;

public class FileListAdapter extends BaseAdapter {
    private Context context;
    private List<FileListItem> itemList;

    public FileListAdapter(Context context, List<FileListItem> itemList) {
        this.context = context;
        this.itemList = itemList;
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
            convertView = inflater.inflate(R.layout.data_list_item, parent, false);
        }

        // Bind data to the custom layout
        TextView uuidTextView = convertView.findViewById(R.id.listUuid);
        TextView nameTextView = convertView.findViewById(R.id.listName);

        FileListItem listItem = itemList.get(position);
        uuidTextView.setText(listItem.getUuid());
        nameTextView.setText(listItem.getName());

        return convertView;
    }
}
