package com.action.rest.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.action.rest.R;

public class TrainFacialRecognition extends Activity implements OnClickListener {

	private final int CAMERA_PIC_REQUEST_CODE = 1337;

	// debug
	private String TAG = "AN9";

	// UI parts
	private AlertDialog alertDialog;
	private Button takePhoto;
	private Button trainButton;
	private Button saveButton;
	private ImageView picture;
	private TextView pictureStatus;

	// needed for threading
	private Handler guiThread;
	private ExecutorService transThread;
	private Runnable detectTask;
	private Runnable saveTask;
	private Runnable trainTask;
	protected Future currentTask;

	private String FILENAME = "temp_save.jpg";
	private String tid;

	private String user;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.register);

		// Get user from last activity
		Bundle extras = TrainFacialRecognition.this.getIntent().getExtras();
		this.user = extras.getString("user") + "@unlockface.com";
		this.initThreading();
		this.findViews();
		this.setListeners();
		this.setupUI();

	}

	private void setupUI() {
		this.trainButton.setVisibility(Button.INVISIBLE);
		this.saveButton.setVisibility(Button.INVISIBLE);
		this.pictureStatus.setText("No picture");

	}

	private void setListeners() {
		this.takePhoto.setOnClickListener(this);
		this.trainButton.setOnClickListener(this);
		this.saveButton.setOnClickListener(this);

	}

	private void findViews() {
		this.takePhoto = (Button) this.findViewById(R.id.takePhoto);
		this.trainButton = (Button) this.findViewById(R.id.trainButton);
		this.saveButton = (Button) this.findViewById(R.id.saveButton);
		this.picture = (ImageView) this.findViewById(R.id.picture);
		this.pictureStatus = (TextView) this.findViewById(R.id.pictureStatus);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.takePhoto:
			this.startCamera();
			break;
		case R.id.saveButton:
			if (this.tid == null || this.tid.equals("No face detected")) {
				this.alertDialog = new AlertDialog.Builder(this).create();
				this.alertDialog.setTitle("Info");
				this.alertDialog.setMessage("No face detected, please take another picture");
				this.alertDialog.show();
			} else {
				this.queueTask(this.saveTask, 1);
			}
			break;
		case R.id.trainButton:
			this.queueTask(this.trainTask, 1);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == this.CAMERA_PIC_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

			FileOutputStream fos;
			try {
				fos = this.openFileOutput(this.FILENAME, Context.MODE_PRIVATE);
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.e(this.TAG, "onActivityResult() FileNotFoundException " + e.getMessage());
			} catch (IOException e) {
				Log.e(this.TAG, "onActivityResult() IOException " + e.getMessage());
			}

			File imageFile = this.getFilesDir();
			File picFile = new File(imageFile.getAbsolutePath() + "/" + this.FILENAME);
			Bitmap test = BitmapFactory.decodeFile(picFile.getAbsolutePath());
			this.picture.setImageBitmap(test);

			// Make buttons visible
			this.trainButton.setVisibility(Button.VISIBLE);
			this.saveButton.setVisibility(Button.VISIBLE);
			// Run detect task
			this.queueTask(this.detectTask, 1);

		}
	}

	private void startCamera() {
		// Get camera picture
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		this.startActivityForResult(cameraIntent, this.CAMERA_PIC_REQUEST_CODE);
	}

	private void initThreading() {
		this.guiThread = new Handler();
		this.transThread = Executors.newSingleThreadExecutor();
		// This task does a translation and updates the screen
		this.detectTask = new Runnable() {
			private String TAG = "AN9";

			@Override
			public void run() {

				// Cancel previous task if one exists
				if (TrainFacialRecognition.this.currentTask != null) {
					TrainFacialRecognition.this.currentTask.cancel(true);
				}

				// TODO: Indicate activity
				File imageFile = TrainFacialRecognition.this.getFilesDir();
				File picFile = new File(imageFile.getAbsolutePath() + "/" + TrainFacialRecognition.this.FILENAME);

				try {
					Detect detectTask = new Detect(TrainFacialRecognition.this, picFile);
					TrainFacialRecognition.this.currentTask = TrainFacialRecognition.this.transThread
							.submit(detectTask);
				} catch (RejectedExecutionException e) {
					Log.e(this.TAG, "detectTask() RejectedExecutionException " + e.getMessage());
				}

			}
		};

		this.saveTask = new Runnable() {
			private String TAG = "AN9";

			@Override
			public void run() {

				// Cancel previous detection of face if there is one
				if (TrainFacialRecognition.this.currentTask != null) {
					TrainFacialRecognition.this.currentTask.cancel(true);
				}
				// TODO: Indicate activity
				try {
					DetectSave saveTask = new DetectSave(TrainFacialRecognition.this, TrainFacialRecognition.this.tid,
							TrainFacialRecognition.this.user);
					TrainFacialRecognition.this.currentTask = TrainFacialRecognition.this.transThread.submit(saveTask);
				} catch (RejectedExecutionException e) {
					Log.e(this.TAG, "saveTask() RejectedExecutionException " + e.getMessage());
				}

			}
		};
		this.trainTask = new Runnable() {
			private String TAG = "AN9";

			@Override
			public void run() {

				// Cancel previous detection of face if there is one
				if (TrainFacialRecognition.this.currentTask != null) {
					TrainFacialRecognition.this.currentTask.cancel(true);
				}
				// TODO: Indicate activity
				try {
					Train trainTask = new Train(TrainFacialRecognition.this, TrainFacialRecognition.this.user);
					TrainFacialRecognition.this.currentTask = TrainFacialRecognition.this.transThread.submit(trainTask);
				} catch (RejectedExecutionException e) {
					Log.e(this.TAG, "trainTask() RejectedExecutionException " + e.getMessage());
				}

			}
		};
	}

	/** Request an update to start after a short delay */
	private void queueTask(Runnable task, long delayMillis) {
		// Cancel previous update if it hasn't started yet
		this.guiThread.removeCallbacks(task);
		// Start an update if nothing happens after a few milliseconds
		this.guiThread.postDelayed(task, delayMillis);
	}

	public void setPicturestatus(final String status) {

		final TextView view = this.pictureStatus;
		this.guiThread.post(new Runnable() {

			@Override
			public void run() {
				view.setText(status);
			}
		});
	}

	public void setLocalTid(final String tid) {
		this.tid = tid;
	}

}