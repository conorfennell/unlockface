package com.action.rest.face;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.util.Log;

public class Train extends Activity implements Runnable {
	private String TRAIN_URI = "http://api.face.com/faces/train.json";
	// My API key
	private String API_KEY = "ee028ebb0ed0203740ea1c129977cc84";
	// My API secret
	private String API_SECRET = "976f979ca8549d9424b6090e28dfd8c4";
	private String TAG = "AN9";
	private final TrainFacialRecognition trainFacialRecognition;
	private String user;

	public Train(TrainFacialRecognition trainFacialRecognition, String user) {
		this.user = user;
		this.trainFacialRecognition = trainFacialRecognition;
	}

	@Override
	public void run() {
		this.trainFacialRecognition.setPicturestatus("Training user.....");
		String response = this.trainFace(this.user);
		if (response.equals("No face detected")) {
			this.trainFacialRecognition.setPicturestatus(response);
		} else {
			this.trainFacialRecognition.setPicturestatus("Added to facial recognition set");
		}
	}

	private String trainFace(String userId) {
		HttpURLConnection con;
		URL url;
		String payload = null;
		try {
			url = new URL(this.TRAIN_URI + "?api_key=" + this.API_KEY + "&api_secret=" + this.API_SECRET + "&uids="
					+ userId);

			con = (HttpURLConnection) url.openConnection();
			// Check if task has been interrupted
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			con.setReadTimeout(10000 /* milliseconds */);
			con.setConnectTimeout(15000 /* milliseconds */);

			con.setRequestMethod("GET");
			con.setDoInput(true);
			// Start the query
			Log.i(this.TAG, "Start trainFace " + this.TRAIN_URI);
			con.connect();
			Log.i(this.TAG, "Finish trainFace " + this.TRAIN_URI);
			// Check if task has been interrupted
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			// Read results from the query
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			payload = reader.readLine();
			Log.i(this.TAG, "TrainFace response  " + payload);
			reader.close();

			if (con != null) {
				con.disconnect();
			}
		} catch (MalformedURLException e) {
			Log.e(this.TAG, "train() MalformedURLException " + e.getMessage());
		} catch (IOException e) {
			Log.e(this.TAG, "train() IOException " + e.getMessage());
		} catch (InterruptedException e) {
			Log.e(this.TAG, "train() InterruptedException " + e.getMessage());
		}

		return payload;
	}

}