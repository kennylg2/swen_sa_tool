/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockreporter.scrappers;

import java.io.IOException;
import stockreporter.daomodels.StockSummary;
import stockreporter.daomodels.StockTicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import stockreporter.StockReporter;
import stockreporter.Utility;
import stockreporter.daomodels.StockDateMap;

/**
 *
 * @author Herve Tchoufong
 */
public class InvestopediaScraper extends StockScraper {
    
    public InvestopediaScraper(){
        super();
    }
    
    public void scrapeAllSummaryData(){
        for(StockTicker stockTicker: stockTickers)
            scapeSingleSummaryData(stockTicker);
    }
    
    public void scapeSingleSummaryData(StockTicker stockTicker){        
        System.out.println(stockTicker.getSymbol());
        String url = "https://www.investopedia.com/markets/stocks/"+stockTicker.getSymbol().toLowerCase();
        try {
            Connection jsoupConn = Jsoup.connect(url);
            Document document = jsoupConn.referrer("http://www.google.com") .timeout(1000*5).get();

            StockDateMap stockDateMap = new StockDateMap();
            stockDateMap.setSourceId(2);
            stockDateMap.setTickerId(stockTicker.getId());
            stockDateMap.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            int last_inserted_id = dao.insertStockDateMap(stockDateMap);
        
            Element table2 = document.select("table").get(2);
            Elements rows = table2.select("tr");    
            StockSummary summaryData = new StockSummary();
            
            int rowNum=0;
            String prevClosePrice = rows.get(rowNum).select("td").get(1).text();
            summaryData.setPrevClosePrice(Utility.convertStringCurrency(Utility.isBlank(prevClosePrice)?"0":prevClosePrice));
            rowNum++;
            
            String openPrice = rows.get(rowNum).select("td").get(1).text();
            summaryData.setOpenPrice(Utility.convertStringCurrency(Utility.isBlank(openPrice)?"0":openPrice));
            rowNum++;
            
            String daysRangeMax = Utility.getRangeMinAndMax(rows.get(rowNum).select("td").get(1).text())[0].trim();
            summaryData.setDaysRangeMin(Utility.convertStringCurrency(Utility.isBlank(daysRangeMax)?"0":daysRangeMax));            
            String daysRangeMin = Utility.getRangeMinAndMax(rows.get(rowNum).select("td").get(1).text())[1].trim();
            summaryData.setDaysRangeMax(Utility.convertStringCurrency(Utility.isBlank(daysRangeMin)?"0":daysRangeMin));
            rowNum++;
                    
            String fiftyTwoWeeksMin = Utility.getRangeMinAndMax(rows.get(rowNum).select("td").get(1).text())[0].trim();
            summaryData.setFiftyTwoWeeksMin(Utility.convertStringCurrency(Utility.isBlank(fiftyTwoWeeksMin)?"0":fiftyTwoWeeksMin));
            String fiftyTwoWeeksMax = Utility.getRangeMinAndMax(rows.get(rowNum).select("td").get(1).text().trim())[1].trim();
            summaryData.setFiftyTwoWeeksMax(Utility.convertStringCurrency(Utility.isBlank(fiftyTwoWeeksMax)?"0":fiftyTwoWeeksMax));
            rowNum++;
                    
            String peRatio = rows.get(rowNum).select("td").get(1).text();
            summaryData.setPeRatio(Utility.convertStringCurrency(Utility.isBlank(peRatio)?"0":peRatio));
            
            rowNum=0;
            Element table3 = document.select("table").get(3);
            rows = table3.select("tr");    
            
            String betaCoefficient = rows.get(rowNum).select("td").get(1).text();
            summaryData.setBetaCoefficient(Utility.convertStringCurrency(Utility.isBlank(betaCoefficient)?"0":betaCoefficient));
            rowNum++;
            
            String volume = rows.get(rowNum).select("td").get(1).text();
            summaryData.setVolume(Utility.convertStringCurrency(Utility.isBlank(volume)?"0":volume).longValue());
            rowNum++;
            
            String dividend = Utility.getNumeratorAndDenominator(rows.get(rowNum).select("td").get(1).text())[0].trim();
            summaryData.setDividentYield(Utility.convertStringCurrency(Utility.isBlank(dividend)?"0":dividend));
            rowNum++;
            
            String marketCap = rows.get(rowNum).select("td").get(1).text();
            summaryData.setMarketCap(Utility.convertStringCurrency(Utility.isBlank(marketCap)?"0":marketCap));
            rowNum++;
            
            String eps = rows.get(rowNum).select("td").get(1).text();
            summaryData.setEps(Utility.convertStringCurrency(Utility.isBlank(eps)?"0":eps));
            
            dao.insertStockSummaryData(summaryData);
        } catch (IOException ex) {
            Logger.getLogger(StockReporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}