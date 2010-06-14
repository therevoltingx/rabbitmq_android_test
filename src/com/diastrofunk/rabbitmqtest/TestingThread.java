/**
 * 
 */
package com.diastrofunk.rabbitmqtest;
import android.util.Log;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import java.util.TimerTask;
import java.util.Timer;

/**
 * @author miguelmorales
 *
 */
public class TestingThread extends Thread
{
	private final static String TAG = "rabbitmq_test:TestingThread";
	
	private String mHostname;
	private int mPort;
	private String mUsername;
	private String mPassword;
	private String mVHost;
    private String mQueuename;
    
	private Handler mHandler;
	
	private JavaTests jtests;
	
	private static final int REPORTING_TIME_MS = 10000; //10 seconds;
	private static final int N_RUNS = 100; 
	
	TestingThread(Handler handler, String hostname, int port, String username, String password, String vhost, String queuename)
    {
		mHandler = handler;
        mHostname = hostname;
        mPort = port;
        mUsername = username;
        mPassword = password;
        mVHost = vhost;
        mQueuename = queuename;
    }
    @Override
    public void run()
    {
        jtests = new JavaTests(mHostname, mPort, mUsername, mPassword, mVHost);
        
        RCTask java_rc_task = new RCTask(jtests);
        Timer java_rc_timer = new Timer();
        java_rc_timer.schedule(java_rc_task, 1000, REPORTING_TIME_MS);
        
        Log.d(TAG, "starting java client tests...");
        jtests.connect();
        //jtests.test_consume_by_standard_basic_get(mQueuename);
        //jtests.test_consume_by_consumer(mQueuename);
        jtests.test_consume_by_direct_lib(mQueuename); 
        jtests.disconnect();
        
        /*
        //Log.d(TAG, "starting ndk client tests...");
        NDKTests ndktests = new NDKTests(mHostname, mPort, mUsername, mPassword, mVHost);
        ndktests.test_basic(mQueuename);
        
        RCTask ndk_rc_task = new RCTask(ndktests);
        Timer ndk_rc_timer = new Timer();
        ndk_rc_timer.schedule(ndk_rc_task, 1000, REPORTING_TIME_MS);
        */
    }
    
    /*reporting and controlling task*/
    private int mCurrentRCRuns = 0;
    private boolean mStopTest = false;
    class RCTask extends TimerTask
    {
    	BaseTest mTester;
    	RCTask(BaseTest tester)
    	{
    	    mTester = tester;
    	}
    	
    	@Override
    	public void run()
    	{
    	    //Log.d(TAG, "Running reporting task...");
    	    mTester.PrintReport();
    	    
    	    if (mCurrentRCRuns == N_RUNS)
    	    {
    	    	//stop current test....
    	    	mStopTest = true;
    	    	mTester.StopTest();
    	    }
    	}
    }
    
}
