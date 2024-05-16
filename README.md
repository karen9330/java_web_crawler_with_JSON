# java-
使用json爬證交所的資料並做簡單的分析

外部API : Json
Note:建議把Json.jar和code放在同一份資料夾，並在終端及執行時輸入：

編譯-> javac -cp ".:./json.jar" StockParser.java 

執行-> java -cp ".:./json.jar" StockParser  

# 程式碼解釋：
主要分成3個class：StockParser（程式的進入點）、JsonFetcher（爬證交所的資料）、Analysis（分析爬到的資料）

1.爬取的資料有：當日發行量加權股價指數、當日成交量前10名、當日成交量前10名年初的收盤價、連續5天的股價

2.股票的分析：分析成交量前十名及其年初的收盤價漲跌幅、計算5天的平均股價，並計算漲跌幅
