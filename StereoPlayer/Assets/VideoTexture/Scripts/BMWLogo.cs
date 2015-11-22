using UnityEngine;
using System.Collections;

public class BMWLogo : MonoBehaviour {

	// Use this for initialization
	void Start () {
		StartCoroutine (LogoScreen ());
	}
	
	// Update is called once per frame
	void Update () {
	
	}

	IEnumerator LogoScreen()
	{
		yield return new WaitForSeconds(2);
		
		Application.LoadLevel("stereoScene");
		yield return 0;
	}
}
