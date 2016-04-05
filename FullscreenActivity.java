package com.brian.fireworks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.brian.fireworks.R.string.abc_action_menu_overflow_description;

public class FullscreenActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener {

	private static final boolean AUTO_HIDE = true;


	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;


	private static final int UI_ANIMATION_DELAY = 300;
	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int SHADE_MENU_ID = Menu.FIRST+1;
	private static final int BRUSH_SIZE_ID = Menu.FIRST+2;
	private static final int EMBOSS_MENU_ID = Menu.FIRST + 3;
	private static final int BLUR_MENU_ID = Menu.FIRST + 4;
	private static final int ERASE_MENU_ID = Menu.FIRST + 5;
	private static final int SRCATOP_MENU_ID = Menu.FIRST + 6;
	private static final int Wallpaper = Menu.FIRST + 7;
	private static final int Save = Menu.FIRST+8;
	private static final int CLEAR = Menu.FIRST+9;
	private static final int ABOUT = Menu.FIRST+10;
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static final String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	private final DecimalFormat df = new DecimalFormat("#");
	DisplayMetrics metrics = new DisplayMetrics();
	private static Context context;

	private final Handler mHideHandler = new Handler();
	private final Runnable mHidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi")
		@Override
		public void run() {
		}
	};

	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
		}
	};

	private View mContentView;
	private View mControlsView;
	private final Runnable mShowPart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
			mControlsView.setVisibility(View.VISIBLE);
		}
	};
	private boolean mVisible;
	private DrawingView dv;
	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;
	private PorterDuff.Mode modePD;
	public int height;
	public int width;

	private static void setOverflowButtonColor(final Activity activity, final ColorFilter colorFilter) {
		final String overflowDescription = activity.getString(abc_action_menu_overflow_description);
		final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
		final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				final ArrayList<View> outViews = new ArrayList<>();
				decorView.findViewsWithText(outViews, overflowDescription,
						View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
				if (outViews.isEmpty()) {
					return;
				}
				AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
				overflow.setColorFilter(colorFilter);
				removeOnGlobalLayoutListener(decorView,this);
			}
		});
	}

	private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		}
		else {
			v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setLogo(R.mipmap.ic_launcher);
			actionBar.setDisplayUseLogoEnabled(true);
		}
		setContentView(R.layout.activity_fullscreen);
		FullscreenActivity.context = getApplicationContext();

		WindowManager w = getWindowManager();
		w.getDefaultDisplay().getMetrics(metrics);
		height = metrics.heightPixels;
		width = metrics.widthPixels;

		mVisible = true;
		dv = new DrawingView(this);
		setContentView(dv);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		int brushSize = 15;
		mPaint.setStrokeWidth(brushSize);
		modePD = PorterDuff.Mode.SRC_OVER;
		mPaint.setXfermode(new PorterDuffXfermode(modePD));
		mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 12, mPaint.getStrokeWidth()/2f);
		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

		if (actionBar != null) {
			actionBar.setBackgroundDrawable(new ColorDrawable(mPaint.getColor()));
		}
	}

	public static Context getAppContext() {
		return FullscreenActivity.context;
	}

	public void colorChanged(int color) {
		mPaint.setColor(color);
		changeActionBarColor(color);

	}

	private void changeActionBarColor(int color) {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(new ColorDrawable(color));
			final float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);

			if(ColorUtils.calculateLuminance(color)>0.5) {
				Spannable text = new SpannableString(actionBar.getTitle());
				text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				actionBar.setTitle(text);
				final PorterDuffColorFilter colorFilter
						= new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
				setOverflowButtonColor(this, colorFilter);

			}else{
				Spannable text = new SpannableString(actionBar.getTitle());
				text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				actionBar.setTitle(text);
				final PorterDuffColorFilter colorFilter
						= new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
				setOverflowButtonColor(this, colorFilter);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, COLOR_MENU_ID, 0, "Colour").setShortcut('1', 'c');
		menu.add(0, SHADE_MENU_ID, 0, "Shade").setShortcut('2', 's');
		menu.add(0,BRUSH_SIZE_ID,0,"Size").setShortcut('3', 'z').setTitle("Size: " + (int) mPaint.getStrokeWidth());
		menu.add(0, EMBOSS_MENU_ID, 0, "Shadow").setShortcut('4', 't').setCheckable(true).setChecked(false);
		menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'b').setCheckable(true).setChecked(false);
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('6', 'r').setCheckable(true).setChecked(false);
		menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('7', 'p').setCheckable(true).setChecked(false);
		menu.add(0, Wallpaper, 0, "Wallpaper").setShortcut('8', 'w');
		menu.add(0, Save, 0, "Save").setShortcut('9','v');
		menu.add(0, CLEAR, 0, "Clear");
		menu.add(0,ABOUT,0,"About").setShortcut('0', 'a');
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.getItem(2).setTitle("Size: " + (int) mPaint.getStrokeWidth());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setXfermode(new PorterDuffXfermode(modePD));
		if(modePD== PorterDuff.Mode.SRC_OVER){mPaint.setAlpha(0xFF);}

		switch (item.getItemId()) {
			case COLOR_MENU_ID:
				new ColorPickerDialog(this, this, mPaint.getColor()).show();

				return true;
			case SHADE_MENU_ID:
				shadeDialog();
				return true;
			case BRUSH_SIZE_ID:
				sizeDialog2();
				return true;
			case EMBOSS_MENU_ID:
				if (mPaint.getMaskFilter() != mEmboss) {
					mPaint.setMaskFilter(mEmboss);
					item.setChecked(true);
				} else {
					mPaint.setMaskFilter(null);
					item.setChecked(false);
				}
				return true;
			case BLUR_MENU_ID:
				if (mPaint.getMaskFilter() != mBlur) {
					mPaint.setMaskFilter(mBlur);
					item.setChecked(true);
				} else {
					mPaint.setMaskFilter(null);
					item.setChecked(false);
				}
				return true;
			case ERASE_MENU_ID:
				if(!item.isChecked()) {
					modePD = PorterDuff.Mode.CLEAR;
					mPaint.setXfermode(new PorterDuffXfermode(modePD));
					mPaint.setAlpha(0x80);
					item.setChecked(true);
					return true;
				}else{
					modePD = PorterDuff.Mode.SRC_OVER;
					mPaint.setXfermode(new PorterDuffXfermode(modePD));
					mPaint.setAlpha(0xFF);
					item.setChecked(false);
					return true;
				}
			case SRCATOP_MENU_ID:
				if(!item.isChecked()) {
					modePD = PorterDuff.Mode.SRC_ATOP;
					mPaint.setXfermode(new PorterDuffXfermode(modePD));
					mPaint.setAlpha(0x80);
					item.setChecked(true);
					return true;
				}else{
					modePD = PorterDuff.Mode.SRC_OVER;
					mPaint.setXfermode(new PorterDuffXfermode(modePD));
					mPaint.setAlpha(0xFF);
					item.setChecked(false);
					return true;
				}
			case Wallpaper:

				setAsWallpaper();

				return true;
			case Save:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					requestPermissions(PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
				}else{
					saveMethod();
				}
				return true;
			case CLEAR:
				dv.mBitmap.eraseColor(Color.BLACK);
				return true;
			case ABOUT:
				aboutDialog();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void aboutDialog() {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View viewLayout = inflater.inflate(R.layout.about_dialog_layout,(ViewGroup)findViewById(R.id.about_dialog));
		final TextView textView = (TextView)viewLayout.findViewById(R.id.aboutTextView);

		textView.setText(R.string.about_text);
		dialog.setTitle("About");
		dialog.setView(viewLayout);
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface buttonDialog, int which) {
				buttonDialog.dismiss();
			}
		});
		dialog.create();
		dialog.show();
	}

	private void shadeDialog() {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View viewLayout = inflater.inflate(R.layout.shade_dialog_layout,(ViewGroup) findViewById(R.id.shade_dialog));

		final TextView textView = (TextView)viewLayout.findViewById(R.id.shadeTextView);
		final ImageView imageView = (ImageView)viewLayout.findViewById(R.id.imageView);
		final SeekBar seekbar = (SeekBar) viewLayout.findViewById(R.id.shadeSeekbar);
		seekbar.setMax(100);

		int color = mPaint.getColor();
		final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadePaint.setStrokeWidth(15);
		shadePaint.setStyle(Paint.Style.FILL);
		shadePaint.setColor(color);
		createStrokeBitMap(imageView, color);

		final float[] hsl = new float[3];
		ColorUtils.colorToHSL(color, hsl);
		seekbar.setProgress((int) (hsl[2] * 100));

		dialog.setTitle("Choose a shade:");
		int initial = (int) (hsl[2]*100);
		textView.setText(df.format(initial));
		dialog.setView(viewLayout);

		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				double progressDouble = (double) progress;
				textView.setText(df.format(progress));
				float l = (float) (progressDouble / 100);
				float[] newHSL = {hsl[0], hsl[1], l};
				int newColor = ColorUtils.HSLToColor(newHSL);
				mPaint.setColor(newColor);
				colorChanged(mPaint.getColor());
				shadePaint.setColor(newColor);
				createStrokeBitMap(imageView, newColor);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface buttonDialog, int which){
				buttonDialog.dismiss();
			}
		});
		dialog.create();
		dialog.show();
	}

	private void createStrokeBitMap(ImageView imageView, int color) {
		Bitmap bitMap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		bitMap = bitMap.copy(bitMap.getConfig(), true);
		Canvas imageCanvas = new Canvas(bitMap);
		Paint shadePaint = new Paint();
		shadePaint.setAntiAlias(true);
		shadePaint.setColor(color);
		shadePaint.setStyle(Paint.Style.FILL);
		shadePaint.setStrokeWidth(15);
		imageCanvas.drawCircle(imageCanvas.getHeight() / 2, imageCanvas.getWidth() / 2, 40, shadePaint);
		imageView.setImageBitmap(bitMap);
	}

	private void createSizeBitMap(ImageView imageView, float size) {
		Bitmap bitMap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
		bitMap = bitMap.copy(bitMap.getConfig(), true);
		Canvas imageCanvas = new Canvas(bitMap);
		Paint shadePaint = new Paint();
		shadePaint.setAntiAlias(true);
		shadePaint.setColor(mPaint.getColor());
		shadePaint.setStyle(Paint.Style.FILL);
		shadePaint.setStrokeWidth(size);
		imageCanvas.drawCircle(imageCanvas.getHeight() / 2, imageCanvas.getWidth() / 2, shadePaint.getStrokeWidth() / 2, shadePaint);
		imageView.setImageBitmap(bitMap);

	}

	private void sizeDialog2(){
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View viewLayout = inflater.inflate(R.layout.brush_size_dialog_layout,(ViewGroup) findViewById(R.id.brush_size_dialog));

		final ImageView imageView = (ImageView)viewLayout.findViewById(R.id.sizeImageView);
		final TextView textView = (TextView)viewLayout.findViewById(R.id.sizeTextView);
		SeekBar seekbar = (SeekBar) viewLayout.findViewById(R.id.sizeSeekbar);
		seekbar.setMax(100);
		seekbar.setProgress(convertSizeToProgress(mPaint.getStrokeWidth()));

		final int color = mPaint.getColor();
		final Paint sizePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sizePaint.setStrokeWidth(mPaint.getStrokeWidth());
		sizePaint.setStyle(Paint.Style.FILL);
		sizePaint.setColor(color);
		createSizeBitMap(imageView, mPaint.getStrokeWidth());

		dialog.setTitle("Choose a brush size:\nCurrent size is " + (int) mPaint.getStrokeWidth());
		textView.setText(df.format((int) mPaint.getStrokeWidth()));
		dialog.setView(viewLayout);

		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int size = convertProgressToSize(progress);
				textView.setText(df.format(size));
				changeBrushSize(size);
				sizePaint.setStrokeWidth(size);
				createSizeBitMap(imageView, sizePaint.getStrokeWidth());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			public int convertProgressToSize(int progress){
				double progressDouble = ((double) progress)/100;
				return (int)(500*(Math.pow(Math.E,4.39445*progressDouble)-1)/80);
			}

		});


		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface buttonDialog, int which){
				buttonDialog.dismiss();
			}
		});
		dialog.create();
		dialog.show();
	}

	private void changeBrushSize(int size){
		mPaint.setStrokeWidth(size);
	}

	private int convertSizeToProgress(float size){
		double sizeDouble = ((double) size);
		double result = 100*(((Math.log(((4*sizeDouble)+1)/25))/4.39445));
		System.out.println("before: " + result);
		if(result<16){
			result=(size+1)*(1+(size/3));
		}
		System.out.println("after: " + result);
		return (int)result;
	}

	private void saveMethod(){
		AlertDialog.Builder editalert = new AlertDialog.Builder(FullscreenActivity.this);
		editalert.setTitle("Please enter the name of the file:");
		final EditText input = new EditText(FullscreenActivity.this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		editalert.setView(input);
		editalert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				dv.setDrawingCacheEnabled(true);
				Bitmap bitmap = dv.getDrawingCache();
				String path = Environment.getExternalStorageDirectory().getAbsolutePath();
				new File(path+"/WallpaperApp").mkdirs();
				path = path.concat("/WallpaperApp");

				File file = new File(path, name + ".png");

				try {
					if (!file.exists()) {
						file.createNewFile();

						FileOutputStream ostream = new FileOutputStream(file);
						bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
						ostream.close();
						dv.invalidate();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dv.setDrawingCacheEnabled(false);
				}
			}
		});
		editalert.show();
	}

	private boolean shouldAskPermission(){

		return(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1);

	}

	private void setAsWallpaper(){
		Bitmap bitmap = dv.getDrawingCache();

		WallpaperManager myWallpaperManager
				= WallpaperManager.getInstance(getApplicationContext());
		if(bitmap!=null) {
			try {
				myWallpaperManager.setBitmap(bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
		switch(permsRequestCode){
			case REQUEST_EXTERNAL_STORAGE:
				if(hasPermission(WRITE_EXTERNAL_STORAGE) && hasPermission(READ_EXTERNAL_STORAGE)){
					Toast.makeText(this,"Success",Toast.LENGTH_LONG).show();
					saveMethod();
				}
				break;
		}
	}

	private boolean hasPermission(String permission){
		if(shouldAskPermission()){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				return(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED);
			}
		}
		return true;
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		delayedHide();
	}


	private void hide() {
		ActionBar actionBar = getSupportActionBar();
		mVisible = false;

		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	@SuppressLint("InlinedApi")
	private void show() {
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;

		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	private void delayedHide() {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, 100);
	}

	public class DrawingView extends View {
		private static final float TOUCH_TOLERANCE = 4;
		final Context context;
		private final Path mPath,mPath2,mPath3,mPath4;
		private final Paint mBitmapPaint;
		private final Paint circlePaint;
		private final Path circlePath;
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private float mX, mY;

		public DrawingView(Context c) {
			super(c);
			context = c;
			mPath = new Path();
			mPath2 = new Path();
			mPath3 = new Path();
			mPath4 = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			circlePaint = new Paint();
			circlePath = new Path();
			circlePaint.setAntiAlias(true);
			circlePaint.setStyle(Paint.Style.STROKE);
			circlePaint.setStrokeJoin(Paint.Join.MITER);
			circlePaint.setStrokeWidth(4f);
			setDrawingCacheEnabled(true);
		}


		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			canvas.drawPath(mPath, mPaint);
			canvas.drawPath(mPath2, mPaint);
			canvas.drawPath(mPath3, mPaint);
			canvas.drawPath(mPath4, mPaint);
			canvas.drawPath(circlePath, circlePaint);
		}

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath2.reset();
			mPath3.reset();
			mPath4.reset();
			mPath.moveTo(x, y);
			mPath2.moveTo(mCanvas.getWidth()-x, mCanvas.getHeight()-y);
			mPath3.moveTo(mCanvas.getWidth()-x, y);
			mPath4.moveTo(x, mCanvas.getHeight()-y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mPath2.quadTo(mCanvas.getWidth()-mX, mCanvas.getHeight()-mY, mCanvas.getWidth()-(x + mX) / 2, mCanvas.getHeight()-(y + mY) / 2);
				mPath3.quadTo(mCanvas.getWidth()-mX, mY, mCanvas.getWidth()-(x + mX) / 2, (y + mY) / 2);
				mPath4.quadTo(mX, mCanvas.getHeight()-mY, (x + mX) / 2, mCanvas.getHeight()-(y + mY) / 2);
				mX = x;
				mY = y;
				circlePath.reset();
				circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			mPath2.lineTo(mCanvas.getWidth()-mX, mCanvas.getHeight()-mY);
			mPath3.lineTo(mCanvas.getWidth()-mX, mY);
			mPath4.lineTo(mX, mCanvas.getHeight()-mY);

			circlePath.reset();
			mCanvas.drawPath(mPath, mPaint);
			mCanvas.drawPath(mPath2, mPaint);
			mCanvas.drawPath(mPath3, mPaint);
			mCanvas.drawPath(mPath4, mPaint);
			mPath.reset();
			mPath2.reset();
			mPath3.reset();
			mPath4.reset();
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					touch_start(x, y);
					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:
					touch_move(x, y);
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					touch_up();
					invalidate();
					break;
			}
			return true;
		}
	}
}
