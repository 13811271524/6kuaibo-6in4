package de.blinkt.kuaibo4.fragments;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.blinkt.kuaibo4.R;

public class AboutFragment extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	
    	
    	View v= inflater.inflate(R.layout.about, container, false);
    	TextView ver = (TextView) v.findViewById(R.id.version);
    	
    	String version;
    	String name="Openvpn";
		try {
			PackageInfo packageinfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			version = packageinfo.versionName;
			name = getString(R.string.app);
		} catch (NameNotFoundException e) {
			version = "error fetching version";
		}

    	//version = "";//��ʱ�ÿմ�
    	ver.setText(getString(R.string.version_info,name,version));
    	return v;
    }

}
