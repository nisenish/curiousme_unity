package co.moodme.android.curiousme;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GifView extends View {
 
 private InputStream gifInputStream;
 private Movie gifMovie;
 private int movieWidth, movieHeight;
 private long movieDuration;
 private long mMovieStart;

 public GifView(Context context) {
  super(context);
  init(context);
 }

 public GifView(Context context, AttributeSet attrs) {
  super(context, attrs);
  init(context);
 }

 public GifView(Context context, AttributeSet attrs, 
   int defStyleAttr) {
  super(context, attrs, defStyleAttr);
  init(context);
 }
 
 private void init(Context context){
  setFocusable(true);
 // TODO:use this for video streaming: File f = new File(getCacheDir()+"/m1.map");
  try {
	  gifInputStream = context.getAssets().open("giphy.gif");
	  int size = gifInputStream.available();
	  //Log.e("LeezaRicci", "LeezaRicci " + size);
	  gifMovie = Movie.decodeStream(gifInputStream);
	  //Log.e("LeezaRicci", "LeezaRicci decoded");
	  movieWidth = gifMovie.width();
	  //Log.e("LeezaRicci", "LeezaRicci movieWidth:" + movieWidth);
	  movieHeight = gifMovie.height();
	  //Log.e("LeezaRicci", "LeezaRicci movieHeight:" + movieHeight);
	  movieDuration = gifMovie.duration();
	  //Log.e("LeezaRicci", "LeezaRicci movieDuration:" + movieDuration);
  } catch (IOException e) {
	  e.printStackTrace();
  }
 }

 @Override
 protected void onMeasure(int widthMeasureSpec, 
   int heightMeasureSpec) {
  setMeasuredDimension(movieWidth, movieHeight);
 }
 
 public int getMovieWidth(){
  return movieWidth;
 }
 
 public int getMovieHeight(){
  return movieHeight;
 }
 
 public long getMovieDuration(){
  return movieDuration;
 }

 @Override
 protected void onDraw(Canvas canvas) {

  long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {   // first time
            mMovieStart = now;
        }
        
        if (gifMovie != null) {

            int dur = gifMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }

            int relTime = (int)((now - mMovieStart) % dur);
            
            gifMovie.setTime(relTime);

            gifMovie.draw(canvas, 0, 0);
            invalidate();
            
        }
        
 }

}