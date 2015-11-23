package com.sogwangtec.beijingsunyong;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import com.google.vrtoolkit.cardboard.CardboardDeviceParams;
import com.google.vrtoolkit.cardboard.CardboardDeviceParams.VerticalAlignmentType;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.plugins.unity.UnityCardboardActivity;
import com.sogwangtec.beijingsunyong.VideoTexture.MEDIAPLAYER_STATE;
import com.unity3d.player.UnityPlayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import co.moodme.android.curiousme.ShakeListener;

public class VideoActivity extends UnityCardboardActivity {
	
	public VideoTexture		videoTexture = null;
	
	private LinearLayout	controlWindow;
	private RelativeLayout	progressbarWindow;
	private ImageView		playButtonView;
	private ImageView		stopButtonView;
	private TextView		startTimeView;
	private TextView		endTimeView;
	private	TextView		shakeCenterTextView;
	
	private LinearLayout 	shakeContainerLayout;
	private	TextView		shakeLeftTextView;
	private	TextView		shakeRightTextView;
	
	private ProgressBar		downloadProgressbar;
	private ProgressBar		waitingProgressbar;
	
	private LinearLayout 	waitingContainerLayout;
	//private ProgressBar		waitingLeftProgressbar;
	//private ProgressBar		waitingRightProgressbar;
	WebView 				waitingLeftGif, waitingRightGif;
	//private TextView		switchModeView;
	private TextView		calibrationView;
	private SeekBar			playSeekBar;
	private	boolean			mediaControlShowing;
	private boolean			seekBarSelected;
	
	private Bitmap			playBmp;
	private Bitmap			pauseBmp;
	private Bitmap			stopBmp;
	private Bitmap			switchBmp;
	
	private int				duration;
	private int				curDuration;
	
	private int				shakeCount;
	private int				playCount;
	
	/* Shaker stuff */
	private Vibrator vibrator;
	private ShakeListener mShaker;
	final Context context = this;
	
	private Handler uiHandler = new Handler();
	private Handler updateHandler = new Handler();
	private Handler updateTimeHandler = new Handler();
	//private Handler waitPlayHandler = new Handler();
	private Handler renderSceneHandler = new Handler(); 
	private Handler mediaControlEnableHandler = new Handler();

	private Handler donwlodingShowControlsHandler = new Handler();
	private Handler donwlodingHideControlsHandler = new Handler();
	private Handler shakeShowControlsHandler = new Handler();
	private Handler downloadErrorControlsHandler = new Handler();

	public CardboardView		cardboardView = null;
	public boolean				calibrationSetting = false;
	//private Handler synchronizeCelibratorHandler = new Handler();
	/*
	final String			fixString = "å›ºå®š";
	final String			threedString = "VR";*/
	final String			motionString = "Motion";
	final String			touchString = "Touch";
	final String			exitString = "Exit";
	final int				weightSum = 16;
	final int 				MY_REQUEST_BLUETOOTH = 1002;
	final int				max_video = 45; // number of videos are 44 right now
	final int				max_loader = 13; // number of videos are 44 right now
	
	//17, 4, 6, 16 10, 11, 18, 20
	//final int[]				videoOrder = {1, 9, 5, 15, 14, 8, 13, 2, 7, 3, 19, 12, 17, 4, 6, 16, 10, 11, 18, 20};
	//5, 13, 10, 18, 1, 16, 20, 15, 2, 11, 8, 17, 9, 12, 3, 19, 4, 6, 7, 14
	//final int[]				playOrder = {5, 13, 10, 18, 1, 16, 20, 15, 2, 11, 8, 17, 9, 12, 3, 19, 4, 6, 7, 14};

    Random r = new Random();

