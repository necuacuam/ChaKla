#pragma once

#include "esp_err.h"
#include <stddef.h>

esp_err_t init_sdcard(void);

esp_err_t read_wifi_credentials(
    char *ssid_out, size_t ssid_len,
    char *pass_out, size_t pass_len
);
