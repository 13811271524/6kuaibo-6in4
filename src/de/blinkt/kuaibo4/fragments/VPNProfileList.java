package de.blinkt.kuaibo4.fragments;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.blinkt.kuaibo4.ConfigConverter;
import de.blinkt.kuaibo4.FileSelect;
import de.blinkt.kuaibo4.LaunchVPN;
import de.blinkt.kuaibo4.VpnProfile;
import de.blinkt.kuaibo4.core.ProfileManager;
import de.blinkt.kuaibo4.R;

public class VPNProfileList extends ListFragment {

	public final static int RESULT_VPN_DELETED = Activity.RESULT_FIRST_USER;
	private static final int START_VPN_CONFIG = 92;
	private static final int SELECT_PROFILE = 43;
	private static final int IMPORT_PROFILE = 231;

	class VPNArrayAdapter extends ArrayAdapter<VpnProfile> {

		public VPNArrayAdapter(Context context, int resource,
				int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			View titleview = v.findViewById(R.id.vpn_list_item_left);
			TextView showview = (TextView)v.findViewById(R.id.vpn_item_title);
			showview.setText("¿ªÊ¼");
			titleview.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					VpnProfile profile =(VpnProfile) getListAdapter().getItem(position);
					startVPN(profile);
				}
			});
			
			View exitview = v.findViewById(R.id.vpn_list_item_right);
			exitview.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					System.exit(0);
				}
			});

			return v;
		}
	}

	private ArrayAdapter<VpnProfile> mArrayadapter;
	protected VpnProfile mEditProfile=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (getPM().getProfiles().isEmpty()){
			startImportConfig();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.vpn_profile_list, container,false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter();
	}

	class VpnProfileNameComperator implements Comparator<VpnProfile> {

		@Override
		public int compare(VpnProfile lhs, VpnProfile rhs) {
			return lhs.mName.compareTo(rhs.mName);
		}

	}

	private void setListAdapter() {
		mArrayadapter = new VPNArrayAdapter(getActivity(),R.layout.vpn_list_item,R.id.vpn_item_title);
		Collection<VpnProfile> allvpn = getPM().getProfiles();

		TreeSet<VpnProfile> sortedset = new TreeSet<VpnProfile>(new VpnProfileNameComperator()); 
		sortedset.addAll(allvpn);
		mArrayadapter.addAll(sortedset);

		setListAdapter(mArrayadapter);
	}


	public void startImportConfig() {
		Intent intent = new Intent(getActivity(),FileSelect.class);
		intent.putExtra(FileSelect.NO_INLINE_SELECTION, true);
		intent.putExtra(FileSelect.WINDOW_TITLE, R.string.import_configuration_file);
		startActivityForResult(intent, SELECT_PROFILE);
	}


	private ProfileManager getPM() {
		return ProfileManager.getInstance(getActivity());
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == RESULT_VPN_DELETED){
			if(mArrayadapter != null && mEditProfile !=null)
				mArrayadapter.remove(mEditProfile);
		}

		if(resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == START_VPN_CONFIG) {
			String configuredVPN = data.getStringExtra(VpnProfile.EXTRA_PROFILEUUID);

			VpnProfile profile = ProfileManager.get(getActivity(), configuredVPN);
			getPM().saveProfile(getActivity(), profile);
			// Name could be modified, reset List adapter
			//setListAdapter();  //sunshine

		} else if(requestCode== SELECT_PROFILE) {
			String filedata = data.getStringExtra(FileSelect.RESULT_DATA);
			Intent startImport = new Intent(getActivity(),ConfigConverter.class);
			startImport.setAction(ConfigConverter.IMPORT_PROFILE);
			Uri uri = new Uri.Builder().path(filedata).scheme("file").build();
			startImport.setData(uri);
			startActivityForResult(startImport, IMPORT_PROFILE);
		} else if(requestCode == IMPORT_PROFILE) {
			String profileUUID = data.getStringExtra(VpnProfile.EXTRA_PROFILEUUID);
			mArrayadapter.add(ProfileManager.get(getActivity(), profileUUID));
		}
	}

	private void startVPN(VpnProfile profile) {

		getPM().saveProfile(getActivity(), profile);

		Intent intent = new Intent(getActivity(),LaunchVPN.class);
		intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
		intent.setAction(Intent.ACTION_MAIN);
		startActivity(intent);
	}
}
