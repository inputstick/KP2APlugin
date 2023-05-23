package com.inputstick.apps.kp2aplugin;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<SelectAppActivity.AppInfo> {

    private final List<SelectAppActivity.AppInfo> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView name;
        protected ImageView icon;
    }

    CustomListAdapter(Activity context, List<SelectAppActivity.AppInfo> list) {
        super(context, R.layout.row_apps, list);
        this.context = context;
        this.list = list;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.row_apps, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.name);
            viewHolder.icon = view.findViewById(R.id.icon);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(list.get(position).getName());
        holder.icon.setImageDrawable(list.get(position).getIcon());
        return view;
    }
    
}