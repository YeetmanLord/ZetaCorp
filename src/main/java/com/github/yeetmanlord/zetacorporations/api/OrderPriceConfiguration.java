package com.github.yeetmanlord.zetacorporations.api;

public class OrderPriceConfiguration {
    /**
     * The type of order to be executed, either MARKET, LIMIT or STOP
     */
    private OrderType orderType;

    /**
     * The price at which the order should be executed when the orderType is LIMIT or STOP
     */
    private final double price; // Limit/Stop price

    /**
     * The type of transaction to be executed, either SHARE_ISSUE, BUY or SELL.
     * SHARE_ISSUE is only used when issuing shares to investors by the company
     */
    private final TransactionType transactionType;

    public OrderPriceConfiguration(OrderType orderType, double price, TransactionType transactionType) {
        this.orderType = orderType;
        this.price = price;
        this.transactionType = transactionType;
    }

    public boolean shouldExecute(double currentSecurityPrice) {
        if (currentSecurityPrice == 0) return false;
        if (transactionType == TransactionType.BUY) {
            return switch (orderType) {
                case MARKET -> true;
                case LIMIT -> currentSecurityPrice <= price;
                case STOP -> currentSecurityPrice >= price;
            };
        }
        else {
            return switch (orderType) {
                case MARKET -> true;
                case LIMIT -> currentSecurityPrice >= price;
                case STOP -> currentSecurityPrice <= price;
            };
        }
    }

    public void setOrderType(OrderType type) {
        this.orderType = type;
    }

    public enum OrderType {
        MARKET,
        LIMIT,
        STOP;
    }

    public enum TransactionType {
        SHARE_ISSUE,
        BUY,
        SELL;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public double getPrice() {
        return price;
    }
}
