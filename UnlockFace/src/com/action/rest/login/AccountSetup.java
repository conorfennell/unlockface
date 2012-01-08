package com.action.rest.login;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.action.rest.R;

public class AccountSetup extends Activity implements DialogInterface.OnClickListener, OnClickListener {

	private final String TAG = "AN9";
	private EditText userName;
	private EditText password;
	private Button submitButton;
	private AlertDialog alertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.account_setup);

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
			if (!this.isAccountSetup()) {
				if (this.userName.getText().toString().equals("Username")
						|| this.userName.getText().toString().length() == 0) {
					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Info");
					this.alertDialog.setMessage("User name cannot be Username or empty");
					this.alertDialog.show();
				}
				if (this.password.getText().toString().equals("Password")
						|| this.password.getText().toString().length() < 4) {
					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Info");
					this.alertDialog.setMessage("Password cannot be Password or less than 4 characters");
					this.alertDialog.show();
				} else {
					this.saveAccountDetails(this.userName.getText().toString(), this.password.getText().toString());
					this.alertDialog = new AlertDialog.Builder(this).create();
					this.alertDialog.setTitle("Important");
					this.alertDialog
							.setMessage("Registered with \n\tUsername: "
									+ this.userName.getText().toString()
									+ "\n\t Password: "
									+ this.password.getText().toString()
									+ "\nPlease remember this as there is no way to recover the password or username. Also you can only have one account per phone. The only way to setup another account is reinstalling the app");
					this.alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
					this.alertDialog.show();
				}
			}
			break;

		}
	}

	private Boolean isAccountSetup() {
		String FILENAME = "login.txt";
		Boolean isAccountSetup = null;

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
				this.alertDialog = new AlertDialog.Builder(this).create();
				this.alertDialog.setTitle("Info");
				this.alertDialog.setMessage("Already Regeistered, have to reinstall to change user");
				this.alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK", this);
				this.alertDialog.setCancelable(false);

				this.alertDialog.show();

			}

			fis.close();
			// if account is setup
			isAccountSetup = true;

		} catch (FileNotFoundException e) {
			// if account not setup
			isAccountSetup = false;
		} catch (IOException e) {
			Log.e(this.TAG, "getAccountDetails() IOException " + e.getMessage());
		}
		return isAccountSetup;

	}

	private void saveAccountDetails(String userName, String password) {
		String FILENAME = "login.txt";

		FileOutputStream fos;
		try {
			fos = this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write(userName.getBytes());
			fos.write(",".getBytes());
			fos.write(password.getBytes());
			fos.close();

		} catch (FileNotFoundException e) {
			Log.e(this.TAG, "saveAccountDetails() FileNotFoundException " + e.getMessage());
		} catch (IOException e) {
			Log.e(this.TAG, "saveAccountDetails() IOException " + e.getMessage());
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