	protected void onCreate(Bundle savedInstanceState)
	{
        Log.e("VideoTexture :", "1");
	    //requestWindowFeature(Window.FEATURE_NO_TITLE);
		shakeCount = playCount  = 0;
	    super.onCreate(savedInstanceState);
	    
	    readResources();

	    mediaControlShowing = false;
	    seekBarSelected = false;
	    duration = -1;
	    curDuration = 0;

        progressbarWindow = new RelativeLayout(this);
        progressbarWindow.setBackgroundColor(Color.parseColor("#00000000"));	//R:61 G:64 B:69
        RelativeLayout topRelativeLayout = new RelativeLayout(this);
        addContentView(topRelativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        topRelativeLayout.addView(progressbarWindow);
        
        RelativeLayout.LayoutParams topParams =(RelativeLayout.LayoutParams)progressbarWindow.getLayoutParams();
        topParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        topParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        
        progressbarWindow.setLayoutParams(topParams);
        progressbarWindow.setVisibility(View.GONE);

        downloadProgressbar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        downloadProgressbar.setBackgroundColor(Color.parseColor("#00000000"));
        downloadProgressbar.setMax(100);
        downloadProgressbar.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        progressbarWindow.addView(downloadProgressbar);

        LayerDrawable progressDrawable = (LayerDrawable)downloadProgressbar.getProgressDrawable();
        ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);
        progressDrawable.setDrawableByLayerId(android.R.id.background, drawable);
        
        //center
        RelativeLayout centerRelativeLayout = new RelativeLayout(this);
        addContentView(centerRelativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
        		(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()));
        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        
        waitingProgressbar = new ProgressBar(this);
        waitingProgressbar.setBackgroundColor(Color.parseColor("#00000000"));
        waitingProgressbar.setMax(100);
        waitingProgressbar.setLayoutParams(centerParams);
        waitingProgressbar.setVisibility(View.GONE);
        centerRelativeLayout.addView(waitingProgressbar);

        RelativeLayout shakeCenterRelativeLayout = new RelativeLayout(this);
        addContentView(shakeCenterRelativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        RelativeLayout.LayoutParams shakeCenterParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        		ViewGroup.LayoutParams.MATCH_PARENT
        		//(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics())
        		);
        shakeCenterParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        shakeCenterTextView = new TextView(this);
        shakeCenterTextView.setBackgroundColor(Color.TRANSPARENT);
        shakeCenterTextView.setGravity(Gravity.CENTER);
        shakeCenterTextView.setText("Shake It!");
        shakeCenterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
        shakeCenterTextView.setLayoutParams(shakeCenterParams);
        shakeCenterRelativeLayout.addView(shakeCenterTextView);
        shakeCenterTextView.setVisibility(View.GONE);

        //shake text layout
        RelativeLayout centerVRRelativeLayout = new RelativeLayout(this);
        addContentView(centerVRRelativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        RelativeLayout centerVRContainerLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams vrParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        centerVRContainerLayout.setLayoutParams(vrParam);
        centerVRRelativeLayout.addView(centerVRContainerLayout);
        centerVRContainerLayout.setBackgroundColor(Color.TRANSPARENT);

        //textview
        shakeContainerLayout = new LinearLayout(this);
        shakeContainerLayout.setOrientation(LinearLayout.HORIZONTAL);
        shakeContainerLayout.setWeightSum(2);
        shakeContainerLayout.setBackgroundColor(Color.TRANSPARENT);
        
        centerVRContainerLayout.addView(shakeContainerLayout);
               
        RelativeLayout.LayoutParams controlContainerparams =(RelativeLayout.LayoutParams)shakeContainerLayout.getLayoutParams();
        controlContainerparams.addRule(RelativeLayout.CENTER_IN_PARENT);
        controlContainerparams.width = ViewGroup.LayoutParams.MATCH_PARENT; 
        controlContainerparams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        shakeContainerLayout.setLayoutParams(controlContainerparams);
        
        shakeLeftTextView = new TextView(this);
        shakeLeftTextView.setBackgroundColor(Color.TRANSPARENT);
        shakeLeftTextView.setGravity(Gravity.CENTER);
        shakeLeftTextView.setText("Shake It!");
        shakeLeftTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
        shakeLeftTextView.setLayoutParams(shakeCenterParams);
        shakeContainerLayout.addView(shakeLeftTextView);
        
		LinearLayout.LayoutParams textViewParams = (LinearLayout.LayoutParams)shakeLeftTextView.getLayoutParams();
		textViewParams.width = 0;
		textViewParams.weight = 1;
		textViewParams.gravity = Gravity.CENTER;
		shakeLeftTextView.setLayoutParams(textViewParams);

        shakeRightTextView = new TextView(this);
        shakeRightTextView.setBackgroundColor(Color.TRANSPARENT);
        shakeRightTextView.setGravity(Gravity.CENTER);
        shakeRightTextView.setText("Shake It!");
        shakeRightTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
        shakeRightTextView.setLayoutParams(shakeCenterParams);
        shakeContainerLayout.addView(shakeRightTextView);
        
		textViewParams = (LinearLayout.LayoutParams)shakeRightTextView.getLayoutParams();
		textViewParams.width = 0;
		textViewParams.weight = 1;
		textViewParams.gravity = Gravity.CENTER;
		shakeRightTextView.setLayoutParams(textViewParams);
		
		shakeContainerLayout.setVisibility(View.VISIBLE);
		
		//waiting view
        waitingContainerLayout = new LinearLayout(this);
        waitingContainerLayout.setOrientation(LinearLayout.HORIZONTAL);
        waitingContainerLayout.setWeightSum(2);
        waitingContainerLayout.setBackgroundColor(Color.TRANSPARENT);
        
        centerVRContainerLayout.addView(waitingContainerLayout);
               
        RelativeLayout.LayoutParams waitingContainerparams =(RelativeLayout.LayoutParams)waitingContainerLayout.getLayoutParams();
        waitingContainerparams.addRule(RelativeLayout.CENTER_IN_PARENT);
        waitingContainerparams.width = ViewGroup.LayoutParams.MATCH_PARENT; 
        waitingContainerparams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        waitingContainerLayout.setLayoutParams(waitingContainerparams);
        
        
        
        // Left and Right gif views
        
        //Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
        //int catWidth = display.getWidth() / 2;
        
		int randomLoaderCode = r.nextInt(max_loader - 1) + 1;
		
        waitingLeftGif = new WebView(this);
        waitingLeftGif.loadUrl("file:///android_asset/loaders/" + randomLoaderCode + ".gif");
        //waitingLeftGif.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        waitingLeftGif.getSettings().setLoadWithOverviewMode(true);
        waitingLeftGif.getSettings().setUseWideViewPort(true);
        //waitingLeftGif.setPadding(0, 0, catWidth, 0);
        waitingLeftGif.setBackgroundColor(Color.TRANSPARENT);
        waitingLeftGif.setLayoutParams(shakeCenterParams);
        waitingContainerLayout.addView(waitingLeftGif);
        Log.e("LeezaRicci", "LeezaRicci leftview height: " + waitingLeftGif.getContentHeight() );
        
		LinearLayout.LayoutParams waitingViewParams = (LinearLayout.LayoutParams)waitingLeftGif.getLayoutParams();
		waitingViewParams.width = ViewGroup.LayoutParams.MATCH_PARENT; 
		waitingViewParams.weight = 1;
		waitingViewParams.gravity = Gravity.CENTER;
		waitingLeftGif.setLayoutParams(waitingViewParams);
		
        waitingRightGif = new WebView(this);
        waitingRightGif.loadUrl("file:///android_asset/loaders/" + randomLoaderCode + ".gif");
        //waitingRightGif.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        waitingRightGif.getSettings().setLoadWithOverviewMode(true);
        waitingRightGif.getSettings().setUseWideViewPort(true);
        //waitingRightGif.setPadding(0, 0, catWidth, 0);
        waitingRightGif.setBackgroundColor(Color.TRANSPARENT);
        waitingRightGif.setLayoutParams(shakeCenterParams);
        waitingContainerLayout.addView(waitingRightGif);
        Log.e("LeezaRicci", "LeezaRicci rightview added");
        
        
		waitingViewParams = (LinearLayout.LayoutParams)waitingRightGif.getLayoutParams();
		waitingViewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		waitingViewParams.weight = 1;
		waitingViewParams.gravity = Gravity.CENTER;
		waitingRightGif.setLayoutParams(waitingViewParams);
        
        
        
/*        
        waitingLeftProgressbar = new ProgressBar(this);
        waitingLeftProgressbar.setBackgroundColor(Color.TRANSPARENT);
        waitingLeftProgressbar.setLayoutParams(shakeCenterParams);
        waitingContainerLayout.addView(waitingLeftProgressbar);
        
		LinearLayout.LayoutParams waitingViewParams = (LinearLayout.LayoutParams)waitingLeftProgressbar.getLayoutParams();
		waitingViewParams.width = 0;
		waitingViewParams.weight = 1;
		waitingViewParams.gravity = Gravity.CENTER;
		waitingLeftProgressbar.setLayoutParams(waitingViewParams);

        waitingRightProgressbar = new ProgressBar(this);
        waitingRightProgressbar.setBackgroundColor(Color.TRANSPARENT);
        waitingRightProgressbar.setLayoutParams(shakeCenterParams);
        waitingContainerLayout.addView(waitingRightProgressbar);
        
		waitingViewParams = (LinearLayout.LayoutParams)waitingRightProgressbar.getLayoutParams();
		waitingViewParams.width = 0;
		waitingViewParams.weight = 1;
		waitingViewParams.gravity = Gravity.CENTER;
		waitingRightProgressbar.setLayoutParams(waitingViewParams);
*/		
		waitingContainerLayout.setVisibility(View.GONE);

        /////////////////////////
        controlWindow = new LinearLayout(this);
        controlWindow.setBackgroundColor(Color.parseColor("#3D4045"));	//R:61 G:64 B:69
        RelativeLayout relativeLayout = new RelativeLayout(this);
        
        controlWindow.setClickable(true);

        addContentView(relativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(controlWindow);
        controlWindow.setOrientation(LinearLayout.HORIZONTAL);
        controlWindow.setWeightSum(weightSum);

        
        stopButtonView = new ImageView(this);
        stopButtonView.setImageBitmap(stopBmp);
        controlWindow.addView(stopButtonView);

        playButtonView = new ImageView(this);
        playButtonView.setImageBitmap(playBmp);
        controlWindow.addView(playButtonView);
        
        startTimeView = new TextView(this);
        startTimeView.setGravity(Gravity.CENTER);
        startTimeView.setText("00:00:00");
        startTimeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        controlWindow.addView(startTimeView);
        
        playSeekBar = new SeekBar(this);
        controlWindow.addView(playSeekBar);

        endTimeView = new TextView(this);
        endTimeView.setGravity(Gravity.CENTER);
        endTimeView.setText("00:00:00");
        endTimeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        controlWindow.addView(endTimeView);
		
        calibrationView = new TextView(this);
        //switchModeView.setImageBitmap(switchBmp);
        Drawable d = new BitmapDrawable(getResources(), switchBmp);
        calibrationView.setBackgroundDrawable(d);
        calibrationView.setText(motionString);
        calibrationView.setGravity(Gravity.CENTER);
        calibrationView.setTextColor(Color.BLACK);
        controlWindow.addView(calibrationView);
/*
        switchModeView = new TextView(this);
        //switchModeView.setImageBitmap(switchBmp);
        d = new BitmapDrawable(getResources(), switchBmp);
        switchModeView.setBackgroundDrawable(d);
        switchModeView.setText(exitString);
        switchModeView.setGravity(Gravity.CENTER);
        switchModeView.setTextColor(Color.BLACK);
        controlWindow.addView(switchModeView);
*/
        RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams)controlWindow.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT; 
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        
        controlWindow.setLayoutParams(params);
        controlWindow.setVisibility(View.GONE);
        
        layoutSubViews();
        
        updateTimeHandler.postDelayed(updateCurTimeUI, 20);
        mediaControlEnableHandler.postDelayed(controlEnableThread, 1 * 1000);
        //bluetoothHandler.postDelayed(enableBluetoothThread, 1000 * 60);
        //
        UnityPlayer.UnitySendMessage("Head", "enableCamera", "");

        initParams();

    	/* Shaker stuff */
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mShaker = new ShakeListener(this);
		mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				vibrator.vibrate(100);

				int randomVideoCode = r.nextInt(max_video - 1) + 1;

				showDownlodingControls();
				//loadVideo("http://www.moodme.co/curiousme/content/" + randomVideoCode + ".mp4");
				//UnityPlayer.UnitySendMessage("videoSphere", "DownloadVideo", "http://www.moodme.co/curiousme/content/" + randomVideoCode + ".mp4");
				videoTexture.Stop();
				switchingPlayButtonImage();
				//setUpdateUI(0);
				Stop();
//				UnityPlayer.UnitySendMessage("videoSphere", "DownloadVideo", "http://www.moodme.co/curiousme/content/" + randomVideoCode + ".mp4");
//				UnityPlayer.UnitySendMessage("videoSphere", "DownloadAudio", "http://www.moodme.co/curiousme/content/" + randomVideoCode + ".mp3");
/*
				if( shakeCount < 20 )
					UnityPlayer.UnitySendMessage("videoSphere", "DownloadVideo", "http://www.moodme.co/curiousme/content/" + videoOrder[shakeCount++] + ".mp4");
				else {
					UnityPlayer.UnitySendMessage("videoSphere", "DownloadVideo", "http://www.moodme.co/curiousme/content/" + videoOrder[(playCount++) % (max_video - 1)] + ".mp4");
				}
*/
				Log.e("VideoTexture :", "VideoTexture test log: " + randomVideoCode);
				
                UnityPlayer.UnitySendMessage("videoSphere", "DownloadVideo", "http://www.moodme.co/curiousme/content/" + randomVideoCode + ".mp4");

				//UnityPlayer.UnitySendMessage("videoSphere", "DownloadVideo", randomVideoCode + ".mp4");
				//videoTexture.Load("http://www.moodme.co/curiousme/content/" + randomVideoCode + ".mp4", 0);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void initParams() {
		cardboardView = this.getCardboardView();
		/*ScreenParams sparam = view.getScreenParams();
		sparam.setHeight(0);
		view.updateScreenParams(sparam);
		*/
		CardboardDeviceParams dparam = cardboardView.getCardboardDeviceParams();
		dparam.setVerticalAlignment(VerticalAlignmentType.TOP);
		dparam.setScreenToLensDistance(0.038f);
	
		cardboardView.updateCardboardDeviceParams(dparam);
	}
	
	private void layoutSubViews()
	{		
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)playButtonView.getLayoutParams();
		params.width = 0;
		params.weight = 1;
		params.gravity = Gravity.CENTER;
		playButtonView.setLayoutParams(params);
		
		params = (LinearLayout.LayoutParams)stopButtonView.getLayoutParams();
		params.width = 0;
		params.weight = 1;
		params.gravity = Gravity.CENTER;
		stopButtonView.setLayoutParams(params);

		params = (LinearLayout.LayoutParams)startTimeView.getLayoutParams();
		params.width = 0;
		params.weight = 2;
		params.gravity = Gravity.CENTER;
		startTimeView.setLayoutParams(params);

		params = (LinearLayout.LayoutParams)playSeekBar.getLayoutParams();
		params.width = 0;
        params.weight = 8;
        params.gravity = Gravity.CENTER;
        playSeekBar.setLayoutParams(params);
        playSeekBar.setMax(0);
        playSeekBar.setProgress(0);

		params = (LinearLayout.LayoutParams)endTimeView.getLayoutParams();
		params.width = 0;
        params.weight = 2;
        params.gravity = Gravity.CENTER;
        endTimeView.setLayoutParams(params);

		params = (LinearLayout.LayoutParams)calibrationView.getLayoutParams();
		params.width = 0;
        params.weight = 2;
        params.gravity = Gravity.CENTER;
        params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        calibrationView.setLayoutParams(params);
        //playSeekBar.getProgressDrawable().setColorFilter(Color.RED, Mode.SRC_IN);
        //playSeekBar.setBackgroundColor(Color.BLUE);
/*        
		params = (LinearLayout.LayoutParams)switchModeView.getLayoutParams();
		params.width = 0;
        params.weight = 1;
        params.gravity = Gravity.CENTER;
        params.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        switchModeView.setLayoutParams(params);
*/        
	}
	
