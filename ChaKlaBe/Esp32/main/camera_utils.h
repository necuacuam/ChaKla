#pragma once
#include "esp_err.h"
#include "esp_camera.h"

esp_err_t init_camera(void);
camera_fb_t* capture_frame(void);
void stop_camera(void);
