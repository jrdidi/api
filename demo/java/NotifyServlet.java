import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class NotifyServlet extends HttpServlet {

    public void init() throws ServletException {
        // Do required initialization
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> urlParam = new HashMap<>();
        // 收集 URL 中的参数
        urlParam.put("appId", request.getParameter("appId"));
        urlParam.put("apiKey", request.getParameter("apiKey"));
        urlParam.put("jrddInputCharset", request.getParameter("jrddInputCharset"));
        urlParam.put("jrddSignType", request.getParameter("jrddSignType"));

        Map<String, String> postParam = new HashMap<>();
        // 收集 Body 中的参数
        postParam.put("jrddNotifyId", request.getParameter("jrddNotifyId"));
        postParam.put("jrddNotifyTime", request.getParameter("jrddNotifyTime"));
        postParam.put("jrddOrderId", request.getParameter("jrddOrderId"));
        postParam.put("appOrderId", request.getParameter("appOrderId"));
        postParam.put("orderType", request.getParameter("orderType"));
        postParam.put("orderAmount", request.getParameter("orderAmount"));
        postParam.put("orderCoinSymbol", request.getParameter("orderCoinSymbol"));
        postParam.put("orderStatus", request.getParameter("orderStatus"));
        postParam.put("statusReason", request.getParameter("statusReason"));
        postParam.put("orderRemark", request.getParameter("orderRemark"));
        postParam.put("orderPayTypeId", request.getParameter("orderPayTypeId"));
        postParam.put("payAccountId", request.getParameter("payAccountId"));
        postParam.put("payQRUrl", request.getParameter("payQRUrl"));
        postParam.put("payAccountUser", request.getParameter("payAccountUser"));
        postParam.put("payAccountInfo", request.getParameter("payAccountInfo"));

        // 验证签名是否匹配
        String signGet = request.getParameter("jrddSignContent"); // 从 URL query 参数中收到的签名
        String signExpect = API.buildSign(urlParam, postParam); // 自己计算的出来签名
        if (!signExpect.equals(signGet)) {
            System.err.println("BAD SIGNATURE");
            return;
        }

        // 这里进行业务处理 TODO
        if (request.getParameter("orderStatus").equals("1")) {
            // 订单创建成功
        } else if (request.getParameter("orderStatus").equals("7")) {
            // 订单完成
        } else if (request.getParameter("orderStatus").equals("8")) {
            // 订单取消（超时自动取消）
            System.err.println("timeout!!!");
        } else if (request.getParameter("orderStatus").equals("5")) {
            if (request.getParameter("statusReason").equals("19")) {
                // 也是订单完成（最终由客服人工干预后完成交易）
            } else if (request.getParameter("statusReason").equals("20")) {
                // 也是订单取消（最终由客服人工干预后取消交易）
            }
        }

        // 当业务处理完成后，返回 SUCCESS
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.write("SUCCESS");
    }

    public void destroy() {
        // do nothing.
    }
}
