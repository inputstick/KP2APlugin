package com.inputstick.apps.kp2aplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;


public class SelectAppActivity extends ListActivity {

    public static String RESULT_PACKAGE = "package_name";
    
    private static List<AppInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<AppInfo> adapter = new CustomListAdapter(this, appList);
        setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	AppInfo appInfo = appList.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_PACKAGE, appInfo.getPackage());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
                
        setResult(RESULT_CANCELED);
        //failsafe:
        if (appList == null) {
        	finish();
        }                
    }

    public static void getInstalledApps(Context ctx) {
    	appList = new ArrayList<AppInfo>();
		final PackageManager pm = ctx.getPackageManager();		
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_GIDS);
		for (ApplicationInfo appInfo : packages) {
			if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {				
				Drawable icon = pm.getApplicationIcon(appInfo); //never null				
				String name = pm.getApplicationLabel(appInfo).toString();
				if (name != null) {
					appList.add(new AppInfo(name, appInfo.packageName, icon));
				}
			}
		}	
		Collections.sort(appList, new Comparator<AppInfo>() {
	        @Override
	        public int compare(AppInfo ai1, AppInfo ai2) {
	        	return ai1.getName().compareToIgnoreCase(ai2.getName());
	        }
	    });
    }

    public static class AppInfo {
        private String mName;
        private String mAppPackage;
        private Drawable mIcon;
        
        public AppInfo(String name, String appPackage, Drawable icon){
            mName = name;
            mAppPackage = appPackage;
            mIcon = icon;
        }
        public String getName() {
            return mName;
        }
        public Drawable getIcon() {
            return mIcon;
        }
        public String getPackage() {
            return mAppPackage;
        }
    }
    
}