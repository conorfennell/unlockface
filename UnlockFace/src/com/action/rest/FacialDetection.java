package com.action.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

public class FacialDetection extends Activity implements Runnable {
	// Static request number for the camera
	private static final int CAMERA_PIC_REQUEST = 1337;
	private final String DETECT_URI = "http://api.face.com/faces/detect.json";
	private final String SAVE_URI = "http://api.face.com/tags/save.json";
	private final String RECOGNIZE_URI = "http://api.face.com/faces/recognize.json";
	private final String TRAIN_URI = "http://api.face.com/faces/train.json";
	// My API key
	private final String API_KEY = "ee028ebb0ed0203740ea1c129977cc84";
	// My API secret
	private final String API_SECRET = "976f979ca8549d9424b6090e28dfd8c4";
	private static final String TAG = "AN9";
	private final UnLockFace translate;
	private final File image;

	FacialDetection(UnLockFace translate, File image) {
		this.translate = translate;
		this.image = image;
	}

	@Override
	public void run() {

		this.translate.toggleProgressBar(1);
		String tid = this.detectFace(this.image);
		tid = this.saveDetection(tid, "conor@unlockface.com");
		String tFace = null;
		try {
			tFace = this.trainFace(null, "conor@unlockface.com");
		} catch (InterruptedException e) {
			Log.d(TAG, "InterruptedException", e);

		}

		this.translate.setTranslated(tid);

		this.translate.setLockStatus(tFace);
		this.translate.toggleProgressBar(0);
	}

	/**
	 * Takes a picture and detects if it
	 * 
	 * @param picture
	 * @return
	 */
	private String detectFace(File picture) {

		String payLoad = null;
		String pictureId = null;
		HashMap<String, String> properties = new HashMap<String, String>();

		properties.put("api_key", this.API_KEY);
		properties.put("api_secret", this.API_SECRET);

		Log.i(TAG, "Start detectFace " + this.DETECT_URI);
		StringBuilder builder = this.sendPhoto(picture, properties, this.DETECT_URI);
		Log.i(TAG, "Finish detectFace " + this.DETECT_URI);

		// TODO:Check for failure of request
		try {
			//
			payLoad = new JSONObject(builder.toString()).getJSONArray("photos").getString(0);
			pictureId = (String) new JSONObject(payLoad).getJSONArray("tags").getJSONObject(0).get("tid");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return pictureId;

	}

	private String recognizeFace(File picture) {

		String payLoad = null;
		String pictureId = null;
		HashMap<String, String> properties = new HashMap<String, String>();

		properties.put("api_key", this.API_KEY);
		properties.put("api_secret", this.API_SECRET);
		properties.put("uids", "conor@unlockface.com");
		Log.i(TAG, "Start recognizeFace " + this.RECOGNIZE_URI);
		StringBuilder builder = this.sendPhoto(picture, properties, this.RECOGNIZE_URI);
		Log.i(TAG, "Finish recognizeFace " + this.RECOGNIZE_URI);
		// TODO:Check for failure of request
		// Parse get translated text
		// try {
		// //
		// payLoad = new
		// JSONObject(builder.toString()).getJSONArray("photos").getString(0);
		// pictureId = (String) new
		// JSONObject(payLoad).getJSONArray("tags").getJSONObject(0).get("tid");
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }

		// return pictureId;
		return builder.toString();

	}

	private String trainFace(String tid, String userId) throws InterruptedException {
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
			Log.i(TAG, "Start trainFace " + this.TRAIN_URI);
			con.connect();
			Log.i(TAG, "Finish trainFace " + this.TRAIN_URI);
			// Check if task has been interrupted
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			// Read results from the query
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			payload = reader.readLine();
			reader.close();
			payload.length();

			if (con != null) {
				con.disconnect();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return payload;
	}

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
			payload.length();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return payload;
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
			Log.i(TAG, "Start sendPhoto( " + uri + " Length of file in bytes: " + picture.length());
			HttpResponse response = httpClient.execute(httpPost);
			Log.i(TAG, "End sendPhoto( " + uri + " Length of file in bytes: " + picture.length());
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder;
	}

}
