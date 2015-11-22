package com.sogwangtec.beijingsunyong;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressLint("InlinedApi")
public class LoginActivity extends Activity {
	
	private Bitmap			backgroundBmp_portrait;
	private Bitmap			backgroundBmp_landscape;
	private Bitmap			exitBmp;
	ImageView 				backgroundImageView;
	final Context context = this;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		readResources();
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		
		RelativeLayout guideLayout = new RelativeLayout(this);
		guideLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		this.setContentView(guideLayout);
		guideLayout.setBackgroundColor(Color.BLACK);
		
		backgroundImageView = new ImageView(this);
		if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
			backgroundImageView.setImageBitmap(backgroundBmp_landscape);
		else
			backgroundImageView.setImageBitmap(backgroundBmp_portrait);
		backgroundImageView.setBackgroundColor(Color.BLACK);
		backgroundImageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		guideLayout.addView(backgroundImageView);
		
		ImageView exitButton = new ImageView(this);
		exitButton.setImageBitmap(exitBmp);
		guideLayout.addView(exitButton);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)exitButton.getLayoutParams();
		params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
		params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
		exitButton.setLayoutParams(params);

		
		exitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
				startActivity(intent);
				finish();				
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	backgroundImageView.setImageBitmap(backgroundBmp_landscape);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	backgroundImageView.setImageBitmap(backgroundBmp_portrait);
	    }
	}

	private void readResources() 
	{
        try {
        	InputStream in = getAssets().open("firstscreen_portrait.png");
        	backgroundBmp_portrait = BitmapFactory.decodeStream(in);
        	in.close();

        	in = getAssets().open("firstscreen_landscape.png");
        	backgroundBmp_landscape = BitmapFactory.decodeStream(in);
        	in.close();

        	in = getAssets().open("exit.png");
        	exitBmp = BitmapFactory.decodeStream(in);
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
}
