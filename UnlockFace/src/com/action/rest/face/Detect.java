package com.action.rest.face;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

/**
 * Used to detect a face in a photo
 * 
 * @author Conor
 * 
 */
public class Detect extends Activity implements Runnable {

	private String DETECT_URI = "http://api.face.com/faces/detect.json";
	// My API key
	private String API_KEY = "ee028ebb0ed0203740ea1c129977cc84";
	// My API secret
	private String API_SECRET = "976f979ca8549d9424b6090e28dfd8c4";
	private String TAG = "AN9";
	private final File image;
	private final TrainFacialRecognition register;

	/**
	 * Constructor for Detect class It takes in the TrainFacialRecognition
	 * activity so it can update the TrainFacialRecognition UI from this Thread.
	 * This class is for using the Face.com API:
	 * http://api.face.com/faces/detect.json
	 * 
	 * @param register
	 * @param image
	 */
	public Detect(TrainFacialRecognition register, File image) {
		this.image = image;
		this.register = register;
	}

	@Override
	public void run() {
		this.register.setPicturestatus("Detecting face....");
		String tid = this.detectFace(this.image);
		if (tid.equals("No face detected")) {
			this.register.setPicturestatus(tid);
			this.register.setLocalTid(tid);
		} else {
			this.register.setLocalTid(tid);
			this.register.setPicturestatus("Face detected");
		}
	}

	/**
	 * Sends a picture to http://api.face.com/faces/detect.json for face
	 * detection
	 * 
	 * @param picture
	 * @return
	 */
	private String detectFace(File picture) {

		String payLoad = null;
		String pictureId = null;
		JSONArray jsonArray = null;
		HashMap<String, String> properties = new HashMap<String, String>();

		properties.put("api_key", this.API_KEY);
		properties.put("api_secret", this.API_SECRET);

		Log.i(this.TAG, "Start detectFace " + this.DETECT_URI);
		StringBuilder builder = this.sendPhoto(picture, properties, this.DETECT_URI);
		Log.i(this.TAG, "Finish detectFace " + this.DETECT_URI);
		try {
			payLoad = new JSONObject(builder.toString()).getJSONArray("photos").getString(0);
			jsonArray = new JSONObject(payLoad).getJSONArray("tags");
			if (jsonArray.length() != 0) {
				// Parse get translated text
				pictureId = (String) new JSONObject(payLoad).getJSONArray("tags").getJSONObject(0).get("tid");
			} else {
				pictureId = "No face detected";
			}
		} catch (JSONException e) {
			Log.e(this.TAG, "detectFace() JSONException");
		}

		return pictureId;

	}

	/**
	 * Sending a photo
	 * 
	 * @param picture
	 *            file of a picture with a face
	 * @param parts
	 *            contains other information for sending in the post
	 * @param uri
	 *            the uri end point to send the information
	 * @return the response from the service
	 */
	private StringBuilder sendPhoto(File picture, HashMap<String, String> parts, String uri) {
		MultipartEntity entity = new MultipartEntity();
		StringBuilder returnResponse = null;
		HttpPost httpPost = new HttpPost(uri);
		HttpClient httpClient = new DefaultHttpClient();
		try {
			// Get all the properties
			for (Entry<String, String> entry : parts.entrySet()) {
				FormBodyPart formBodyPart = new FormBodyPart(entry.getKey(), new StringBody(entry.getValue()));
				entity.addPart(formBodyPart);
			}
			entity.addPart("image", new FileBody(picture));

			httpPost.setEntity(entity);
			Log.i(this.TAG, "Start sendPhoto( " + uri + " Length of file in bytes: " + picture.length());
			HttpResponse response = httpClient.execute(httpPost);
			Log.i(this.TAG, "End sendPhoto( " + uri + " Length of file in bytes: " + picture.length());
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			returnResponse = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				returnResponse.append(line).append("\n");
			}

		} catch (UnsupportedEncodingException e) {
			Log.e(this.TAG, "detect() UnsupportedEncodingException " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e(this.TAG, "detect() ClientProtocolException" + e.getMessage());
		} catch (IOException e) {
			Log.e(this.TAG, "detect() IOException" + e.getMessage());
		}
		return returnResponse;
	}

}