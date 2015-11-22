// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using UnityEngine;
using UnityEngine.UI;

public class CardboardHead : MonoBehaviour {
  // Which types of tracking this instance will use.
  public bool trackRotation = true;
  public bool trackPosition = true;

	private Vector2	prevTouchVec;
	private float mouseX = 0;
	private float mouseY = 0;
	private float mouseZ = 0;
	
	// If set, the head transform will be relative to it.
  public Transform target;

	public Transform tempTarget;
	public Transform sceneTransform;

	public bool 	 fixCamera;

	public bool 	 recenterCamera;

	public Quaternion	recentCardboardQuaternion;

	//
	public GameObject	text1;
	public GameObject	text2;
	public GameObject	text3;
	//


  // Determine whether head updates early or late in frame.
  // Defaults to false in order to reduce latency.
  // Set this to true if you see jitter due to other scripts using this
  // object's orientation (or a child's) in their own LateUpdate() functions,
  // e.g. to cast rays.
  public bool updateEarly = false;

  // Where is this head looking?
  public Ray Gaze {
    get {
      UpdateHead();
      return new Ray(transform.position, transform.forward);
    }
  }

  private bool updated;

  void Update() {
    updated = false;  // OK to recompute head pose.
    if (updateEarly) {
      UpdateHead();
    }

  }

  // Normally, update head pose now.
  void LateUpdate() {
    UpdateHead();
  }

  // Compute new head pose.
	private void UpdateHead() {
		if (fixCamera) {
			HandleControls();
			//transform.eulerAngles = new Vector3(0, 0, 0);
			return;
		}
		
		if (updated) {  // Only one update per frame, please.
			return;
		}
		updated = true;
		if (!Cardboard.SDK.UpdateState ()) {
			return;
		}
		/*
		if (trackRotation) {
			var rot = Cardboard.SDK.HeadRotation;
			if (target == null) {
				transform.localRotation = rot;
			} else {
				transform.rotation = rot * target.rotation;
			}
		}
		
		if (trackPosition) {
			Vector3 pos = Cardboard.SDK.HeadPosition;
			if (target == null) {
				transform.localPosition = pos;
			} else {
				transform.position = target.position + target.rotation * pos;
			}
		}*/

		Quaternion rot = Quaternion.identity;
		if (trackRotation) {
			rot = Cardboard.SDK.HeadRotation;// * Quaternion.Inverse(recentCardboardQuaternion);
			if (target == null) {
				tempTarget.localRotation = rot;
			} else {
				tempTarget.rotation = rot * target.rotation;
			}
		}
		
		if (trackPosition) {
			Vector3 pos = Cardboard.SDK.HeadPosition;
			if (target == null) {
				tempTarget.localPosition = pos;
			} else {
				tempTarget.position = target.position + target.rotation * pos;
			}
		}    

		Vector3 angle = tempTarget.eulerAngles;
		transform.rotation = Quaternion.Slerp (transform.rotation, Quaternion.Euler (angle.x, angle.y, angle.z), 0.6f);
	}

	/*
	private void OnGUI() {
		string coord = "" + transform.eulerAngles;
		GUI.Label(new Rect(0, 0, 300, 100), coord);
	}*/

	public void enableCamera() {
		fixCamera = false;
	}

	public void disableCamera() {
		fixCamera = true;
		//Cardboard.SDK.Recenter ();
	}

	public void calibrationCamera() {
		//Cardboard.SDK.Recenter ();
		//recenterCamera = !recenterCamera;
		Cardboard.SDK.VRModeEnabled = fixCamera;
		fixCamera = !fixCamera;
	}

	public void HandleControls()
	{
		if (Input.touchCount > 0)
		{
			Touch touch = Input.GetTouch(0);
			if (touch.phase == TouchPhase.Began)
			{
				prevTouchVec = touch.position;
			}
			if( touch.phase == TouchPhase.Moved ) {
//				Input
				Vector2 delta = touch.position - prevTouchVec;
				if( delta.x > 0 )
					mouseY -= 2;
				if( delta.x < 0 )
					mouseY += 2;

				if( mouseY >= 360 )
					mouseY = 0;
				if( mouseY < 0 )
					mouseY = 360;

//				mouseX += delta.y;
				transform.rotation = Quaternion.Slerp (transform.rotation, Quaternion.Euler(mouseX, mouseY, 0), 0.6f);
				prevTouchVec = touch.position;
			}
		}
	}
	/*
	public void OnGUI() {
		if (fixCamera) {
			GUI.Label(new Rect(0, 0, 1000, 500), Input.GetAxis("") + ":" + prevTouchVec.x);
		}
	}*/
}
