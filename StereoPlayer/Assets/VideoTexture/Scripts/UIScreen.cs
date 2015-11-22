using UnityEngine;
using System.Collections;

public class UIScreen : MonoBehaviour {
	public GUISkin skin;
	public GameObject	alertBox;
	private bool		alertShowing;

	string password = "";
	// Use this for initialization
	void Start () {
		alertBox.SetActive (false);
		alertShowing = false;
		Debug.Log (Screen.dpi);
	}
	
	// Update is called once per frame
	void Update () {
	
	}

	void OnGUI() {
		/*if (alertShowing == false) {
			GUI.skin.settings.cursorColor = Color.black;
			GUI.skin.settings.selectionColor = new Color (51.0f / 255.0f, 153.0f / 255.0f, 1.0f);
			GUI.Box (new Rect (Screen.width / 2 - Screen.dpi / 96 * 150, 
			                   Screen.height / 2 - Screen.dpi / 96 * 30, 
			                   Screen.dpi / 96 * 300, 
			                   Screen.dpi / 96 * 60), 
			         "", skin.GetStyle ("box"));
			password = GUI.PasswordField (new Rect (Screen.width / 2 - Screen.dpi / 96 * 140, 
			                                        Screen.height / 2 - Screen.dpi / 96 * 20, 
			                                        Screen.dpi / 96 * 200, 
			                                        Screen.dpi / 96 * 40),
			                              password, '*', skin.GetStyle ("textfield"));
			if (GUI.Button (new Rect (Screen.width / 2 + Screen.dpi / 96 * 70, Screen.height / 2 - Screen.dpi / 96 * 20, Screen.dpi / 96 * 70, Screen.dpi / 96 * 40), "", skin.GetStyle ("button"))) {
				if (password == "111") {
					Application.LoadLevel ("stereoScene");
				} else {
					alertShowing = true;
					alertBox.SetActive (true);
				}
			}
		}*/
	}

	public void HideAlertBox() {
		alertBox.SetActive (false);
		alertShowing = false;
	}

	public void OnGotoCardboardScreen() {
		Application.LoadLevel ("stereoScene");
	}
}
