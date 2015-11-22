using UnityEngine;
using System.Collections;

public class VideoOperator : MonoBehaviour {
	//public GameObject	panel;
	public GameObject	playButton;
	public GameObject theatre;
	
	#region private data
	MediaPlayerCtrl media;
	#endregion
	// Use this for initialization
	void Start () {
		//splashCanvas = GetComponent<Canvas> ();
		//media = theatre.GetComponent<MediaPlayerCtrl> ();
		//Invoke ("GotoMediaPlay", 3.0f);
	}
	
	public void GotoMediaPlay() {
		//playButton.SetActive (false);
		//media.Play ();
	}
	
	void Update() 
	{
		//if (media.GetCurrentState () == MediaPlayerCtrl.MEDIAPLAYER_STATE.END) {
		//	playButton.SetActive (true);
		//	Invoke ("GotoMediaPlay", 3.0f);
		//}
	}
}
