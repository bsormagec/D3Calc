package jodroid.d3calc.fragments;

import jodroid.d3calc.R;
import jodroid.d3calc.adapters.D3ItemArrayAdapter;
import jodroid.d3obj.D3Item;
import jodroid.d3obj.D3ItemLite;
import jodroid.d3obj.D3Profile;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
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

public class ItemListFragment extends HeroFragment implements OnItemClickListener {
	
	private ProgressDialog progressDialog = null;
	private D3ItemArrayAdapter mAdapter = null;

	public ItemListFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ListView retView = new ListView(getActivity());
		if (mHero != null) {
			if (mHero.items.itemArray == null) mHero.items.buildItemArray();
//			for (int i = 0; i < mHero.items.itemArray.length; i++) {
//				D3ItemLite item = mHero.items.itemArray[i];
//				if (!(item instanceof D3Item)) {
//					getUrlItem(D3Url.item2Url(item), i);
//				}
//			}
			mAdapter = new D3ItemArrayAdapter(
					getActivity(),
					R.layout.item_list_item,
					mHero.items.itemArray);
			retView.setAdapter(mAdapter);
			retView.setOnItemClickListener(this);
		}
		return retView;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View itemView, int position, long id) {
//		Toast.makeText(getActivity(), D3Url.item2Url(mHero.items.itemArray[position]), Toast.LENGTH_LONG).show();
		D3ItemLite item = mHero.items.itemArray[position];
		if (!(item instanceof D3Item)) {
			Log.i(this.getClass().getSimpleName(), "getting item : "+item.itemSlot);
			progressDialog = ProgressDialog.show(getActivity(), "", "Getting item ("+position+") : "+item.itemSlot);
			getUrlItem(D3Url.item2Url(item), position);
		}
	}
	
	/**
	 * Get and parse a JSON item (from D3api) to provide a hierarchical representation of this file in the form of D3Obj.<br/>
	 * The HttpRequest is asynchronous.
	 * @param url where to find the JSON file
	 * @return the player profile's instance
	 */
	public void getUrlItem(String url, final int position) {
		D3json.get(url, null, new JsonHttpResponseHandler() {
			public synchronized void onSuccess(JSONObject obj) {
				try {
					String code = obj.getString("code");
					if (code != null) {
						Log.w(getActivity().getClass().getName(), "code="+code);
						Toast.makeText(getActivity(), obj.getString("reason"), Toast.LENGTH_LONG).show();
						return;
					}
				}
				catch (JSONException e) {}
				D3Item item = new D3Item(mHero.items.itemArray[position]);
				Log.i(this.getClass().getSimpleName(), "parsing item ("+position+") : "+item.itemSlot);
				item.jsonBuild(obj);
				mHero.items.itemArray[position] = item;
				mAdapter.notifyDataSetChanged();
				if (progressDialog != null) progressDialog.dismiss();
			}
			
			public synchronized void onFailure(Throwable e, JSONObject obj) {
				if (progressDialog != null) progressDialog.dismiss();
				Log.e(D3Profile.class.getSimpleName(), "json failure: "+e.getMessage());
			}
		});
	}
}