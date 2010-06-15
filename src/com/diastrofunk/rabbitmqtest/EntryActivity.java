package com.diastrofunk.rabbitmqtest;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

public class EntryActivity extends Activity 
{
	private static final String TAG = "rabbitmqtest:EntryActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EntryActivity mSelfActivity = this;
        
        final Button login_button = (Button) findViewById(R.id.start_tests);
        final EditText hostname_edittext = (EditText) findViewById(R.id.hostname);
        final EditText username_edittext = (EditText) findViewById(R.id.username);
        final EditText password_edittext = (EditText) findViewById(R.id.password);
        final EditText vhost_edittext = (EditText) findViewById(R.id.vhost);
        final EditText queuename_edittext = (EditText) findViewById(R.id.queue);
        
        hostname_edittext.setText("dev.rabbitmq.com");
        username_edittext.setText("guest");
        password_edittext.setText("guest");
        vhost_edittext.setText("/");
        queuename_edittext.setText("test");
        
        login_button.setOnClickListener(new View.OnClickListener() 
        {
			
			public void onClick(View v) 
			{
				ProgressDialog dialog = ProgressDialog.show(mSelfActivity, "", "Please wait...", true);
				Log.d(TAG, "Starting tests....");
				
				Handler TThreadHandler = new Handler()
				{
				    @Override
				    public void handleMessage(Message msg)
				    {
				            	
				    }
				};
				TestingThread t_thread = new TestingThread(TThreadHandler, 
						hostname_edittext.getText().toString(), 
						5672, 
						username_edittext.getText().toString(), 
						password_edittext.getText().toString(), 
						vhost_edittext.getText().toString(),
						queuename_edittext.getText().toString()
						);
				t_thread.start();
			}
		});
    }
}