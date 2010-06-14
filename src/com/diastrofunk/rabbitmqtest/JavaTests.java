/**
 * 
 */
package com.diastrofunk.rabbitmqtest;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.*;
import com.rabbitmq.client.impl.AMQChannel.SimpleBlockingRpcContinuation;
import android.util.Log;
/**
 * @author miguelmorales
 *
 */
public class JavaTests implements BaseTest
{
	private static final String TAG = "rabbitmq_test:JavaTests";
	
	private String mHostname;
	private int mPort;
	private String mUsername;
	private String mPassword;
	private String mVHost;
	
	private Connection mConnection;
    JavaTests(String hostname, int port, String username, String password, String vhost)
    {
        mHostname = hostname;
        mPort = port;
        mUsername = username;
        mPassword = password;
        mVHost = vhost;
    }
    
    boolean connect()
    {
    	ConnectionParameters params = new ConnectionParameters();
    	params.setUsername(mUsername);
    	params.setPassword(mPassword);
    	params.setVirtualHost(mVHost);
    	params.setRequestedHeartbeat(0);
    	
    	ConnectionFactory factory = new ConnectionFactory(params);
    	try
    	{
    	    mConnection = factory.newConnection(mHostname, mPort);
    	}
    	catch (Exception e)
    	{
    	    Log.d(TAG, "Failed to connect to server!");
    	    e.printStackTrace();
    	    return false;
    	}
    	
    	return true;
    }
    
    public void disconnect()
    {
    	try
    	{
    	    mConnection.close();
    	}
    	catch (Exception e)
    	{
    		Log.d(TAG, "Exception closing connection!");
    		e.printStackTrace();
    		return;
    	}
    }
    
    /*begin tests*/
    public boolean mStopTest = false;
    public int mConsumedMessages = 0;
    
    void test_consume_by_direct_lib(String queuename)
    {
    	try
    	{
    		boolean noAck = true;
    	    AMQConnection mAMQConnection = (AMQConnection) mConnection;    
    	    AMQChannel mChannel = mAMQConnection._channelManager.createChannel(mAMQConnection);
    	    AMQImpl.Basic.Get getCache = new AMQImpl.Basic.Get(1, queuename, noAck);
            //AMQCommand get_cmd = new AMQCommand(getCache);
            
            Log.d(TAG, "starting to consume....");
            while (!mStopTest)
            {
            	SimpleBlockingRpcContinuation k = new SimpleBlockingRpcContinuation();
                mChannel.rpc(getCache, k); //TODO: this allocates in: AMQPChannel #291
            	AMQCommand getCommand = k.getReply();
            	byte[] body = getCommand.getContentBody();
            	
            	if (body == null)
            	{
            		continue;
            	}
            	else
            	{
            		mConsumedMessages++;
            	}
            	
            }
            Log.d(TAG, "stopped consuming.");
    	}
    	catch (Exception e)
    	{
    		Log.d(TAG, "Exception!");
    		e.printStackTrace();
    	}
    }
    
    void test_consume_by_consumer(String queuename)
    {
        Channel channel;
        boolean noAck = true;
        QueueingConsumer consumer;
        try
        {
            channel = mConnection.createChannel();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queuename, noAck, consumer);   
        }
        catch (Exception e)
        {
            Log.d(TAG, "Failed to create channel.");
            return;
        }
        
        Log.d(TAG, "Starting to consume messages...");
        mConsumedMessages = 0;
        
        while (mStopTest)
        {
            QueueingConsumer.Delivery delivery;
            try
            {
                delivery = consumer.nextDelivery();
                mConsumedMessages++;
                
                //TODO: do something here...
            }
            catch (Exception e)
            {
                 Log.d(TAG, "Exception on grabbing delivery!");
                 e.printStackTrace();
                 return;
            }
                 
        }
        
        try
        {       
            channel.close();
        }
        catch (Exception e)
        {
        	Log.d(TAG, "Exception closing channel!");
        	e.printStackTrace();
        	return;
        }
    }
    
    void test_consume_by_standard_basic_get(String queuename)
    {
    	Channel channel;
    	try
    	{
    	    channel = mConnection.createChannel();
    	}
    	catch (Exception e)
    	{
    		Log.d(TAG, "Failed to create channel!!!");
    		e.printStackTrace();
    		return;
    	}
    	
    	boolean noAck = true;
    	Log.d(TAG, "Starting to consume messages...");
    	mConsumedMessages = 0;
        while (!mStopTest)
        {
        	GetResponse response;
        	try
        	{
        	    response = channel.basicGet(queuename, noAck);
        	}
        	catch (Exception e)
        	{
        		Log.d(TAG, "Exception executing basic get!");
        		return;
        	}
        	if (response == null)
        	{
        		continue;
        	}
        	
        	//TODO: do something here...
        	mConsumedMessages++;
        }
        Log.d(TAG, "Stopped consuming messages, any gc runs?");
        
        try
        {       
            channel.close();
        }
        catch (Exception e)
        {
        	Log.d(TAG, "Exception closing channel!");
        	e.printStackTrace();
        	return;
        }
    }
    
    /*reporting/testing implementation*/
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
