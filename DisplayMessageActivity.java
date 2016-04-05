package com.parent.luckynumbers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_display_message);
		super.onCreate(savedInstanceState);
		ActionBar bar = this.getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));

		Intent intent = getIntent();

		TextView textView2 = new TextView(this);
		textView2.setTextSize(40);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		LinearLayout mainLayout = (LinearLayout) findViewById(R.id.linearLayout1);
		mainLayout.addView(textView2, params);

		// Create the text view
		TextView textView = new TextView(this);
		textView.setTextSize(40);

		textView.setBackgroundColor(getResources().getColor(R.color.light));

		if (intent.hasExtra(MainActivity.LOTTO_MESSAGE)) {

			int[] message1 = intent
					.getIntArrayExtra(MainActivity.LOTTO_MESSAGE);

			textView.setText(Integer.toString(message1[0]) + "  "
					+ Integer.toString(message1[1]) + "  "
					+ Integer.toString(message1[2]) + "  "
					+ Integer.toString(message1[3]) + "  "
					+ Integer.toString(message1[4]) + "  "
					+ Integer.toString(message1[5]));

			mainLayout.addView(textView, params);

		}

		if (intent.hasExtra(MainActivity.UK_MESSAGE)) {

			int[] message1 = intent.getIntArrayExtra(MainActivity.UK_MESSAGE);

			textView.setText(Integer.toString(message1[0]) + "  "
					+ Integer.toString(message1[1]) + "  "
					+ Integer.toString(message1[2]) + "  "
					+ Integer.toString(message1[3]) + "  "
					+ Integer.toString(message1[4]) + "  "
					+ Integer.toString(message1[5]));

			mainLayout.addView(textView, params);

		}

		if (intent.hasExtra(MainActivity.BRASIL_MESSAGE)) {

			int[] message1 = intent.getIntArrayExtra(MainActivity.BRASIL_MESSAGE);

			textView.setText(Integer.toString(message1[0]) + "  "
					+ Integer.toString(message1[1]) + "  "
					+ Integer.toString(message1[2]) + "  "
					+ Integer.toString(message1[3]) + "  "
					+ Integer.toString(message1[4]) + "  "
					+ Integer.toString(message1[5]));

			mainLayout.addView(textView, params);

		}

		if (intent.hasExtra(CustomActivity.CHOICE)) {

			int choice = intent.getIntExtra(CustomActivity.CHOICE, 0);
			System.out.println(choice);
			int fullSelection = intent.getIntExtra(CustomActivity.TOTAL, 0);
			System.out.println(fullSelection);

			PickNumbers myNumbers = new PickNumbers(fullSelection, choice);
			ArrayList<Integer> myNumbersArray = myNumbers.result;
			String s = "";
			for (int i = 0; i < choice; i++) {
				s = s.concat((Integer.toString(myNumbersArray.get(i)) + "  "));
				System.out.println(s);
			}
			textView.setText(s);
			mainLayout.addView(textView, params);

		}

		if (intent.hasExtra(MainActivity.EURO_MESSAGE)) {

			int[] message1 = intent.getIntArrayExtra(MainActivity.EURO_MESSAGE);
			int[] message2 = intent
					.getIntArrayExtra(MainActivity.STARS_MESSAGE);

			textView.setText(Integer.toString(message1[0]) + "  "
					+ Integer.toString(message1[1]) + "  "
					+ Integer.toString(message1[2]) + "  "
					+ Integer.toString(message1[3]) + "  "
					+ Integer.toString(message1[4]) + "\n"
					+ Integer.toString(message2[0]) + "  "
					+ Integer.toString(message2[1]));

			mainLayout.addView(textView, params);
		}

		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setHomeButtonEnabled(true);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public Drawable loadDataFromAsset(int number) {

		// load image
		try {
			AssetManager a = getAssets();
			System.out.println(a.list("images/NumberPics")[number]);
			InputStream ims = getAssets().open(
					"/images/NumberPics/image" + number + ".png");
			// load image as Drawable
			System.out.println(ims.toString());
			Drawable d = Drawable.createFromStream(ims, null);
			return d;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public static int getDrawable(Context context, String name) {
		Assert.assertNotNull(context);
		Assert.assertNotNull(name);

		return context.getResources().getIdentifier(name, "drawable",
				context.getPackageName());
	}

}
