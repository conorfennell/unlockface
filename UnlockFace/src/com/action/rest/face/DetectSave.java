package com.action.rest.face;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

public class DetectSave extends Activity implements Runnable {

	private String SAVE_URI = "http://api.face.com/tags/save.json";
	// My API key
	private String API_KEY = "ee028ebb0ed0203740ea1c129977cc84";
	// My API secret
	private String API_SECRET = "976f979ca8549d9424b6090e28dfd8c4";
	private String TAG = "AN9";
	private final TrainFacialRecognition register;
	private String user;
	private String tid;

	/**
	 * Constructor for DetectSave class It takes in the TrainFacialRecognition
	 * activity so it can update the TrainFacialRecognition UI.
	 * 
	 * @param register
	 * @param tid
	 *            identifier for the picture
	 * @param user
	 *            user you want to save the detected face for
	 */
	public DetectSave(TrainFacialRecognition register, String tid, String user) {
		this.user = user;
		this.tid = tid;
		this.register = register;
	}

	@Override
	public void run() {
		this.register.setPicturestatus("Saving face....");
		String tid = this.saveDetection(this.tid, this.user);
		if (tid.equals("No face detected")) {
			this.register.setPicturestatus(tid);
		} else {
			this.register.setPicturestatus("Face saved");
		}
	}

	/**
	 * Saves the detected face with a temporary id to the specified user by
	 * calling face.com API http://api.face.com/tags/save.json
	 * 
	 * @param tid
	 *            temporary picture id
	 * @param userId
	 * @return
	 */
	private String saveDetection(String tid, String userId) {
		HttpURLConnection con;
		URL url;
		String payload = null;
		try {
			url = new URL(this.SAVE_URI + "?api_key=" + this.API_KEY + "&api_secret=" + this.API_SECRET + "&uid="
					+ userId + "&tids=" + tid);

			con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(10000 /* milliseconds */);
			con.setConnectTimeout(15000 /* milliseconds */);

			con.setRequestMethod("GET");
			con.setDoInput(true);
			// Start the query
			con.connect();

			// Read results from the query
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			payload = reader.readLine();
			reader.close();
			payload = new JSONObject(payload).getString("status");
		} catch (MalformedURLException e) {
			Log.e(this.TAG, "detectSave() MalformedURLException " + e.getMessage());
		} catch (IOException e) {
			Log.e(this.TAG, "detectSave() IOException " + e.getMessage());
		} catch (JSONException e) {
			Log.e(this.TAG, "detectSave() JSONException " + e.getMessage());
		}

		return payload;
	}

}