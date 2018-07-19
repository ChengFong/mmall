************************************************************

此專案只有電商"後台的API"

網站框架主要使用Maven + Spring + SpringMVC + MyBatis 所構建而成的

(1) 使用Redis做session的儲存與驗證Token的儲存
<br />
(2) 使用Maven配置環境隔離(dev、prod)

根據不同環境打包指令 : mvn clean package -Dmaven.test.skip=true -Pdev

************************************************************

測試圖片上傳網站 :
<br />
http://selltest.nctu.me
<br />
API網站(詳細的API使用，請參考GitHub的wiki文檔) :
<br />
http://selltest.nctu.me/ + API

帳號 : admin
密碼 : admin

************************************************************

參考API網址

http://git.oschina.net/imooccode/happymmallwiki/wikis/home

************************************************************