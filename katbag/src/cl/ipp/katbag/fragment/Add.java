/*
 * Author: Miguel Angel Bravo (@MiguelAngelBrav)
 * The Android Open Source Project Katbag is licensed under the General GPLv3.
 * 
 */

package cl.ipp.katbag.fragment;

import org.holoeverywhere.widget.ProgressBar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cl.ipp.katbag.MainActivity;
import cl.ipp.katbag.R;
import cl.ipp.katbag.core.KatbagUtilities;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class Add extends Fragment implements OnClickListener {

	private Tracker tracker;
	
	static View v = null;
	public String type_app;
	public EditText name_app;
	public String title;
	public ImageView image_type_app;
	public static long id_app = -1; // initial value
	public static String name_app_text = "";
	public MainActivity mainActivity;
	public RelativeLayout config_app_worlds, config_app_drawings, config_app_developments, config_app_pages;
	public Fragment mFragment;
	public static final int MAX_LENGTH = 30;
	public ProgressBar progress;
	public int score_id_and_name = 0, score = 0;
	public static final int SCORE_FOR_HAVE_ID_AND_NAME = 20;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.tracker = EasyTracker.getInstance(this.getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) super.getActivity();
		v = inflater.inflate(R.layout.fragment_add, container, false);

		// rescues parameters
		Bundle bundle = getArguments();
		if (bundle != null) {
			type_app = bundle.getString("type_app");
		}

		progress = (ProgressBar) v.findViewById(R.id.progress);

		setTitleAndImageForTypeApp();

		name_app = (EditText) v.findViewById(R.id.name_app);
		name_app.setFilters(new InputFilter[] { KatbagUtilities.katbagAlphaNumericFilter, new InputFilter.LengthFilter(MAX_LENGTH) });
		name_app.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					validateName();
				}

				return false;
			}
		});

		config_app_worlds = (RelativeLayout) v.findViewById(R.id.config_app_worlds);
		config_app_drawings = (RelativeLayout) v.findViewById(R.id.config_app_drawings);
		config_app_developments = (RelativeLayout) v.findViewById(R.id.config_app_developments);
		config_app_pages = (RelativeLayout) v.findViewById(R.id.config_app_pages);

		config_app_worlds.setOnClickListener(this);
		config_app_drawings.setOnClickListener(this);
		config_app_developments.setOnClickListener(this);
		config_app_pages.setOnClickListener(this);

		if (type_app.contentEquals(MainActivity.TYPE_APP_GAME)) {
			config_app_worlds.setVisibility(View.VISIBLE);
			config_app_developments.setVisibility(View.VISIBLE);
			config_app_drawings.setVisibility(View.VISIBLE);
			config_app_pages.setVisibility(View.GONE);

		} else if (type_app.contentEquals(MainActivity.TYPE_APP_BOOK)) {
			config_app_worlds.setVisibility(View.VISIBLE);
			config_app_developments.setVisibility(View.GONE);
			config_app_drawings.setVisibility(View.VISIBLE);
			config_app_pages.setVisibility(View.VISIBLE);

		} else if (type_app.contentEquals(MainActivity.TYPE_APP_COMICS)) {
			config_app_worlds.setVisibility(View.VISIBLE);
			config_app_developments.setVisibility(View.GONE);
			config_app_drawings.setVisibility(View.VISIBLE);
			config_app_pages.setVisibility(View.VISIBLE);
		}

		return v;
	}

	// set title and image for type app
	public void setTitleAndImageForTypeApp() {
		image_type_app = (ImageView) v.findViewById(R.id.config_image_type_app);

		if (type_app.contentEquals(MainActivity.TYPE_APP_GAME)) {
			title = getString(R.string.title_activity_add_game);
			image_type_app.setImageResource(R.drawable.katbag_icon_game);
		} else if (type_app.contentEquals(MainActivity.TYPE_APP_BOOK)) {
			title = getString(R.string.title_activity_add_book);
			image_type_app.setImageResource(R.drawable.katbag_icon_book);
		} else if (type_app.contentEquals(MainActivity.TYPE_APP_COMICS)) {
			title = getString(R.string.title_activity_add_comics);
			image_type_app.setImageResource(R.drawable.katbag_icon_comics);
		}
	}

	public void validateName() {
		name_app.setText(name_app.getText().toString().trim());
		if (name_app.getText().toString().length() > MAX_LENGTH) {
			name_app.setText(name_app.getText().toString().substring(0, MAX_LENGTH).trim());
		}

		if (name_app.getText().toString().contentEquals("")) {
			KatbagUtilities.message(mainActivity.context, getString(R.string.name_app_empty));

		} else if (name_app.getText().toString().length() < 3) {
			KatbagUtilities.message(mainActivity.context, getString(R.string.name_app_short));

		} else {
			if (id_app == -1) { // insert new register
				id_app = mainActivity.katbagHandler.insertApp(name_app.getText().toString(), type_app);
				KatbagUtilities.message(mainActivity.context, getString(R.string.name_app_new) + " (id app: " + String.valueOf(id_app) + ")");
			} else { // update new register
				mainActivity.katbagHandler.updateNameApp(id_app, name_app.getText().toString());
				KatbagUtilities.message(mainActivity.context, getString(R.string.name_app_update) + " (id app: " + String.valueOf(id_app) + ")");
			}

			name_app_text = name_app.getText().toString();

			score_id_and_name = SCORE_FOR_HAVE_ID_AND_NAME;
			estimatedProgress();

			onResume();
		}
	}

	@Override
	public void onClick(View v) {

		mainActivity.hideSoftKeyboard();

		if (id_app == -1) {
			KatbagUtilities.message(mainActivity.context, getString(R.string.app_empty));

		} else {
			switch (v.getId()) {
			case R.id.config_app_worlds:
				mFragment = new Worlds();
				break;

			case R.id.config_app_drawings:
				mFragment = new Drawings();
				break;

			case R.id.config_app_developments:
				mFragment = new Develop();
				break;

			case R.id.config_app_pages:
				mFragment = new Pages();
				break;
			}

			Bundle bundle = new Bundle();
			bundle.putLong("id_app", id_app);
			bundle.putString("type_app", type_app);
			mFragment.setArguments(bundle);

			FragmentTransaction t = getActivity().getSupportFragmentManager().beginTransaction();
			t.replace(R.id.fragment_main_container, mFragment);
			t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			t.addToBackStack(mFragment.getClass().getSimpleName());
			t.commit();
		}
	}

	public void estimatedProgress() {
		if (id_app == -1) {
			score_id_and_name = 0;
			score = 0;
		} else {
			score_id_and_name = SCORE_FOR_HAVE_ID_AND_NAME;
			score = mainActivity.katbagHandler.estimatedProgress(id_app);
		}

		progress.setProgress(score_id_and_name + score);
	}

	@Override
	public void onResume() {
		if (!name_app_text.contentEquals("")) {
			title = getString(R.string.add_edit_app) + " " + name_app_text;
		}
		mainActivity.getSupportActionBar().setTitle(title);
		name_app.setText(name_app_text);

		estimatedProgress();

		if (!MainActivity.TABLET)
			mainActivity.slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		super.onResume();
		
		this.tracker.set(Fields.SCREEN_NAME, getClass().getSimpleName());
        this.tracker.send( MapBuilder.createAppView().build() );		
	}
}
