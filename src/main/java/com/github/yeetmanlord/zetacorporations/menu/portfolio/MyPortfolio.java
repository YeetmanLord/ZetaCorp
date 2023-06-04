package com.github.yeetmanlord.zetacorporations.menu.portfolio;

import com.github.yeetmanlord.zeta_core.api.util.input.PlayerUtil;
import com.github.yeetmanlord.zeta_core.menus.AbstractGUIMenu;
import com.github.yeetmanlord.zeta_core.menus.animation.AnimatedItem;
import com.github.yeetmanlord.zeta_core.menus.animation.BuiltinAnimation;
import com.github.yeetmanlord.zeta_core.menus.animation.IAnimatable;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import com.github.yeetmanlord.zetacorporations.api.Portfolio;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class MyPortfolio extends AbstractGUIMenu implements IAnimatable {

    List<AnimatedItem> animatedItems = new ArrayList<>();

    private Portfolio portfolio;

    public MyPortfolio(PlayerUtil helper) {
        super(helper, "&0My Portfolio", 54);
        portfolio = ZetaCorporations.getInstance().getPortfolioData().getPortfolio(helper.getOwner().getUniqueId());
    }

    @Override
    public void setItems() {
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(20, this.makeItem(Material.PAPER, "&fMy Orders", "",
                "&7Click to view all of your unfilled orders."), this), "My Orders", 2, "&f", "&f&l"));

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(22, this.makeItem(Material.GOLD_INGOT, "&6My Stocks", "",
                "&7Click to view all of your stocks."), this), "My Stocks", 2, "&6", "&6&l"));

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(24, this.makeItem(Material.SPYGLASS, "&bSearch Companies", "",
                "&7Click to search for companies to buy shares in."), this), "Search Companies", 2, "&b", "&b&l"));

        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(48, this.makeItem(Material.WRITABLE_BOOK, "&ePortfolio Stats", "",
                "&7Current Cash: &a$&6" + ZetaCorporations.DEFAULT_FORMAT.format(portfolio.getCash()),
                "&7Net Worth: &a$&6" + ZetaCorporations.DEFAULT_FORMAT.format(portfolio.getBalance()),
                "&7Total Companies: &a" + portfolio.getPositions().size()), this), "Portfolio Stats", 2, "&e", "&e&l"));


        createCloser();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        switch (e.getSlot()) {
            case 49 -> this.close();
            case 20 -> new MyOrders(this.menuUtil, this, portfolio).open();
            case 22 -> new MyStocks(this.menuUtil, this, portfolio).open();
            case 24 -> new StockScreener(this.menuUtil, this, portfolio).open();
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
