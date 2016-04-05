package com.parent.luckynumbers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.CalendarView;

@SuppressLint("NewApi")
public class MainActivity extends android.support.v4.app.FragmentActivity
		implements CreditsFragment.OnFragmentInteractionListener {

	public static final String EURO_MESSAGE = "com.example.euronumbers.EURO_MESSAGE";
	public static final String STARS_MESSAGE = "com.example.euronumbers.STARS_MESSAGE";
	public static final String LOTTO_MESSAGE = "com.example.euronumbers.LOTTO_MESSAGE";
	public static final String UK_MESSAGE = "com.example.euronumbers.UK_MESSAGE";
	public static final String BRASIL_MESSAGE = "com.example.euronumbers.BRASIL_MESSAGE";
	private Spinner spinner;
	private Button submit;
	private RadioGroup radioGroup;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private RadioButton radioButton4;
	private RadioButton radioButton5;
	private Button preferencesButton1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			ActionBar bar = this.getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
		}

		SharedPreferences shared = getSharedPreferences(
				"com.example.euronumbers_preferences", MODE_PRIVATE);
		System.out.println(shared.getString("listThemePref", "X"));
		String style = shared.getString("listThemePref", "AppThemePO");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup2);
		radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
		radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
		radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
		radioButton4 = (RadioButton) findViewById(R.id.radioButton4);
		radioButton5 = (RadioButton) findViewById(R.id.radioButton5);

		GridLayout bgElement = (GridLayout) findViewById(R.id.main_container);
	}

	@Override
	protected void onResume() {
		GridLayout bgElement = (GridLayout) findViewById(R.id.main_container);
		SharedPreferences shared = getSharedPreferences(
				"com.example.euronumbers_preferences", MODE_PRIVATE);
		String style = shared.getString("listThemePref", "AppThemeBW");
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);

			startActivity(settingsActivity);
			return true;
		case R.id.calendar:
			Intent calendarActivity = new Intent(getBaseContext(),
					Calendar.class);

			startActivity(calendarActivity);
			return true;
		case R.id.notifications:
			Intent notificationsActivity = new Intent(getBaseContext(),
					Notifications.class);

			startActivity(notificationsActivity);
			return true;
		case R.id.toast:
			Toast.makeText(getBaseContext(), R.string.toast_message,
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.credits:
			Intent creditsActivity = new Intent(getBaseContext(),
					Credits.class);

			startActivity(creditsActivity);
					return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addListenerOnButton() {
		submit = (Button) findViewById(R.id.submit_button);
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int i = radioGroup.getCheckedRadioButtonId();

				RadioButton radioButton = (RadioButton) findViewById(i);

				Toast.makeText(MainActivity.this, radioButton.getText(),
						Toast.LENGTH_SHORT).show();

				submitGameMessage(v);
			}
		});
	}

	public void addListenerOnButton2() {
		preferencesButton1 = (Button) findViewById(R.id.preferences1);

		preferencesButton1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendForSettings(v);
			}
		});
	}

	public void sendForSettings(View view) {
		Intent settingsActivity = new Intent(getBaseContext(),
				Preferences.class);

		startActivity(settingsActivity);
	}

	public void submitGameMessage(View view) {
		Intent intent = new Intent(this, DisplayMessageActivity.class);

		int sid;
		sid = radioGroup.getCheckedRadioButtonId();

		System.out.println(sid);

		switch (sid) {
		case R.id.radioButton1:
			int[] lottoSix = convertIntegers(pickNumbers(45, 6));
			intent.putExtra(LOTTO_MESSAGE, lottoSix);
			break;
		case R.id.radioButton2:
			int[] euroFive = convertIntegers(pickNumbers(50, 5));
			intent.putExtra(EURO_MESSAGE, euroFive);
			int[] euroTwo = convertIntegers(pickNumbers(12, 2));
			intent.putExtra(STARS_MESSAGE, euroTwo);
			break;
		case R.id.radioButton3:
			int[] ukSix = convertIntegers(pickNumbers(49, 6));
			intent.putExtra(UK_MESSAGE, ukSix);
			break;
		case R.id.radioButton4:
			int[] brasilSix = convertIntegers(pickNumbers(60, 6));
			intent.putExtra(BRASIL_MESSAGE, brasilSix);
			break;
		case R.id.radioButton5:
			intent = new Intent(this, CustomActivity.class);
			break;
		}
		startActivity(intent);
	}

	public void sendMessageLotto(View view) {
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		int[] lottoSix = convertIntegers(pickNumbers(45, 6));
		intent.putExtra(LOTTO_MESSAGE, lottoSix);
		startActivity(intent);

	}

	/** Called when the user clicks the Send button */
	public void sendMessageEuro(View view) {
		// Do something in response to button
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		int[] euroFive = convertIntegers(pickNumbers(50, 5));
		intent.putExtra(EURO_MESSAGE, euroFive);
		int[] euroTwo = convertIntegers(pickNumbers(12, 2));
		intent.putExtra(STARS_MESSAGE, euroTwo);
		startActivity(intent);
	}

	public static ArrayList<Integer> firstFive() { // Generate the five numbers
		ArrayList<Integer> result = new ArrayList<Integer>(); // array to hold
		// the five
		// numbers
		ArrayList<Integer> range = new ArrayList<Integer>(); // Numbers that can
		// be chosen
		for (int i = 1; i <= 50; i++) {
			range.add(i); // Makes array from 1 to 50
		}

		for (int i = 0; i < 5; i++) {
			int a = 1 + (int) (Math.random() * ((range.size()) - 1));
			result.add(range.get(a));
			range.remove(a);
		}

		Collections.sort(result);
		return result;
	}

	public static ArrayList<Integer> pickNumbers(int x, int y) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Integer> range = new ArrayList<Integer>();

		for (int i = 1; i <= x; i++) {
			range.add(i); // Makes array from 1 to x
		}

		for (int i = 0; i < y; i++) {
			int a =  (int) (Math.random() * ((range.size())));
			result.add(range.get(a));
			range.remove(a);
		}

		Collections.sort(result);
		return result;
	}

	public static ArrayList<Integer> stars() { // Generate the five numbers
		ArrayList<Integer> result = new ArrayList<Integer>(); // array to hold
		// the five
		// numbers
		ArrayList<Integer> range = new ArrayList<Integer>(); // Numbers that can
		// be chosen
		for (int i = 1; i <= 11; i++) {
			range.add(i); // Makes array from 1 to 11
		}

		for (int i = 0; i < 2; i++) {
			int a = 1 + (int) (Math.random() * ((range.size()) - 1));
			result.add(range.get(a));
			range.remove(a);
		}

		Collections.sort(result);
		return result;
	}

	public static int[] convertIntegers(ArrayList<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		Fragment newFragment = new CreditsFragment();
		android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		transaction.replace(R.id.credits, newFragment);
		transaction.addToBackStack(null);

		transaction.commit();
	}
}