	private void readResources() 
	{
        try {
        	InputStream in = getAssets().open("play.png");
        	playBmp = BitmapFactory.decodeStream(in);
        	in.close();
        	
        	in = getAssets().open("pause.png");
        	pauseBmp = BitmapFactory.decodeStream(in);
        	in.close();

        	in = getAssets().open("stop.png");
        	stopBmp = BitmapFactory.decodeStream(in);
        	in.close();
        	
        	in = getAssets().open("option.png");
        	switchBmp = BitmapFactory.decodeStream(in);
        	in.close();
        	//Bitmap
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("VideoTexture :", "VideoTexture Bitmap exception" + e.getMessage());
			e.printStackTrace();
		}
        //this.m_MediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        //afd.close();

	}
	/*
	@Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);          
    }
	*/
	@Override
	public void onResume() {
		super.onResume();
		if( videoTexture != null ) {
			if( videoTexture.m_iCurrentState != MEDIAPLAYER_STATE.READY ) {
				videoTexture.m_iCurrentState = MEDIAPLAYER_STATE.PLAYING;
				videoTexture.Pause();
			//videoTexture.m_iCurrentState = MEDIAPLAYER_STATE.PAUSED;
			//videoTexture.Play(videoTexture.GetSeekPosition());
				this.switchingPlayButtonImage();
			}

    		uiHandler.removeCallbacks(updateMediaPlayUI);
    		controlWindow.setVisibility(View.VISIBLE);
    		uiHandler.postDelayed(updateMediaPlayUI, 3 * 1000);
    		
    		//UnityPlayer.UnitySendMessage("Head", "calibrationCamera", "");
		}
	}
	
