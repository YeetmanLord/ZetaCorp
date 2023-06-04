package com.github.yeetmanlord.zetacorporations.api;

import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Company {

    private double balance;

    private double shareholderReservedBalance;

    private String name;
    private final String ticker;
    private String description;

    /**
     * Determines who can access the corporate funds
     */
    private final List<UUID> owners;

    /**
     * Determines who can access the corporate funds
     */
    private final UUID ceo;

    private double lastPrice;

    private final List<Order> openBuyOrders;
    private final List<Order> openSellOrders;

    private ItemStack icon;

    private long totalSharesIssued;

    private boolean shouldSave = true;

    // Calculated attribute for the number of shares that are currently in circulation
    private long sharesOutstanding;


    public Company(double balance, String name, String ticker, String description, List<UUID> owners, UUID ceo, double lastPrice, List<Order> openBuyOrders, List<Order> openSellOrders, long sharesIssued) {
        this.balance = balance;
        this.name = name;
        this.ticker = ticker.toUpperCase();
        this.description = description;
        this.owners = owners;
        this.lastPrice = lastPrice;
        this.openBuyOrders = openBuyOrders;
        this.openSellOrders = openSellOrders;
        this.ceo = ceo;

        this.icon = new ItemStack(Material.GOLD_BLOCK);
        this.totalSharesIssued = sharesIssued;

        ZetaCorporations.getInstance().getCompanyData().companies.put(this.ticker, this);
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Gets the lowest price that someone is willing to sell for (the ask price)
     *
     * @return The lowest limit price currently in the order book (Sell orders)
     */
    public double getAskPrice() {
        return openSellOrders.stream().mapToDouble(order -> order.getOrderConfig().getPrice()).min().orElse(0);
    }

    /**
     * Gets the highest price that someone is willing to buy for (the bid price)
     *
     * @return The highest limit price currently in the order book (Buy orders)
     */
    public double getBidPrice() {
        return openBuyOrders.stream().mapToDouble(order -> order.getOrderConfig().getPrice()).max().orElse(0);
    }

    public double getMidPrice() {
        return (getAskPrice() + getBidPrice()) / 2;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public List<UUID> getOwners() {
        return owners;
    }

    public String getDescription() {
        return description;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
        if (shouldSave)
            ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void onSharesBought(long quantity, double price) {
        this.balance += (price * quantity * (1 - ZetaCorporations.getInstance().getGeneralConfig().getShareholderReservePercent()));
        this.shareholderReservedBalance += (price * quantity * ZetaCorporations.getInstance().getGeneralConfig().getShareholderReservePercent());
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);

        this.updateSharesOutstanding();
    }

    public void onDeposit(double amount, Player depositer) {
        if (owners.contains(depositer.getUniqueId())) {
            if (amount > ZetaCorporations.getEconomy().getBalance(depositer)) {
                depositer.sendMessage(ChatColor.RED + "You do not have enough money to deposit that much!");
                return;
            }
            this.balance += amount * 0.8;
            this.shareholderReservedBalance += amount * 0.2;
            ZetaCorporations.getEconomy().withdrawPlayer(depositer, amount);
            broadcastMessage("&a" + depositer.getName() + " &7has deposited &a$" + ZetaCorporations.DEFAULT_FORMAT.format(amount) + "&7 into the company!");

            ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
        }
    }

    public void onWithdraw(double amount, Player withdrawer) {
        if (owners.contains(withdrawer.getUniqueId())) {
            if (amount > this.balance) {
                withdrawer.sendMessage(ChatColor.RED + "You do not have enough money in the company to withdraw that much!");
                return;
            }
            this.balance -= amount;
            ZetaCorporations.getEconomy().depositPlayer(withdrawer, amount);
            broadcastMessage("&a" + withdrawer.getName() + " &7has withdrawn &a$" + ZetaCorporations.DEFAULT_FORMAT.format(amount) + "&7 from the company!");
        }

        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public List<Order> getOpenBuyOrders() {
        return openBuyOrders;
    }

    public List<Order> getOpenSellOrders() {
        return openSellOrders;
    }

    public void addSellOrder(Order order) {
        this.openSellOrders.add(order);
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void addBuyOrder(Order order) {
        this.openBuyOrders.add(order);
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    /**
     * Gets Buy Order With Best Price (Limit Order)
     */
    public Order getBestOpenBuyOrder() {

        return openBuyOrders.stream().filter((order) -> order.getOrderConfig().getOrderType() != OrderPriceConfiguration.OrderType.MARKET && !order.isCancelled())
                .max(Comparator.comparingDouble(o -> o.getOrderConfig().getPrice())).orElse(null);
    }

    /**
     * Gets Sell Order With Best Price (Limit Order)
     */
    public Order getBestOpenSellOrder() {
        return openSellOrders.stream().filter((order) -> order.getOrderConfig().getOrderType() != OrderPriceConfiguration.OrderType.MARKET && !order.isCancelled())
                .min(Comparator.comparingDouble(o -> o.getOrderConfig().getPrice())).orElse(null);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
        if (shouldSave) ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public UUID getCeo() {
        return ceo;
    }

    public void removeOwner(UUID owner) {
        owners.remove(owner);
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void addOwner(UUID uniqueId) {
        owners.add(uniqueId);

        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void delete() {
        List<Portfolio> shareholderPortfolios = ZetaCorporations.getInstance().getPortfolioData().getShareholders(this);
        // First liquidate all shares and pay out the shareholders
        List<Share> shares = new ArrayList<>();
        for (Portfolio portfolio : shareholderPortfolios) {
            shares.add(portfolio.getPositions().get(this.ticker));
        }
        // Order by largest position first
        shares.sort(Comparator.comparingLong(Share::getQuantity).reversed());
        for (Share share : shares) {
            // Investors must make their money back first
            double minPayout = share.getQuantity() * share.getAveragePrice();
            double percentOfCompany = share.getQuantity() / (double) this.totalSharesIssued;
            double payout = percentOfCompany * this.shareholderReservedBalance;
            if (payout < minPayout) {
                payout = minPayout;
            }
            // Pay out the investor
            this.shareholderReservedBalance -= payout;
            if (this.shareholderReservedBalance < 0) {
                payout += this.shareholderReservedBalance;
                this.shareholderReservedBalance = balance;
                // Subtract from the balance
                this.balance = 0;
                if (payout < 0) {
                    payout = 0;

                }
            }

            if (payout == 0) {
                OfflinePlayer player = ZetaCorporations.getInstance().getServer().getOfflinePlayer(share.getPortfolio().getOwner());
                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', this.name) + ChatColor.RED +
                            "was unable to pay you for your shares, as the company has no money left" +
                            " after the company was deleted!");
                }
            } else {
                OfflinePlayer player = ZetaCorporations.getInstance().getServer().getOfflinePlayer(share.getPortfolio().getOwner());
                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(ChatColor.GREEN + "You have been paid out $" + ZetaCorporations.DEFAULT_FORMAT.format(payout) + " for your shares in "
                            + ChatColor.translateAlternateColorCodes('&', this.name) +
                            ChatColor.GREEN +
                            " after the company was deleted!");
                }
                ZetaCorporations.getEconomy().depositPlayer(player, payout);
            }
            share.getPortfolio().getPositions().remove(this.ticker);
        }

        this.balance += this.shareholderReservedBalance; // Give the rest of the money to the company, if any is left

        // Payout to owners (if any money is left) (CEO gets an immediate 10% of the balance) before the rest is distributed equally
        double payout = this.balance * 0.1;
        this.balance -= payout;
        OfflinePlayer ceo = ZetaCorporations.getInstance().getServer().getOfflinePlayer(this.ceo);
        if (ceo.isOnline() && ceo.getPlayer() != null) {
            ceo.getPlayer().sendMessage(ChatColor.GREEN + "You have been paid out $" + ZetaCorporations.DEFAULT_FORMAT.format(payout) + " for your service as CEO in "
                    + ChatColor.translateAlternateColorCodes('&', this.name) +
                    ChatColor.GREEN +
                    " after the company was deleted!");
        }
        ZetaCorporations.getEconomy().depositPlayer(ceo, payout);

        int numOwners = owners.size();
        if (numOwners > 0) {
            payout = this.balance / numOwners;
            for (UUID owner : owners) {
                OfflinePlayer player = ZetaCorporations.getInstance().getServer().getOfflinePlayer(owner);
                ZetaCorporations.getEconomy().depositPlayer(player, payout);

                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(ChatColor.GREEN + "You have been paid out $" + ZetaCorporations.DEFAULT_FORMAT.format(payout) + " for your ownership in "
                            + ChatColor.translateAlternateColorCodes('&', this.name) +
                            ChatColor.GREEN +
                            " after the company was deleted!");
                }
            }
        }

        List<Order> avoidConcurrentMod = new ArrayList<>(openBuyOrders);
        for (Order order : avoidConcurrentMod) {
            order.cancel();
        }
        avoidConcurrentMod = new ArrayList<>(openSellOrders);
        for (Order order : avoidConcurrentMod) {
            order.cancel();
        }

        ZetaCorporations.getInstance().getCompanyData().deleteCompany(this);
        this.balance = 0;
        this.shareholderReservedBalance = 0;
    }

    /**
     * Pays a dividend to all shareholders of the company
     *
     * @param percent The percent of total company assets to pay out (Including reserved shareholder balance)
     */
    public void payDividend(double percent) {
        long totalSharesOutstanding = 0;
        List<Portfolio> shareholderPortfolios = ZetaCorporations.getInstance().getPortfolioData().getShareholders(this);
        List<Share> shares = new ArrayList<>();
        for (Portfolio portfolio : shareholderPortfolios) {
            Share share = portfolio.getPositions().get(this.ticker);
            totalSharesOutstanding += share.getQuantity();
            shares.add(share);
        }
        this.sharesOutstanding = totalSharesOutstanding;

        double dividendBalance = this.balance * percent;
        double dividendReserved = this.shareholderReservedBalance * percent;
        double dividend = dividendBalance + dividendReserved;
        double dividendPerShare = dividend / this.sharesOutstanding;
        this.balance -= dividendBalance;
        this.shareholderReservedBalance -= dividendReserved;

        for (Share share : shares) {
            double payout = share.getQuantity() * dividendPerShare;
            OfflinePlayer player = ZetaCorporations.getInstance().getServer().getOfflinePlayer(share.getPortfolio().getOwner());
            ZetaCorporations.getEconomy().depositPlayer(player, payout);

            if (player.isOnline() && player.getPlayer() != null) {
                player.getPlayer().sendMessage(ChatColor.GREEN + "You have received a dividend of " + ChatColor.GOLD + "$" + ZetaCorporations.DEFAULT_FORMAT.format(payout) + ChatColor.GREEN +
                        " per share for your " + ChatColor.GOLD + share.getQuantity() + ChatColor.GREEN + " shares in " + ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', this.name) + ChatColor.GREEN + "!");
            }
        }

        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void payDividendCash(double dollarsPerShare) {
        long totalSharesOutstanding = 0;
        List<Portfolio> shareholderPortfolios = ZetaCorporations.getInstance().getPortfolioData().getShareholders(this);
        List<Share> shares = new ArrayList<>();
        for (Portfolio portfolio : shareholderPortfolios) {
            Share share = portfolio.getPositions().get(this.ticker);
            totalSharesOutstanding += share.getQuantity();
            shares.add(share);
        }
        this.sharesOutstanding = totalSharesOutstanding;

        double totalDividend = totalSharesOutstanding * dollarsPerShare;
        if (totalDividend > this.shareholderReservedBalance) {
            if (totalDividend > this.shareholderReservedBalance + this.balance) {
                totalDividend = this.shareholderReservedBalance + this.balance;
                this.balance = 0;
            } else {
                this.balance -= totalDividend - this.shareholderReservedBalance;
            }
            this.shareholderReservedBalance = 0;
        } else {
            this.shareholderReservedBalance -= totalDividend;
        }

        double dividendPerShare = totalDividend / totalSharesOutstanding;

        for (Share share : shares) {
            double payout = share.getQuantity() * dividendPerShare;
            OfflinePlayer player = ZetaCorporations.getInstance().getServer().getOfflinePlayer(share.getPortfolio().getOwner());
            ZetaCorporations.getEconomy().depositPlayer(player, payout);

            if (player.isOnline() && player.getPlayer() != null) {
                player.getPlayer().sendMessage(ChatColor.GREEN + "You have received a dividend of " + ChatColor.GOLD + "$" + ZetaCorporations.DEFAULT_FORMAT.format(payout) + ChatColor.GREEN +
                        " per share for your " + ChatColor.GOLD + share.getQuantity() + ChatColor.GREEN + " shares in " + ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', this.name) + ChatColor.GREEN + "!");
            }
        }

        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void setName(String name) {
        this.name = name;
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void setDescription(String description) {
        this.description = description;
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void issueShares(long sharesIssued, double value) {
        this.totalSharesIssued += sharesIssued;
        Order order = new Order(this, sharesIssued, new OrderPriceConfiguration(
                OrderPriceConfiguration.OrderType.LIMIT,
                value,
                OrderPriceConfiguration.TransactionType.SHARE_ISSUE
        ), null);
        openSellOrders.add(order);
        ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public long getSharesIssued() {
        return totalSharesIssued;
    }

    public double getShareholderReservedBalance() {
        return shareholderReservedBalance;
    }

    public void setShareholderReservedBalance(double shareHolderReservedBalance) {
        this.shareholderReservedBalance = shareHolderReservedBalance;
        if (shouldSave) ZetaCorporations.getInstance().getCompanyData().saveCompany(this);
    }

    public void broadcastMessage(String message) {
        for (UUID owner : owners) {
            Player player = ZetaCorporations.getInstance().getServer().getPlayer(owner);
            if (player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }

    public long getSharesOutstanding() {
        return this.sharesOutstanding;
    }

    public void updateSharesOutstanding() {
        // Update the number of shares that are currently in circulation
        long totalSharesOutstanding = 0;
        List<Portfolio> shareholderPortfolios = ZetaCorporations.getInstance().getPortfolioData().getShareholders(this);
        for (Portfolio portfolio : shareholderPortfolios) {
            Share share = portfolio.getPositions().get(this.ticker);
            totalSharesOutstanding += share.getQuantity();
        }
        this.sharesOutstanding = totalSharesOutstanding;
    }

    public double getMarketCap() {
        return this.sharesOutstanding * this.lastPrice;
    }
}
