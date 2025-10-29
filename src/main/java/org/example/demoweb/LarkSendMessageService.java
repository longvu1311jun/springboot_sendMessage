package org.example.demoweb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LarkSendMessageService {
    private static final String APP_ID = "cli_a8563adbd3b95010";
    private static final String APP_SECRET = "cE1L6Q1GgLF1ZZlovVJmvgY7P7GXSeeR";
    private static final int INACTIVE_DAYS = Integer.parseInt(System.getenv().getOrDefault("INACTIVE_DAYS", "3"));

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

    // Lấy open_id từ email
    public String getOpenIdByEmail(String token, String email) throws Exception {
        URL url = new URL("https://open.larksuite.com/open-apis/contact/v3/users/batch_get_id?user_id_type=open_id");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        JSONObject body = new JSONObject();
        JSONArray emails = new JSONArray();
        emails.put(email);
        body.put("emails", emails);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        JSONObject resp = new JSONObject(sb.toString());
        System.out.println("API response: " + resp.toString()); // Debug response

        JSONArray userList = resp.optJSONObject("data").optJSONArray("user_list");
        if (userList != null && userList.length() > 0 && userList.getJSONObject(0).has("user_id")) {
            String openId = userList.getJSONObject(0).getString("user_id");
            System.out.println("OpenID: " + openId);
            return openId;
        } else {
            throw new RuntimeException("Không tìm thấy open_id cho email: " + email + ". Response: " + resp.toString());
        }
    }

    // Lấy user_id từ email
    public String getUserIdByEmail(String token, String email) throws Exception {
        URL url = new URL("https://open.larksuite.com/open-apis/contact/v3/users/batch_get_id?user_id_type=user_id");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        JSONObject body = new JSONObject();
        JSONArray emails = new JSONArray();
        emails.put(email);
        body.put("emails", emails);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        JSONObject resp = new JSONObject(sb.toString());
        JSONArray userList = resp.optJSONObject("data").optJSONArray("user_list");
        if (userList != null && userList.length() > 0 && userList.getJSONObject(0).has("user_id")) {
            String userId = userList.getJSONObject(0).getString("user_id");
            System.out.println("UserID: " + userId);
            return userId;
        } else {
            throw new RuntimeException("Không tìm thấy user_id cho email: " + email + ". Response: " + resp.toString());
        }
    }

    // Truyền ngày bắt đầu, kết thúc vào hàm lấy activity
    public Map<String, List<UserActivity>> getAllUserActivities(String token, String startDate, String endDate) throws Exception {
        Map<String, List<UserActivity>> userActivityMap = new HashMap<>();

        String pageToken = null;
        boolean hasMore = true;

        while (hasMore) {
            StringBuilder urlBuilder = new StringBuilder("https://open.larksuite.com/open-apis/admin/v1/admin_user_stats?");
            urlBuilder.append("start_date=").append(startDate).append("&end_date=").append(endDate);
            if (pageToken != null) urlBuilder.append("&page_token=").append(pageToken);
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONObject data = json.getJSONObject("data");
            JSONArray items = data.optJSONArray("items");
            if (items == null) break;

            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < items.length(); i++) {
                JSONObject u = items.getJSONObject(i);
                String userName = u.optString("user_name", "Unknown");
                LocalDate date = LocalDate.parse(u.getString("date"), dateFmt);
                int active = u.optInt("im_active_flag", 0);

                userActivityMap.computeIfAbsent(userName, k -> new ArrayList<>())
                        .add(new UserActivity(date, active));
            }

            hasMore = data.optBoolean("has_more", false);
            pageToken = data.optString("page_token", null);
        }

        return userActivityMap;
    }

    public void sendTextMessage(String token, String openId, String message) throws Exception {
        String urlStr = "https://open.larksuite.com/open-apis/im/v1/messages?receive_id_type=open_id";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        JSONObject body = new JSONObject();
        body.put("receive_id", openId);
        body.put("msg_type", "text");
        body.put("content", new JSONObject().put("text", message).toString()); // content là chuỗi JSON
        body.put("uuid", UUID.randomUUID().toString());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        System.out.println("📤 Response gửi tin nhắn: " + sb.toString());
    }

    // Hàm run mới nhận tham số từ controller
    public String run(String startDate, String endDate, String email) throws Exception {
        String token = getTenantAccessToken();
        String openId = getOpenIdByEmail(token, email); // Sử dụng openId
        Map<String, List<UserActivity>> userActivityMap = getAllUserActivities(token, startDate, endDate);

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        List<String> inactiveNDays = new ArrayList<>();
        List<String> inactiveLast7Days = new ArrayList<>();

        for (Map.Entry<String, List<UserActivity>> entry : userActivityMap.entrySet()) {
            String userName = entry.getKey();
            List<UserActivity> logs = entry.getValue();

            logs.sort(Comparator.comparing(u -> u.date, Comparator.reverseOrder()));

            List<UserActivity> lastN = logs.stream()
                    .filter(l -> !l.date.isAfter(today.minusDays(1)))
                    .limit(INACTIVE_DAYS)
                    .toList();

            if (lastN.size() == INACTIVE_DAYS && lastN.stream().allMatch(l -> l.imActiveFlag == 0)) {
                inactiveNDays.add(userName);
            }

            logs.stream()
                    .filter(l -> !l.date.isAfter(sevenDaysAgo) && l.imActiveFlag == 0)
                    .findFirst()
                    .ifPresent(l -> inactiveLast7Days.add(userName));
        }

        StringBuilder msg = new StringBuilder();
      System.out.println("LongVT\n");
        msg.append("📊 **Báo cáo hoạt động nhân viên**\n\n");
        msg.append(String.format("🚨 Không hoạt động liên tục %d ngày: %d nhân viên\n", INACTIVE_DAYS, inactiveNDays.size()));
        if (!inactiveNDays.isEmpty()) {
            msg.append("Danh sách: \n");
            for (String name : inactiveNDays) {
                msg.append(name).append("\n");
            }
            msg.append("\n");
        }

        msg.append(String.format("😴 Không hoạt động trong 7 ngày: %d nhân viên\n", inactiveLast7Days.size()));
        if (!inactiveLast7Days.isEmpty()) {
            msg.append("Danh sách: \n");
            for (String name : inactiveLast7Days) {
                msg.append(name).append("\n");
            }
        }

        sendTextMessage(token, openId, msg.toString()); // Truyền openId
        return "Message sent successfully!";
    }
}
