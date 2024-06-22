# 簡介
使用java.net的API去爬證交所的資料，並使用json來解析資料，最後做簡單的分析

# 如何使用
### 在 MAC、 Linux
```
javac -cp ".:./json.jar" StockParser.java  //compile

java -cp ".:./json.jar" StockParser       //run
```

### 在 Windows
```
javac -cp ".;./json.jar" StockParser.java  //compile

java -cp ".;./json.jar" StockParser       //run
```

# 程式碼解釋：

主要分成3個class：StockParser、JsonFetcher、Analysis

```
1.StockParser：程式的進入點，執行程式後會跑出分析的股票資訊。
2.JsonFetcher：爬證交所的資料（當日發行量加權股價指數、當日成交量前10名、當日成交量前10名年初的收盤價、連續5天的股價）。
3.Analysis：分析爬到的資料，如：成交量前十名及其年初的收盤價漲跌幅、計算5天的平均股價，並計算漲跌幅。

```
