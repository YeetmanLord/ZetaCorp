package com.github.yeetmanlord.zetacorporations.api;

import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class Order {

    /**
     * The company that this order is for.
     * If the order config transaction type is share issue, this is the company that the shares are being issued for
     * and any shares that are bought will be added to this company's balance.
     */
    @NonNull
    private final Company company;
    /**
     * Amonunt of shares to buy
     */
    private long quantity;

    /**
     * The player that holds the order. Can be null if the this is a share issue order.
     */
    @Nullable
    private final UUID portfolioOwner;

    /**
     * The configuration for this order.
     */
    @NonNull
    private final OrderPriceConfiguration orderConfig;

    private final UUID orderID;

    private boolean isCancelled = false;

    public Order(Company company, long quantity, OrderPriceConfiguration orderConfig, @Nullable UUID portfolioOwner, @Nullable UUID orderID) {
        this.company = company;
        this.quantity = quantity;
        this.orderConfig = orderConfig;
        this.portfolioOwner = portfolioOwner;

        if (orderID == null) {
            this.orderID = ZetaCorporations.getInstance().getOrderManager().registerOrder(this);
        } else {
            this.orderID = orderID;
            ZetaCorporations.getInstance().getOrderManager().registerOrder(orderID, this);
        }
    }

    public Order(Company company, long quantity, OrderPriceConfiguration orderConfig, @Nullable UUID portfolioOwner) {
        this(company, quantity, orderConfig, portfolioOwner, null);
    }

    /**
     * @return Internal order ID. Used for tracking orders.
     */
    public UUID getOrderID() {
        return orderID;
    }

    /**
     * Checks if an order can execute and executes it if it can.
     *
     * @see Order#fillOrder(Order, boolean, boolean) for actual order filling.
     */
    public void checkAndExecute() {
        switch (orderConfig.getTransactionType()) {
            case BUY -> {
                if (orderConfig.shouldExecute(company.getAskPrice())) {
                    switch (orderConfig.getOrderType()) {
                        case MARKET -> {
                            Order bestSellLimit = company.getBestOpenSellOrder();
                            if (bestSellLimit != null) {
                                fillOrder(bestSellLimit, true, true);
                            }
                        }

                        case STOP -> this.getOrderConfig().setOrderType(OrderPriceConfiguration.OrderType.MARKET);
                        case LIMIT -> {
                            Order bestSellLimit = company.getBestOpenSellOrder();
                            if (bestSellLimit != null && bestSellLimit.getOrderConfig().getPrice() <= orderConfig.getPrice()) {
                                fillOrder(bestSellLimit, true, false);
                            }
                        }
                    }

                }
            }
            case SELL -> {
                if (orderConfig.shouldExecute(company.getBidPrice())) {
                    switch (orderConfig.getOrderType()) {
                        case MARKET -> {
                            Order bestBuyLimit = company.getBestOpenBuyOrder();
                            if (bestBuyLimit != null) {
                                fillOrder(bestBuyLimit, false, true);
                            }
                        }
                        case STOP -> this.getOrderConfig().setOrderType(OrderPriceConfiguration.OrderType.MARKET);
                        case LIMIT -> {
                            Order bestBuyLimit = company.getBestOpenBuyOrder();
                            if (bestBuyLimit != null && bestBuyLimit.getOrderConfig().getPrice() >= orderConfig.getPrice()) {
                                fillOrder(bestBuyLimit, false, false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fills the order with the other order.
     *
     * @param otherOrder The order to fill this order with.
     * @param buy        Whether this order is a buy order. Convenience parameter.
     * @param market     Whether this order is a market order. Convenience parameter.
     */
    private void fillOrder(Order otherOrder, boolean buy, boolean market) {
        Portfolio thisPortfolio = null;
        Portfolio otherPortfolio = null;
        if (this.portfolioOwner != null) {
            thisPortfolio = ZetaCorporations.getInstance().getPortfolioData().getPortfolio(this.portfolioOwner);
        }

        if (otherOrder.getPortfolioOwner() != null) {
            otherPortfolio = ZetaCorporations.getInstance().getPortfolioData().getPortfolio(otherOrder.getPortfolioOwner());
        }

        double price = otherOrder.getOrderConfig().getPrice();
        if (buy) {
            if (thisPortfolio == null) {
                throw new IllegalStateException("Cannot buy shares without a portfolio owner");
            }
            double cash = thisPortfolio.getCash();
            double totalCost = price * quantity;
            if (market) {
                while (quantity > 0) {
                    price = otherOrder.getOrderConfig().getPrice();
                    cash = thisPortfolio.getCash();
                    totalCost = price * quantity;
                    if (cash < totalCost) {
                        this.cancel();

                        this.sendMessage("&aYou do not have enough cash to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &ashares in &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + "&a!");
                        ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, 0, price);
                    }
                    long otherQuantity = otherOrder.getQuantity();
                    if (quantity >= otherQuantity) {
                        this.quantity -= otherQuantity;
                        thisPortfolio.onBuy(otherQuantity, price, company);
                        if (otherPortfolio != null) {
                            otherOrder.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                            otherPortfolio.onSell(otherQuantity, price, company);
                            otherOrder.cancel();
                            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, otherQuantity, price);
                        } else {
                            // Company is issuing shares
                            otherOrder.sendMessage("&aAn investor has bought &e" + otherQuantity + " &anewly issued shares in your company!");
                            company.onSharesBought(otherQuantity, price);
                            otherOrder.cancel();
                        }
                        if (quantity > 0) {
                            this.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + otherQuantity + " shares)");
                            otherOrder = company.getBestOpenSellOrder();
                            // No more orders to fill
                            if (otherOrder == null) {
                                this.sendMessage("&eThere are no more sell orders to fill your order, so it is being cancelled.");
                                this.cancel();
                                return;
                            }
                        } else {
                            this.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                            this.cancel();
                            break;
                        }
                    } else {
                        thisPortfolio.onBuy(quantity, price, company);
                        otherOrder.quantity -= quantity;
                        if (otherPortfolio != null) {
                            otherOrder.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + ZetaCorporations.INT_FORMAT.format(quantity) + " shares)");
                            otherPortfolio.onSell(quantity, price, company);
                            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, quantity, price);
                        } else {
                            // Company is issuing shares
                            otherOrder.sendMessage("&aAn investor has bought &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &anewly issued shares in your company!");
                            company.onSharesBought(quantity, price);
                        }
                        this.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                        this.cancel();
                        ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, quantity, price);
                        this.quantity = 0;
                    }
                }
                return;
            }

            ZetaCorporations.getInstance().getPluginLogger().debug("Buy at limit: " + otherOrder.company.getName());
            // Limit order
            if (cash < totalCost) {
                this.cancel();
                this.sendMessage("&aYou do not have enough cash to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &ashares in &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + "&a!");
                ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, 0, price);
                return;
            }
            long otherQuantity = otherOrder.getQuantity();

            if (this.quantity > otherQuantity) {
                thisPortfolio.onBuy(otherQuantity, price, company);
                this.quantity -= otherQuantity;
                if (otherPortfolio != null) {
                    otherOrder.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                    otherPortfolio.onSell(otherQuantity, price, company);
                    otherOrder.cancel();
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, otherQuantity, price);
                } else {
                    // Company is issuing shares
                    otherOrder.sendMessage("&aAn investor has bought &e" + otherQuantity + " &anewly issued shares in your company!");
                    company.onSharesBought(otherQuantity, price);
                    otherOrder.cancel();
                }
                this.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + otherQuantity + " shares)");
                ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, otherQuantity, price);
            } else if (quantity == otherQuantity) {
                thisPortfolio.onBuy(quantity, price, company);
                this.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                this.cancel();
                if (otherPortfolio != null) {
                    otherOrder.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                    otherPortfolio.onSell(quantity, price, company);
                    otherOrder.cancel();
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, quantity, price);
                } else {
                    // Company is issuing shares
                    otherOrder.sendMessage("&aAn investor has bought &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &anewly issued shares in your company!");
                    company.onSharesBought(quantity, price);
                    otherOrder.cancel();
                }
                ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, quantity, price);
            } else {
                thisPortfolio.onBuy(quantity, price, company);
                otherOrder.quantity -= quantity;
                if (otherPortfolio != null) {
                    otherOrder.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + ZetaCorporations.INT_FORMAT.format(quantity) + " shares)");
                    otherPortfolio.onSell(quantity, price, company);
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, quantity, price);
                } else {
                    // Company is issuing shares
                    otherOrder.sendMessage("&aAn investor has bought &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &anewly issued shares in your company!");
                    company.onSharesBought(quantity, price);
                }
                this.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                this.cancel();
                ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, quantity, price);
                this.quantity = 0;
            }
            return;
        }

        // Sell order
        if (thisPortfolio == null || otherPortfolio == null) {
            String extraMsg = "";
            if (thisPortfolio == null) {
                extraMsg = "The seller does not exist!";
            } else {
                extraMsg = "The buyer does not exist!";
            }
            if (thisPortfolio == null && otherPortfolio == null) {
                extraMsg = "Both the buyer and the seller do not exist!";
            }
            throw new IllegalStateException("A sell order must have a buyer and a seller! " + extraMsg);
        }

        /* Possibly unnecessary, need to verify that...
        // Sell orders are processed from the perspective of the BUYER
        price = this.getOrderConfig().getPrice();
        double cash = otherPortfolio.getCash();
        double totalCost = otherOrder.quantity * price;

        if (market) {
            while (quantity > 0) {
                cash = otherPortfolio.getCash();
                totalCost = price * otherOrder.quantity;
                if (cash < totalCost) {
                    otherOrder.cancel();
                    otherOrder.sendMessage("&aYou do not have enough cash to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &ashares in &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + "&a!");
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, 0, price);
                }
                long otherQuantity = otherOrder.getQuantity();
                if (quantity >= otherQuantity) {
                    this.quantity -= otherQuantity;
                    thisPortfolio.onSell(otherQuantity, price, company);

                    otherOrder.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                    otherPortfolio.onBuy(otherQuantity, price, company);
                    otherOrder.cancel();
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, otherQuantity, price);

                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, otherQuantity, price);
                    if (quantity > 0) {
                        this.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + otherQuantity + " shares)");
                        otherOrder = company.getBestOpenSellOrder();
                        // No more orders to fill
                        if (otherOrder == null) {
                            return;
                        }
                    } else {
                        this.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                        this.cancel();
                        break;
                    }
                } else {
                    thisPortfolio.onSell(quantity, price, company);

                    otherOrder.quantity -= quantity;
                    otherOrder.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + ZetaCorporations.INT_FORMAT.format(quantity) + " shares)");
                    otherPortfolio.onBuy(quantity, price, company);
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, quantity, price);

                    this.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
                    this.cancel();
                    ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, quantity, price);
                    this.quantity = 0;
                }
            }
            return;
        }
        
        // Limit order
        if (cash < totalCost) {
            otherOrder.cancel();
            otherOrder.sendMessage("&aYou do not have enough cash to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &ashares in &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + "&a!");
            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, 0, price);
            return;
        }
        
        if (this.quantity > otherOrder.getQuantity()) {
            thisPortfolio.onSell(otherOrder.getQuantity(), price, company);
            this.quantity -= otherOrder.getQuantity();
            
            otherOrder.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
            otherPortfolio.onBuy(otherOrder.getQuantity(), price, company);
            otherOrder.cancel();
            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, otherOrder.getQuantity(), price);
            
            this.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + otherOrder.getQuantity() + " shares)");
            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, otherOrder.getQuantity(), price);
        } else if (quantity == otherOrder.getQuantity()) {
            thisPortfolio.onSell(quantity, price, company);
            this.sendMessage("&aYour sell order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
            this.cancel();
            
            otherOrder.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been completely filled!");
            otherPortfolio.onBuy(quantity, price, company);
            otherOrder.cancel();
            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, quantity, price);
            
            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(this, quantity, price);
        } else {
            thisPortfolio.onSell(quantity, price, company);

            otherOrder.quantity -= quantity;
            otherOrder.sendMessage("&aYour buy order on &e" + ChatColor.translateAlternateColorCodes('&', company.getName()) + " &ahas been partially filled! (" + ZetaCorporations.INT_FORMAT.format(quantity) + " shares)");
            otherPortfolio.onBuy(quantity, price, company);
            ZetaCorporations.getInstance().getOrderManager().cancelInvalidOrders(otherOrder, quantity, price);
        } */
    }

    public OrderPriceConfiguration getOrderConfig() {
        return orderConfig;
    }

    @Nullable
    public UUID getPortfolioOwner() {
        return portfolioOwner;
    }

    public Company getCompany() {
        return company;
    }

    public long getQuantity() {
        return quantity;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() {
        this.isCancelled = true;
        ZetaCorporations.getInstance().getOrderManager().cancelOrder(this);
        if (this.orderConfig.getTransactionType() == OrderPriceConfiguration.TransactionType.BUY) {
            this.company.getOpenBuyOrders().remove(this);
        } else {
            this.company.getOpenSellOrders().remove(this);
        }
    }

    private void sendMessage(String message) {
        if (this.portfolioOwner != null) {
            Player player = Bukkit.getPlayer(this.portfolioOwner);
            if (player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        } else {
            this.company.broadcastMessage(message);
        }
    }
}
