package jodroid.d3calc.fragments;

import jodroid.d3calc.HeroStripActivity;
import jodroid.d3calc.ProfileListContent;
import jodroid.d3calc.R;
import jodroid.d3calc.adapters.D3ObjArrayAdapter;
import jodroid.d3obj.D3Profile;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import d3api.D3Url;
import d3api.D3json;

public class ProfileDetailFragment extends Fragment implements OnItemClickListener {

    public static final String ARG_PROFILE_ID = "profile_id";
    private D3Profile playerProfile = null;

    ProfileListContent.ProfileItem mItem;
    
    private ProgressDialog progressDialog;

    public ProfileDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_PROFILE_ID)) {
            mItem = ProfileListContent.ITEM_MAP.get(getArguments().getString(ARG_PROFILE_ID));
            playerProfile = new D3Profile();
            progressDialog = ProgressDialog.show(getActivity(), "", "Loading profile ...");
//            getUrlProfile("http://www.ecole.ensicaen.fr/~reynaud/android/solo-2284.json"); // dev example
            String url = D3Url.playerProfile2Url(mItem);
            Log.i(this.getClass().getSimpleName(), url);
            getUrlProfile(url);
        }
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_detail, container, false);
        if (mItem != null) {
        	String str = mItem.toString()+" on "+mItem.battlehost;
        	getActivity().setTitle(str);
        }
        return rootView;
    }
    
    /**
     * Build objects from the JSON file and display them
     * @param obj a JSON object parsed from the file
     */
    private void buildAndDisplay(JSONObject obj) {
    	playerProfile.jsonBuild(obj);
    	Activity act = getActivity();
    	if (act == null) return;
    	if (getView() == null) return;
//    	if (act.getClass() == ProfileDetailActivity.class) return; // wrong : the master activity is sometimes another activity
    	
    	act.setTitle(playerProfile.toString());
    	
    	playerProfile.kills.fieldsToView(getView());
    	
    	ListView lv = (ListView)getView().findViewById(R.id.listHeroesLite);
    	D3ObjArrayAdapter adapter = new D3ObjArrayAdapter(getActivity(), R.layout.hero_list_item, playerProfile.heroes);
    	lv.setAdapter(adapter);
//    	lv.setAdapter(new ArrayAdapter<D3HeroLite>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, playerProfile.heroes));
    	adapter.notifyDataSetChanged();
    	
    	lv.setOnItemClickListener(this);
    }
    
    
    /**
	 * Get and parse a JSON player profile (from D3api) to provide a hierarchical representation of this file in the form of D3Obj.<br/>
	 * The HttpRequest is asynchronous.
	 * @param url where to find the JSON file
	 * @return the player profile's instance
	 */
	public void getUrlProfile(String url) {
		D3json.get(url, null, new JsonHttpResponseHandler() {
			public void onSuccess(JSONObject obj) {
				try {
					String code = obj.getString("code");
					if (code != null) {
						Log.w(getActivity().getClass().getName(), "code="+code);
						Toast.makeText(getActivity(), obj.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
				}
				catch (JSONException e) {}
				buildAndDisplay(obj);
				if (progressDialog != null) progressDialog.dismiss();
			}
			
			public void onFailure(Throwable e, JSONObject obj) {
				if (progressDialog != null) progressDialog.dismiss();
				Log.e(D3Profile.class.getSimpleName(), "json failure: "+e.getMessage());
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View itemView, int position, long id) {
//		Intent heroIntent = new Intent(getActivity(), HeroDropdownActivity.class);
		Intent heroIntent = new Intent(getActivity(), HeroStripActivity.class);
		heroIntent.putExtra(ARG_PROFILE_ID, mItem.id);
//		Log.i(this.getClass().getSimpleName(), "id="+playerProfile.heroes[position].id);
		heroIntent.putExtra(HeroStripActivity.ARG_HERO_ID, Long.toString(playerProfile.heroes[position].id));
		startActivity(heroIntent);
	}
}