	public void switchingPlayButtonImage()
	{
		if( videoTexture.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING ) {
			playButtonView.setImageBitmap(pauseBmp);
		}
		else {
			playButtonView.setImageBitmap(playBmp);
		}
	}
//	
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		super.dispatchTouchEvent(event);
		
		if( mediaControlShowing )
			StereoProcess(event);
		return true;
	}

	private void StereoProcess(MotionEvent event) {
		switch( event.getAction() ) {
		case MotionEvent.ACTION_DOWN:
			this.seekBarSelected = guessSeekBar(event);
			break;
		
		case MotionEvent.ACTION_MOVE: 
		{
			if( this.seekBarSelected ) {
				this.moveSeekThumb(event);
			}
		}
		break;
		
		case MotionEvent.ACTION_UP:
		{
			if( seekBarSelected ) {
				if( this.videoTexture.m_iCurrentState == MEDIAPLAYER_STATE.END ||
						this.videoTexture.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED ) {
							renderSceneHandler.postDelayed(RenderAndPause, 5 * 33);		//1s per 30 frames
				}
				
				videoTexture.updateMediaPosition(curDuration);
			}
			seekBarSelected = false;

			if( controlWindow.getVisibility() == View.VISIBLE ) {
				Rect playButtonFrame = new Rect(((View)(playButtonView.getParent())).getX() + playButtonView.getX(),
						((View)(playButtonView.getParent())).getY() + playButtonView.getY(),
						playButtonView.getWidth(), playButtonView.getHeight());

				if( playButtonFrame.containedXY(event.getX(), event.getY()) ) {
					try {
						videoTexture.dispatchPlayEvent(false);
						switchingPlayButtonImage();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					//stop button
					Rect stopButtonFrame = new Rect(((View)(stopButtonView.getParent())).getX() + stopButtonView.getX(),
							((View)(stopButtonView.getParent())).getY() + stopButtonView.getY(),
							stopButtonView.getWidth(), stopButtonView.getHeight());
					if( stopButtonFrame.containedXY(event.getX(), event.getY()) ) {
						try {
							videoTexture.dispatchPlayEvent(true);
							switchingPlayButtonImage();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						//stop button
						/*Rect switchButtonFrame = new Rect(((View)(switchModeView.getParent())).getX() + switchModeView.getX(),
								((View)(switchModeView.getParent())).getY() + switchModeView.getY(),
								switchModeView.getWidth(), switchModeView.getHeight());
						if( switchButtonFrame.containedXY(event.getX(), event.getY()) ) {
							try {
								/*switchFixMode = !switchFixMode;
								switchModeView.setText(switchFixMode ? this.threedString : this.fixString);
								if( switchFixMode )
									UnityPlayer.UnitySendMessage("Head", "disableCamera", "");
								else
									UnityPlayer.UnitySendMessage("Head", "enableCamera", "");
								* /
								//finish();
								 Intent startMain = new Intent(Intent.ACTION_MAIN);
							     startMain.addCategory(Intent.CATEGORY_HOME);
							     startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							     startActivity(startMain);
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else*/ {
							Rect calibrationButtonFrame = new Rect(((View)(calibrationView.getParent())).getX() + calibrationView.getX(),
									((View)(calibrationView.getParent())).getY() + calibrationView.getY(),
									calibrationView.getWidth(), calibrationView.getHeight());
							if( calibrationButtonFrame.containedXY(event.getX(), event.getY()) ) {
								try {
									UnityPlayer.UnitySendMessage("Head", "calibrationCamera", "");
									calibrationSetting = !calibrationSetting;
									this.calibrationView.setText(calibrationSetting ? touchString : motionString);
									
									if( waitingProgressbar.getVisibility() == View.VISIBLE ||
										waitingContainerLayout.getVisibility() == View.VISIBLE ) 
										showDownlodingControls();
									else if( shakeCenterTextView.getVisibility() == View.VISIBLE ||
											 shakeContainerLayout.getVisibility() == View.VISIBLE )
												showShakeControls();
										
									//Toast.makeText(this, "æ–¹å�‘æ ¡å‡†", Toast.LENGTH_SHORT).show();
									//ComponentName devAdminReceiver; // this would have been declared in your class body
									//devAdminReceiver = new ComponentName(this, deviceAdminReceiver.class);
									//mDPM.lockNow();
									//Common.mPolicyManager.lockNow();
									//index = 0;
									//initValue = true;
									//yBasePos = 0;
								} catch (SecurityException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalStateException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							else {						
								if( mediaControlShowing == false )
									return;
								
								
								if( controlWindow.getVisibility() == View.GONE ) {
									controlWindow.setVisibility(View.VISIBLE);
									uiHandler.postDelayed(updateMediaPlayUI, 8 * 1000);
								}
								else if( controlWindow.getVisibility() == View.VISIBLE ) {
									
									Rect controlFrame = new Rect(controlWindow.getX(), controlWindow.getY(), controlWindow.getWidth(), controlWindow.getHeight());
									if( controlFrame.containedXY(event.getX(), event.getY()) ) {
										//guessSeekBarEvent(event);
										return;
									}

									controlWindow.setVisibility(View.GONE);
									uiHandler.removeCallbacks(updateMediaPlayUI);
								}	//else
							}	//else
						}	//else
					}	//else
				}	//else
			}	//if
			else {
				
				//if( mediaControlShowing == false )
				//	return;
				controlWindow.setVisibility(View.VISIBLE);
				uiHandler.postDelayed(updateMediaPlayUI, 8 * 1000);
			}

				break;
			}
		}
	}
	
	private boolean guessSeekBar(MotionEvent event) {
		if( controlWindow.getVisibility() == View.GONE )
			return false;
		
		Rect seekBarBounds = new Rect(controlWindow.getX() + playSeekBar.getX(), controlWindow.getY() + playSeekBar.getY(),
				playSeekBar.getWidth(), playSeekBar.getHeight());
		
		if( seekBarBounds.containedXY(event.getX(), event.getY()) ) {
			//
			float tapPos = event.getX() - seekBarBounds.mx;
			int seekPos = (int)(this.duration * tapPos * 1.0f / seekBarBounds.mWidth);
			playSeekBar.setProgress(seekPos);
			
			return true;
		}
		
		return false;
	}
	
	private void moveSeekThumb(MotionEvent event) {
		Rect seekBarBounds = new Rect(controlWindow.getX() + playSeekBar.getX(), controlWindow.getY() + playSeekBar.getY(),
				playSeekBar.getWidth(), playSeekBar.getHeight());
		
		float tapPos = event.getX() - seekBarBounds.mx;
		int seekPos = (int)(this.duration * tapPos * 1.0f / seekBarBounds.mWidth);
		
		if( this.videoTexture.m_iCurrentState == MEDIAPLAYER_STATE.END ||
			this.videoTexture.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED ) {
				renderSceneHandler.postDelayed(RenderAndPause, 5 * 33);		//1s per 30 frames
		}

		curDuration = seekPos;
		playSeekBar.setProgress(seekPos);
		videoTexture.updateMediaPosition(seekPos);		
	}
	
	//
	public void setDuration(int _duration) {
		if( this.duration == _duration )
			return;
		
		this.duration = _duration;
		playSeekBar.setMax(_duration);

		curDuration = 0;
		updateHandler.postDelayed(updateText, 20);
	}
	
	public void setUpdateUI(int _pos) {
		if( seekBarSelected )
			return;
		
		curDuration = _pos;
		playSeekBar.setProgress(_pos);
	}
	
	public void Stop() {
		playSeekBar.setProgress(0);
		curDuration = 0;
	}
	
	public void DownloadError() {
		downloadErrorControlsHandler.postDelayed(downloadErrorThread, 50);
	}
	
    String formatTime(int timeValue) {
		int sec = timeValue / 1000;
		int h = sec / 3600;
		int m = (sec % 3600) / 60;
		int s = sec % 60;
		
        String str_hours = String.valueOf(h);
        if( h < 10 )
            str_hours = "0" + str_hours;
        String str_mins = String.valueOf(m);
        if( m < 10 )
            str_mins = "0" + str_mins;
        String str_secs = String.valueOf(s);
        if( s < 10 )
            str_secs = "0" + str_secs;
        if( str_secs.equals("60") )
            str_secs = "59";
        
        return str_hours + ":" + str_mins + ":" + str_secs;
    }
    
    public void progressbarStatus(float value) {
    	downloadProgressbar.setProgress((int)(value * 100));
    }
    

	private Runnable updateMediaPlayUI = new Runnable() {
        public void run() {
            //update ui interface
        	controlWindow.setVisibility(View.GONE);
        }

    };

	private Runnable updateText = new Runnable() {
        public void run() {
        	endTimeView.setText(formatTime(duration));
        }

    };
    
    //updateCurTimeUI
	private Runnable updateCurTimeUI = new Runnable() {
        public void run() {
        	if( curDuration <= duration ) {
        		startTimeView.setText(formatTime(curDuration));
        	}
        	
        	updateTimeHandler.postDelayed(updateCurTimeUI, 20);
        }

    };
    
    private Runnable RenderAndPause = new Runnable() {
    	public void run() {
    		//videoTexture.RenderScene();
    		videoTexture.Pause();
    	}
    };
    
    private Runnable controlEnableThread = new Runnable() {
    	public void run() {
    		mediaControlShowing = true;
    		
    		uiHandler.removeCallbacks(updateMediaPlayUI);
    		controlWindow.setVisibility(View.VISIBLE);
    		uiHandler.postDelayed(updateMediaPlayUI, 3 * 1000);
    	}
    };
    
    //other controls
    public void showDownlodingControls() {
    	donwlodingShowControlsHandler.postDelayed(downlodingShowControlsThread, 50);
    }

    public void hideDownlodingControls() {
    	donwlodingHideControlsHandler.postDelayed(downlodingHideControlsThread, 50);
    }

    public void showShakeControls() {
    	shakeShowControlsHandler.postDelayed(shakeShowControlsThread, 50);
    }

    private Runnable downlodingShowControlsThread = new Runnable() {
    	public void run() {
    		shakeContainerLayout.setVisibility(View.GONE);
    		shakeCenterTextView.setVisibility(View.GONE);
    		
    		progressbarWindow.setVisibility(View.VISIBLE);
    		if( calibrationSetting ) {//touch
    			waitingProgressbar.setVisibility(View.VISIBLE);
    			waitingContainerLayout.setVisibility(View.GONE);
    		}
    		else {
    			waitingProgressbar.setVisibility(View.GONE);
    			waitingContainerLayout.setVisibility(View.VISIBLE);    			
    		}
    	}
    };

    private Runnable downlodingHideControlsThread = new Runnable() {
    	public void run() {
    		shakeContainerLayout.setVisibility(View.GONE);
    		shakeCenterTextView.setVisibility(View.GONE);
    		
    		progressbarWindow.setVisibility(View.GONE);
   			waitingContainerLayout.setVisibility(View.GONE);
   			waitingProgressbar.setVisibility(View.GONE);
    	}
    };
    
    private Runnable shakeShowControlsThread = new Runnable() {
    	public void run() {
			shakeContainerLayout.setVisibility(View.GONE);
			shakeCenterTextView.setVisibility(View.GONE);

    		if( calibrationSetting ) {
    			shakeCenterTextView.setVisibility(View.VISIBLE);
    		}
    		else {
    			shakeContainerLayout.setVisibility(View.VISIBLE);
    		}
    	}
    };
    
    //
    private Runnable downloadErrorThread = new Runnable() {
    	public void run() {
    		Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show();
    		hideDownlodingControls();
    		showShakeControls();
    	}
    };
    
	private class Rect {
		float mx;
		float my;
		float mWidth;
		float mHeight;
		
		Rect(float x, float y, float width, float height) {
			mx = x;
			my = y;
			mWidth = width;
			mHeight = height;
		}
		
		boolean containedXY(float x, float y) {
			if( mx <= x && x <= (mx + mWidth) &&
				my <= y && y <= (my + mHeight) )
					return true;
			return false;
		}
	}
}
