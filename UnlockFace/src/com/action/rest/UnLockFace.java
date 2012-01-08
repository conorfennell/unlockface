package com.action.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.action.rest.face.Recognize;

public class UnLockFace extends Activity implements DialogInterface.OnClickListener, OnClickListener, TextWatcher {
	private final String TAG = "AN9";
	private Integer CAMERA_PIC_REQUEST = 1337;
	private TextView confidenceText;
	private TextView statusText;
	private ProgressBar progressBar;

	// Declarations for threading
	private Handler guiThread;
	private ExecutorService transThread;
	@SuppressWarnings("rawtypes")
	private Future transPending;
	private Runnable recognizeTask;

	private Button processButton;
	private Button takePicture;

	private String user;
	private ImageView image;
	private EditText password;
	private Button unlockWithPassword;
	private AlertDialog alertDialog;

	@Override
	public void onBackPressed() {
		// empty, to prevent leaving the locked screen
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.unlockface);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		// Get user from last activity
		Bundle extras = UnLockFace.this.getIntent().getExtras();
		this.user = extras.getString("user") + "@unlockface.com";
		this.initThreading();
		this.findViews();
		this.setListeners();
		this.setupUI();

	}

	private void setupUI() {
		this.processButton.setVisibility(Button.INVISIBLE);
		this.confidenceText.setVisibility(TextView.INVISIBLE);
		this.image.requestFocus();
		this.image.requestFocusFromTouch();

	}

	private void startCamera() {
		// Get camera picture
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		this.startActivityForResult(cameraIntent, this.CAMERA_PIC_REQUEST);
	}

	private void findViews() {
		this.confidenceText = (TextView) this.findViewById(R.id.translated_text);
		this.statusText = (TextView) this.findViewById(R.id.retranslated_text);
		this.processButton = (Button) this.findViewById(R.id.processImage);
		this.takePicture = (Button) this.findViewById(R.id.takePictureButton);
		this.image = (ImageView) this.findViewById(R.id.faceImage);
		this.password = (EditText) this.findViewById(R.id.pass);
		this.unlockWithPassword = (Button) this.findViewById(R.id.unlock_with_password);
	}

	private void setListeners() {

		this.processButton.setOnClickListener(this);
		this.takePicture.setOnClickListener(this);
		this.confidenceText.addTextChangedListener(this);
		this.unlockWithPassword.setOnClickListener(this);
	}

	private void initThreading() {
		this.guiThread = new Handler();
		this.transThread = Executors.newSingleThreadExecutor();

		this.recognizeTask = new Runnable() {
			@Override
			public void run() {

				// Cancel previous recognize task
				if (UnLockFace.this.transPending != null) {
					UnLockFace.this.transPending.cancel(true);
				}

				// Let user know we're doing something
				UnLockFace.this.confidenceText.setText("Recognizing...");
				UnLockFace.this.statusText.setText("Recognizing...");
				// Begin the recognize request now but don't wait for it
				File imageFile = UnLockFace.this.getFilesDir();
				File picFile = new File(imageFile.getAbsolutePath() + "/temp_save.jpg");
				try {
					Recognize recognize = new Recognize(UnLockFace.this, picFile, UnLockFace.this.user);
					UnLockFace.this.transPending = UnLockFace.this.transThread.submit(recognize);
				} catch (RejectedExecutionException e) {
					// Unable to start new task
					UnLockFace.this.confidenceText.setText(R.string.facial_recognition_error);
					UnLockFace.this.statusText.setText(R.string.facial_recognition_error);
				}

			}
		};
	}

	/** Request an update to start after a short delay */
	private void queueUpdate(Runnable task, long delayMillis) {
		// Cancel previous update if it hasn't started yet
		this.guiThread.removeCallbacks(task);
		// Start an update if nothing happens after a few milliseconds
		this.guiThread.postDelayed(task, delayMillis);
	}

	/** Modify text on the screen (called from another thread) */
	public void setTranslated(String text) {
		this.guiSetText(this.confidenceText, text);
	}

	public void toggleProgressBar(final Integer toggle) {
		final ProgressBar view = this.progressBar;
		this.guiThread.post(new Runnable() {
			@Override
			public void run() {
				view.setVisibility(toggle);
			}
		});
	}

	/** Modify text on the screen (called from another thread) */
	public void setLockStatus(String text) {
		this.guiSetText(this.statusText, text);
	}

	/** All changes to the GUI must be done in the GUI thread */
	private void guiSetText(final TextView view, final String text) {
		this.guiThread.post(new Runnable() {
			@Override
			public void run() {
				view.setText(text);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == this.CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {
			Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

			String FILENAME = "temp_save.jpg";

			FileOutputStream fos;
			try {
				fos = this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.e(this.TAG, "onActivityResult() FileNotFoundException " + e.getMessage());
			} catch (IOException e) {
				Log.e(this.TAG, "onActivityResult() IOException " + e.getMessage());
			}

			File imageFile = this.getFilesDir();
			File picFile = new File(imageFile.getAbsolutePath() + "/temp_save.jpg");
			Bitmap picture = BitmapFactory.decodeFile(picFile.getAbsolutePath());
			this.image.setImageBitmap(picture);

			this.processButton.setVisibility(Button.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.processImage:
			UnLockFace.this.queueUpdate(this.recognizeTask, 1);
			break;
		case R.id.takePictureButton:
			this.startCamera();
			break;
		case R.id.unlock_with_password:
			this.getAccountDetails(this.password.getText().toString());
			break;
		}

	}

	private void getAccountDetails(String password) {
		String FILENAME = "login.txt";

		FileInputStream fis;
		try {
			String userNamePassword = "";
			String[] array;
			fis = this.openFileInput(FILENAME);
			BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
			while ((userNamePassword = buf.readLine()) != null) {
				Log.i("AN9", userNamePassword);

				array = userNamePassword.split(",");
				// checks password
				if (password.equals(array[1])) {
					Log.i("AN9", "Unlocked with password");

					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Info");
					this.alertDialog.setMessage("Unlocked with password");
					this.alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
					this.alertDialog.setCancelable(false);

					this.alertDialog.show();
				} else {
					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Info");
					this.alertDialog.setMessage("Incorrect Password");
					this.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", this);
					this.alertDialog.show();
				}
			}

			fis.close();

		} catch (FileNotFoundException e) {
			this.alertDialog = new AlertDialog.Builder(this).create();
			this.alertDialog.setTitle("Info");
			this.alertDialog.setMessage("ERROR no password found");
			this.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", this);
			this.alertDialog.show();
		} catch (IOException e) {
			Log.e(this.TAG, "getAccountDetails() IOException " + e.getMessage());
		}

	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!(s.toString().equals("Recognizing...") || s.toString().equals("Unauthorised access!")
				|| s.toString().equals("No face detected") || s.toString().equals("Reocgnized!"))) {
			this.setLockStatus("Reocgnized!");
			this.unlock(Integer.valueOf(s.toString()));
		}

	}

	private void unlock(Integer confidence) {
		Log.i("AN9", "Confidence " + confidence);

		if (confidence > 70) {
			this.alertDialog = new AlertDialog.Builder(this).create();
			this.alertDialog.setTitle("Info");
			this.alertDialog.setMessage("Unlocked with facial recognition");
			this.alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
			this.alertDialog.setCancelable(false);
			this.alertDialog.show();
		} else {
			this.alertDialog = new AlertDialog.Builder(this).create();
			this.alertDialog.setTitle("Info");
			this.alertDialog.setMessage("Confidence of user in picture too low! Train to make it better.");
			this.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", this);
			this.alertDialog.setCancelable(false);
			this.alertDialog.show();
		}

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (AlertDialog.BUTTON_POSITIVE == which) {
			super.onBackPressed();
		}

	}
}