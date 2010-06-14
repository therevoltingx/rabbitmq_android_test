/**
 * 
 */
package com.diastrofunk.rabbitmqtest;

import android.util.Log;

/**
 * @author miguelmorales
 *
 */
public class NDKTests implements BaseTest
{
	private static final String TAG = "rabbitmq_test:NDKTests";
	
	private String mHostname;
	private int mPort;
	private String mUsername;
	private String mPassword;
	private String mVHost;
	
    NDKTests(String hostname, int port, String username, String password, String vhost)
    {
    	 mHostname = hostname;
         mPort = port;
         mUsername = username;
         mPassword = password;
         mVHost = vhost;
    }
    
    static 
    {
        System.loadLibrary("rabbitmq-android");
    }
    private native int connect(String hostname, int port, String username, String password, String vhost);
    private native void disconnect();
    private native int createchannel();
    private native byte[] basicconsume(int channel, String queue);
    
    /*begin tests*/
    public boolean mStopTest = false;
    public int mConsumedMessages = 0;
    
    public void test_basic(String queuename)
    {
    	mConsumedMessages = 0;
    	Log.d(TAG, "connecting to mq server");
        connect(mHostname, mPort, mUsername, mPassword, mVHost);
        System.gc();
        
        int channel = createchannel();
        while (!mStopTest)
        {
            basicconsume(channel, "18_area_queue");
            //Log.d(TAG, "consumed message");
            mConsumedMessages++;
        }
        
        disconnect();
        	
    }
    
    public void PrintReport()
    {
        Log.d(TAG, "Consumed: " + mConsumedMessages + " so far.");	
    }
    
    public void StopTest()
    {
        Log.d(TAG, "stopping current test.");
        mStopTest = true;
    }
}
