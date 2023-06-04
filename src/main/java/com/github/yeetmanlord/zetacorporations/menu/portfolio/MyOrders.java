package com.github.yeetmanlord.zetacorporations.menu.portfolio;

import com.github.yeetmanlord.zeta_core.api.util.PluginUtilities;
import com.github.yeetmanlord.zeta_core.api.util.input.PlayerUtil;
import com.github.yeetmanlord.zeta_core.menus.AbstractGUIMenu;
import com.github.yeetmanlord.zeta_core.menus.AbstractPaginatedMenu;
import com.github.yeetmanlord.zetacorporations.api.Order;
import com.github.yeetmanlord.zetacorporations.api.Portfolio;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class MyOrders extends AbstractPaginatedMenu<Order> {

    private Portfolio portfolio;

    public MyOrders(PlayerUtil helper, AbstractGUIMenu parent, Portfolio portfolio) {
        super(helper, "&1My Orders", 54, 45, parent);
        this.portfolio = portfolio;
    }

    @Override
    public void renderPage() {
        this.items = portfolio.getOrders();
        for (int i = getItemsPerPage() * getPage(); i < getItemsPerPage() * (getPage() + 1); i++) {
            if (i >= this.items.size()) break;
            int slot = i - (getItemsPerPage() * getPage());
            Order order = this.items.get(i);
            this.inv.setItem(slot, this.makeItem(Material.PAPER, order.getCompany().getName(), "",
                    "&7Ticker: &7" + order.getCompany().getTicker(), "", "&e&lOrder Info:",
                    "&7Quantity: &e" + order.getQuantity(), "&7Type: &e" + PluginUtilities.titleCase(order.getOrderConfig().getOrderType().name()),
                    "&7Transaction: &e" + PluginUtilities.titleCase(order.getOrderConfig().getTransactionType().name()), "", "&c&lShift click to cancel this order."));
            this.slotToIndex.put(slot, i);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        super.handleClick(e);

        if (e.getSlot() < this.getItemsPerPage() && e.getCurrentItem() != null) {
            Order order = this.items.get(slotToIndex.get(e.getSlot()));
            if (e.isShiftClick()) {
                order.cancel();
                this.refresh();
            }
        }
    }
}
