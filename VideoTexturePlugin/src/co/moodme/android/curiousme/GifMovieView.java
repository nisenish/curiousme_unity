package co.moodme.android.curiousme;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

public class GifMovieView extends View {
	private Movie mMovie;
	InputStream mStream;
	private long mMoviestart;
	
	public GifMovieView(Context context, InputStream stream) {
        super(context);

        mStream = stream;
        mMovie = Movie.decodeStream(mStream);
    }
	
    @Override
    protected void onDraw(Canvas canvas) {
       canvas.drawColor(Color.TRANSPARENT);
       super.onDraw(canvas);
       final long now = SystemClock.uptimeMillis();
       Paint p = new Paint();
       p.setAntiAlias(true);
       if (mMoviestart == 0) {
          mMoviestart = now;
       }
       int relTime = (int)((now - mMoviestart) % mMovie.duration());
       mMovie.setTime(relTime);
       try {
    	   mMovie.draw(canvas, 0, 0);
       } catch (Exception e) {
           Log.e("LeezaRicci", "LeezaRicci oops");
    	   e.printStackTrace();
       }
       Log.e("LeezaRicci", "LeezaRicci 9");
       this.invalidate();
       Log.e("LeezaRicci", "LeezaRicci 10");
    }

}
