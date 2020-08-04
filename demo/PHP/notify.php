<?php
require_once('config.php');

$sLogFilePath = '/tmp/log/jrdidi.com_notify_' . date("Y-m-d") . '.log';

$aBasicParams = $_GET;

if (!isset($_GET['jrddSignContent'])) {
	die('ILLEGAL REQUEST');
}

/* *********************************
 * ********** 通知请求验签 *********
 * *********************************
 */

//jrddSignContent 字段不参与签名
unset($aBasicParams['jrddSignContent']);
$sToBeSigned = getToBeSignedStringV2($aBasicParams, $_POST);

//使用私钥进行签名，得到签名后内容
$sSignContent = hash_hmac('SHA256', $sToBeSigned, $sSecretKey);
//echo "签名后字符串：\n".$sSignContent."\n";

if ($sSignContent !== $_GET['jrddSignContent']) {
	die('BAD SIGNATURE');
}

/* *********************************
 * ********** 处理回调结果 *********
 * *********************************
 */

$sLogContent = date('H:i:s') . " - 收到来自jrdidi.com的回调请求：\n";

$sLogContent .= "商户appId：" . $_GET['appId'] . "\n";
$sLogContent .= "商户apiKey：" . $_GET['apiKey'] . "\n";
$sLogContent .= "JRDD的签名：" . $_GET['jrddSignContent'] . "\n";

$sLogContent .= "商户订单号：" . $_POST['appOrderId'] . "\n";
$sLogContent .= "JRDD订单号：" . $_POST['jrddOrderId'] . "\n";
$sLogContent .= "订单状态编号：" . $_POST['orderStatus'] . "\n";
$sLogContent .= "原因编号：" . $_POST['statusReason'] . "\n";

$sLogContent .= "收到HTTP GET：\n\n";
$sLogContent .= var_export($_GET, true) . "\n\n";
$sLogContent .= "收到HTTP POST：\n\n";
$sLogContent .= var_export($_POST, true) . "\n\n";

//记录回调日志
file_put_contents($sLogFilePath, $sLogContent, FILE_APPEND | LOCK_EX);

switch ($_POST['orderStatus']) {
	case '1':
		//订单创建成功
		break;
	case '7':
		//订单完成
		break;
	case '8':
		//订单取消（超时自动取消）
		break;
	case '5':
		if ($_POST['statusReason'] == 19) {
			//5 19为订单完成（最终由客服人工干预后完成交易）
		} else if ($_POST['statusReason'] == 20) {
			//5 20为订单取消（最终由客服人工干预后取消交易）
		}
		break;
}

echo 'SUCCESS';
