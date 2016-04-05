package com.brian.clickthebutton;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button button, shop;
	private static int letterTotal = 0;
	private static int lettersPerSecond = 0;
	private static int cats, bicycles, motorbikes, vans, trucks, barge, boat,
			ferry, liner, cargoShip, glider, helicopter, airplane, rocket,
			shuttle, spaceship;
	private static int[] items = { cats, bicycles, motorbikes, vans, trucks,
			barge, boat, ferry, liner, cargoShip, glider, helicopter, airplane,
			rocket, shuttle, spaceship };
	private TextView lettersTotalView, lettersPerSecondView;
	long millis, elapsed = 0, currentLettersPerSecond, time1 = 0, time2 = 0, startUpTime,lastCurrentLettersPerSecond=0;
	double smoothedAverage;
	private int currentLetters = 0, newLetters, letters1, letters2;
	private boolean timeCheck = false;
	DecimalFormat format = new DecimalFormat("0.00");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar bar = this.getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red_dark)));

		setContentView(R.layout.activity_main);
		startUpTime = System.currentTimeMillis();
		button = (Button) findViewById(R.id.mainButton);
		button.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	button.setScaleX((float) .8);
					button.setScaleY((float) .8);
					buttonPressed();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	button.setScaleX((float) 1);
					button.setScaleY((float) 1);
		        }
				return true;
		    }
		});



		lettersTotalView = (TextView) findViewById(R.id.totalLettersView);
		lettersPerSecondView = (TextView) findViewById(R.id.lettersPerSecondView);
		shop = (Button) findViewById(R.id.shopButton);

		shop.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	shop.setBackgroundResource(R.drawable.garage_original_selected_300);
		        	shopPressed();
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	shop.setBackgroundResource(R.drawable.garage_original_text_300);
		        }
				return true;
			}
		});

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(100);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								updateTotalLettersView();
							}
						});
					}
				} catch (InterruptedException e) {

				}
			}
		};

		final Handler h = new Handler();
		final int delay = 2000; // milliseconds
		//final int delay = 200;
		h.postDelayed(new Runnable() {
			public void run() {
				// do something
				letterTotal += lettersPerSecond;
				h.postDelayed(this, delay);
			}
		}, delay);

		Thread t2 = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {

						Thread.sleep(1000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								letterTotal += lettersPerSecond;
							}
						});
					}
				} catch (InterruptedException e) {

				}
			}
		};

		Thread t3 = new Thread() {
			@Override
			public void run() {
				try {

					while (!isInterrupted()) {
						Thread.sleep(1000);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (timeCheck) {
									time1 = System.currentTimeMillis();
									elapsed = time1 - time2;
									letters1 = letterTotal;
									newLetters = letters1 - letters2;
								} else {
									time2 = System.currentTimeMillis();
									elapsed = time2 - time1;
									letters2 = letterTotal;
									newLetters = letters2 - letters1;
								}

								if (elapsed != 0) {
									try {
										currentLettersPerSecond = newLetters
												/ (elapsed / 1000);
										smoothedAverage = (currentLettersPerSecond*0.9)+(lastCurrentLettersPerSecond*0.1);
										lastCurrentLettersPerSecond = currentLettersPerSecond;
									} catch (ArithmeticException e) {
										e.printStackTrace();
									}
									updateLettersPerSecondView(smoothedAverage);
								}
								timeCheck = !timeCheck;
							}
						});
					}
				} catch (InterruptedException e) {

				}
			}
		};

		t.start();
		t3.start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}


	private void buttonPressed() {
		letterTotal++;
	}

	private void shopPressed() {
		Intent intent = new Intent(this, ShopActivity.class);
		startActivity(intent);
	}

	private void updateTotalLettersView() {
		lettersTotalView.setText(Integer.toString(letterTotal));
	}

	private void updateLettersPerSecondView(double smoothedAverage2) {
		lettersPerSecondView.setText(format.format(smoothedAverage2)
						+ " letters per second!");
	}

	public static void increaseTotalPerSecond(double d) {
		lettersPerSecond += d;
		System.out.println("lettersPerSecond: " + lettersPerSecond);
	}

	public static int getTotalLetters() {
		return letterTotal;
	}

	public static void changeTotalLetters(double d) {
		letterTotal += d;
	}

	public static void increaseItem(int index) {
		switch (index) {
		case 0: {
			increaseTotalPerSecond(1);
			items[index]++;
			break;
		}
		case 1: {
			increaseTotalPerSecond(2);
			items[index]++;
			break;
		}
		case 2: {
			increaseTotalPerSecond(5);
			items[index]++;
			break;
		}
		case 3: {
			increaseTotalPerSecond(10);
			items[index]++;
			break;
		}
		case 4: {
			increaseTotalPerSecond(15);
			items[index]++;
			break;
		}
		case 5: {
			increaseTotalPerSecond(25);
			items[index]++;
			break;
		}
		case 6: {
			increaseTotalPerSecond(40);
			items[index]++;
			break;
		}
		case 7: {
			increaseTotalPerSecond(60);
			items[index]++;
			break;
		}
		case 8: {
			increaseTotalPerSecond(100);
			items[index]++;
			break;
		}
		case 9: {
			increaseTotalPerSecond(135);
			items[index]++;
			break;
		}
		case 10: {
			increaseTotalPerSecond(200);
			items[index]++;
			break;
		}
		case 11: {
			increaseTotalPerSecond(400);
			items[index]++;
			break;
		}
		case 12: {
			increaseTotalPerSecond(750);
			items[index]++;
			break;
		}
		case 13: {
			increaseTotalPerSecond(1200);
			items[index]++;
			break;
		}
		case 14: {
			increaseTotalPerSecond(2000);
			items[index]++;
			break;
		}
		case 15: {
			increaseTotalPerSecond(3000);
			items[index]++;
			break;
		}
		}
	}

	public static int getItem(int index) {
		return items[index];
	}
}
