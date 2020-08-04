<?php
/* *********************************
 * ******* 基础配置信息 ************
 * *********************************
 */
// JRDiDi系统的下单API⽹关地址【测试环境】
$sGatewayURL = 'http://13.250.12.109:8084';
// JRDiDi系统的下单API⽹关地址【正式环境】
//$sGatewayURL = 'https://jrdidi.com';

$sApiVersion = '1.1';

$sAppId = 'xxxxxxxxxxxxxxx';
// 公钥
$sApiKey = 'xxxxxxxxxxxx';//注意替换成（由JRDiDi平台分配的）自己的公钥
// 加密私钥
$sSecretKey = 'xxxxxxxxxxxxxxxx';//注意替换成（由JRDiDi平台分配的）自己的加密私钥

$sAppSignType = 'HMAC-SHA256';
$sAppServerNotifyUrl = 'http://13.250.12.109:8084/notify.php';//这里改成自己的接收异步回调通知的API地址，订单状态发生关键变更的时候，JRDiDi系统会第一时间调用这个API发送通知
$sAppReturnPageUrl = 'http://13.250.12.109:8084/return.php';//这里改成支付成功后跳转的页面，这个页面可以用来接收订单同步回调通知参数

function getToBeSignedStringV1($aBasicParams, $aBodyParams, $sHTTPMethod, $sURI, $bIsNotify = false) {
	//所有参数合集（不包括 appSignContent 字段））
	ksort($aBasicParams);
	if (!$bIsNotify) {
		ksort($aBodyParams);//1.0的签名，接收参数和回调传参的方式不同，一个是form data格式，一个是http body，这导致签名的方法不一致，接收时要求json排序，但回调时又不要求，给API使用方造成很大困扰。这个问题在1.1中已经修复，此处是为了兼容旧版
	}
	//保证拼接json时所有的值都是字符串类型（因为jrdidi服务器在验签的之前通过HTTP接收到的参数值都是字符串）
	array_walk($aBodyParams, function(&$value){
		$value = (string)$value;
	});
	$sToBeSigned = $sHTTPMethod . $sURI . '?' . http_build_query($aBasicParams) . json_encode($aBodyParams, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);

	return $sToBeSigned;
}

function getToBeSignedStringV2($aBasicParams, $aRequestParams = array()) {
	//所有参数合集（不包括 appSignContent 字段））
	$aAllParams = array_merge($aBasicParams, $aRequestParams);
	ksort($aAllParams);
	$sToBeSigned = http_build_query($aAllParams);

	return $sToBeSigned;
}

function buildAutoSubmitForm($sRequestURL, $sHTTPMethod, $aFormParams) {
	// action 是JRDiDi系统的下单请求API地址
	// method='POST' 使用POST方法提交
	// method='GET' 使⽤GET方法提交
	// target='_blank' 在用户浏览器中新开标签页打开下单页面
	$sHtml = "<form id='jrdidi_submit' name='jrdidi_submit' action='". $sRequestURL ."' method='" . $sHTTPMethod. "'>";
	// 将所有参数都拼装成form表单待提交的参数
	foreach ($aFormParams as $key => $value) {
		$value = str_replace("'", "&apos;", $value);
		$sHtml.= "<input type='hidden' name='".$key."' value='".$value."'/>";
	}
	//submit按钮控件请不要含有name属性
	$sHtml = $sHtml."<input type='submit' value='ok' style='display:none;''></form>";
	// ⽤用JavaScript自动提交form表单
	$sHtml = $sHtml."<script>document.forms['jrdidi_submit'].submit();</script>";

	return $sHtml;
}
