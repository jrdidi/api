import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class API {
    // 下面是接口请求地址，请联系客服获取
    public static String serviceBaseURL = "http://13.250.12.109:8084";

    public static String serviceURLDeposit = serviceBaseURL + "/order/deposit/create";
    public static String serviceURLWithdraw = serviceBaseURL + "/order/withdraw/create";

    // 请替换下面 3 个配置
    private static String APP_ID = "xxxxxxxxxxxxxxx";
    private static String API_KEY = "xxxxxxxxxxxxxxx";
    private static String SECRET_KEY = "xxxxxxxxxxxxxxx";

    private static String APP_SING_TYPE = "HMAC-SHA256";
    private static String API_VERSION = "1.1";
    private static String INPUT_CHARSET = "UTF-8";

    public static void main(String[] args) {
        String html = depositExample();  // 把生成的HTML form表单返回给用户浏览器，可以完成下单。
        System.out.println(html);
//        html = withdrawExample();
//        System.out.println(html);
    }

    public static String depositExample() {
        // 创建“充值/支付”订单
        String appUserId = "user1"; // 商家系统进行下单用户的 UserID
        String appOrderId = "order_" + (new Date()).getTime();
        String orderAmount = "700";
        String orderCoinSymbol = "CNY";
        String orderPayTypeId = "4"; // 工商银行
        String orderRemark = "remark";
        String appServerNotifyUrl = "http://13.250.12.109:8084/notify.php"; // 这里改成自己的接收异步回调通知的API地址，订单状态发生关键变更的时候，JRDiDi系统会第一时间调用这个API发送通知
        String appReturnPageUrl = "http://13.250.12.109:8084/return.php"; // 这里改成支付成功后跳转的页面
        String html = deposit(appUserId, appOrderId, orderAmount, orderCoinSymbol, orderPayTypeId, orderRemark, appServerNotifyUrl, appReturnPageUrl);
        return html;
    }

    public static String withdrawExample() {
        // 创建“提现/收款”订单
        String appUserId = "user1"; // 商家系统进行下单用户的 UserID
        String appOrderId = "order_" + (new Date()).getTime();
        String orderAmount = "700";
        String orderCoinSymbol = "CNY";
        String orderPayTypeId = "4"; // 工商银行
        String orderRemark = "remark";
        String appServerNotifyUrl = "http://13.250.12.109:8084/notify.php"; // 这里改成自己的接收异步回调通知的API地址，订单状态发生关键变更的时候，JRDiDi系统会第一时间调用这个API发送通知
        String appReturnPageUrl = "http://13.250.12.109:8084/return.php"; // 这里改成支付成功后跳转的页面
        String payAccountId = "1234"; // 银行卡卡号
        String payQRUrl = "";
        String payAccountUser = "张三"; // 收款人的真实姓名
        String payAccountInfo = "xx支行"; // 填写分行或支行名称
        String html2 = withdraw(appUserId, appOrderId, orderAmount, orderCoinSymbol, orderPayTypeId, orderRemark,
                payAccountId, payQRUrl, payAccountUser, payAccountInfo, appServerNotifyUrl, appReturnPageUrl);
        return html2;
    }

    public static String deposit(String appUserId,
                               String appOrderId,
                               String orderAmount,
                               String orderCoinSymbol,
                               String orderPayTypeId,
                               String orderRemark,
                               String appServerNotifyUrl,
                               String appReturnPageUrl
    ) {
            // 构造签名原串
            Map<String, String> urlParam = new HashMap<>();
            // URL param
            urlParam.put("appId", APP_ID);
            urlParam.put("apiKey", API_KEY);
            urlParam.put("appSignType", APP_SING_TYPE);
            urlParam.put("apiVersion", API_VERSION);
            urlParam.put("inputCharset", INPUT_CHARSET);

            Map<String, String> postParam = new HashMap<>();
            // Body param
            postParam.put("appUserId", appUserId);
            postParam.put("appOrderId", appOrderId);
            postParam.put("orderAmount", orderAmount);
            postParam.put("orderCoinSymbol", orderCoinSymbol);
            postParam.put("orderRemark", orderRemark);
            postParam.put("orderPayTypeId", orderPayTypeId);
            postParam.put("appServerNotifyUrl", appServerNotifyUrl);
            postParam.put("appReturnPageUrl", appReturnPageUrl);

            // 生成签名
            String appSignContent = buildSign(urlParam, postParam);

            // 完整的API请求URL
            String requestURL = serviceURLDeposit + "?appId=" + encodeValue(APP_ID) + "&apiKey=" + encodeValue(API_KEY)
                    + "&inputCharset=" + encodeValue(INPUT_CHARSET) + "&apiVersion=" + encodeValue(API_VERSION)
                    + "&appSignType=" + encodeValue(APP_SING_TYPE) + "&appSignContent=" + encodeValue(appSignContent);

            String html = "<form id='jrdidi_submit' name='jrdidi_submit' action='" + requestURL + "' method='POST'>";
            for (Map.Entry<String, String> e : postParam.entrySet()) {
                String value =  e.getValue().replace("'", "&apos;");
                html += "<input type='hidden' name='" + e.getKey() + "' value='" + value + "'/>";
            }

            // submit按钮控件请不要含有name属性
            html += "<input type='submit' value='ok' style='display:none;''></form>";
            // 用JavaScript自动提交form表单
            html += "<script>document.forms['jrdidi_submit'].submit();</script>";

            return html;
    }

    public static String withdraw(String appUserId,
                                 String appOrderId,
                                 String orderAmount,
                                 String orderCoinSymbol,
                                 String orderPayTypeId,
                                 String orderRemark,
                                 String payAccountId,
                                 String payQRUrl,
                                 String payAccountUser,
                                 String payAccountInfo,
                                 String appServerNotifyUrl,
                                 String appReturnPageUrl
    ) {
        // 构造签名原串
        Map<String, String> urlParam = new HashMap<>();
        // URL param
        urlParam.put("appId", APP_ID);
        urlParam.put("apiKey", API_KEY);
        urlParam.put("appSignType", APP_SING_TYPE);
        urlParam.put("apiVersion", API_VERSION);
        urlParam.put("inputCharset", INPUT_CHARSET);

        Map<String, String> postParam = new HashMap<>();
        // Body param
        postParam.put("appUserId", appUserId);
        postParam.put("appOrderId", appOrderId);
        postParam.put("orderAmount", orderAmount);
        postParam.put("orderCoinSymbol", orderCoinSymbol);
        postParam.put("orderRemark", orderRemark);
        postParam.put("orderPayTypeId", orderPayTypeId);
        postParam.put("payAccountId", payAccountId);
        postParam.put("payQRUrl", payQRUrl);
        postParam.put("payAccountUser", payAccountUser);
        postParam.put("payAccountInfo", payAccountInfo);
        postParam.put("appServerNotifyUrl", appServerNotifyUrl);
        postParam.put("appReturnPageUrl", appReturnPageUrl);

        // 生成签名
        String appSignContent = buildSign(urlParam, postParam);

        // 完整的API请求URL
        String requestURL = serviceURLWithdraw + "?appId=" + encodeValue(APP_ID) + "&apiKey=" + encodeValue(API_KEY)
                + "&inputCharset=" + encodeValue(INPUT_CHARSET) + "&apiVersion=" + encodeValue(API_VERSION)
                + "&appSignType=" + encodeValue(APP_SING_TYPE) + "&appSignContent=" + encodeValue(appSignContent);

        String html = "<form id='jrdidi_submit' name='jrdidi_submit' action='" + requestURL + "' method='POST'>";
        for (Map.Entry<String, String> e : postParam.entrySet()) {
            String value =  e.getValue().replace("'", "&apos;");
            html += "<input type='hidden' name='" + e.getKey() + "' value='" + value + "'/>";
        }

        // submit按钮控件请不要含有name属性
        html += "<input type='submit' value='ok' style='display:none;''></form>";
        // 用JavaScript自动提交form表单
        html += "<script>document.forms['jrdidi_submit'].submit();</script>";

        return html;
    }

    // 生成签名串
    public static String buildSign(Map<String, String> urlParam, Map<String, String> postParam) {
        Map<String, String> map = new HashMap<>();

        // Merge two maps
        map.putAll(urlParam);
        map.putAll(postParam);

        List<String> sortedKeys = new ArrayList(map.keySet());
        Collections.sort(sortedKeys);

        StringBuffer sb = new StringBuffer();
        for (String sortedKey : sortedKeys) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(sortedKey + "=" + encodeValue(map.get(sortedKey)));
        }

        String toBeSigned = sb.toString();
        // For debug
        // System.err.println(toBeSigned);

        return HmacSHA256(toBeSigned, SECRET_KEY); // 返回签名后的内容
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String HmacSHA256(String msg, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return bytesToHex(sha256_HMAC.doFinal(msg.getBytes()));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    // URL Encode
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
