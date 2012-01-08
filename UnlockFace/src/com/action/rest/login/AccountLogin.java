package com.action.rest.login;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.action.rest.R;

public class AccountLogin extends Activity implements DialogInterface.OnClickListener, OnClickListener {

	private final String TAG = "AN9";
	// UI parts
	private EditText userName;
	private EditText password;
	private Button submitButton;
	private AlertDialog alertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.account_login);

		this.findViews();
		this.setListeners();

	}

	private void findViews() {
		this.userName = (EditText) this.findViewById(R.id.userName);
		this.password = (EditText) this.findViewById(R.id.password);
		this.submitButton = (Button) this.findViewById(R.id.submit);

	}

	private void setListeners() {
		this.userName.setOnClickListener(this);
		this.password.setOnClickListener(this);
		this.submitButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.userName:
			this.userName.setText("");
			break;
		case R.id.password:
			this.password.setText("");
			break;
		case R.id.submit:
			this.getAccountDetails(this.userName.getText().toString(), this.password.getText().toString());
			break;

		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void getAccountDetails(String userName, String password) {
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
				// checks username and password
				if (userName.equals(array[0]) && password.equals(array[1])) {
					Log.i("AN9", "Logged in");

					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Info");
					this.alertDialog.setMessage("Logged In");
					this.alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
					this.alertDialog.setCancelable(false);

					this.alertDialog.show();
				} else {
					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Info");
					this.alertDialog.setMessage("Incorrect Username or Password");
					this.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", this);
					this.alertDialog.show();
				}
			}

			fis.close();

		} catch (FileNotFoundException e) {
			this.alertDialog = new AlertDialog.Builder(this).create();
			this.alertDialog.setTitle("Info");
			this.alertDialog.setMessage("Nobody Registered");
			this.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", this);
			this.alertDialog.show();
		} catch (IOException e) {
			Log.e(this.TAG, "getAccountDetails() IOException " + e.getMessage());
		}

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		if (AlertDialog.BUTTON_POSITIVE == which) {
			Intent data = new Intent();
			// pass data back to the Welcome Activity
			data.putExtra("user", this.userName.getText().toString());
			this.setResult(RESULT_OK, data);
			this.finish();
		}
	}

}