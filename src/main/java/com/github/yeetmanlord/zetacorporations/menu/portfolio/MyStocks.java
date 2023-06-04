package com.github.yeetmanlord.zetacorporations.menu.portfolio;

import com.github.yeetmanlord.zeta_core.api.util.input.IChatInputable;
import com.github.yeetmanlord.zeta_core.api.util.input.InputType;
import com.github.yeetmanlord.zeta_core.api.util.input.PlayerUtil;
import com.github.yeetmanlord.zeta_core.menus.AbstractGUIMenu;
import com.github.yeetmanlord.zeta_core.menus.AbstractPaginatedMenu;
import com.github.yeetmanlord.zeta_core.menus.animation.AnimatedItem;
import com.github.yeetmanlord.zeta_core.menus.animation.BuiltinAnimation;
import com.github.yeetmanlord.zeta_core.menus.animation.IAnimatable;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import com.github.yeetmanlord.zetacorporations.api.OrderPriceConfiguration;
import com.github.yeetmanlord.zetacorporations.api.Portfolio;
import com.github.yeetmanlord.zetacorporations.api.Share;
import com.github.yeetmanlord.zetacorporations.util.InputParser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class MyStocks extends AbstractPaginatedMenu<Share> {

    private final Portfolio portfolio;

    public MyStocks(PlayerUtil helper, AbstractGUIMenu parent, Portfolio portfolio) {
        super(helper, "&9My Stocks", 54, 45, parent);
        this.portfolio = portfolio;
    }

    @Override
    public void renderPage() {
        this.items = new ArrayList<>(portfolio.getPositions().values());
        for (int i = getItemsPerPage() * getPage(); i < getItemsPerPage() * (getPage() + 1); i++) {
            if (i >= this.items.size()) break;
            int slot = i - (getItemsPerPage() * getPage());
            Share share = this.items.get(i);
            this.inv.setItem(slot, this.makeItemFromExisting(share.getCompany().getIcon(), share.getCompany().getName(), "",
                    "&7Ticker: &7" + share.getCompany().getTicker(), "", "&e&lShare Info:",
                    "&7Quantity: &e" + share.getQuantity(), "&7Value: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(share.getValue()),
                    "&7Average Buy Price: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(share.getAveragePrice()), "", "&7Click to configure sell order."));
            this.slotToIndex.put(slot, i);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        super.handleClick(e);

        if (e.getSlot() < this.getItemsPerPage() && e.getCurrentItem() != null) {
            Share share = this.items.get(slotToIndex.get(e.getSlot()));
            new OrderMenu(this.menuUtil, this, share).open();
        }
    }

    class OrderMenu extends AbstractGUIMenu implements IChatInputable, IAnimatable {

        private final List<AnimatedItem> animatedItems = new ArrayList<>();
        private final Share share;

        private long quantity = 0;
        private OrderPriceConfiguration.OrderType orderType = OrderPriceConfiguration.OrderType.MARKET;
        private double price = 0;

        public OrderMenu(PlayerUtil helper, AbstractGUIMenu parent, Share share) {
            super(helper, "&9Sell &6" + share.getCompany().getTicker(), 54, parent);
            this.share = share;
        }

        @Override
        public int getAnimationInterval() {
            return 8;
        }

        @Override
        public List<AnimatedItem> getAnimatedItems() {
            return this.animatedItems;
        }

        @Override
        public void setItems() {
            if (orderType == OrderPriceConfiguration.OrderType.MARKET) {
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(21, this.makeItem(Material.GOLD_INGOT, "&6Order Type",
                        "", "&e&lMarket", "&8Limit", "&7Click to change to a limit order."), this), "Order Type", 2, "&6", "&6&l"));
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(23, this.makeItem(Material.HOPPER,
                        "&6Quantity", "&7Click to change the quantity of shares you want to sell.",
                        "&7Current: &e" + ZetaCorporations.INT_FORMAT.format(quantity),
                        "&7Maximum: &e" + ZetaCorporations.INT_FORMAT.format(share.getQuantity())), this), "Quantity", 2, "&6", "&6&l"));
            } else {
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(20, this.makeItem(Material.GOLD_INGOT, "&6Order Type",
                        "", "&8Market", "&e&lLimit", "&7Click to change to a limit order."), this), "Order Type", 2, "&6", "&6&l"));
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(22, this.makeItem(Material.HOPPER, "&6Quantity",
                        "&7Click to change the quantity of shares you want to sell.", "&7Current: &e" + ZetaCorporations.INT_FORMAT.format(quantity),
                        "&7Maximum: &e" + ZetaCorporations.INT_FORMAT.format(share.getQuantity())),
                        this), "Quantity", 2, "&6", "&6&l"));
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(24, this.makeItem(Material.EMERALD, "&6Price",
                        "&7Click to change the price per share you want to sell.", "&7Current: &e$" + ZetaCorporations.DEFAULT_FORMAT.format(price)),
                        this), "Price", 2, "&6", "&6&l"));
            }

            createCloser();
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(52, this.makeItem(Material.RED_WOOL, "&4Cancel Order",
                    "&7Click to cancel this order."), this), "Cancel Order", 2, "&4", "&4&l"));
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(53, this.makeItem(Material.LIME_WOOL, "&aConfirm Order",
                    "&7Click to place this order."), this), "Confirm Order", 2, "&a", "&a&l"));
        }

        @Override
        public void handleClick(InventoryClickEvent e) {
            switch (e.getSlot()) {
                case 21, 20 -> {
                    if (orderType == OrderPriceConfiguration.OrderType.MARKET) {
                        orderType = OrderPriceConfiguration.OrderType.LIMIT;
                    } else {
                        orderType = OrderPriceConfiguration.OrderType.MARKET;
                    }
                    this.animatedItems.clear();
                    this.refresh();
                }
                case 23, 22 -> this.setInput(InputType.NUMBER, "&aInput the number of shares to sell.");
                case 24 -> this.setInput(InputType.NUMBER1, "&aInput the price per share.");
                case 52, 49 -> {
                    this.close();
                    this.orderType = OrderPriceConfiguration.OrderType.MARKET;
                    this.quantity = 0;
                    this.price = 0;
                }
                case 53 -> {
                    if (price <= 0 && orderType == OrderPriceConfiguration.OrderType.LIMIT) {
                        menuUtil.getOwner().sendMessage(ChatColor.RED + "You must set a price for a limit order.");
                        break;
                    }
                    if (quantity <= 0) {
                        menuUtil.getOwner().sendMessage(ChatColor.RED + "You must set a quantity greater than 0.");
                        break;
                    }
                    if (this.orderType == OrderPriceConfiguration.OrderType.MARKET) {
                        if (MyStocks.this.portfolio.verifySell(share.getCompany(), quantity)) {
                            portfolio.sellMarket(share.getCompany(), quantity);
                            this.close();
                        } else {
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInsufficient shares to sell &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &cshares of " + share.getCompany().getName()));
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTry cancelling open orders or sell less shares"));
                        }
                    } else {
                        if (MyStocks.this.portfolio.verifySell(share.getCompany(), quantity)) {
                            portfolio.sellLimit(share.getCompany(), quantity, price);
                            this.close();
                        } else {
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInsufficient shares to sell &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &cshares of " + share.getCompany().getName()));
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTry cancelling open orders or sell less shares"));
                        }
                    }
                }
            }
        }


        @Override
        public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
            switch (type) {
                case NUMBER -> {
                    try {
                        long number = InputParser.parseLong(event.getMessage());
                        if (number <= 0) {
                            menuUtil.getOwner().sendMessage(ChatColor.RED + "You must input a positive number.");
                            break;
                        }
                        this.quantity = number;
                        this.animatedItems.clear();
                        this.refresh();
                    } catch (NumberFormatException e) {
                        menuUtil.getOwner().sendMessage(ChatColor.RED + "You must input a number.");
                    }
                }
                case NUMBER1 -> {
                    try {
                        double number = InputParser.parseMoney(event.getMessage());
                        if (number <= 0) {
                            menuUtil.getOwner().sendMessage(ChatColor.RED + "You must input a positive number.");
                            break;
                        }
                        this.price = number;
                        this.animatedItems.clear();
                        this.refresh();
                    } catch (NumberFormatException e) {
                        menuUtil.getOwner().sendMessage(ChatColor.RED + "You must input a number.");
                    }
                }
            }
        }
    }
}
