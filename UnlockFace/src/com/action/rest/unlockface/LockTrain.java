package com.action.rest.unlockface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.action.rest.R;
import com.action.rest.UnLockFace;
import com.action.rest.face.TrainFacialRecognition;

public class LockTrain extends Activity implements OnClickListener {

	private Button lockPhoneButton;
	private Button trainFacialRecognitionButton;
	private TextView lockTrainText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.lock_train);

		this.findViews();
		this.setListeners();
		this.setupUI();

	}

	private void setupUI() {
		Bundle extras = this.getIntent().getExtras();
		this.lockTrainText.setText("Logged in as " + extras.getString("user"));

	}

	private void findViews() {
		this.lockPhoneButton = (Button) this.findViewById(R.id.lockPhone);
		this.trainFacialRecognitionButton = (Button) this.findViewById(R.id.trainFacialRecognition);
		this.lockTrainText = (TextView) this.findViewById(R.id.lockTrainText);
	}

	private void setListeners() {
		this.lockPhoneButton.setOnClickListener(this);
		this.trainFacialRecognitionButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		Intent intent;
		Bundle extras = this.getIntent().getExtras();
		switch (view.getId()) {

		case R.id.lockPhone:
			intent = new Intent(this, UnLockFace.class);
			intent.putExtra("user", extras.getString("user"));
			this.startActivity(intent);
			break;

		case R.id.trainFacialRecognition:
			intent = new Intent(this, TrainFacialRecognition.class);
			intent.putExtra("user", extras.getString("user"));
			this.startActivity(intent);
			break;

		}
	}

}