import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.Collections;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONObject;


public class StockParser{
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        HashMap<Integer,JsonFetcher> firstTenStocks = JsonFetcher.getFirstTens();
        TreeMap<Float,String> analysisOne = Analysis.priceChange(firstTenStocks);
        TreeMap<Float,String> analysisTwo = Analysis.movingAverage(firstTenStocks);

        System.out.println("今日發行量加權股價指數資訊和簡單建議：");
        System.out.println("收盤指數："+JsonFetcher.getStockIndex().getCloseIndex());
        System.out.println("漲幅點數："+JsonFetcher.getStockIndex().getChangePips());
        System.out.println("漲幅百分比："+JsonFetcher.getStockIndex().getChangePrice()+"%");
        Analysis.suggestionOfMarket(JsonFetcher.getStockIndex());
        System.out.println("==============================");
        System.out.println("今日股票交易量前十名：");
        firstTenStocks.forEach((rank, stock) -> {
        System.out.println("Rank: " + rank + " | Stock No: " + stock.getStockNo() +
                               " | Stock Name: " + stock.getStockName() +
                               " | Closed Price: " + stock.getClosedPrice());
        });
        System.out.println("==============================");
        System.out.println("今日前十名股票及其01/02的收盤價漲跌幅：");
        for (Map.Entry<Float, String> entry : analysisOne.entrySet()) {
            System.out.println("Price Change: " + entry.getKey() +"%" + ", Stock Name: " + entry.getValue());
        }
        System.out.println("==============================");
        System.out.println("今日前十名股票的五日移動平均：");
        for (Map.Entry<Float, String> entry : analysisTwo.entrySet()) {
            System.out.println("Five days moving average: " + entry.getKey() +"%" + ", Stock Name: " + entry.getValue());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000000;
        System.out.println("==============================");
        System.out.println("time: " +duration + " s");
    }
}
class JsonFetcher {

    //前十名
    String stockNo;
    String stockName;
    float closedPrice;
    //大盤指數
    float closeIndex = 0;
    float changePips = 0;
    float changePrice = 0;

    public String getStockNo() {
        return stockNo;
    }

    public String getStockName() {
        return stockName;
    }

    public float getClosedPrice() {
        return closedPrice;
    }

    public float getCloseIndex() {
        return closeIndex;
    }
    
    public float getChangePips() {
        return changePips;
    }

    public float getChangePrice() {
        return changePrice;
    }
    //construcotr
    public JsonFetcher(String stockNo, String stockName, float closedPrice) {
        this.stockNo = stockNo;
        this.stockName = stockName;
        this.closedPrice = closedPrice;
    }
    public JsonFetcher(float closeIndex,float changePips,float changePrice) {
        this.closeIndex = closeIndex;
        this.changePips = changePips;
        this.changePrice = changePrice;
    }

    public static HashMap<Integer, JsonFetcher> getFirstTens(){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.twse.com.tw/rwd/zh/afterTrading/MI_INDEX20?response=json&_=" + System.currentTimeMillis()))
            .build();

        HashMap<Integer,JsonFetcher > stockData = new HashMap<>();
        try{
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            String jsonResponse = response.body();
        
            JSONObject obj = new JSONObject(jsonResponse); 
            JSONArray data = obj.getJSONArray("data"); 

            for (int i = 0; i < 10; i++) {
                JSONArray stock = data.getJSONArray(i);
                Integer rank = stock.getInt(0); // 股票排名
                String stockNo = stock.getString(1); //股票代號
                String stockName = stock.getString(2); // 股票名稱
                Float closedPrice = Float.parseFloat(stock.getString(8)); //收盤價
                JsonFetcher stockInfo = new JsonFetcher(stockNo, stockName, closedPrice);

                stockData.put(rank,stockInfo);
            }
            

        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return stockData;
    }

    public static float getJanuarySecondPrices(String stockNo){
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date=20240101&stockNo=putStockNumber&response=json&_=";
        url = url.replaceAll("putStockNumber", stockNo);
        if(stockNo.equals("00940")){
            url = url.replaceAll("20240101", "20240401");
        }

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + System.currentTimeMillis())).build();            
        float closedPrice =0;
        try{
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            String jsonResponse = response.body();
            JSONObject obj = new JSONObject(jsonResponse); 
            if (obj.has("data")) {
                JSONArray data = obj.getJSONArray("data");
                JSONArray stock = data.getJSONArray(0);//1月2號的股票資訊
                closedPrice = Float.parseFloat(stock.getString(6)); // 收盤價
            }
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return closedPrice;
    }
    
