package com.action.rest.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.action.rest.R;
import com.action.rest.unlockface.LockTrain;

public class Welcome extends Activity implements OnClickListener {

	private Integer lockTrainRequestCode = 1111;
	private Button loginButton;
	private Button registerButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.welcome);

		this.findViews();
		this.setListeners();

	}

	private void findViews() {
		this.loginButton = (Button) this.findViewById(R.id.login);
		this.registerButton = (Button) this.findViewById(R.id.register);

	}

	private void setListeners() {
		this.loginButton.setOnClickListener(this);
		this.registerButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		Intent intent;
		switch (view.getId()) {
		case R.id.login:
			intent = new Intent(this, AccountLogin.class);
			// this.startActivity(intent);
			this.startActivityForResult(intent, this.lockTrainRequestCode);
			break;
		case R.id.register:
			intent = new Intent(this, AccountSetup.class);
			this.startActivityForResult(intent, this.lockTrainRequestCode);
			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent;
		if (requestCode == this.lockTrainRequestCode && resultCode == RESULT_OK) {
			intent = new Intent(this, LockTrain.class);
			// getting user from returned activity and adding it to the next
			// activity
			intent.putExtra("user", data.getStringExtra("user"));
			this.startActivity(intent);
		}
	}

}