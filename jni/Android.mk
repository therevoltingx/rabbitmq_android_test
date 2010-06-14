LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := rabbitmq-android
LOCAL_C_INCLUDES := rabbitmq-c/librabbitmq
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES := ndktests.c rabbitmq-c/examples/example_utils.c rabbitmq-c/librabbitmq/amqp_api.c rabbitmq-c/librabbitmq/amqp_connection.c  rabbitmq-c/librabbitmq/amqp_debug.c  rabbitmq-c/librabbitmq/amqp_framing.c  rabbitmq-c/librabbitmq/amqp_mem.c  rabbitmq-c/librabbitmq/amqp_socket.c  rabbitmq-c/librabbitmq/amqp_table.c
include $(BUILD_SHARED_LIBRARY)
###################################################