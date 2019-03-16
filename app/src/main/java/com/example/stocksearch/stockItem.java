package com.example.stocksearch;

public class stockItem {
    private String ticker;
    private String securityName;

    public stockItem(String ticker, String securityName) {
        this.ticker = ticker;
        this.securityName = securityName;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getSecurityName() {
        return securityName;
    }

    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }
}
