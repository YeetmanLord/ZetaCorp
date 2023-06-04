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
import com.github.yeetmanlord.zetacorporations.api.Company;
import com.github.yeetmanlord.zetacorporations.api.OrderPriceConfiguration;
import com.github.yeetmanlord.zetacorporations.api.Portfolio;
import com.github.yeetmanlord.zetacorporations.util.InputParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StockScreener extends AbstractPaginatedMenu<Company> implements IChatInputable,IAnimatable {

    private final List<AnimatedItem> animatedItems = new ArrayList<>();

    private String searchTerm = "";
    protected Portfolio portfolio;

    public StockScreener(PlayerUtil helper, AbstractGUIMenu parent, Portfolio portfolio) {
        super(helper, "&3Company Research", 54, 45, parent);
        this.portfolio = portfolio;
    }

    @Override
    public void renderPage() {
        this.items = ZetaCorporations.getInstance().getCompanyData().searchCompanies(searchTerm);
        for (int i = getItemsPerPage() * getPage(); i < getItemsPerPage() * (getPage() + 1); i++) {
            if (i >= this.items.size()) break;
            int slot = i - (getItemsPerPage() * getPage());
            Company company = this.items.get(i);
            this.slotToIndex.put(slot, i);
            this.inv.setItem(slot, this.makeItemFromExisting(company.getIcon(), company.getName(), "",
                    "&7Ticker: &7" + company.getTicker(), "",
                    "&e&lCompany Info:",
                    "&7Description: &e" + company.getDescription(),
                    "&7CEO: &e" + Bukkit.getOfflinePlayer(company.getCeo()).getName(),
                    "&7Company Balance: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(company.getBalance() + company.getShareholderReservedBalance()),
                    "&6Left click to view more info and to place an order."));
        }
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(48, this.makeItem(Material.OAK_SIGN, "&eSearch",
                "&7Search for a company by name or ticker.", "&7Current Search: &e" + (searchTerm.equals("") ? "&cNone" : searchTerm)), this),
                "Search", 2, "&e", "&e&l"));
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        super.handleClick(e);
        if (e.getSlot() == 48) {
            this.setInput(InputType.STRING, "&6Enter company name or ticker");
        }

        if (e.getSlot() < 45 && e.getCurrentItem() != null) {
            Company company = this.items.get(this.slotToIndex.get(e.getSlot()));
            new CompanyInfo(this.menuUtil, this, company).open();
        }
    }

    @Override
    public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
        if (type == InputType.STRING) {
            this.searchTerm = event.getMessage();
            this.animatedItems.clear();
            this.refresh();
        }
    }


    @Override
    public int getAnimationInterval() {
        return 8;
    }

    @Override
    public List<AnimatedItem> getAnimatedItems() {
        return this.animatedItems;
    }
    class CompanyInfo extends AbstractGUIMenu implements IAnimatable {

        private final List<AnimatedItem> animatedItems = new ArrayList<>();
        private final Company company;

        public CompanyInfo(PlayerUtil helper, AbstractGUIMenu parent, Company company) {
            super(helper, "&3Company Info", 54, parent);
            this.company = company;
        }

        @Override
        public void setItems() {
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(20, this.makeItem(Material.BOOK, "&6Stock Stats",
                    "&7Ask Price: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(company.getAskPrice()),
                    "&7Bid Price: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(company.getBidPrice()),
                    "&7Last Purchase Price: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(company.getLastPrice()),
                    "&7Market Cap: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(company.getMarketCap())), this),
                    "Stock Stats", 2, "&6", "&6&l"));

            ItemStack ceoHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = ceoHead.getItemMeta();
            OfflinePlayer player = Bukkit.getOfflinePlayer(company.getCeo());
            if (meta instanceof SkullMeta skullMeta && !skullMeta.hasOwner()) {
                skullMeta.setOwningPlayer(player);
                ceoHead.setItemMeta(skullMeta);
            }

            if (!player.isOnline()) {
                this.inv.setItem(22, this.makeItemFromExisting(ceoHead, "&6CEO and Owners", "&7Click to view the CEO and owners of this company."));
            } else {
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(22, this.makeItemFromExisting(ceoHead, "&6CEO and Owners",
                        "&7Click to view the CEO and owners of this company."), this), "CEO and Owners", 2, "&6", "&6&l"));
            }

            String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3MDBhMDEwNDJlMWJhOTg5OGUwYzQ2M2E2ODkyNzdhZjk3ODViODg3Nzg3MDEwZDM4MzRkY2I5NTJkN2E1ODgifX19";
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(24, this.makeSkullWithCustomTexture("&aTrading Statistics", new String[]{
                    "&7Open Buy Orders: &e" + ZetaCorporations.INT_FORMAT.format(company.getOpenBuyOrders().size()),
                    "&7Open Sell Orders: &e" + ZetaCorporations.INT_FORMAT.format(company.getOpenSellOrders().size()),
                    "&7Mid Price: &e$&6" + ZetaCorporations.DEFAULT_FORMAT.format(company.getMidPrice()),
                    "&7Outstanding Shares: &e" + ZetaCorporations.INT_FORMAT.format(company.getSharesOutstanding()),
                    "&7Unbought Issued Shares: &e" + ZetaCorporations.INT_FORMAT.format(company.getSharesIssued() - company.getSharesOutstanding()),
                    "&7Issued Shares: &e" + ZetaCorporations.INT_FORMAT.format(company.getSharesIssued())
            }, texture), this), "Trading Statistics", 2, "&a", "&a&l"));

            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(48, this.makeItem(Material.BARREL, "&6Place Order",
                    "&7Click to place an order to buy shares for this company."), this), "Place Order", 2, "&6", "&6&l"));
            createCloser();
        }

        @Override
        public void handleClick(InventoryClickEvent e) {
            switch (e.getSlot()) {
                case 22 -> new CompanyOwners(this.menuUtil, this, company).open();
                case 48 -> new CompanyOrder(this.menuUtil, this, company).open();
                case 49 -> this.close();
            }
        }

        @Override
        public int getAnimationInterval() {
            return 8;
        }

        @Override
        public List<AnimatedItem> getAnimatedItems() {
            return this.animatedItems;
        }
    }

    class CompanyOrder extends AbstractGUIMenu implements IChatInputable, IAnimatable {

        private final List<AnimatedItem> animatedItems = new ArrayList<>();
        private final Company company;

        private long quantity = 0;
        private OrderPriceConfiguration.OrderType orderType = OrderPriceConfiguration.OrderType.MARKET;
        private double price = 0;

        public CompanyOrder(PlayerUtil helper, AbstractGUIMenu parent, Company company) {
            super(helper, "&9Buy &6" + company.getTicker(), 54, parent);
            this.company = company;
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
            this.animatedItems.clear();
            if (orderType == OrderPriceConfiguration.OrderType.MARKET) {
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(21, this.makeItem(Material.GOLD_INGOT, "&6Order Type",
                        "", "&e&lMarket", "&8Limit", "&7Click to change to a limit order."), this), "Order Type", 2, "&6", "&6&l"));
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(23, this.makeItem(Material.HOPPER,
                        "&6Quantity", "&7Click to change the quantity of shares you want to buy.",
                        "&7Current: &e" + ZetaCorporations.INT_FORMAT.format(quantity)), this), "Quantity", 2, "&6", "&6&l"));
            } else {
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(20, this.makeItem(Material.GOLD_INGOT, "&6Order Type",
                        "", "&8Market", "&e&lLimit", "&7Click to change to a limit order."), this), "Order Type", 2, "&6", "&6&l"));
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(22, this.makeItem(Material.HOPPER, "&6Quantity",
                        "&7Click to change the quantity of shares you want to buy.", "&7Current: &e" + ZetaCorporations.INT_FORMAT.format(quantity)),
                        this), "Quantity", 2, "&6", "&6&l"));
                this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(24, this.makeItem(Material.EMERALD, "&6Price",
                        "&7Click to change the price per share you want to buy.", "&7Current: &e$" + ZetaCorporations.DEFAULT_FORMAT.format(price)),
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
                case 23, 22 -> this.setInput(InputType.NUMBER, "&aInput the number of shares to buy.");
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
                        if (StockScreener.this.portfolio.verifyBuy(company, quantity, company.getAskPrice() * (1 + ZetaCorporations.getInstance().getGeneralConfig().getMarketOrderMargin()))) { // TODO: marketPriceMargin
                            portfolio.buyMarket(company, quantity);
                            this.close();
                        } else {
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInsufficient funds to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &cshares of " + company.getName()));
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTry cancelling open orders or buy less shares"));
                        }
                    } else {
                        if (StockScreener.this.portfolio.verifyBuy(company, quantity, price)) {
                            portfolio.buyLimit(company, quantity, price);
                            this.close();
                        } else {
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInsufficient funds to buy &e" + ZetaCorporations.INT_FORMAT.format(quantity) + " &cshares of " + company.getName()));
                            menuUtil.getOwner().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTry cancelling open orders or buy less shares"));
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

    static class CompanyOwners extends AbstractPaginatedMenu<UUID> implements IAnimatable {

        private List<AnimatedItem> animatedItems = new ArrayList<>();

        private Company company;

        CompanyOwners(PlayerUtil helper, AbstractGUIMenu parent, Company company) {
            super(helper, "&6Company Owners", 54, 45, parent);
            this.company = company;
        }

        @Override
        public void renderPage() {
            this.items = company.getOwners();
            for (int i = getItemsPerPage() * getPage(); i < getItemsPerPage() * (getPage() + 1); i++) {
                if (i >= items.size()) break;
                int slot = i - (getItemsPerPage() * getPage());
                UUID owner = items.get(i);
                OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null && !meta.hasOwner()) {
                    meta.setOwningPlayer(player);
                }
                head.setItemMeta(meta);
                if (player.isOnline()) {
                    if (owner.equals(company.getCeo())) {
                        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(slot, this.makeItemFromExisting(head, "&6" + player.getName(), "",
                                "&7This is the &6CEO&7 of the company."), this), Objects.requireNonNull(player.getName()), 2, "&6", "&6&l"));
                        continue;
                    }
                    this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(slot, this.makeItemFromExisting(head, "&a" + player.getName(), "",
                            "&7This player is a partial owner of the company."), this), Objects.requireNonNull(player.getName()), 2, "&a", "&a&l"));
                } else {
                    // Non-animated as that causes issues for whatever reason
                    if (owner.equals(company.getCeo())) {
                        this.inv.setItem(slot, this.makeItemFromExisting(head, "&6" + player.getName(), "",
                                "&7This is the &6CEO&7 of the company."));
                        continue;
                    }
                    this.inv.setItem(slot, this.makeItemFromExisting(head, "&7" + player.getName(), "",
                            "&7This player is a partial owner of the company."));
                }
                this.slotToIndex.put(slot, i);
            }
        }

        @Override
        public void handleClick(InventoryClickEvent e) {
            super.handleClick(e);
        }

        @Override
        public int getAnimationInterval() {
            return 8;
        }

        @Override
        public List<AnimatedItem> getAnimatedItems() {
            return animatedItems;
        }
    }
}
