<?php
require_once('config.php');

/* *********************************
 * ********** API请求参数 **********
 * *********************************
 */

$sAppUserId = 10006; //商户当前登录用户的唯一ID
$sAppOrderId = time(); //商户为当前登录用户生成的一个唯一订单号

// 由基本请求参数组成的PHP数组
$aBasicParams = array(
	'inputCharset' => 'UTF-8',//这里改成自己系统的字符编码
	'apiVersion' => $sApiVersion,
	'appId' => $sAppId,
	'apiKey' => $sApiKey,
	'appSignType' => $sAppSignType,
	//'appSignContent' => '', //注意，appSignContent字段本身不参与签名
);

$aAPIConfig = array(
	'deposit' => array(
		'uri' => '/order/deposit/create',
		'method' => 'POST',
		'request_params' => array(
			'appUserId' => $sAppUserId,
			'appOrderId' => $sAppOrderId,
			'orderAmount' => 10,
			'orderCoinSymbol' => 'CNY',
			'orderPayTypeId' => 1,
			'orderRemark' => '这里是订单备注',
			'appServerNotifyUrl' => $sAppServerNotifyUrl,
			'appReturnPageUrl' => $sAppReturnPageUrl
		)
	),
	'withdraw' => array(
		'uri' => '/order/withdraw/create',
		'method' => 'POST',
		'request_params' => array(
			'appUserId' => $sAppUserId,
			'appOrderId' => $sAppOrderId,
			'orderAmount' => 1,
			'orderCoinSymbol' => 'CNY',
			'orderPayTypeId' => 1,
			'orderRemark' => '这里是订单备注',
			'payAccountId' => '6213560000000000023232',
			'payQRUrl'   =>  'https://xxx.com/qrcode.png',
			'payAccountUser' => '真实姓名',
			'payAccountInfo' => '分行支行名称',
			'appServerNotifyUrl' => $sAppServerNotifyUrl,
			'appReturnPageUrl' => $sAppReturnPageUrl
		)
	),
	'query' => array(
		'uri' => '/order/query',
		'method' => 'GET',
		'request_params' => array(
			'appUserId' => $sAppUserId,
			'appOrderId' => $sAppOrderId,
			'responseFormat' => 'html'
		)
	),
);

$sAPI = 'deposit';
//$sAPI = 'withdraw';
//$sAPI = 'query';

/* *********************************
 * ********** API请求签名 **********
 * *********************************
 */

$sURI = $aAPIConfig[$sAPI]['uri'];
$sHTTPMethod = $aAPIConfig[$sAPI]['method'];
//由所有经过商家校验有效性的订单请求参数组成的PHP数组 
$aRequestParams = $aAPIConfig[$sAPI]['request_params'];

$sToBeSigned = '';
if ($sApiVersion == '1.0') {
	$sToBeSigned = getToBeSignedStringV1($aBasicParams, $aRequestParams, $sHTTPMethod, $sURI);
	echo "待签名字符串（v1.0）：\n".$sToBeSigned."\n";
} else {
	$sToBeSigned = getToBeSignedStringV2($aBasicParams, $aRequestParams);
	echo "待签名字符串（v1.1）：\n".$sToBeSigned."\n";
}

//使用私钥进行签名，得到签名后内容
$sAppSignContent = hash_hmac('SHA256', $sToBeSigned, $sSecretKey);
//echo "签名后字符串：\n".$sAppSignContent."\n";

/* *********************************
 * ********** 发起API请求 **********
 * *********************************
 */

//拼接出下单的完整URL地址，以及需要放在隐藏表单用来提交的参数
//请求方法不同，拼装的方法也不同
if ($sHTTPMethod == 'GET') {
	$sRequestURL = $sGatewayURL . $sURI;
	$aFormParams = array_merge($aBasicParams, $aRequestParams, array('appSignContent' => $sAppSignContent));
} else {
	$sRequestURL = $sGatewayURL . $sURI . '?' . http_build_query($aBasicParams) . '&appSignContent=' . $sAppSignContent;
	$aFormParams = $aRequestParams;
}

/*
	开始下单，拼装一个可以自动提交的form表单，返回给用户浏览器，用户浏览器执行后会实现自动重定向到下单页面
 */
$sHtml = buildAutoSubmitForm($sRequestURL, $sHTTPMethod, $aFormParams);
//file_put_contents('test.html', $sHtml);

echo $sHtml; //将拼装完成的HTML代码返回给用户浏览器。⽤户浏览器执行HTML代码会自动向JRDiDi系统接⼝提交订单