    public static JsonFetcher getStockIndex(){
        LocalDate today = LocalDate.now();
        String todayDate = String.valueOf(today).replaceAll("-", "");
        String url = "https://www.twse.com.tw/rwd/zh/afterTrading/MI_INDEX?date=TODAYDATE&type=IND&response=json&_=";
        url = url.replaceAll("TODAYDATE", todayDate);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + System.currentTimeMillis())).build();
        JsonFetcher stockIndex = new JsonFetcher(0, 0, 0);
    
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            String jsonResponse = response.body();
            JSONObject obj = new JSONObject(jsonResponse);

            JSONArray tables = obj.getJSONArray("tables");
            if (tables.length() > 0) {
                JSONObject firstTable = tables.getJSONObject(0); // 第一个表格
                JSONArray data = firstTable.getJSONArray("data");
                JSONArray stock = data.getJSONArray(1);
                float closedIndex = Float.parseFloat(stock.getString(1).replaceAll(",", ""));
                float changePips = Float.parseFloat(stock.getString(3));
                float changePrice = Float.parseFloat(stock.getString(4));
                stockIndex = new JsonFetcher(closedIndex, changePips, changePrice);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return stockIndex;
    }

    public static float getMovingAverage(String stockNo){
        LocalDate today = LocalDate.now();
        String todayDate = String.valueOf(today).replaceAll("-", "");
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY?date=TODAYDATE&stockNo=putStockNumber&response=json&_=";
        url = url.replaceAll("TODAYDATE", todayDate);
        url = url.replaceAll("putStockNumber", stockNo);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + System.currentTimeMillis())).build();            
        float closedPrice =0;
        try{
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            String jsonResponse = response.body();
            JSONObject obj = new JSONObject(jsonResponse); 
            
            int total = obj.getInt("total");
            if (obj.has("data")) {
                JSONArray data = obj.getJSONArray("data");
                for(int i= 0;i<total;i++){
                    JSONArray stock = data.getJSONArray(i);
                    if(i >= total-5){
                        closedPrice += Float.parseFloat(stock.getString(6)); // 收盤價總和
                    }
                }
            }
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return (closedPrice/5);
    }
}

class Analysis{
    public static TreeMap<Float,String> priceChange(HashMap<Integer,JsonFetcher> firstTenStocks){
        TreeMap<Float,String> priceChangeMap = new TreeMap<>(Collections.reverseOrder());
        for(int i=1;i<11;i++){
            String stockName = firstTenStocks.get(i).getStockName();
            String stockNo = firstTenStocks.get(i).getStockNo();
            float closedPrice = firstTenStocks.get(i).getClosedPrice();
            float januarySecondClosedPrice = JsonFetcher.getJanuarySecondPrices(stockNo);
            float change = ((closedPrice - januarySecondClosedPrice)/januarySecondClosedPrice)*100;
            BigDecimal roundUpChange = new BigDecimal(change).setScale(2, RoundingMode.HALF_UP);
            priceChangeMap.put(roundUpChange.floatValue(), stockName);
        }

        return priceChangeMap;
    }

    public static void suggestionOfMarket(JsonFetcher stockIndex){
        if(stockIndex.getChangePrice()>= 1.0){
            System.out.println("請不用擔心，可以盡情看股市");
        }
        else if(stockIndex.getChangePrice()<= -1.0){
            System.out.println("為了您今天的美好心情，請勿觀看今日股市");
        }
        else{
            System.out.println("今日股市無太大波動，請斟酌瀏覽");
        }
    }

    public static TreeMap<Float,String> movingAverage(HashMap<Integer,JsonFetcher> firstTenStocks){

        TreeMap<Float,String> movingAvg = new TreeMap<>(Collections.reverseOrder());
        for(int i=1;i<11;i++){
            String stockName = firstTenStocks.get(i).getStockName();
            String stockNo = firstTenStocks.get(i).getStockNo();
            float closedPrice = firstTenStocks.get(i).getClosedPrice();

            float fiveDaysAvg = JsonFetcher.getMovingAverage(stockNo);
            float avgChange = ((closedPrice - fiveDaysAvg)/fiveDaysAvg)*100;
            BigDecimal roundUpChange = new BigDecimal(avgChange).setScale(2, RoundingMode.HALF_UP);
            movingAvg.put(roundUpChange.floatValue(), stockName);
        }
        return movingAvg;
    }
}