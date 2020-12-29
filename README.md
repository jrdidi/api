# JRDD系统接入说明文档

### **最新API版本号**
* 1.1 （即 `apiVersion=1.1`）

### **最后修订时间**
* 2019年01月31日

### **目录**
* [Part 01：商家接入前准备工作](#part01)
* [Part 02：商家用户创建“充值/支付”订单接入流程](#part02)
* [Part 03：商家用户创建“提现/收款”订单接入流程](#part03)
* [Part 04：商家服务器端查询订单的API及返回响应参数](#part04)
* [Part 05：商家用户打开订单页面的接入流程](#part05)
* [Part 06：商家服务器端接收订单信息更新异步通知](#part06)
* [Part 07：JRDD系统“充值/支付”订单状态表](#part07)
* [Part 08：JRDD系统“提现/收款”订单状态表](#part08)
* [Part 09：签名/验签的方法](#part09)
* [Part 10：JRDD平台支付方式对应ID列表](#part10)

### <span id="part01">Part 01：商家接入前准备工作</span>

* 商家须向JRDD提供商家的名称、网站域名等基本信息
* 商家从JRDD平台获取商家ID（appId）、API Key（apiKey）和Secret Key（secretKey）等基本信息

### <span id="part02">Part 02：商家用户创建“充值/支付”订单接入流程</span>

* **Base URI**：https://api.didipays.com/order/deposit/create

* **HTTP请求方法**：POST

* **基本参数（基本参数需拼接后放在URL中）**  

| 字段名 | 描述 |
|:--|:--|
| appId | 商家的唯一ID （必填）|
| apiKey | API Key, 在JRDD服务器后台设置）|
| inputCharset | 商家向API传入参数内容的字符编码，目的是为了防止字符乱码（非必填，默认为UTF-8。例如商家系统内部是GB2312编码，则需指定inputCharset=GB2312） |
| apiVersion | API版本号（必填，请以本文档开头部分的最新版本号为准，且默认值为最新版本号）|
| appSignType | 商家使用的签名算法名称(非必填，默认为 HMAC-SHA256 ，且目前仅支持 HMAC-SHA256) |
| appSignContent | 商家签名后的字符串。注意<font color=#ff4500>这个字段及其字段值都是不参与签名的</font>，只有完成签名后才需要附加到基本请求参数中。详细签名方法可见 [Part 09：签名/验签的方法](#part09) |
	
* **完整的API请求URL**：
 
```
https://api.didipays.com/order/deposit/create?appId=[由JRDD平台分配的appId]&apiKey=[由JRDD平台分配的apiKey]  
&inputCharset=UTF-8&apiVersion=1.1&appSignType=HMAC-SHA256&appSignContent=[签名内容]
```

* **POST请求参数列表（以下参数需通过 application/x-www-form-urlencoded 请求格式提交给上述完整请求URL）**

| 字段名 | 描述 |
|:--|:--|
| appUserId | 商家系统当前登录进行下单用户的UserID（必填。这个用户ID需保证在商家系统内部全局永久唯一，即每个用户对应一个唯一的UserID。一般情况下，直接把商家系统内部数据库中用户对应的唯一数字ID传参即可。如果商家系统内部的UserID包含用户的隐私信息，则可以通过增加一个用户ID字段或对原UserID进行加密转化处理即可。JRDD平台需要根据这个参数来实现虚拟用户的自动注册和会话登录。如果没有这个UserID或者该UserID在商家系统内并不全局唯一，那么JRDD平台就无法区分单个用户的合法登录状态，从而无法进行订单相关内容的权限控制，带来系统的安全和隐私风险。例如：如果商家的A用户和B用户下单时都传入了同一个appUserId，那么就会导致A用户可以访问到B用户的历史订单记录、B用户也可以对A用户的订单进行取消、退款等操作这样的现象，因为他们共享appUserId就等同于共享一个登录状态。这样的情形一旦发生，对于商家自己的系统是有极大风险的。因此，商家的技术团队在接入JRDD系统时，应严格保证每个用户的appUserId在商家系统内部的全局永久唯一性。<font color=#ff4500;>因商家技术团队接入API时未能严格按照要求传参而导致的任何问题，责任由商家自负，JRDD平台不承担任何责任</font>） |
| appOrderId | 由商家系统内部生成的订单ID(必填，且这个orderId也要在商家系统内保证全局永久唯一，否则会发生订单混乱问题) |
| orderAmount | 本次订单中下单的金额或币的数量（必填。注意，人民币等法币的金额也可以理解为是数量。如果商家的系统没有使用人民币，而是有自己自定义的币种，见orderCoinSymbol字段，则本参数是用户要充值或购买的该自定义币种的数量即可；如果商家的系统直接用人民币，则本参数直接填写用户要充值或支付的人民币金额即可） | 是 |
| orderCoinSymbol | 订单金额符号，支持：CNY/HKD/TWD/SGD/IDR/VND/MYR/USD。默认只支持CNY，其它的币种的支持需要找管理员开通。 |
| orderPayTypeId | 本次订单中用户主动选择使用的付款方式（必填。微信 = 1、支付宝 = 2、云闪付 = 3、工商银行 = 4 ...详见《JRDD平台支付方式对应ID列表》注：暂时不支持微信/支付宝/云闪付！)。必须是和 orderCoinSymbol 匹配的付款方式，比如 orderCoinSymbol 为 HKD 时，则只能选择 Hong Kong 的银行卡付款方式。要查询系统中支持哪些国家和地区和银行卡，可以参考 https://api.didipays.com/order/banks/ |
| orderRemark | 订单备注（非必填） |
| appServerNotifyUrl | 必填。由商家提供的服务器端负责接收订单信息更新异步通知的接口，订单信息更新异步通知消息由JRDD平台主动发起，其通知方式和参数列表详见本文档[Part 06：商家服务器接收订单信息更新异步通知](#part06) |
| appReturnPageUrl | 必填。在用户支付完成之后，JRDD会根据商家传入的appReturnPageUrl参数，通过GET请求的形式将部分支付结果参数回传到商户系统 |

* **接口返回**

```
不同于一般API的JSON格式返回结果，本API的调用方式并非是由服务器端发起调用，而是需要从用户的浏览器发起。商家服务器端应在签名完成后拼装一个HTML隐藏表单和一行JavaScript代码来实现用户浏览器向JRDD下单接口发起下单请求。JRDD下单接口收到下单请求后，会将浏览器重定向到一个订单页面，用户直接在此页面进行订单下一步操作即可。详细调用方法如下：
```


* **下单API调用流程**

```
1、商家的用户在浏览器打开商家的充值或提现操作后台的页面；
2、用户选择充值或提现，并填写充值或提现金额以及付款或收款方式及相关订单信息，点击“提交订单”；
3、用户的订单请求发到商家的服务器，服务器对用户的参数做合法性检验，检验合法后对这些参数进行签名操作，并获得签名结果；
4、商家服务器将经过合法性检查和签名的请求参数拼装成一个HTML form表单和一段包含JavaScript自动提交表单代码的HTML代码
（详见服务器端拼接HTML form表单和JavaScript代码的示例代码），并将这段代码返回给用户浏览器；
5、用户浏览器接收到商家服务器返回的HTML代码，解析执行后实现自动提交表单，浏览器跳转至JRDD的订单页面，开始进行订单流程。

```
详见[商家用户创建“充值/支付”订单接入流程演示图](https://www.processon.com/view/link/5d197784e4b014412aa8ea97 "商家用户创建“充值/支付”订单接入流程演示图")

### <span id="part03">Part 03：商家用户创建“提现/收款”订单接入流程</span>

* **Base URI**：https://api.didipays.com/order/withdraw/create

* **HTTP请求方法**： POST

* **基本参数（基本参数需拼接后放在URL中）**  

| 字段名 | 描述 |
|:--|:--|
| appId | 商家的唯一ID （必填）|
| apiKey | API Key, 在JRDD服务器后台设置）|
| inputCharset | 商家向API传入参数内容的字符编码，目的是为了防止字符乱码（非必填，默认为UTF-8。例如商家系统内部是GB2312编码，则需指定inputCharset=GB2312） |
| apiVersion | API版本号（必填，请以本文档开头部分的最新版本号为准，且默认值为最新版本号）|
| appSignType | 商家使用的签名算法名称(非必填，默认为 HMAC-SHA256 ，且目前仅支持 HMAC-SHA256) |
| appSignContent | 商家签名后的字符串。注意<font color=#ff4500>这个字段及其字段值都是不参与签名的</font>，只有完成签名后才需要附加到基本请求参数中。详细签名方法可见 [Part 09：签名/验签的方法](#part09) |

* **完整的API请求URL**： 
 
```
https://api.didipays.com/order/withdraw/create?appId=[由JRDD平台分配的appId]&apiKey=[由JRDD平台分配的apiKey]  
&inputCharset=UTF-8&apiVersion=1.1&appSignType=HMAC-SHA256&appSignContent=[签名内容]
```
* **POST请求参数列表（以下参数需通过 application/x-www-form-urlencoded 请求格式提交给上述完整请求URL）**

| 字段名 | 描述 |
|:--|:--|
| appUserId | 商家系统当前登录进行下单用户的UserID（必填。这个用户ID需保证在商家系统内部全局永久唯一，即每个用户对应一个唯一的UserID。一般情况下，直接把商家系统内部数据库中用户对应的唯一数字ID传参即可。如果商家系统内部的UserID包含用户的隐私信息，则可以通过增加一个用户ID字段或对原UserID进行加密转化处理即可。JRDD平台需要根据这个参数来实现虚拟用户的自动注册和会话登录。如果没有这个UserID或者该UserID在商家系统内并不全局唯一，那么JRDD平台就无法区分单个用户的合法登录状态，从而无法进行订单相关内容的权限控制，带来系统的安全和隐私风险。例如：如果商家的A用户和B用户下单时都传入了同一个appUserId，那么就会导致A用户可以访问到B用户的历史订单记录、B用户也可以对A用户的订单进行取消、退款等操作这样的现象，因为他们共享appUserId就等同于共享一个登录状态。这样的情形一旦发生，对于商家自己的系统是有极大风险的。因此，商家的技术团队在接入JRDD系统时，应严格保证每个用户的appUserId在商家系统内部的全局永久唯一性。<font color=#ff4500;>因商家技术团队接入API时未能严格按照要求传参而导致的任何问题，责任由商家自负，JRDD平台不承担任何责任</font>） |
| appOrderId | 由商家系统内部生成的订单ID(必填，且这个orderId也要在商家系统内保证全局永久唯一，否则会发生订单混乱问题) |
| orderAmount | 本次订单中下单的金额或币的数量（必填。注意，人民币等法币的金额也可以理解为是数量。如果商家的系统没有使用人民币，而是有自己自定义的币种，见orderCoinSymbol字段，则本参数是用户要充值或购买的该自定义币种的数量即可；如果商家的系统直接用人民币，则本参数直接填写用户要充值或支付的人民币金额即可） |
| orderCoinSymbol | 订单金额符号，支持：CNY/HKD/TWD/SGD/IDR/VND/MYR/USD。默认只支持CNY，其它的币种的支持需要找管理员开通。 |
| orderPayTypeId | 本次订单中用户主动选择使用的付款方式（必填。微信 = 1、支付宝 = 2、云闪付 = 3、工商银行 = 4 ...详见《JRDD平台支付方式对应ID列表》，注：暂时不支持微信/支付宝/云闪付！)。必须是和 orderCoinSymbol 匹配的付款方式，比如 orderCoinSymbol 为 HKD 时，则只能选择 Hong Kong 的银行卡付款方式。要查询系统中支持哪些国家和地区和银行卡，可以参考 https://api.didipays.com/order/banks/ |
| orderRemark | 订单备注（非必填） |
| payAccountId | 收款账户(必填。本字段可指定支付宝支付宝账户名或者银行卡卡号) |
| payQRUrl | 收款二维码（微信、支付宝或者云闪付）图片链接地址，仅当orderPayTypeId的值为1、2、3（即微信、支付宝或云闪付）的时候 |
| payAccountUser | 收款人的真实姓名(必填。微信、支付宝、云闪付和银行卡都必须填写收款账户对应的真实姓名，银行转账和支付宝等陌生人转账时都必须要填写真实姓名） |
| payAccountInfo | 收款账户信息，主要用于接收银行卡的支行信息(本字段根据不同的orderPayTypeId会接收不同参数，微信、支付宝和云闪付可以留空、银行卡则不能留空，必须填写分行或支行名称) |
| appServerNotifyUrl | 必填。由商家提供的服务器端负责接收订单信息更新异步通知的接口，订单信息更新异步通知消息由JRDD平台主动发起，其通知方式和参数列表详见本文档[Part 06：商家服务器接收订单信息更新异步通知](#part06) |
| appReturnPageUrl | 必填。在用户支付完成之后，JRDD会根据商家传入的appReturnPageUrl参数，通过GET请求的形式将部分支付结果参数回传到商户系统 |
| responseFormat | 指定请求返回类型，取值范围为 json 或 html。默认值为html，当指定responseFormat=json的时候，接口将不会返回html页面|

* **接口返回**

```
不同于一般API的JSON格式返回结果，本API的调用方式并非是由服务器端发起调用，而是需要从用户的浏览器发起。商家服务器端应在签名完成后拼装一个HTML隐藏表单和一行JavaScript代码来实现用户浏览器向JRDD下单接口发起下单请求。JRDD下单接口收到下单请求后，会将浏览器重定向到一个订单页面，用户直接在此页面进行订单下一步操作即可。详细调用方法如下：
注：responseFormat = json时，返回相应的订单信息为json格式；
```


* **下单API调用流程**

```
1、商家的用户在浏览器打开商家的充值或提现操作后台的页面；
2、用户选择充值或提现，并填写充值或提现金额以及付款或收款方式及相关订单信息，点击“提交订单”；
3、用户的订单请求发到商家的服务器，服务器对用户的参数做合法性检验，检验合法后对这些参数进行签名操作，并获得签名结果；
4、商家服务器将经过合法性检查和签名的请求参数拼装成一个HTML form表单和一段包含JavaScript自动提交表单代码的HTML代码
（详见服务器端拼接HTML form表单和JavaScript代码的示例代码），并将这段代码返回给用户浏览器；
5、用户浏览器接收到商家服务器返回的HTML代码（或JSON串），解析执行后实现自动提交表单，浏览器跳转至JRDD的订单页面，开始进行订单流程。
```

* **接口响应返回**

```
本接口的响应返回的内容格式依赖于responseFormat这个字段。  

如果responseFormat=json，则响应返回的内容是标准JSON格式的订单详情信息; 失败时会返回类似下面结构：{"code":"100017","msg":"工作时间单笔限额最小720元"}
如果responseFormat=html，则响应返回的内容是一个HTML页面。

以下会根据两种不同返回格式，对API调用方法以及返回内容进行分开解释说明。

```

* **【 responseFormat=json 】请求响应参数列表**


```
[提现/收款订单] 查询请求成功示例：
{
    "appId": "",//商家的唯一ID 
    "appUserId": "",//商家系统当前登录进行下单用户的UserID
    "appOrderId": "",//商家系统内部生成的订单ID
    "jrddOrderId": "",//JRDD系统内部生成的订单ID，与 appOrderId 绝对一一绑定
    "appCoinName": "",//商家系统使用的币种名称
    "appCoinSymbol": "",//商家系统使用的币种符号
    "appCoinRate": "",//商家系统使用的币种和人民币之间的汇率值
    "orderStatus":5, //详见 [Part 09：JRDD系统“提现/收款”订单状态表]  
    "statusReason": 1, //订单当前状态的原因描述，详见 [Part 09：JRDD系统“提现/收款”订单状态表] 
    "orderType": ,//0 = 充值/付款订单 1 = 为提现/收款订单
    "orderAmount": "",//本次订单中下单的金额（如果是商家自定义币种，则为该币种的数量）
    "orderPayTypeId": "",//本次订单中用户主动选择使用的收款方式
    "payAccountId": "",//收款账户
    "payQRUrl": "",//收款二维码（仅当orderPayTypeId的值为1或2）
    "payAccountUser": "",//收款人的真实姓名
    "payAccountInfo": "",//收款账户信息，主要用于接收银行卡的支行信息
    "orderRemark": ""//订单备注
}
```


### <span id="part04">Part 04：商家服务器端查询订单的API及返回响应参数</span>

* **Base URI**：https://api.didipays.com/order/query

* **HTTP请求方法**：GET

* **基本参数（基本参数需拼接后放在URL中）**  

| 字段名 | 描述 |
|:--|:--|
| appId | 商家的唯一ID （必填）|
| apiKey | API Key, 在JRDD服务器后台设置）|
| inputCharset | 商家向API传入参数内容的字符编码，目的是为了防止字符乱码（非必填，默认为UTF-8。例如商家系统内部是GB2312编码，则需指定inputCharset=GB2312） |
| apiVersion | API版本号（必填，请以本文档开头部分的最新版本号为准，且默认值为最新版本号）|
| appSignType | 商家使用的签名算法名称(非必填，默认为 HMAC-SHA256 ，且目前仅支持 HMAC-SHA256) |
| appSignContent | 商家签名后的字符串。注意<font color=#ff4500>这个字段及其字段值都是不参与签名的</font>，只有完成签名后才需要附加到基本请求参数中。详细签名方法可见 [Part 09：签名/验签的方法](#part09)  |

* **完整的API请求URL**： 
 
```
https://api.didipays.com/order/query?appId=[由JRDD平台分配的appId]&apiKey=[由JRDD平台分配的apiKey]  
&inputCharset=UTF-8&apiVersion=1.1&appSignType=HMAC-SHA256&appSignContent=[签名内容]
```

* **GET请求参数列表**

| 字段名 | 描述 |
|:--|:--|
| appUserId | 商家系统当前登录进行下单用户的UserID（必填。这个用户ID需保证在商家系统内部全局永久唯一，即每个用户对应一个唯一的UserID。一般情况下，直接把商家系统内部数据库中用户对应的唯一数字ID传参即可。如果商家系统内部的UserID包含用户的隐私信息，则可以通过增加一个用户ID字段或对原UserID进行加密转化处理即可。JRDD平台需要根据这个参数来实现虚拟用户的自动注册和会话登录。如果没有这个UserID或者该UserID在商家系统内并不全局唯一，那么JRDD平台就无法区分单个用户的合法登录状态，从而无法进行订单相关内容的权限控制，带来系统的安全和隐私风险。例如：如果商家的A用户和B用户下单时都传入了同一个appUserId，那么就会导致A用户可以访问到B用户的历史订单记录、B用户也可以对A用户的订单进行取消、退款等操作这样的现象，因为他们共享appUserId就等同于共享一个登录状态。这样的情形一旦发生，对于商家自己的系统是有极大风险的。因此，商家的技术团队在接入JRDD系统时，应严格保证每个用户的appUserId在商家系统内部的全局永久唯一性。<font color=#ff4500;>因商家技术团队接入API时未能严格按照要求传参而导致的任何问题，责任由商家自负，JRDD平台不承担任何责任</font>） |
| appOrderId | 由商家系统内部生成的订单ID(必填，且这个orderId也要在商家系统内保证全局永久唯一，否则会发生订单混乱问题) |
| responseFormat | 指定请求返回类型，取值范围为 json 或 html。默认值为json，即默认返回为标准的JSON格式。当指定responseFormat=html的时候，接口将不会返回JSON，而是直接打开页面。当商家服务器端希望通过调用此订单API来获取订单状态的时候，使用JSON格式即可；当商家希望把用户引导进入之前意外中断的订单页面的时候，请按照既定流程从用户浏览器端向本接口发起请求（详见下面的订单查询API调用流程和代码示例）|


* **接口响应返回**

```
本接口的响应返回的内容格式依赖于responseFormat这个字段。  

如果responseFormat=json，则响应返回的内容是标准JSON格式的订单详情信息;  
如果responseFormat=html，则响应返回的内容是一个HTML页面。

以下会根据两种不同返回格式，对API调用方法以及返回内容进行分开解释说明。

```

* **【 responseFormat=json 】请求响应参数列表**

[充值/付款订单]返回订单状态对应说明列表详见 [Part 07：JRDD系统“充值/支付”订单状态表](#part07)  
[提现/收款订单]返回订单状态对应说明列表详见 [Part 08：JRDD系统“提现/收款”订单状态表](#part08)

```
[充值/付款订单] 查询请求成功示例：
{
    "appId": "",//商家的唯一ID 
    "appUserId": "",//商家系统当前登录进行下单用户的UserID
    "appOrderId": "",//商家系统内部生成的订单ID
    "jrddOrderId": "",//JRDD系统内部生成的订单ID，与 appOrderId 绝对一一绑定
    "appCoinName": "",//商家系统使用的币种名称
    "appCoinSymbol": "",//商家系统使用的币种符号
    "appCoinRate": "",//商家系统使用的币种和人民币之间的汇率值
    "orderStatus":5,//详见 [Part 08：JRDD系统“充值/支付”订单状态表]  
    "statusReason": 1, //订单当前状态的原因描述，详见 [Part 08：JRDD系统“充值/支付”订单状态表]  
    "orderType": ,//0 = 充值/付款订单 1 = 提现/收款订单
    "orderAmount": "",//本次订单中下单的金额（如果是商家自定义币种，则为该币种的数量）
    "orderPayTypeId": "",//本次订单中用户主动选择使用的付款方式
    "orderRemark": ""//订单备注
}


[提现/收款订单] 查询请求成功示例：
{
    "appId": "",//商家的唯一ID 
    "appUserId": "",//商家系统当前登录进行下单用户的UserID
    "appOrderId": "",//商家系统内部生成的订单ID
    "jrddOrderId": "",//JRDD系统内部生成的订单ID，与 appOrderId 绝对一一绑定
    "appCoinName": "",//商家系统使用的币种名称
    "appCoinSymbol": "",//商家系统使用的币种符号
    "appCoinRate": "",//商家系统使用的币种和人民币之间的汇率值
    "orderStatus":5, //详见 [Part 09：JRDD系统“提现/收款”订单状态表]  
    "statusReason": 1, //订单当前状态的原因描述，详见 [Part 09：JRDD系统“提现/收款”订单状态表] 
    "orderType": ,//0 = 充值/付款订单 1 = 为提现/收款订单
    "orderAmount": "",//本次订单中下单的金额（如果是商家自定义币种，则为该币种的数量）
    "orderPayTypeId": "",//本次订单中用户主动选择使用的收款方式
    "payAccountId": "",//收款账户
    "payQRUrl": "",//收款二维码（仅当orderPayTypeId的值为1或2）
    "payAccountUser": "",//收款人的真实姓名
    "payAccountInfo": "",//收款账户信息，主要用于接收银行卡的支行信息
    "orderRemark": ""//订单备注
}


查询请求失败示例：

{
    "status": "fail",
    "err_msg": "create order failed",
    "err_code": 20504,
    "data": null
}
```

### <span id="part05">Part 05：商家用户打开订单页面的接入流程</span>

* **【 responseFormat=html 】用户查看订单详情（或订单意外中断后重新进入订单）流程**

当responseFormat=html时，本API的调用方式并非是由服务器端发起调用，而是需要从用户的浏览器发起。商家服务器端应在签名完成后拼装一个HTML隐藏表单和一行JavaScript代码来实现用户浏览器向JRDD查看订单接口发起查看订单请求。JRDD接口收到查看订单请求后，会直接打开一个订单页面。详细调用方法如下：

```
1、商家的用户在浏览器打开商家的历史订单列表页面；
2、用户选择某个订单并点击“查看订单详情”；
3、用户的订单查看请求由浏览器发到商家的服务器，商家服务器对用户的参数做合法性检验，检验合法后对这些参数进行签名操作，并获得签名结果；
4、商家服务器将经过合法性检查和签名的请求参数拼装成一个HTML form隐藏表单和一段包含JavaScript自动提交表单代码的HTML代码
（详见服务器端拼接HTML form隐藏表单和JavaScript代码的示例代码），并将这段代码返回给用户浏览器；
5、用户浏览器接收到商家服务器返回的HTML代码，解析执行后实现自动提交表单，浏览器跳转至JRDD的订单页面。

```
详见[用户查看订单详情（或订单意外中断后重新进入订单）流程演示图](https://www.processon.com/view/link/5d197887e4b0a916e8f94cb1 "用户查看订单详情（或订单意外中断后重新进入订单）流程演示图")

### <span id="part06">Part 06：商家服务器端接收订单信息更新异步通知</span>
* [appServerNotifyUrl]   
* 说明

```
这是由商家在用户下单时传递给JRDD系统的负责接收订单状态变更通知的API，由JRDD系统主动通过POST方法调用传参实现异步通知。JRDD系统内部对于调用此接口失败的情况设有自动和手工重试机制，以保障订单状态通知的到达和准确性。

这个由商家提供的接口，与JRDD提供的订单查询API，可以理解为是两种不同的保障订单状态同步的机制。一个是商家被动接收订单状态变更通知，一个是商家可以主动查询获取订单状态变更通知。相比之下，前者由JRDD系统发起，通知更加及时，避免轮询，可有效保障用户体验。后者需要轮询，会一定程度浪费系统资源，且及时性并不好。

因此，当商家希望获得订单状态变更的同步通知时，应尽量以appServerNotifyUrl被调用的结果为准。

商家负责接入JRDD系统的技术团队应保证接收到订单信息更新的异步通知后的后续处理流程的完整性。即，JRDD的服务器会将商家用户订单状态变更的实时状态同步给商家服务器端，商家服务器端应严格按照接收到的通知内容处理后续业务逻辑。如果对某些接收到的通知未做处理，则会出现流程处理不完整的现象，从而进一步导致用户数据错乱甚至是用户投诉。

例如，用户的充值订单因操作超时被取消，JRDD系统会将此状态通知到商家服务器，商家服务器收到这个通知后，可以通过发送手机短信或站内消息的形式将此通知内容转达给商家用户。如果商家服务器收到JRDD系统的通知但却没有通知到用户，用户则会对订单状态的变更不知情。

```

* **Base URI**：[商家下单时传入的字段 appServerNotifyUrl ] 

* **方法**： POST

* **基本参数（基本参数需拼接后放在URL中）**  

| 字段名 | 描述 |
|:--|:--|
| appId | 商家的唯一ID （必填）|
| apiKey | API Key, 在JRDD服务器后台设置）|
| jrddInputCharset | JRDD系统向商家的 `appServerNotifyUrl` 传入参数内容的字符编码，目的是为了防止字符乱码（默认为UTF-8。如果商家系统内部是非UTF-8编码，则需指定将参数内容的编码由UTF-8转为商家自己系统的编码） |
| jrddSignType | JRDD系统使用的签名算法名称(默认为 HMAC-SHA256 ) |
| jrddSignContent | 经过JRDD系统签名后的字符串，供商家内部验证签名用。<font color=#ff4500>这个字段及其字段值都是不参与签名的</font>，只有完成签名后才附加到基本请求参数中。详细签名方法可见 [Part 09：签名/验签的方法](#part09)  |

* **以上基本参数拼装后完整的请求URL示例**： 
 
```
appServerNotifyUrl?appId=[由JRDD平台分配给商家的appId]&apiKey=[由JRDD平台分配给商家的apiKey]  
&jrddInputCharset=UTF-8&jrddSignType=HMAC-SHA256&jrddSignContent=[经过JRDD系统签名后的字符串]
```
* **POST请求参数列表（以下参数将通过 application/x-www-form-urlencoded 请求格式提交给上述完整的请求回调URL）**

| 字段名 | 描述 |
|:--|:--|
| jrddNotifyId |（必填）通知消息的唯一ID，用以避免重复通知。商家收到通知并成功处理订单后续逻辑后，可在数据库使用这个ID来标记该条通知的状态为“已接收”，下次再接到相同ID的通知就可以做忽略处理。如果通知接收失败（例如商家服务器故障），JRDD系统则会将这条通知放入重试队列进行自动重新通知。直到商家对该条通知返回Success的时候，该条通知才会在JRDD系统内标被记为“已成功发送”并停止进行推送通知。 |
| jrddNotifyTime |（必填）发送通知请求的时间 |
| jrddOrderId |（必填）通知消息的唯一ID，用以避免重复通知。商家收到通知并成功处理 |
| appOrderId |由商家系统内部生成的订单ID(必填，且这个appOrderId也要在商家系统内保证全局永久唯一，否则会发生订单混乱问题) |
| orderType | 订单类型，取值范围为0和1。orderType为0代表是充值/付款订单，为1代表是提现/收款订单 |
| orderAmount |（必填）订单的金额（如果商家是自定义币种，则为商家的币种数量） |
| orderCoinSymbol | 订单金额符号，支持：CNY/HKD/TWD/SGD/IDR/VND/MYR/USD。默认只支持CNY，其它的币种的支持需要找管理员开通。 |
| orderStatus | 订单状态 |
| statusReason | 订单状态具体原因 |
| orderRemark | 下单时填写的订单备注 |
| orderPayTypeId | 本次订单中使用的付款或收款方式 |
| payAccountId | 收款账户,仅针对提现单 |
| payQRUrl | 收款二维码 |
| payAccountUser | 收款人的真实姓名,仅针对提现单 |
| payAccountInfo | 收款账户信息，主要用于接收银行卡的支行信息,仅针对提现单 |

* **返回内容约定**

商家服务器端接收到消息并进行正确解析和处理后，应该返回 SUCCESS 这7个字符，任何其它额外返回（多一个空格都不行）都会被认为是消息通知失败。如果商家服务器反馈给JRDD的字符不是 SUCCESS 这7个字符，JRDD服务器会不断重发通知，直到超过24小时24分钟。一般情况下，25小时以内完成8次通知（通知的间隔频率一般是：4分钟、10分钟、10分钟、1小时、2小时、6小时、15小时）

```
SUCCESS
```

* **异步通知触发机制**

异步通知触发机制，即JRDD平台在哪些情形下会向商家服务器发送异步通知。详细信息可分别参考订单状态表中“是否会向商家服务器发送异步通知”以及“商家收到异步通知时是否需要通知到用户”的相关说明:

### <span id="part07">Part 07：JRDD系统“充值/支付”订单状态表</span>

| 订单状态编号（orderStatus） | 状态原因编号（statusReason） | 状态文案 | 订单状态详细描述 | 原因文案 | 原因详细描述 | 是否会向商家服务器发送异步通知 | 商家收到异步通知时是否需要通知到用户 | 通知给用户的文案（参考） | 
|:--|:--|:--|:--|:--|:--|:--|:--|:--|
| 1 | - | 已创建 | JRDD系统内部的订单已经生成（jrddOrderId在数据库内已经创建） | - | - | 是 | 否 | | 
| 7 | - | 已完成 | 订单顺利完成最终结算，钱币两清互相到账且无任何冻结 | - | - | 是 | 是 | 您的订单[订单号]已处理完成，请及时查看您的账户余额 | 
| 5 | 19 | 已完成 | 订单最终由客服人工处理为交易完成 | - |  | 是 | 是 | 您的订单[订单号]已处理完成，请及时查看您的账户余额 | 
| 8 | - | 已取消 | 订单被系统最终取消，且钱币两清原路退回无任何冻结 | 系统自动取消订单 | 用户始终没有标记“我已完成付款”直到付款超出系统设定的操作时间，系统开始自动取消订单的倒计时，直到最终倒计时结束订单被自动取消，承兑商的币同时被解冻 | 是 | 是 | 您的订单[订单号]已被取消，如有疑问，请联系客服 | 
| 5 | 20 | 已取消 | 订单最终由客服人工处理为交易取消 | - |  | 是 | 是 | 您的订单[订单号]已被取消，如有疑问，请联系客服 | 

### <span id="part08">Part 08：JRDD系统“提现/收款”订单状态表</span>
| 订单状态编号（orderStatus） | 状态原因编号（statusReason） | 订单状态描述 | 状态文案 | 原因文案 | 原因详细描述 | 是否会向商家服务器发送异步通知 | 商家收到异步通知时是否需要通知到用户 | 通知给用户的文案 | 
|:--|:--|:--|:--|:--|:--|:--|:--|:--|
| 1 | - | 已创建 | JRDD系统内部的订单已经生成（jrddOrderId在数据库内已经创建） | - | - | 是 | 否 | | 
| 7| -  | 已完成 | 订单顺利完成最终结算，钱币两清互相到账且无任何冻结 | - | - | 是 | 是 | 您的订单[订单号]已处理完成，请及时查看您的账户余额 | 
| 5 | 19 | 已完成 | 订单最终由客服人工处理为交易完成 | - |  | 是 | 是 | 您的订单[订单号]已处理完成，请及时查看您的账户余额 | 
| 5 | 20 | 已取消 | 订单最终由客服人工处理为交易取消 | - |  | 是 | 是 | 您的订单[订单号]已被取消，如有疑问，请联系客服 | 

### <span id="part09">Part 09：签名/验签的方法</span>
* [在线验签调试工具](http://18.162.146.233:8084/demo/sign)

* **签名方法（流程与规则）<font color=#ff4500>主要用于商家发起API请求场景</font>**

* 第一步 : 把所有API请求需要发送的参数（包括通过GET放在URL中发送的基本参数和通过POST发送的form data参数，但<font color=#ff4500>不包括`appSignContent`</font>）的参数名和参数值按照如下规则拼接成“待签名字符串”：
	* 1、对当前API请求所有的参数（包括所有基本参数和请求参数，<font color=#ff4500>包括参数值为空的，不包括`appSignContent`</font>）的“参数值”进行url_encode；
	* 2、对第1步url_encode后的参数，按“参数名”升序排序，排序的规则如下：
		* 按照所有参数名的第一个字符的键值ASCII码递增排序（即字母升序排序）；
		* 如果遇到多个参数名的第一个字符是相同字符的情况，则按照参数名的第二个字符的键值ASCII码递增排序，以此类推。    
	* 3、把第2步排序后的参数，拼装成`key1=value1&key2=value2&key3=value3`的字符串
	*<font color=#ff4500>注意请严格按照以上3步的顺序进行操作拼接，顺序颠倒会导致签名错误</font>

* 第二步 : 将第一步拼装得到的“待签名字符串”，结合`secretKey`，用指定的签名方法（目前仅支持 HMAC-SHA256 ）进行加密，获得加密后的字符串；
* 第三步 : 将第二步生成的加密后字符串作为`appSignContent`的值附加到完整的API请求URL，作为API请求目标地址使用。

* **签名示例（下单）**

本示例中，我们以典型的`充值/支付下单接口`请求的为例，演示如何对充值/支付订单的请求进行签名，以及使用签名发起充值/支付下单请求。
	
* 假定以下场景和参数：

	* 1、商家在JRDD平台的唯一ID `appId=101`
	* 2、商家的加密公钥 `apiKey=sf0897fads97fd89a0a7f`
	* 3、商家的加密私钥（不可以泄露，也不需要传参） `secretKey=141j23kjtjk13j1hg512f1uo35`
	* 4、签名方法 `appSignType=HMAC-SHA256`
	* 5、API版本号最新为 `apiVersion=1.1`
	* 6、商家内部系统的字符编码为 `inputCharset=UTF-8`
	* 7、商家系统当前登录准备下单进行充值/付款的用户为 `appUserId=423112`
	* 8、商家系统内部为用户 appUserId=423112 生成了一个全局永久唯一的内部订单号 `appOrderId=No1014231121232243`
	* 9、商家用户 appUserId=423112 选择充值/付款的金额是 `orderAmount=10` （即充值/付款10元）
	* 10、商家系统支持的货币为 `orderCoinSymbol=CNY`
	* 11、用户选择了微信支付方式 `orderPayTypeId=1`
	* 12、本单的订单备注为 `orderRemark=This is an order`
	* 13、商家用于接收JRDD平台异步通知的API地址为 `appServerNotifyUrl=https://api.mydomain.com/notify`
	* 14、商家设定的用户下单完成后的跳转页面地址为 `appReturnPageUrl=https://www.mydomain.com/return`

* 第一步：拼接“待签名字符串”

	* 对充值/付款API所有的参数（包括所有基本参数和请求参数，不包括`appSignContent`）的参数值进行url_encode，然后按参数名第一个字符的键值ASCII码递增排序规则进行排序，再按顺序拼接成如下结果：  

```  
apiKey=sf0897fads97fd89a0a7f&apiVersion=1.1&appId=101&appOrderId=No1014231121232243&appReturnPageUrl  
=https%3A%2F%2Fwww.mydomain.com%2Freturn&appServerNotifyUrl=https%3A%2F%2Fapi.mydomain.com%2Fnotify  
&appSignType=HMAC-SHA256&appUserId=423112&inputCharset=UTF-8&orderAmount=10&orderCoinSymbol=CNY  
&orderPayTypeId=1&orderRemark=This+is+an+order  
```

* 第二步 ：结合secretKey`141j23kjtjk13j1hg512f1uo35`对"待签名字符串"进行HMAC-SHA256加密，得到加密后的字符串

```  
12d6c6ea63429a0a71435b42aaca659d54715b8e62a02b3c7a475f2c7a577138  
```

* 第三步 ：将第二步生成的加密后字符串作为`appSignContent`的值附加到完整的请求URL中

```
https://api.didipays.com/order/deposit/create?apiKey=sf0897fads97fd89a0a7f&apiVersion=1.1&appId=101&appSignType=HMAC-SHA256&inputCharset=UTF-8  
&appSignContent=12d6c6ea63429a0a71435b42aaca659d54715b8e62a02b3c7a475f2c7a577138
```

* 第四步 ：使用签名发起充值/支付下单请求进行下单

	* API请求方法：`POST`
	* API请求需要通过POST方法传的参数：
		* appUserId=423112  
		* appOrderId=No1014231121232243
		* orderAmount=10
		* orderCoinSymbol=CNY
		* orderRemark=This+is+an+order
		* appServerNotifyUrl=https%3A%2F%2Fapi.mydomain.com%2Fnotify
		* appReturnPageUrl=https%3A%2F%2Fwww.mydomain.com%2Freturn
	* API请求地址：  
```
https://api.didipays.com/order/deposit/create?apiKey=sf0897fads97fd89a0a7f&apiVersion=1.1&appId=101&appSignType=HMAC-SHA256&inputCharset=UTF-8&appSignContent=12d6c6ea63429a0a71435b42aaca659d54715b8e62a02b3c7a475f2c7a577138
```

* **验签方法（流程与规则）<font color=#ff4500>主要用于商家接收回调时验签的场景</font>**

* 第一步 : 把所有收到的HTTP请求参数（包括通过GET从URL中获取的基本参数和通过POST从form data中获取到的请求参数，但<font color=#ff4500>不包括`jrddSignContent`</font>）的参数名和参数值按照如下规则拼接成“待验签字符串”：
	* 1、对所有收到的HTTP请求参数（包括通过GET从URL中获取的基本参数和通过POST从form data中获取到的请求参数，但<font color=#ff4500>不包括`jrddSignContent`</font>）的“参数值”进行url_encode；
	* 2、对第1步ur_encode后的参数，按“参数名”升序排序，排序的规则如下：
		* 按照所有参数名的第一个字符的键值ASCII码递增排序（即字母升序排序）；
		* 如果遇到多个参数名的第一个字符是相同字符的情况，则按照参数名的第二个字符的键值ASCII码递增排序，以此类推。    
	* 3、把第2步排序后的参数按顺序用`&`符号连接拼装成`key1=value1&key2=value2&key3=value3`格式的字符串  
	
		**注意：请严格按照以上3步的顺序进行操作拼接，顺序颠倒会导致验签失败**

* 第二步 : 将第一步拼装得到的“待验签字符串”，结合`secretKey`，用指定的签名方法（目前仅支持 HMAC-SHA256 ）进行加密，获得加密后的字符串；
* 第三步 : 将第二步生成的加密后字符串的内容值，跟通过HTTP GET获取到的`jrddSignContent`参数的值进行比对。如果二者的值相等，则为验签通过。如果二者的值不相等，则为验签失败。

`如果发生验签失败的情况，请仔细检查拼接“待验签字符串”的流程是否都严格按要求执行，并检查私钥是否正确`


### <span id="part10">Part 10：JRDD平台支付方式对应ID列表</span>
| 支付方式 | 对应ID |
|:--|:--|
| USDT(ERC20) | 10001 |
| 中国工商银行 | 4 | 
| 中国农业银行 | 5 | 
| 中国银行 | 6 | 
| 中国建设银行 | 7 | 
| 交通银行 | 8 | 
| 中国邮政储蓄银行 | 9 | 
| 中国光大银行 | 10 |
| 中国民生银行 | 11 |
| 招商银行 | 12 |
| 中信银行 | 13 |
| 华夏银行 | 14 |
| 上海浦东发展银行 | 15 |
| 平安银行 | 16 |
| 广发银行 | 17 |
| 兴业银行 | 18 |
| 浙商银行 | 19 |
| 渤海银行 | 20 |
| 恒丰银行 | 21 |
| 汇丰银行(中国) | 22 |
| 恒生银行(中国) | 23 |
| 农村商业银行 | 24 |
| 宁波银行 | 25 |

- 中国大陆银行列表请参考： https://api.didipays.com/order/banks?nationCode=86  
- 越南银行列表请参考： https://api.didipays.com/order/banks?nationCode=84
- 马来西亚银行列表请参考： https://api.didipays.com/order/banks?nationCode=60
- 香港银行列表请参考： https://api.didipays.com/order/banks?nationCode=852
- 美国银行列表请参考： https://api.didipays.com/order/banks?nationCode=1
- 新加坡银行列表请参考： https://api.didipays.com/order/banks?nationCode=65
- 印度尼西亚银行列表请参考： https://api.didipays.com/order/banks?nationCode=62

详见：[JRDD平台支付方式对应ID列表](https://api.didipays.com/order/banks/)
