LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := apptroy
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES:= dump.cpp


include $(BUILD_SHARED_LIBRARY)
