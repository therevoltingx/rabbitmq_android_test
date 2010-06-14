#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#include <stdint.h>
#include <unistd.h>
#include <assert.h>

#include <string.h>
#include <jni.h>
#include <android/log.h>

#include <amqp.h>
#include <amqp_framing.h>

#include "../examples/example_utils.h"

#include "ndktests.h"

const char *TAG = "rabbitmq_test:JNI:ndktests.c";
amqp_connection_state_t g_conn;
int g_sockfd;
int g_channel = 1;
jbyteArray jbytes_cache;

#define BUFFER_SIZE 1024

/*begin native implementations*/
int internal_connect(const char *hostname, int port, const char *username, const char *password, cont char *vhost)
{
	int sockfd;

    g_conn = amqp_new_connection();

    sockfd = amqp_open_socket(hostname, port);
    amqp_set_sockfd(g_conn, sockfd);
    amqp_login(g_conn, vhost, 0, 131072, 0, AMQP_SASL_METHOD_PLAIN, username, password);

    g_sockfd = sockfd;
    return 0;
}

int create_channel()
{
	int channel = g_channel++;
	amqp_channel_open(g_conn, channel);
	die_on_amqp_error(amqp_get_rpc_reply(g_conn), "Opening channel");
	return channel;
}

void internal_basic_consume(JNIEnv * env, int channel, const char *queuename)
{
    //like the rabbitmq-c/examples/amqp_listenq.c
	amqp_basic_consume(g_conn, channel, amqp_cstring_bytes(queuename), AMQP_EMPTY_BYTES, 0, 0, 0);
	die_on_amqp_error(amqp_get_rpc_reply(g_conn), "Consuming");

	{
		amqp_frame_t frame;
		int result;
        amqp_basic_deliver_t *d;
		amqp_basic_properties_t *p;
		size_t body_target;
		size_t body_received;

        while(1)
        {
        	amqp_maybe_release_buffers(g_conn);
			result = amqp_simple_wait_frame(g_conn, &frame);
			if (result <= 0) break;

			if (frame.frame_type != AMQP_FRAME_METHOD) continue;
			if (frame.payload.method.id != AMQP_BASIC_DELIVER_METHOD) continue;

			d = (amqp_basic_deliver_t *) frame.payload.method.decoded;

			result = amqp_simple_wait_frame(g_conn, &frame);
			if (result <= 0) break;
			if (frame.frame_type != AMQP_FRAME_HEADER)
			{
				return;//abort();
			}

			p = (amqp_basic_properties_t *) frame.payload.properties.decoded;
			body_target = frame.payload.properties.body_size;
			body_received = 0;

			while (body_received < body_target)
			{
				result = amqp_simple_wait_frame(g_conn, &frame);
				if (result <= 0) break;
				if (frame.frame_type != AMQP_FRAME_BODY)
				{
					//fprintf(stderr, "Expected body!");
					return;//abort();
				}
				body_received += frame.payload.body_fragment.len;
				assert(body_received <= body_target);
			}
			
			//if (frame.payload.body_fragment.len > BUFFER_SIZE)
			//{
				//__android_log_write(ANDROID_LOG_DEBUG, TAG, "MESSAGE BIGGER THAN BUFFER!");
				//return;
			//}
			//(*env)->SetByteArrayRegion(env, jbytes_cache, 0, frame.payload.body_fragment.len, frame.payload.body_fragment.bytes);
			
			return;
        }
	}
}

/*begin java interface functions*/
jint JNICALL Java_com_diastrofunk_rabbitmqtest_NDKTests_connect
  (JNIEnv *env, jobject obj, jstring jhostname, jint port, jstring jusername, jstring jpassword, jstring jvhost)
{
    const char *hostname;
	const char *username;
	const char *password;
    const char *vhost;
	 
	hostname = (*env)->GetStringUTFChars(env, jhostname, 0);
	username = (*env)->GetStringUTFChars(env, jusername, 0);
	password = (*env)->GetStringUTFChars(env, jpassword, 0);
    vhost = (*env)->GetStringUTFChars(env, jvhost, 0);
	
	__android_log_write(ANDROID_LOG_DEBUG, TAG, "CONNECTING TO MQ SERVER");
	__android_log_write(ANDROID_LOG_DEBUG, TAG, hostname);
	__android_log_write(ANDROID_LOG_DEBUG, TAG, username);

	internal_connect(hostname, port, username, password, vhost);

	__android_log_write(ANDROID_LOG_DEBUG, TAG, "CONNECTED TO MQ SERVER");

	(*env)->ReleaseStringUTFChars(env, jhostname, hostname);
	(*env)->ReleaseStringUTFChars(env, jusername, username);
	(*env)->ReleaseStringUTFChars(env, jpassword, password);
    (*env)->ReleaseStringUTFChars(env, jvhost, vhost);

	/*init some stuff*/
	jbytes_cache = (*env)->NewByteArray(env, BUFFER_SIZE);

	return 1;
}

void JNICALL Java_com_diastrofunk_rabbitmqtest_NDKTests_disconnect
  (JNIEnv *env, jobject obj)
{
    amqp_connection_close(g_conn, AMQP_REPLY_SUCCESS);
    amqp_destroy_connection(g_conn);
    close(g_sockfd);
    __android_log_write(ANDROID_LOG_DEBUG, TAG, "DISCONNECTED FROM MQ SERVER");
}

jint JNICALL Java_com_diastrofunk_rabbitmqtest_NDKTests_createchannel
  (JNIEnv * env, jobject obj)
{
    return create_channel();
}

jbyteArray JNICALL Java_com_diastrofunk_rabbitmqtest_NDKTests_basicconsume
  (JNIEnv *env, jobject obj, jint channel, jstring jqueue_name)
{
	const char *queue_name;
	queue_name = (*env)->GetStringUTFChars(env, jqueue_name, 0);

	//internal_basic_consume(env, channel, queue_name);

	(*env)->ReleaseStringUTFChars(env, jqueue_name, queue_name);

	return jbytes_cache;
}


