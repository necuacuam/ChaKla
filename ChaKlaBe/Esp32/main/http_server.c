#include "http_server.h"
#include "esp_log.h"
#include "esp_http_server.h"
#include <string.h>
#include <stdlib.h>
#include <sys/time.h>
#include <inttypes.h>
#include "stream_control.h"
#include "esp_camera.h"
#include "esp_mac.h"
#include "camera_utils.h"  

#define MIN(a, b) ((a) < (b) ? (a) : (b))
#define HTTPD_409_CONFLICT 409

static const char *TAG = "HTTP_SERVER";
static httpd_handle_t server = NULL;

// --- /stream ---
static esp_err_t stream_handler(httpd_req_t *req) {
    if (is_streaming) {
        httpd_resp_send_err(req, HTTPD_409_CONFLICT, "Stream already in use");
        return ESP_OK;
    }

    esp_err_t err = init_camera();
    if (err != ESP_OK) {
        httpd_resp_send_err(req, HTTPD_500_INTERNAL_SERVER_ERROR, "Camera init failed");
        return err;
    }

    ESP_LOGI(TAG, "Camera started.");

    is_streaming = true;
    camera_fb_t *fb = NULL;
    esp_err_t res = ESP_OK;

    httpd_resp_set_type(req, "multipart/x-mixed-replace; boundary=frame");

    while (is_streaming) {
        fb = esp_camera_fb_get();
        if (!fb) {
            ESP_LOGE(TAG, "Camera capture failed");
            continue;
        }

        char header[64];
        snprintf(header, sizeof(header),
                 "--frame\r\nContent-Type: image/jpeg\r\nContent-Length: %u\r\n\r\n", fb->len);

        if ((res = httpd_resp_send_chunk(req, header, strlen(header))) != ESP_OK ||
            (res = httpd_resp_send_chunk(req, (const char *)fb->buf, fb->len)) != ESP_OK ||
            (res = httpd_resp_send_chunk(req, "\r\n", 2)) != ESP_OK) {

            ESP_LOGW(TAG, "Stream send failed");
            esp_camera_fb_return(fb);
            break;
        }

        esp_camera_fb_return(fb);
        fb = NULL;
        vTaskDelay(30 / portTICK_PERIOD_MS);
    }

    ESP_LOGW(TAG, "Stream data ends");
    is_streaming = false;
    stop_camera();
    ESP_LOGI(TAG, "Camera stopped.");

    return res;
}

static esp_err_t picture_handler(httpd_req_t *req) {
    ESP_LOGI(TAG, "📸 /picture handler invoked");

    if (is_streaming) {
        ESP_LOGW(TAG, "Rejected: stream is active");
        httpd_resp_send_err(req, HTTPD_409_CONFLICT, "Streaming in progress");
        return ESP_FAIL;
    }

    ESP_LOGI(TAG, "Initializing camera for snapshot");
    esp_err_t err = init_camera();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Camera init failed: %s", esp_err_to_name(err));
        httpd_resp_send_err(req, HTTPD_500_INTERNAL_SERVER_ERROR, "Camera init failed");
        return err;
    }

    camera_fb_t *fb = esp_camera_fb_get();
    if (!fb) {
        ESP_LOGE(TAG, "Frame capture failed");
        stop_camera();
        httpd_resp_send_err(req, HTTPD_500_INTERNAL_SERVER_ERROR, "Capture failed");
        return ESP_FAIL;
    }

    ESP_LOGI(TAG, "Captured frame size: %d bytes", fb->len);
    httpd_resp_set_type(req, "image/jpeg");

    // Optional: delay to avoid early return affecting network transmission
    vTaskDelay(10 / portTICK_PERIOD_MS);

    err = httpd_resp_send(req, (const char *)fb->buf, fb->len);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to send frame: %s", esp_err_to_name(err));
    } else {
        ESP_LOGI(TAG, "JPEG image sent successfully");
    }

    esp_camera_fb_return(fb);
    stop_camera();
    ESP_LOGI(TAG, "📷 Snapshot handler completed");
    return err;
}


// --- /status ---
static esp_err_t status_get_handler(httpd_req_t *req) {
    const char* resp = "ok";
    httpd_resp_send(req, resp, HTTPD_RESP_USE_STRLEN);
    return ESP_OK;
}

// --- /settime ---
static esp_err_t settime_post_handler(httpd_req_t *req) {
    char buf[64];
    int ret = httpd_req_recv(req, buf, MIN(req->content_len, sizeof(buf) - 1));
    if (ret <= 0) {
        ESP_LOGE(TAG, "Failed to receive body");
        httpd_resp_send_err(req, HTTPD_400_BAD_REQUEST, "No body received");
        return ESP_FAIL;
    }

    buf[ret] = '\0';
    ESP_LOGI(TAG, "Received time string: %s", buf);

    time_t new_time = strtoll(buf, NULL, 10);
    struct timeval tv = { .tv_sec = new_time, .tv_usec = 0 };
    settimeofday(&tv, NULL);
    ESP_LOGI(TAG, "System time set to %" PRId64, (int64_t)new_time);

    httpd_resp_sendstr(req, "time set");
    return ESP_OK;
}

// --- URI table ---
static const httpd_uri_t status_uri = {
    .uri      = "/status",
    .method   = HTTP_GET,
    .handler  = status_get_handler,
    .user_ctx = NULL
};

static const httpd_uri_t settime_uri = {
    .uri      = "/settime",
    .method   = HTTP_POST,
    .handler  = settime_post_handler,
    .user_ctx = NULL
};

// --- Start HTTP server ---
esp_err_t start_http_server(void) {
    httpd_config_t config = HTTPD_DEFAULT_CONFIG();
    config.uri_match_fn = httpd_uri_match_wildcard;

    if (httpd_start(&server, &config) != ESP_OK) {
        ESP_LOGE(TAG, "Failed to start HTTP server");
        return ESP_FAIL;
    }

    httpd_register_uri_handler(server, &status_uri);
    httpd_register_uri_handler(server, &settime_uri);

    httpd_register_uri_handler(server, &(httpd_uri_t){
        .uri = "/stream", .method = HTTP_GET, .handler = stream_handler });

        httpd_register_uri_handler(server, &(httpd_uri_t){
    .uri = "/picture", .method = HTTP_GET, .handler = picture_handler });


    ESP_LOGI(TAG, "HTTP server started with all handlers");
    return ESP_OK;
}
