package com.github.yeetmanlord.zetacorporations.menu;

import com.github.yeetmanlord.zeta_core.api.util.input.IChatInputable;
import com.github.yeetmanlord.zeta_core.api.util.input.InputType;
import com.github.yeetmanlord.zeta_core.api.util.input.PlayerUtil;
import com.github.yeetmanlord.zeta_core.menus.AbstractGUIMenu;
import com.github.yeetmanlord.zeta_core.menus.animation.AnimatedItem;
import com.github.yeetmanlord.zeta_core.menus.animation.BuiltinAnimation;
import com.github.yeetmanlord.zeta_core.menus.animation.IAnimatable;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import com.github.yeetmanlord.zetacorporations.util.InputParser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

public class ConfigMenu extends AbstractGUIMenu implements IChatInputable, IAnimatable {

    private final List<AnimatedItem> animatedItems = new ArrayList<>();

    public ConfigMenu(PlayerUtil helper) {
        super(helper, "&0Zeta Corporations Config", 54);
    }

    @Override
    public void setItems() {
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(19, this.makeItem(Material.GOLD_NUGGET, "&aMarket Order Margin", "", "&7Set how much extra money a player must have",
                "&7to place a market order.",
                "&7This ensures that a user can execute the market",
                "&7order while taking into account the possible price fluctuation.",
                "&7Current: &e" + ZetaCorporations.DEFAULT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMarketOrderMargin() * 100) + "%",
                "&7Default: &e10%"), this), "Market Order Margin", 2, "&a", "&a&l"));

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(22, this.makeItem(Material.WRITABLE_BOOK, "&6Max Shares Issued",
                "", "&7Determines the maximum amount of shares a company can issue.",
                "&7Current: &e" + ZetaCorporations.INT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssued()),
                "&7Default: &e2,500,000"), this), "Max Shares Issued", 2, "&6", "&6&l"));

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(25, this.makeItem(Material.PAPER, "&eMax Shares Issued At Once",
                "", "&7Limits the maximum amount of shares a company can issue at once.",
                "&7Current: &e" + ZetaCorporations.INT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxSharesIssuedAtOnce()),
                "&7Default: &e1,000,000"), this), "Max Shares Issued At Once", 2, "&e", "&e&l"));

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(29, this.makeItem(Material.EMERALD, "&2Max Share Issue Price",
                "", "&7Limits the maximum price a company can issue shares at.",
                "&7Current: &e$" + ZetaCorporations.DEFAULT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getMaxShareIssuePrice()),
                "&7Default: &e$1,000.00"), this), "Max Share Issue Price", 2, "&2", "&2&l"));


        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(31, this.makeItem(Material.IRON_BARS, "&9Shareholder Reserve Percentage",
                "", "&7Determines the percentage of investments that must be reserved for shareholders.",
                "&7This is to ensure that shareholders can be paid out if the company is liquidated (AKA deleted).",
                "&7Current: &e" + ZetaCorporations.DEFAULT_FORMAT.format(ZetaCorporations.getInstance().getGeneralConfig().getShareholderReservePercent() * 100) + "%",
                "&7Default: &e50%"), this), "Shareholder Reserve Percentage", 2, "&9", "&9&l"));


        if (ZetaCorporations.getInstance().getGeneralConfig().doesReservePortionOfDeposit()) {
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(33, this.makeItem(Material.LIME_DYE, "&aReserve Portion of Deposit",
                    "", "&7Determines whether a portion of the deposit is reserved for shareholders.",
                    "&7This is to ensure that shareholders can be paid out if the company is liquidated (AKA deleted).",
                    "&7Current: &aEnabled",
                    "&7Default: &aEnabled"), this), "Reserve Portion of Deposit", 2, "&a", "&a&l"));
        } else {
            this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(33, this.makeItem(Material.RED_DYE, "&cReserve Portion of Deposit",
                    "", "&7Determines whether a portion of the deposit is reserved for shareholders.",
                    "&7This is to ensure that shareholders can be paid out if the company is liquidated (AKA deleted).",
                    "&7Current: &cDisabled",
                    "&7Default: &aEnabled"), this), "Reserve Portion of Deposit", 2, "&c", "&c&l"));
        }

        createCloser();

    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        switch (e.getSlot()) {
            case 19 -> this.setInput(InputType.NUMBER, "&aEnter the new value");
            case 22 -> this.setInput(InputType.NUMBER1, "&aEnter the new value");
            case 25 -> this.setInput(InputType.NUMBER2, "&aEnter the new value");
            case 29 -> this.setInput(InputType.NUMBER3, "&aEnter the new value");
            case 31 -> this.setInput(InputType.NUMBER4, "&aEnter the new value");
            case 33 -> {
                ZetaCorporations.getInstance().getGeneralConfig().setReservePortionOfDeposit(!ZetaCorporations.getInstance().getGeneralConfig().doesReservePortionOfDeposit());
                this.animatedItems.clear();
                this.refresh();
            }
            case 49 -> this.close();
        }
    }

    @Override
    public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
        switch (type) {
            case NUMBER -> {
                try {
                    double value = InputParser.parsePercent(event.getMessage());
                    ZetaCorporations.getInstance().getGeneralConfig().setMarketOrderMargin(value);
                    this.animatedItems.clear();
                    this.refresh();

                } catch (NumberFormatException e) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number!");
                }
            }
            case NUMBER1 -> {
                try {
                    long value = InputParser.parseLong(event.getMessage());
                    ZetaCorporations.getInstance().getGeneralConfig().setMaxSharesIssued(value);
                    this.animatedItems.clear();
                    this.refresh();

                } catch (NumberFormatException e) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number!");
                }
            }
            case NUMBER2 -> {
                try {
                    long value = InputParser.parseLong(event.getMessage());
                    ZetaCorporations.getInstance().getGeneralConfig().setMaxSharesIssuedAtOnce(value);
                    this.animatedItems.clear();
                    this.refresh();

                } catch (NumberFormatException e) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number!");
                }
            }
            case NUMBER3 -> {
                try {
                    double value = InputParser.parseMoney(event.getMessage());
                    ZetaCorporations.getInstance().getGeneralConfig().setMaxShareIssuePrice(value);
                    this.animatedItems.clear();
                    this.refresh();

                } catch (NumberFormatException e) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number!");
                }
            }
            case NUMBER4 -> {
                try {
                    double value = InputParser.parsePercent(event.getMessage());
                    ZetaCorporations.getInstance().getGeneralConfig().setShareholderReservePercent(value);
                    this.animatedItems.clear();
                    this.refresh();

                } catch (NumberFormatException e) {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid number!");
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
