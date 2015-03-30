LOCAL_PATH := $(call my-dir)
ifeq ($(USE_ISG_MULTI_CAMERA_ALL_SHOW), true)
    include $(TOP)/packages/apps/Camera/MultiCamera/Android.mk
endif
ifeq ($(USE_ISG_MULTI_CAMERA_ONE_SHOW), true)
    include $(TOP)/packages/apps/Camera/MultiCamera2/Android.mk
endif
# leaving the makefile emtpy to prevent the build
# system from traversing the project
