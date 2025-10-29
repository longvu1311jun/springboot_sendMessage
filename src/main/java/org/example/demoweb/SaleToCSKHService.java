package org.example.demoweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class SaleToCSKHService {
  private static final String APP_ID = "cli_a8563adbd3b95010";
  private static final String APP_SECRET = "cE1L6Q1GgLF1ZZlovVJmvgY7P7GXSeeR";
  private static final Logger LOGGER = Logger.getLogger(SaleToCSKHService.class.getName());
  public String getTenantAccessToken() throws Exception {
    URL url = new URL("https://open.larksuite.com/open-apis/auth/v3/tenant_access_token/internal");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

    JSONObject body = new JSONObject();
    body.put("app_id", APP_ID);
    body.put("app_secret", APP_SECRET);

    try (OutputStream os = conn.getOutputStream()) {
      os.write(body.toString().getBytes(StandardCharsets.UTF_8));
    }

    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null) sb.append(line);
    in.close();

    JSONObject resp = new JSONObject(sb.toString());
    System.out.println("get token ok");
    if (resp.has("tenant_access_token")) {
      return resp.getString("tenant_access_token");
    } else {
      throw new RuntimeException("Không lấy được tenant_access_token: " + resp.toString());
    }
  }
  public JSONObject getDataFormSale(String tableId, String recordId) throws Exception {
    HttpURLConnection conn = null;
    BufferedReader reader = null;
    String accessToken = getTenantAccessToken();
    // Tạo endpoint URL động
    String apiUrl = String.format(
        "https://open.larksuite.com/open-apis/bitable/v1/apps/%s/tables/%s/records/%s",
        "VsLjbnWlfapGXhszsvqlRm6QgIf", tableId, recordId
    );

    try {
      URL url = new URL(apiUrl);
      conn = (HttpURLConnection) url.openConnection();

      // Cấu hình request
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setRequestProperty("Accept", "application/json");

      int responseCode = conn.getResponseCode();
      StringBuilder responseBuilder = new StringBuilder();

      // Đọc stream tùy theo mã phản hồi
      if (responseCode == HttpURLConnection.HTTP_OK) {
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      } else {
        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
      }

      String line;
      while ((line = reader.readLine()) != null) {
        responseBuilder.append(line);
      }

      JSONObject responseJson = new JSONObject(responseBuilder.toString());

      if (responseCode != HttpURLConnection.HTTP_OK) {
        LOGGER.warning("❌ Lark API returned error: " + responseJson.toString());
        throw new RuntimeException("Lark API call failed with status: " + responseCode);
      }

      // ✅ Lark Bitable data nằm trong `data` → `record`
      JSONObject data = responseJson.getJSONObject("data");
      LOGGER.info("✅ Lark Bitable record fetched successfully: " + data.toString());

      return data;

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error fetching record from Lark API: " + e.getMessage(), e);
      throw e;
    } finally {
      if (reader != null) try { reader.close(); } catch (IOException ignored) {}
      if (conn != null) conn.disconnect();
    }
  }
  public String run(String recordID) throws Exception {
    JSONObject record = getDataFormSale("tblpjDNYC7PHWrmX",recordID);
    System.out.println(record.toString());
    return "Message sent successfully!";
  }

}
