package com.sogwangtec.beijingsunyong;

//import com.sogwangtec.beijingsunyong.RemoteControlReceiver;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;
//
/*import android.media.session.MediaSession;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
*/
//
//import com.android.vending.expansion.zipfile.ZipResourceFile;
//import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class VideoTexture
  implements MediaPlayer.OnPreparedListener, 
  			 MediaPlayer.OnBufferingUpdateListener, 
  			 MediaPlayer.OnCompletionListener, 
  			 MediaPlayer.OnErrorListener, 
  			 SurfaceTexture.OnFrameAvailableListener
{
  private VideoActivity m_UnityActivity = null;
  private MediaPlayer m_MediaPlayer = null;
  private MediaPlayer m_musicMediaPlayer = null;

  private int m_iUnityTextureID = -1;
  private int m_iSurfaceTextureID = -1;
  private SurfaceTexture m_SurfaceTexture = null;
  private Surface m_Surface = null;
  private int m_iCurrentSeekPercent = 0;
  private int m_iCurrentSeekPosition = 0;
  private int m_iNativeMgrID;
  private String m_strFileName;
  private int m_iErrorCode;
  private int m_iErrorCodeExtra;
  private boolean m_bRockchip = true;
  private boolean m_bSplitOBB = false;
  private String m_strOBBName;
  public boolean m_bUpdate = false;
  public String m_strAudioFileName;

  private static final int GL_TEXTURE_EXTERNAL_OES = 36197;
  MEDIAPLAYER_STATE m_iCurrentState = MEDIAPLAYER_STATE.NOT_READY;
  
  //private AudioManager am;			//for media button

  static
  {
    System.loadLibrary("VideoTexture");
  } 
  public native int InitApplication(AssetManager paramAssetManager);

  public native void QuitApplication();

  public native void InitView();

  public native void ReleaseView();

  public native void SetWindowSize(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean);

  public native void RenderScene(float[] paramArrayOfFloat, int paramInt1, int paramInt2);

  public native void SetManagerID(int paramInt);

  public native int GetManagerID();

  public native int InitExtTexture();

  public native void SetUnityTextureID(int paramInt);

  public void Destroy() { if (this.m_iSurfaceTextureID != -1)
    {
      int[] textures = new int[1];
      textures[0] = this.m_iSurfaceTextureID;
      GLES20.glDeleteTextures(1, textures, 0);
      this.m_iSurfaceTextureID = -1;
    }

    SetManagerID(this.m_iNativeMgrID);
    QuitApplication();
  }

  public void UnLoad()
  {
	  releaseMediaPlayer();
	  releaseMusicPlayer();
  }
  
  private void releaseMediaPlayer() {
    if (this.m_MediaPlayer != null)
    {
      if (this.m_iCurrentState != MEDIAPLAYER_STATE.NOT_READY)
      {
        try {
          this.m_MediaPlayer.stop();
          this.m_MediaPlayer.release();
        }
        catch (SecurityException e)
        {
          e.printStackTrace();
        }
        catch (IllegalStateException e) {
          e.printStackTrace();
        }
        this.m_MediaPlayer = null;
      }
      else
      {
        try
        {
          this.m_MediaPlayer.release();
        }
        catch (SecurityException e)
        {
          e.printStackTrace();
        }
        catch (IllegalStateException e) {
          e.printStackTrace();
        }
        this.m_MediaPlayer = null;
      }

      if (this.m_Surface != null)
      {
        this.m_Surface.release();
        this.m_Surface = null;
      }

      if (this.m_SurfaceTexture != null)
      {
        this.m_SurfaceTexture.release();
        this.m_SurfaceTexture = null;
      }

      if (this.m_iSurfaceTextureID != -1)
      {
        int[] textures = new int[1];
        textures[0] = this.m_iSurfaceTextureID;
        GLES20.glDeleteTextures(1, textures, 0);
        this.m_iSurfaceTextureID = -1;
      }
    }
  }
  
  private void releaseMusicPlayer() {
    if (this.m_musicMediaPlayer != null)
    {
      if (this.m_iCurrentState != MEDIAPLAYER_STATE.NOT_READY)
      {
        try {
          this.m_musicMediaPlayer.stop();
          this.m_musicMediaPlayer.release();
        }
        catch (SecurityException e)
        {
          e.printStackTrace();
        }
        catch (IllegalStateException e) {
          e.printStackTrace();
        }
        this.m_musicMediaPlayer = null;
      }
      else
      {
        try
        {
          this.m_musicMediaPlayer.release();
        }
        catch (SecurityException e)
        {
          e.printStackTrace();
        }
        catch (IllegalStateException e) {
          e.printStackTrace();
        }
        this.m_musicMediaPlayer = null;
      }
    }
  }

  public boolean Load(String strFileName, int iSeekPosition) throws SecurityException, IllegalStateException, IOException
  {
    UnLoad();
    
    this.m_UnityActivity.hideDownlodingControls();

    this.m_iCurrentState = MEDIAPLAYER_STATE.NOT_READY;

    this.m_strFileName = strFileName;

    this.m_MediaPlayer = new MediaPlayer();
    this.m_MediaPlayer.reset();
    this.m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    this.m_MediaPlayer.setWakeMode(m_UnityActivity.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

    this.m_musicMediaPlayer = new MediaPlayer();
    this.m_musicMediaPlayer.reset();
    this.m_musicMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    this.m_bUpdate = false;

    //video play setting
    if (strFileName.contains("file://"))
    {
      File sourceFile = new File(strFileName.replace("file://", ""));

      if (sourceFile.exists())
      {
        FileInputStream fs = new FileInputStream(sourceFile);
        this.m_MediaPlayer.setDataSource(fs.getFD());
        fs.close();
      }
    }
    else if (strFileName.contains("://"))
    {
    	Log.e("VideoTexture", "VideoTexture " + strFileName);
      try {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("rtsp_transport", "tcp");
        headers.put("max_analyze_duration", "500");
        this.m_MediaPlayer.setDataSource(this.m_UnityActivity, Uri.parse(strFileName), headers);
      }
      catch (IOException e)
      {
        Log.e("Unity", "Error m_MediaPlayer.setDataSource() : " + strFileName);
        e.printStackTrace();

        return false;
      }

    }
    else if (this.m_bSplitOBB)
    {
//      try
//      {
//        ZipResourceFile expansionFile = new ZipResourceFile(this.m_strOBBName);
//
//        Log.e("unity", this.m_strOBBName + " " + strFileName);
//        AssetFileDescriptor afd = expansionFile.getAssetFileDescriptor("assets/" + strFileName);
//
//        ZipResourceFile.ZipEntryRO[] data = expansionFile.getAllEntries();
//
//        for (int i = 0; i < data.length; i++)
//        {
//          Log.e("unity", data[i].mFileName);
//        }
//
//        Log.e("unity", afd + " ");
//        this.m_MediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//      }
//      catch (IOException e)
//      {
//        e.printStackTrace();
//      }
    }
    else
    {
      try
      {
        AssetFileDescriptor afd = this.m_UnityActivity.getAssets().openFd(strFileName);
        this.m_MediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
      }
      catch (IOException e) {
        Log.e("Unity", "Error m_MediaPlayer.setDataSource() : " + strFileName);
        e.printStackTrace();
        return false;
      }

    }

    if (this.m_iSurfaceTextureID == -1)
    {
      this.m_iSurfaceTextureID = InitExtTexture();
    }

    this.m_SurfaceTexture = new SurfaceTexture(this.m_iSurfaceTextureID);
    this.m_SurfaceTexture.setOnFrameAvailableListener(this);
    this.m_Surface = new Surface(this.m_SurfaceTexture);

    this.m_MediaPlayer.setSurface(this.m_Surface);
    this.m_MediaPlayer.setOnPreparedListener(this);
    this.m_MediaPlayer.setOnCompletionListener(this);
    this.m_MediaPlayer.setOnErrorListener(this);

    this.m_MediaPlayer.prepareAsync();
    
    //Audio Player setting
    if (m_strAudioFileName.contains("file://"))
    {
      File sourceFile = new File(m_strAudioFileName.replace("file://", ""));

      if (sourceFile.exists())
      {
        FileInputStream fs = new FileInputStream(sourceFile);
        this.m_musicMediaPlayer.setDataSource(fs.getFD());
        fs.close();
      }
    }
    else if (m_strAudioFileName.contains("://"))
    {
      try {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("rtsp_transport", "tcp");
        headers.put("max_analyze_duration", "500");
        this.m_musicMediaPlayer.setDataSource(this.m_UnityActivity, Uri.parse(m_strAudioFileName), headers);
      }
      catch (IOException e)
      {
        Log.e("Unity", "Error m_MediaPlayer.setDataSource() : " + m_strAudioFileName);
        e.printStackTrace();

        return false;
      }

    }
    else
    {
      try
      {
        AssetFileDescriptor afd = this.m_UnityActivity.getAssets().openFd(m_strAudioFileName);
        this.m_musicMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
      }
      catch (IOException e) {
        Log.e("VideoTexture", "VideoTexture Error m_musicMediaPlayer.setDataSource() : " + m_strAudioFileName);
        e.printStackTrace();
        return false;
      }

    }

    this.m_musicMediaPlayer.prepareAsync();
    this.m_musicMediaPlayer.setOnPreparedListener(this);
    m_musicMediaPlayer.setLooping(true);
    return true;
  }
  
  public synchronized void onFrameAvailable(SurfaceTexture surface)
  {
    this.m_bUpdate = true;
  }

  public void UpdateVideoTexture()
  {
	if( this.m_iCurrentState != MEDIAPLAYER_STATE.PLAYING ) {
		this.m_UnityActivity.setUpdateUI(this.m_MediaPlayer.getCurrentPosition());
		return;
	}
	
    if (!this.m_bUpdate) {
      return;
    }
    
    if (this.m_MediaPlayer != null)
    {
      if (this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING)
      {
    	  RenderScene();
          this.m_UnityActivity.setDuration(this.m_MediaPlayer.getDuration());
          this.m_UnityActivity.setUpdateUI(this.m_MediaPlayer.getCurrentPosition());
      }
    }
  }

  public void RenderScene() {
      SetManagerID(this.m_iNativeMgrID);

      boolean[] abValue = new boolean[1];
      GLES20.glGetBooleanv(GLES20.GL_DEPTH_TEST, abValue, 0);
      GLES20.glDisable(GLES20.GL_DEPTH_TEST);
      this.m_SurfaceTexture.updateTexImage();

      float[] mMat = new float[16];

      this.m_SurfaceTexture.getTransformMatrix(mMat);
/*
      if( this.m_UnityActivity.cardboardView != null ) {
    	  if( this.m_UnityActivity.calibrationSetting ) {
    		  this.m_UnityActivity.cardboardView.resetHeadTracker();
    		  this.m_UnityActivity.calibrationSetting = false;
    	  }
      }
*/      
      RenderScene(mMat, this.m_iSurfaceTextureID, this.m_iUnityTextureID);

      if (abValue[0] != false)
      {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
      }

      abValue = null;
      
      //set ui range
  }
  
  public void HeadLog(float x, float y, float z) {
	  Log.e("VideoTexture", "VideoTexture = X :" + x + " Y : " + y + " Z : " + z);
  }

  public void UnityLog(String log) {
	  Log.e("VideoTexture", "VideoTexture = " + log);
	  //Toast.makeText(m_UnityActivity.context, log, Toast.LENGTH_LONG);
  }

  public void SetAudioPath(String url) {
	  this.m_strAudioFileName = url;
  }
  
  public void DownloadError() {
	  this.m_UnityActivity.DownloadError();
  }

  public void downloadProgress(float value) {
	  this.m_UnityActivity.progressbarStatus(value);
  }

  public void SetRockchip(boolean bValue)
  {
    this.m_bRockchip = bValue;
  }

  public void SetLooping(boolean bLoop)
  {
    if (this.m_MediaPlayer != null)
      this.m_MediaPlayer.setLooping(bLoop);
  }

  public void SetVolume(float fVolume)
  {
    if (this.m_MediaPlayer != null)
    {
      this.m_MediaPlayer.setVolume(fVolume, fVolume);
    }
  }

  public void SetSeekPosition(int iSeek)
  {
    if (this.m_MediaPlayer != null)
    {
      if ((this.m_iCurrentState == MEDIAPLAYER_STATE.READY)   || 
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING) || 
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED)  || 
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.FLICK)  ||
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.END))
      {
        this.m_MediaPlayer.seekTo(iSeek);
      }
    }
  }

  public int GetSeekPosition()
  {
    if (this.m_MediaPlayer != null)
    {
      if ((this.m_iCurrentState == MEDIAPLAYER_STATE.READY)   || 
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING) ||
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED)  ||
    	  (this.m_iCurrentState == MEDIAPLAYER_STATE.END))
      {
        try
        {
          this.m_iCurrentSeekPosition = this.m_MediaPlayer.getCurrentPosition();
        }
        catch (SecurityException e) {
          e.printStackTrace();
        }
        catch (IllegalStateException e) {
          e.printStackTrace();
        }
      }
    }

    return this.m_iCurrentSeekPosition;
  }

  public int GetCurrentSeekPercent()
  {
    return this.m_iCurrentSeekPercent;
  }

  public void Play(int iSeek)
  {
    if (this.m_MediaPlayer != null)
    {
      if ((this.m_iCurrentState == MEDIAPLAYER_STATE.READY) || (this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED) || (this.m_iCurrentState == MEDIAPLAYER_STATE.END) )
      {
        this.m_MediaPlayer.seekTo(iSeek);
        this.m_MediaPlayer.start();
        
        this.m_musicMediaPlayer.seekTo(0);
        this.m_musicMediaPlayer.start();

        this.m_iCurrentState = MEDIAPLAYER_STATE.PLAYING;
        
        this.m_UnityActivity.hideDownlodingControls();
      }
    }
  }

  public void Reset()
  {
    if (this.m_MediaPlayer != null)
    {
      if (this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING)
      {
        this.m_MediaPlayer.reset();
        this.m_musicMediaPlayer.reset();
      }

    }

    this.m_iCurrentState = MEDIAPLAYER_STATE.NOT_READY;
  }

  public void Stop()
  {
    if (this.m_MediaPlayer != null)
    {
      //if (this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING || )
    	if( this.m_iCurrentState != MEDIAPLAYER_STATE.NOT_READY )
      {
        this.m_MediaPlayer.stop();
        this.m_musicMediaPlayer.stop();
      }

    }

    this.m_iCurrentState = MEDIAPLAYER_STATE.NOT_READY;
    m_UnityActivity.switchingPlayButtonImage();
    //m_UnityActivity.setUpdateUI(0);
  }

  public void RePlay()
  {
    if (this.m_MediaPlayer != null)
    {
      if (this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED  || this.m_iCurrentState == MEDIAPLAYER_STATE.END )
      {
        this.m_MediaPlayer.start();
        this.m_iCurrentState = MEDIAPLAYER_STATE.PLAYING;
        
        this.m_musicMediaPlayer.start();
        
        this.m_UnityActivity.hideDownlodingControls();
      }
    }
  }

  public void Pause()
  {
    if (this.m_MediaPlayer != null)
    {
      if (this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING)
      {
        this.m_MediaPlayer.pause();
        this.m_iCurrentState = MEDIAPLAYER_STATE.PAUSED;
        
        this.m_musicMediaPlayer.pause();
      }
    }
  }

  public int GetVideoWidth()
  {
    if (this.m_MediaPlayer != null)
    {
      return this.m_MediaPlayer.getVideoWidth();
    }

    return 0;
  }

  public int GetVideoHeight()
  {
    if (this.m_MediaPlayer != null)
    {
      return this.m_MediaPlayer.getVideoHeight();
    }

    return 0;
  }

  public boolean IsUpdateFrame()
  {
    if (this.m_bUpdate)
    {
      return true;
    }

    return false;
  }

  public void SetUnityTexture(int iTextureID)
  {
    this.m_iUnityTextureID = iTextureID;
    SetManagerID(this.m_iNativeMgrID);
    SetUnityTextureID(this.m_iUnityTextureID);
  }

  public void SetUnityTextureID(Object texturePtr)
  {
  }

  public void SetSplitOBB(boolean bValue, String strOBBName)
  {
    this.m_bSplitOBB = bValue;
    this.m_strOBBName = strOBBName;
  }

  public int GetDuration()
  {
    if (this.m_MediaPlayer != null)
    {
      return this.m_MediaPlayer.getDuration();
    }

    return -1;
  }

  public void SetUnityActivity(Activity unityActivity)
  {
    this.m_UnityActivity = (VideoActivity)unityActivity;
    this.m_UnityActivity.videoTexture = this;
//    
    //am = (AudioManager)unityActivity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    //am.registerMediaButtonEventReceiver(new ComponentName(unityActivity, RemoteControlReceiver.class));
//
    this.m_iNativeMgrID = InitApplication(this.m_UnityActivity.getAssets());
  }

  public int GetStatus()
  {
    return this.m_iCurrentState.GetValue();
  }

  public void SetWindowSize() {
    SetManagerID(this.m_iNativeMgrID);

    SetWindowSize(GetVideoWidth(), GetVideoHeight(), this.m_iUnityTextureID, this.m_bRockchip);
  }

  public int GetError()
  {
    return this.m_iErrorCode;
  }

  public int GetErrorExtra()
  {
    return this.m_iErrorCodeExtra;
  }

  public boolean onError(MediaPlayer player, int errorCode, int errorCodeExtra)
  {
	String strError = "";
	
    if (player == this.m_MediaPlayer)
    {      
      switch (errorCode)
      {
      case 200:
        strError = "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
        break;
      case 100:
        strError = "MEDIA_ERROR_SERVER_DIED";
        break;
      case 1:
        strError = "MEDIA_ERROR_UNKNOWN";
        break;
      default:
        strError = "Unknown error " + errorCode;
      }

      this.m_iErrorCode = errorCode;
      this.m_iErrorCodeExtra = errorCodeExtra;

      this.m_iCurrentState = MEDIAPLAYER_STATE.ERROR;

      return true;
    }

    return false;
  }

  public void onCompletion(MediaPlayer player)
  {
    if (player == this.m_MediaPlayer) {
      this.m_iCurrentState = MEDIAPLAYER_STATE.END;
      this.m_UnityActivity.switchingPlayButtonImage();
      this.m_UnityActivity.setUpdateUI(0);
      this.m_MediaPlayer.seekTo(0);
      //this.m_UnityActivity.hideWaitProgressControl();
      //this.Play(0);
      
      this.m_musicMediaPlayer.seekTo(0);
      this.m_musicMediaPlayer.pause();

      this.m_UnityActivity.showShakeControls();
    }
  }

  public void onBufferingUpdate(MediaPlayer player, int arg1)
  {
    if (player == this.m_MediaPlayer)
      this.m_iCurrentSeekPercent = arg1;
  }
  
  public void OnSeekCompleteListener(MediaPlayer player) {
	  
  }

  public void onPrepared(MediaPlayer player)
  {
    if (player == this.m_MediaPlayer)
    {
      this.m_iCurrentState = MEDIAPLAYER_STATE.READY;

      SetManagerID(this.m_iNativeMgrID);
      this.m_iCurrentSeekPercent = 0;
      this.m_MediaPlayer.setOnBufferingUpdateListener(this);
      
      this.Play(0);
      this.m_UnityActivity.switchingPlayButtonImage();
      //this.m_UnityActivity.hideProgressbar(true);
    }
  }
  
  /*
   * 		if( keyCode >= 19 && keyCode <= 22 ||
			keyCode == 66 || keyCode == 24 || keyCode == 25 )
   */
  public void keyEvent(int keyCode ){
	  if( keyCode == PLAYPAUSE || keyCode == 97/*game mode*/ ) {
		  if( this.m_iCurrentState == MEDIAPLAYER_STATE.READY || this.m_iCurrentState == MEDIAPLAYER_STATE.END || this.m_iCurrentState == MEDIAPLAYER_STATE.STOPPED ) 
			  this.Play(0);
		  else if( this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED ) {
			  this.RePlay();
		  }
		  else if( this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING )
			  this.Pause();
	  }
	  
	  if( keyCode == STOP || keyCode == 89/*small*/ ) {
		  //stop
		  //this.Pause();
		  if( this.m_iCurrentState != MEDIAPLAYER_STATE.PLAYING ) {
			  this.Play(0);
		  }
		  else {
			  this.SetSeekPosition(0);			  
		  }
		  //Load(m_strFileName, 0);
		  //this.m_iCurrentState = MEDIAPLAYER_STATE.END;
		  //m_UnityActivity.setUpdateUI(0);
	  }
	  else if( keyCode == BACKWARD || keyCode == 88/*small*/ ) {
		  if( this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING ) {
			  int pos = this.GetSeekPosition();
			  pos -= 2000;	//per 1s
			  if( pos < 500 )
				  pos = 0;

			  this.updateMediaPosition(pos);
		  }
	  }
	  else if( keyCode == FOREWARD || keyCode == 87/*small*/) {
		  if( this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING ) {
			  int pos = this.GetSeekPosition();
			  int duration = this.GetDuration();
			  pos += 2000;	//per 1s
			  if( pos > duration - 500 )
				  pos = 0;
			  //this.SetSeekPosition(pos);

			  this.updateMediaPosition(pos);
		  }
	  }
  }
  
  public void dispatchPlayEvent(boolean isStop) throws SecurityException, IllegalStateException, IOException {
	  if( !isStop ) {
		  if( this.m_iCurrentState == MEDIAPLAYER_STATE.READY ||
			  this.m_iCurrentState == MEDIAPLAYER_STATE.END || 
			  this.m_iCurrentState == MEDIAPLAYER_STATE.STOPPED )
			  		this.Play(0);
		  else if( this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED ) {
			  		this.RePlay();
		  }
		  else if( this.m_iCurrentState == MEDIAPLAYER_STATE.PLAYING )
			  		this.Pause();
		  else if( this.m_iCurrentState == MEDIAPLAYER_STATE.NOT_READY ) {
			  //Load(m_strFileName, 0);
			  
			  //m_UnityActivity.waitPlay();
		  }
	  }
	  else {
		  this.Pause();
		  this.SetSeekPosition(0);
		  //Load(m_strFileName, 0);
		  this.m_iCurrentState = MEDIAPLAYER_STATE.END;
		  
		  m_UnityActivity.showShakeControls();
		  //m_UnityActivity.setUpdateUI(0);
		  
		  /*if( this.m_iCurrentState != MEDIAPLAYER_STATE.PLAYING ) {
			  this.Play(0);
		  }
		  else {
			  this.SetSeekPosition(0);			  
		  }*/
	  }
  }
    
  public int getTotalFrameTime() {
	  return this.m_MediaPlayer.getDuration();
  }
  
  public void updateMediaPosition(int newPos) {
	  if( this.m_iCurrentState == MEDIAPLAYER_STATE.NOT_READY || 
		  this.m_iCurrentState == MEDIAPLAYER_STATE.READY )
		  		return;
	  
	  this.SetSeekPosition(newPos);
	  if( this.m_iCurrentState == MEDIAPLAYER_STATE.END ||
	      this.m_iCurrentState == MEDIAPLAYER_STATE.PAUSED ) {
		  		this.RePlay();
	  }
  }
/*  
  enum MEDIAPLAYER_BUTTON {
	  NOT_KEY(0),
	  PLAYPAUSE(66),
	  BACKWARD(21),
	  FOREWARD(22),
	  UP(19),
	  DOWN(20),
	  VOLUMNUP(24),
	  VOLUMNDOWN(25);

  }
*/
  final static int NOT_KEY = 0;
  final static int PLAYPAUSE = 66;
  final static int BACKWARD = 21;
  final static int FOREWARD = 22;
  final static int UP = 19;
  final static int DOWN = 20;
  final static int VOLUMUP = 24;
  final static int VOLUMDOWN = 25;
  final static int STOP = 111;
  final static int STANDBY = 82;
  
  public static enum MEDIAPLAYER_STATE
  {
    NOT_READY(0), 
    READY(1), 
    END(2), 
    PLAYING(3), 
    PAUSED(4), 
    STOPPED(5), 
    FLICK(6),
    ERROR(7);

    private int iValue;

    private MEDIAPLAYER_STATE(int i) {
      this.iValue = i;
    }

    public int GetValue() {
      return this.iValue;
    }
  }
}
