#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "esp_log.h"
#include "nvs_flash.h"


#include "sd_utils.h"  
#include "wifi_utils.h"  
#include "http_server.h"  

static const char *TAG = "APP_MAIN";

void app_main(void)
{
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);


    char ssid[64] = {0};
    char password[64] = {0};

    ESP_LOGI(TAG, "Booting ChaKlaBe...");

    if (init_sdcard() != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize SD card.");
        return;
    }

    if (read_wifi_credentials(ssid, sizeof(ssid), password, sizeof(password)) == ESP_OK) {
        if (wifi_connect(ssid, password) == ESP_OK) {
            ESP_LOGI(TAG, "Wi-Fi connected successfully.");
            ESP_ERROR_CHECK(start_http_server());
            ESP_LOGI(TAG, "HTTP server started.");
            //ESP_ERROR_CHECK(init_camera());
            //ESP_LOGI(TAG, "Camera started.");            
        } else {
            ESP_LOGE(TAG, "Wi-Fi connection failed.");
        }
    }
}
