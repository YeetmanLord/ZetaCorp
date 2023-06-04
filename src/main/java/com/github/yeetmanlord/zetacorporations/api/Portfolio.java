package com.github.yeetmanlord.zetacorporations.api;

import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Portfolio {
    private final HashMap<String, Share> positions;
    private final UUID owner;

    public Portfolio(HashMap<String, Share> positions, UUID owner) {
        this.positions = positions;
        this.owner = owner;
    }

    public double getBalance() {
        double balance = this.getCash();
        for (Share share : positions.values()) {
            balance += share.getValue();
        }
        return balance;
    }

    public void addPosition(Share share) {
        positions.put(share.getCompany().getTicker(), share);
    }

    public HashMap<String, Share> getPositions() {
        return positions;
    }

    public double getCash() {
        return ZetaCorporations.getEconomy().getBalance(this.getOwnerPlayer());
    }

    public UUID getOwner() {
        return owner;
    }

    public OfflinePlayer getOwnerPlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

    public void onBuy(long quantity, double price, Company company) {
        double cash = this.getCash();
        if (cash < quantity * price) {
            throw new IllegalArgumentException("Not enough cash to buy " + ZetaCorporations.INT_FORMAT.format(quantity) + " shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()) + " at " + price + " each");
        }
        ZetaCorporations.getEconomy().withdrawPlayer(this.getOwnerPlayer(), quantity * price);
        if (positions.containsKey(company.getTicker())) {
            Share share = positions.get(company.getTicker());
            share.setQuantity(share.getQuantity() + quantity);
            share.setAveragePrice((share.getAveragePrice() * (share.getQuantity() - quantity) + quantity * price) / share.getQuantity());
        } else {
            positions.put(company.getTicker(), new Share(quantity, price, company, this));
        }

        company.setLastPrice(price);
        ZetaCorporations.getInstance().getPortfolioData().savePortfolio(this);
    }

    public void onSell(long quantity, double price, Company company) {
        if (!positions.containsKey(company.getTicker())) {
            throw new IllegalArgumentException("You do not own any shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()));
        }
        Share share = positions.get(company.getTicker());
        if (share.getQuantity() < quantity) {
            throw new IllegalArgumentException("You do not own enough shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()) + " to sell " + quantity);
        }
        share.setQuantity(share.getQuantity() - quantity);
        if (share.getQuantity() == 0) {
            positions.remove(company.getTicker());
        }
        ZetaCorporations.getEconomy().depositPlayer(this.getOwnerPlayer(), quantity * price);
        ZetaCorporations.getInstance().getPortfolioData().savePortfolio(this);
    }

    public long getSharesOwned(Company company) {
        if (!positions.containsKey(company.getTicker())) {
            return 0;
        }
        return positions.get(company.getTicker()).getQuantity();
    }

    public List<Order> getOrders() {
        return ZetaCorporations.getInstance().getOrderManager().getPortfolioOrders(this.getOwner()).stream().filter(order -> !order.isCancelled()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Checks all open orders to see if the player has enough shares to sell (That aren't associated with an order)
     *
     * @param company  The company to check
     * @param quantity The quantity to check
     * @return Whether the player has enough shares to sell
     */
    public boolean verifySell(Company company, long quantity) {
        long sharesOwned = this.getSharesOwned(company);
        long sharesInOrders = 0;
        for (Order order : this.getOrders()) {
            if (order.getCompany().equals(company)) {
                if (order.getOrderConfig().getTransactionType() == OrderPriceConfiguration.TransactionType.SELL) {
                    sharesInOrders += order.getQuantity();
                }
            }
        }
        return sharesOwned - sharesInOrders >= quantity;
    }

    /**
     * Checks all open orders to see if the player has enough cash to buy (That aren't associated with an order)
     *
     * @param company  The company to check
     * @param quantity The quantity to check
     * @param price    The price to check either market ask price or limit price
     * @return Whether the player has enough cash to buy
     */
    public boolean verifyBuy(Company company, long quantity, double price) {
        double cash = this.getCash();
        double cashInOrders = 0;

        for (Order order : this.getOrders()) {
            if (order.getCompany().equals(company)) {
                if (order.getOrderConfig().getTransactionType() == OrderPriceConfiguration.TransactionType.BUY) {
                    if (order.getOrderConfig().getOrderType() == OrderPriceConfiguration.OrderType.MARKET) {
                        cashInOrders += order.getQuantity() * (order.getCompany().getAskPrice() * (1 + ZetaCorporations.getInstance().getGeneralConfig().getMarketOrderMargin()));
                    } else {
                        cashInOrders += order.getQuantity() * order.getOrderConfig().getPrice();
                    }
                }
            }
        }
        return cash - cashInOrders >= quantity * price;
    }

    public void sellMarket(Company company, long quantity) {
        if (!this.verifySell(company, quantity)) {
            throw new IllegalArgumentException("You do not have enough shares to sell " + ZetaCorporations.INT_FORMAT.format(quantity) + " shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()));
        }
        Order order = new Order(company, quantity, new OrderPriceConfiguration(OrderPriceConfiguration.OrderType.MARKET, 0, OrderPriceConfiguration.TransactionType.SELL), this.owner);
        company.addSellOrder(order);

        Player player = this.getOwnerPlayer().getPlayer();
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have placed an order to sell &e" + ZetaCorporations.INT_FORMAT.format(quantity) + "&a shares of " + company.getName() + " &aat market price"));
        }
    }

    public void sellLimit(Company company, long quantity, double price) {
        if (!this.verifySell(company, quantity)) {
            throw new IllegalArgumentException("You do not have enough shares to sell " + ZetaCorporations.INT_FORMAT.format(quantity) + " shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()));
        }
        Order order = new Order(company, quantity, new OrderPriceConfiguration(OrderPriceConfiguration.OrderType.LIMIT, price, OrderPriceConfiguration.TransactionType.SELL), this.owner);
        company.addSellOrder(order);

        Player player = this.getOwnerPlayer().getPlayer();
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have placed an order to sell &e" + ZetaCorporations.INT_FORMAT.format(quantity) + "&a shares of " + company.getName() +  " &aat &e$&6"
                    + ZetaCorporations.DEFAULT_FORMAT.format(price)));
        }
    }

    public void buyMarket(Company company, long quantity) {
        if (!this.verifyBuy(company, quantity, company.getAskPrice())) {
            throw new IllegalArgumentException("You do not have enough cash to buy " + ZetaCorporations.INT_FORMAT.format(quantity) + " shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()));
        }
        Order order = new Order(company, quantity, new OrderPriceConfiguration(OrderPriceConfiguration.OrderType.MARKET, 0, OrderPriceConfiguration.TransactionType.BUY), this.owner);
        company.addBuyOrder(order);

        Player player = this.getOwnerPlayer().getPlayer();
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have placed an order to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + "&a shares of " + company.getName() + " &aat market price"));
        }
    }

    public void buyLimit(Company company, long quantity, double price) {
        if (!this.verifyBuy(company, quantity, price)) {
            throw new IllegalArgumentException("You do not have enough cash to buy " + ZetaCorporations.INT_FORMAT.format(quantity) + " shares of " + ChatColor.translateAlternateColorCodes('&', company.getName()));
        }
        Order order = new Order(company, quantity, new OrderPriceConfiguration(OrderPriceConfiguration.OrderType.LIMIT, price, OrderPriceConfiguration.TransactionType.BUY), this.owner);
        company.addBuyOrder(order);

        Player player = this.getOwnerPlayer().getPlayer();
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have placed an order to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + "&a shares of " + company.getName() +  " &aat &e$&6"
                    + ZetaCorporations.DEFAULT_FORMAT.format(price)));
        }
    }
}
