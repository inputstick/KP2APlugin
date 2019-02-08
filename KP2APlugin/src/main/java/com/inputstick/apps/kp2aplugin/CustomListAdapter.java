package com.inputstick.apps.kp2aplugin;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<SelectAppActivity.AppInfo> {

    private final List<SelectAppActivity.AppInfo> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView name;
        protected ImageView icon;
    }

    public CustomListAdapter(Activity context, List<SelectAppActivity.AppInfo> list) {
        super(context, R.layout.row_apps, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.row_apps, (ViewGroup)null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView)view.findViewById(R.id.name);
            viewHolder.icon = (ImageView)view.findViewById(R.id.icon);
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