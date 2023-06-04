package com.github.yeetmanlord.zetacorporations.api;

public class Share {

    private long quantity;
    private double averagePrice;
    private final Company company;
    private final Portfolio portfolio;

    public Share(long quantity, double averagePrice, Company company, Portfolio portfolio) {
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.company = company;
        this.portfolio = portfolio;
    }

    public long getQuantity() {
        return quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void setAveragePrice(double newPrice) {
        this.averagePrice = newPrice;
    }

    public double getValue() {
        return this.quantity * this.company.getMidPrice();
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public Company getCompany() {
        return company;
    }
}
