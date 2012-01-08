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

import com.action.rest.UnLockFace;

public class Recognize extends Activity implements Runnable {
	private String RECOGNIZE_URI = "http://api.face.com/faces/recognize.json";
	// My API key
	private String API_KEY = "ee028ebb0ed0203740ea1c129977cc84";
	// My API secret
	private String API_SECRET = "976f979ca8549d9424b6090e28dfd8c4";
	private String TAG = "AN9";
	private final File image;
	private final UnLockFace unLockFaceUI;
	private String user;

	/**
	 * Constructor for Recognize class It takes in the UnLockFace activity so it
	 * can update the UnLockFace UI.
	 * 
	 * @param unLockFaceUI
	 * @param image
	 * @param user
	 */
	public Recognize(UnLockFace unLockFaceUI, File image, String user) {
		this.image = image;
		this.unLockFaceUI = unLockFaceUI;
		this.user = user;
	}

	@Override
	public void run() {
		String response = this.recognizeFace(this.image, this.user);
		this.unLockFaceUI.setTranslated(response);
		this.unLockFaceUI.setLockStatus(response);

	}

	/**
	 * Sends the picture and user to face.com API
	 * http://api.face.com/faces/recognize.json
	 * 
	 * @param picture
	 * @param user
	 * @return the confidence that the picture matches the user
	 */
	private String recognizeFace(File picture, String user) {

		String payLoad = null;
		String confidence = null;
		HashMap<String, String> properties = new HashMap<String, String>();

		properties.put("api_key", this.API_KEY);
		properties.put("api_secret", this.API_SECRET);
		properties.put("uids", this.user);
		Log.i(this.TAG, "Start recognizeFace " + this.RECOGNIZE_URI + " File exists: " + picture.exists());
		StringBuilder builder = this.sendPhoto(picture, properties, this.RECOGNIZE_URI);
		Log.i(this.TAG, "Finish recognizeFace " + this.RECOGNIZE_URI);
		// TODO:Check for failure of request
		// Parse json response for recognise face result
		try {
			//
			if (!new JSONObject(builder.toString()).getJSONArray("photos").getJSONObject(0).getJSONArray("tags")
					.isNull(0)) {
				payLoad = new JSONObject(builder.toString()).getJSONArray("photos").getJSONObject(0)
						.getJSONArray("tags").getJSONObject(0).getJSONArray("uids").toString();
				// if face detected but not recognised
				if (payLoad.equals("[]")) {
					payLoad = "Unauthorised access!";
				} else {
					payLoad = new JSONArray(payLoad).getJSONObject(0).getString("confidence");
				}

			} else {
				payLoad = "No face detected";
			}

			confidence = payLoad;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return confidence;
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
		StringBuilder builder = null;
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
			builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

		} catch (UnsupportedEncodingException e) {
			Log.e(this.TAG, "detect() UnsupportedEncodingException " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e(this.TAG, "detect() ClientProtocolException" + e.getMessage());
		} catch (IOException e) {
			Log.e(this.TAG, "detect() IOException" + e.getMessage());
		}
		return builder;
	}

}