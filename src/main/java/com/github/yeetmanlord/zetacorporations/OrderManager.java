package com.github.yeetmanlord.zetacorporations;

import com.github.yeetmanlord.zetacorporations.api.Order;
import com.github.yeetmanlord.zetacorporations.api.OrderPriceConfiguration;
import com.github.yeetmanlord.zetacorporations.api.Portfolio;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OrderManager {

    private final HashMap<UUID, Order> openOrders;
    private final HashMap<UUID, List<Order>> portfolioOrders;

    public OrderExecutionTask orderExecutionTask;

    public OrderManager() {
        this.openOrders = new HashMap<>();
        this.portfolioOrders = new HashMap<>();

        this.orderExecutionTask = new OrderExecutionTask();
    }

    public UUID registerOrder(Order order) {
        UUID uuid = UUID.randomUUID();
        while (openOrders.containsKey(uuid)) {
            uuid = UUID.randomUUID();
        }
        openOrders.put(uuid, order);
        if (order.getPortfolioOwner() != null) {
            portfolioOrders.putIfAbsent(order.getPortfolioOwner(), new ArrayList<>());
            portfolioOrders.get(order.getPortfolioOwner()).add(order);
        }
        return uuid;
    }

    public void registerOrder(UUID uuid, Order order) {
        openOrders.put(uuid, order);
        if (order.getPortfolioOwner() != null) {
            portfolioOrders.putIfAbsent(order.getPortfolioOwner(), new ArrayList<>());
            portfolioOrders.get(order.getPortfolioOwner()).add(order);
        }
    }

    public void removeOrder(UUID uuid) {
        openOrders.remove(uuid);
    }

    public Order getOrder(UUID uuid) {
        return openOrders.get(uuid);
    }

    public HashMap<UUID, Order> getOpenOrders() {
        return openOrders;
    }

    // Since there can be multiple fills per order (Especially limit orders), this method will be called
    // multiple times per order
    public void cancelInvalidOrders(Order order, long sharesTransacted, double fillPrice) {
        if (order.getPortfolioOwner() != null) {
            Portfolio portfolio = ZetaCorporations.getInstance().getPortfolioData().getPortfolio(order.getPortfolioOwner());

            List<Order> openOrders = this.portfolioOrders.get(order.getPortfolioOwner());
            List<Order> ordersOnCompany = new ArrayList<>();
            for (Order openOrder : openOrders) {
                if (openOrder.getCompany().equals(order.getCompany())) {
                    ordersOnCompany.add(openOrder);
                }
            }

            if (order.getOrderConfig().getTransactionType() == OrderPriceConfiguration.TransactionType.SELL) {
                // Check for invalid sell orders, i.e. if the portfolio doesn't have enough shares to sell
                long sharesOwned = portfolio.getSharesOwned(order.getCompany());
                sharesOwned -= sharesTransacted;

                for (Order openOrder : ordersOnCompany) {
                    // Market orders are handled within the order fill method
                    if (openOrder.getOrderConfig().getOrderType() == OrderPriceConfiguration.OrderType.MARKET ||
                            openOrder.getOrderID() == order.getOrderID()) {
                        continue;
                    }
                    // Cannot sell more shares than you own
                    if (openOrder.getOrderConfig().getTransactionType() == OrderPriceConfiguration.TransactionType.SELL &&
                            openOrder.getQuantity() > sharesOwned) {
                        openOrder.cancel();
                    }
                }
            } else if (order.getOrderConfig().getTransactionType() == OrderPriceConfiguration.TransactionType.BUY) {
                // Check for invalid buy orders, i.e. if the portfolio doesn't have enough cash to buy
                double cash = portfolio.getCash();
                cash -= order.getQuantity() * fillPrice;

                for (Order openOrder : ordersOnCompany) {
                    // Market orders are handled within the order fill method
                    if (openOrder.getOrderConfig().getOrderType() == OrderPriceConfiguration.OrderType.MARKET ||
                            openOrder.getOrderID() == order.getOrderID()) {
                        continue;
                    }

                    // Cannot buy more shares than you can afford
                    if (openOrder.getOrderConfig().getTransactionType() == OrderPriceConfiguration.TransactionType.BUY &&
                            openOrder.getQuantity() * openOrder.getOrderConfig().getPrice() > cash) {
                        openOrder.cancel();
                    }
                }
            }
        }
    }

    public void cancelOrder(Order order) {
        if (order.getPortfolioOwner() != null) {
            portfolioOrders.get(order.getPortfolioOwner()).remove(order);
        }
        openOrders.remove(order.getOrderID());
    }

    /**
     * Runs every second to execute or check execution on all open orders
     */
    public class OrderExecutionTask implements Runnable {
        @Override
        public void run() {
            ArrayList<Order> listToAvoidConcurrentModification = new ArrayList<>(openOrders.values());
            for (Order order : listToAvoidConcurrentModification) {
                order.checkAndExecute();
            }
        }
    }

    public List<Order> getPortfolioOrders(UUID portfolioOwner) {
        if (!portfolioOrders.containsKey(portfolioOwner)) {
            portfolioOrders.put(portfolioOwner, new ArrayList<>());
        }
        return portfolioOrders.get(portfolioOwner);
    }
}
