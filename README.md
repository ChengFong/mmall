************************************************************

電商後台的API

網站框架主要使用Maven + Spring + SpringMVC + MyBatis 所構建而成的

(1) 使用Redis做登入Cookie的儲存與驗證Token的儲存
(2) 使用nginx當作圖片路徑
(3) 使用Maven配置環境隔離(dev、prod)

根據不同環境打包指令 : mvn clean package -Dmaven.test.skip=true -Pdev

************************************************************

測試網站 :

http://selltest.nctu.me，此頁面可以測試圖片上傳
http://selltest.nctu.me/ + API

詳細API使用，請參考GitHub的wiki文檔

帳號 : admin
密碼 : admin

************************************************************

參考API網址

http://git.oschina.net/imooccode/happymmallwiki/wikis/home

************************************************************