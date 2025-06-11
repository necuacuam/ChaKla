#include "esp_log.h"
#include "esp_err.h"

#include "driver/sdmmc_host.h"      // SDMMC_HOST_DEFAULT
#include "driver/sdmmc_defs.h"
#include "driver/sdmmc_types.h"     // sdmmc_card_t
#include "sdmmc_cmd.h"              // sdmmc_host_t, sdmmc_slot_config_t, esp_vfs_fat_sdmmc_mount
#include "esp_vfs_fat.h"            // filesystem mount API
#include <string.h>


#define CONFIG_FILE "/sdcard/chaklabe.cfg"
#define MAX_LINE 128

esp_err_t init_sdcard(void) {
    sdmmc_host_t host = SDMMC_HOST_DEFAULT();
    sdmmc_slot_config_t slot_config = SDMMC_SLOT_CONFIG_DEFAULT();
    slot_config.width = 1; // 1-bit mode
    esp_vfs_fat_sdmmc_mount_config_t mount_config = {
        .format_if_mount_failed = false,
        .max_files = 5,
        .allocation_unit_size = 16 * 1024
    };
    sdmmc_card_t *card;
    esp_err_t ret = esp_vfs_fat_sdmmc_mount("/sdcard", &host, &slot_config, &mount_config, &card);
    if (ret != ESP_OK) {
        ESP_LOGE("SD", "Mount failed: %s", esp_err_to_name(ret));
        return ret;
    }
    ESP_LOGI("SD", "SD card mounted.");
    return ESP_OK;
}

esp_err_t read_wifi_credentials(char *ssid_out, size_t ssid_len, char *pass_out, size_t pass_len) {
    FILE *f = fopen(CONFIG_FILE, "r");
    if (!f) {
        ESP_LOGE("SD", "Could not open config file");
        return ESP_FAIL;
    }

    char line[MAX_LINE];
    while (fgets(line, sizeof(line), f)) {
        if (strncmp(line, "ssid=", 5) == 0) {
            strncpy(ssid_out, line + 5, ssid_len - 1);
            ssid_out[strcspn(ssid_out, "\r\n")] = '\0';
        } else if (strncmp(line, "password=", 9) == 0) {
            strncpy(pass_out, line + 9, pass_len - 1);
            pass_out[strcspn(pass_out, "\r\n")] = '\0';
        }
    }

    fclose(f);
    return ESP_OK;
}
