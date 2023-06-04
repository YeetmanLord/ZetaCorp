package com.github.yeetmanlord.zetacorporations.menu.company;

import com.github.yeetmanlord.zeta_core.api.util.input.PlayerUtil;
import com.github.yeetmanlord.zeta_core.menus.AbstractGUIMenu;
import com.github.yeetmanlord.zeta_core.menus.AbstractPaginatedMenu;
import com.github.yeetmanlord.zeta_core.menus.animation.AnimatedItem;
import com.github.yeetmanlord.zeta_core.menus.animation.BuiltinAnimation;
import com.github.yeetmanlord.zeta_core.menus.animation.IAnimatable;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import com.github.yeetmanlord.zetacorporations.api.Company;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MyCompaniesMenu extends AbstractPaginatedMenu<Company> {

    public MyCompaniesMenu(PlayerUtil helper, AbstractGUIMenu parent) {
        super(helper, "&6My Companies", 54, 45, parent);
    }

    @Override
    public void renderPage() {
        this.items = ZetaCorporations.getInstance().getCompanyData().getCompanies(this.menuUtil.getOwner().getUniqueId());
        for (int i = getItemsPerPage() * getPage(); i < getItemsPerPage() * (getPage() + 1); i++) {
            if (i >= items.size()) break;
            int slot = i - (getItemsPerPage() * getPage());
            Company company = items.get(i);
            this.inv.setItem(slot, this.makeItemFromExisting(company.getIcon(), company.getName(), "",
                    "&7Ticker: &7" + company.getTicker(), "&7" + company.getDescription(), "&7Click to view this company."));
            this.slotToIndex.put(slot, i);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        super.handleClick(e);
        ItemStack item = e.getCurrentItem();
        if (e.getSlot() < this.getItemsPerPage() && item != null) {
            Company company = this.items.get(slotToIndex.get(e.getSlot()));
            new CompanyMenu(this.menuUtil, company, this).open();
        }
    }
}
