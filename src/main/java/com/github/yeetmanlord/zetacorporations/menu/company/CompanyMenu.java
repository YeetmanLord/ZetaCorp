package com.github.yeetmanlord.zetacorporations.menu.company;

import com.github.yeetmanlord.reflection_api.util.VersionMaterial;
import com.github.yeetmanlord.zeta_core.api.util.CommandUtil;
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
import com.github.yeetmanlord.zetacorporations.util.InputParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CompanyMenu extends AbstractGUIMenu implements IChatInputable, IAnimatable {

    private List<AnimatedItem> animatedItems = new ArrayList<>();

    private final Company company;

    private long sharesIssued = 0;

    public CompanyMenu(PlayerUtil helper, Company company, AbstractGUIMenu parent) {
        super(helper, company.getName(), 54, parent);
        this.company = company;
    }

    @Override
    public void setItems() {
        if (company.getCeo().equals(this.menuUtil.getOwner().getUniqueId())) {
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(48, this.makeItem(Material.RED_WOOL, "&cDelete Company",
                    "", "&7Click to delete your company."), this), "Delete Company", 2, "&c", "&c&l"));
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(20, this.makeItemFromExisting(this.company.getIcon(), "&aChange Your Company Icon", "",
                            "&7Click to change your company icon.", "&7Then type in the name of the item you want to use as your icon."), this),
                    "Change Your Company Icon", 2, "&a", "&a&l"));

            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(24, this.makeItem(Material.PLAYER_HEAD, "&aManage Owners", "",
                    "&7Click to manage your company owners."), this), "Manage Owners", 2, "&a", "&a&l"));
        } else {
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(22, this.makeItemFromExisting(this.company.getIcon(), "&aChange Your Company Icon", "",
                            "&7Click to change your company icon.", "&7Then type in the name of the item you want to use as your icon."), this),
                    "Change Your Company Icon", 2, "&a", "&a&l"));
        }

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(12, this.makeItem(VersionMaterial.OAK_SIGN.getMaterial(), "&aRename Your Company", "",
                "&7Click to rename your company.", "&7Current Name: &7" + this.company.getName()), this), "Rename Your Company", 2, "&a", "&a&l"));
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(14, this.makeItem(VersionMaterial.OAK_SIGN.getMaterial(), "&aChange Your Company Description", "",
                "&7Click to change your company description.", "&7Current Description: &7" + this.company.getDescription()), this), "Change Your Company Description", 2, "&a", "&a&l"));
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(30, this.makeItem(Material.PAPER, "&aIssue Shares", "",
                "&7Click to issue shares.", "&7Note that funds from investors will be", "&7deposited into your company's bank account.",
                "&a" + ZetaCorporations.INT_FORMAT.format(company.getSharesIssued()) + "&c/&e" + ZetaCorporations.INT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssued()) + " &7shares issued."), this), "Issue Shares", 2, "&a", "&a&l"));
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(32, this.makeItem(Material.IRON_BARS, "&aAccess Corporate Account", "",
                "&7Click to access your company's bank account."), this), "Access Corporate Account", 2, "&a", "&a&l"));

        createCloser();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        if (company.getCeo().equals(this.menuUtil.getOwner().getUniqueId())) {
            switch (e.getSlot()) {
                case 48 -> {
                    this.close();
                    this.company.delete();
                }
                case 20 ->
                        this.setInput(InputType.STRING, "&aEnter the name of the item you want to use as your icon.");
                case 24 -> new ManageOwners(this.menuUtil, this).open();
            }
        } else {
            if (e.getSlot() == 22) {
                this.setInput(InputType.STRING, "&aEnter the name of the item you want to use as your icon.");
            }
        }

        switch (e.getSlot()) {
            case 12 -> this.setInput(InputType.STRING1, "&aEnter the new name of your company.");
            case 14 -> this.setInput(InputType.STRING2, "&aEnter the new description of your company.");
            case 30 -> {
                if (company.getSharesIssued() > ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssued()) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "You have already issued the maximum amount of shares.");
                    return;
                }
                this.setInput(InputType.NUMBER, "&aEnter the amount of shares you want to issue.");
            }
            case 32 -> new CorporateAccount(this.menuUtil, this).open();
            case 49 -> this.close();
        }
    }

    @Override
    public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
        switch (type) {
            case STRING -> {
                try {
                    CommandUtil.Parsed<Material> materialParsed = CommandUtil.parseMaterial(event.getPlayer(), event.getMessage().trim().replace(" ", "_"), "&cInvalid item name.");
                    if (materialParsed.isSuccess()) {
                        this.company.setIcon(new ItemStack(materialParsed.getValue()));
                        this.menuUtil.getOwner().sendMessage(ChatColor.GREEN + "Successfully changed your company icon.");
                    }
                } catch (IllegalArgumentException ignore) {
                }
            }

            case STRING1 -> {
                this.company.setName(event.getMessage().trim());
                this.menuUtil.getOwner().sendMessage(ChatColor.GREEN + "Successfully changed your company name.");
            }

            case STRING2 -> {
                this.company.setDescription(event.getMessage().trim());
                this.menuUtil.getOwner().sendMessage(ChatColor.GREEN + "Successfully changed your company description.");
            }

            case NUMBER -> {
                try {
                    sharesIssued = InputParser.parseLong(event.getMessage().trim());
                    if (sharesIssued > ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssuedAtOnce()) {
                        sharesIssued = ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssuedAtOnce();
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "You can only issue up to " + ZetaCorporations.INT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssuedAtOnce()) +" shares at a time. Changed to " + ZetaCorporations.INT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssuedAtOnce()) +" shares issued.");
                    }
                    if (company.getSharesIssued() + sharesIssued > ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssued()) {
                        sharesIssued = ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssued() - company.getSharesIssued();
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "You can only issue up to " + ZetaCorporations.INT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssued()) + " shares. Changed to " + ZetaCorporations.DEFAULT_FORMAT.format(sharesIssued) + " shares issued.");
                    }
                    this.setInput(InputType.NUMBER1, "&aEnter the price per share.");
                } catch (NumberFormatException ignore) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                }
            }

            case NUMBER1 -> {
                try {
                    double pricePerShare = InputParser.parseMoney(event.getMessage().trim());
                    if (pricePerShare > ZetaCorporations.getInstance().getGeneralConfig().getMaxShareIssuePrice()) {
                        pricePerShare = ZetaCorporations.getInstance().getGeneralConfig().getMaxShareIssuePrice();
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "You can only issue shares at a price of up to $" + ZetaCorporations.DEFAULT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxShareIssuePrice()) + ". Changed to $" + ZetaCorporations.DEFAULT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxShareIssuePrice()) + " per share.");
                    }
                    this.company.issueShares(this.sharesIssued, pricePerShare);
                    this.menuUtil.getOwner().sendMessage(ChatColor.GREEN + "Successfully issued " + ZetaCorporations.DEFAULT_FORMAT.format(this.sharesIssued) + " shares at $" + ZetaCorporations.DEFAULT_FORMAT.format(pricePerShare) + " per share.");
                } catch (NumberFormatException ignore) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                }
            }
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

    class ManageOwners extends AbstractPaginatedMenu<UUID> implements IChatInputable, IAnimatable {

        private List<AnimatedItem> animatedItems = new ArrayList<>();

        ManageOwners(PlayerUtil helper, AbstractGUIMenu parent) {
            super(helper, "&6Manage Owners", 54, 45, parent);
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
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(48, this.makeItem(Material.LIME_WOOL, "&aAdd Owner", "",
                    "&7Click to add an owner to your company."), this), "Add Owner", 2, "&a", "&a&l"));
        }

        @Override
        public void handleClick(InventoryClickEvent e) {
            super.handleClick(e);
            ItemStack item = e.getCurrentItem();
            if (e.getSlot() < this.getItemsPerPage() && item != null) {
                UUID owner = this.items.get(slotToIndex.get(e.getSlot()));
                if (owner.equals(company.getCeo())) return;
                company.removeOwner(owner);
                this.animatedItems.clear();
                this.refresh();
            }

            if (e.getSlot() == 48) {
                this.setInput(InputType.STRING, "&aEnter a player's name.");
            }
        }

        @Override
        public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
            if (type == InputType.STRING) {
                @Deprecated // No other choice since it's player input
                OfflinePlayer player = Bukkit.getOfflinePlayer(event.getMessage());
                if (company.getOwners().contains(player.getUniqueId())) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "That player is already an owner of your company.");
                    return;
                }
                company.addOwner(player.getUniqueId());
                this.menuUtil.getOwner().sendMessage(ChatColor.GREEN + "Successfully added " + player.getName() + " as an owner of your company.");
            }
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

    class CorporateAccount extends AbstractGUIMenu implements IChatInputable, IAnimatable {

        private List<AnimatedItem> animatedItems = new ArrayList<>();

        public CorporateAccount(PlayerUtil helper, AbstractGUIMenu parent) {
            super(helper, "&6Corporate Account", 54, parent);
        }

        @Override
        public void setItems() {
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(11, this.makeItem(Material.PAPER, "&aShareholder Reserved Balance", "",
                    "&7This is the amount of money that", "&7is reserved for shareholders.",
                    "", "&7Amount: &6$" + ZetaCorporations.DEFAULT_FORMAT.format(company.getShareholderReservedBalance())), this), "Shareholder Reserved Balance", 2, "&a", "&a&l"));

            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(15, this.makeItem(Material.ENDER_CHEST, "&aCompany Balance", "",
                    "&7This is the amount of money that", "&7is in your company's bank account.",
                    "", "&7Amount: &6$" + ZetaCorporations.DEFAULT_FORMAT.format(company.getBalance())), this), "Company Balance", 2, "&a", "&a&l"));

            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(30, this.makeItem(Material.DROPPER, "&aWithdraw", "",
                    "&7Click to withdraw money from your", "&7company's bank account."), this), "Withdraw", 2, "&a", "&a&l"));

            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(32, this.makeItem(Material.HOPPER, "&aDeposit", "",
                    "&7Click to deposit money into your", "&7company's bank account."), this), "Deposit", 2, "&a", "&a&l"));

            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(48, this.makeItem(Material.GOLD_INGOT, "&aPay Dividends", "",
                    "&7Click to pay dividends to your", "&7shareholders.", "",
                    "&3Left click for percentage of total balance.", "&3Right click for specific dollar amount.", "", "&cWill pay out dividends using shareholder",
                    "&creserved balance and corporate balance."), this), "Pay Dividends", 2, "&a", "&a&l"));


            createCloser();
        }

        @Override
        public void handleClick(InventoryClickEvent e) {
            switch (e.getSlot()) {
                case 30 -> this.setInput(InputType.NUMBER, "&aEnter the amount you would like to withdraw.");
                case 32 -> this.setInput(InputType.NUMBER1, "&aEnter the amount you would like to deposit.");
                case 48 -> {
                    if (e.getClick() == ClickType.LEFT) {
                        this.setInput(InputType.NUMBER2, "&aEnter the percentage payout.");
                    } else if (e.getClick() == ClickType.RIGHT) {
                        this.setInput(InputType.NUMBER3, "&aEnter the dividend per share ($).");
                    }
                }
                case 49 -> this.close();
            }
        }

        @Override
        public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
            switch (type) {
                case NUMBER -> {
                    try {
                        double parsed = InputParser.parseMoney(event.getMessage());
                        if (parsed == -1) {
                            this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                            return;
                        }
                        if (parsed > company.getBalance()) {
                            this.menuUtil.getOwner().sendMessage(ChatColor.RED + "You do not have enough money in your company's bank account.");
                            return;
                        }
                        company.onWithdraw(parsed, this.menuUtil.getOwner());
                    } catch (NumberFormatException ex) {
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                    }
                }

                case NUMBER1 -> {
                    try {
                        double parsed = InputParser.parseMoney(event.getMessage());
                        if (parsed == -1) {
                            this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                            return;
                        }
                        if (parsed > ZetaCorporations.getEconomy().getBalance(this.menuUtil.getOwner())) {
                            this.menuUtil.getOwner().sendMessage(ChatColor.RED + "You do not have enough money in your personal bank account.");
                            return;
                        }
                        company.onDeposit(parsed, this.menuUtil.getOwner());
                    } catch (NumberFormatException ex) {
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                    }
                }

                case NUMBER2 -> {
                    try {
                        double parsed = InputParser.parsePercent(event.getMessage());
                        if (parsed <= 0 || parsed > 1) {
                            this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                            return;
                        }
                        company.payDividend(parsed);
                        company.broadcastMessage("&a" + this.menuUtil.getOwner().getName() + " has paid out a dividend of " + (parsed * 100) + "% of the book balance to all investors.");
                    } catch (NumberFormatException ex) {
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                    }
                }

                case NUMBER3 -> {
                    try {
                        double parsed = InputParser.parseMoney(event.getMessage());
                        if (parsed == -1) {
                            this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                            return;
                        }
                        company.payDividendCash(parsed);
                        company.broadcastMessage("&a" + this.menuUtil.getOwner().getName() + " has paid out a dividend of $" + ZetaCorporations.DEFAULT_FORMAT.format(parsed) + " per share to all investors.");
                    } catch (NumberFormatException ex) {
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number.");
                    }
                }
            }
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
