package com.github.yeetmanlord.zetacorporations.menu.company;

import com.github.yeetmanlord.reflection_api.util.VersionMaterial;
import com.github.yeetmanlord.zeta_core.api.util.input.IChatInputable;
import com.github.yeetmanlord.zeta_core.api.util.input.InputType;
import com.github.yeetmanlord.zeta_core.api.util.input.PlayerUtil;
import com.github.yeetmanlord.zeta_core.menus.AbstractGUIMenu;
import com.github.yeetmanlord.zeta_core.menus.animation.AnimatedItem;
import com.github.yeetmanlord.zeta_core.menus.animation.BuiltinAnimation;
import com.github.yeetmanlord.zeta_core.menus.animation.IAnimatable;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import com.github.yeetmanlord.zetacorporations.api.Company;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class CompanyConfigMenu extends AbstractGUIMenu implements IChatInputable, IAnimatable {

    private List<AnimatedItem> animatedItems = new ArrayList<>();

    private String companyName = "";

    public CompanyConfigMenu(PlayerUtil helper) {
        super(helper, "&6Corporations", 27, false);
    }

    @Override
    public void setItems() {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = playerHead.getItemMeta();
        if (meta instanceof SkullMeta sMeta && !sMeta.hasOwner()) {
            sMeta.setOwningPlayer(this.menuUtil.getOwner());
            playerHead.setItemMeta(sMeta);
        }
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(11, this.makeItemFromExisting(playerHead, "&aMy Companies", "",
                "&7Click to view your companies."), this), "My Companies", 2, "&a", "&a&l"));
        this.animatedItems.add(BuiltinAnimation.scrollingFormatItem(new AnimatedItem(15, this.makeItemFromExisting(VersionMaterial.LIME_WOOL.getItem(), "&aCreate Company", "",
                "&7Click to create a company."), this), "Create Company", 2, "&a", "&a&l"));


        createCloser();
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        int closeSlot = this.slots - 5;
        switch (e.getSlot()) {
            case 11 -> new MyCompaniesMenu(this.menuUtil, this).open();
            case 15 -> this.setInput(InputType.STRING, "&aEnter the name of your company.");
            default -> {
                if (e.getSlot() == closeSlot) {
                    this.close();
                }
            }
        }
    }

    @Override
    public void processChatInput(InputType type, AsyncPlayerChatEvent event) {
        switch (type) {
            case STRING -> {
                companyName = event.getMessage().trim();
                this.setInput(InputType.STRING1, "&aEnter the ticker symbol of your company.", "&7It must be 5 characters or less and contain only letters and numbers.", "&cLeft click to cancel.");
            }
            case STRING1 -> {
                String ticker = event.getMessage().trim().toUpperCase();
                if (validTickerSymbol(ticker)) {
                    if (ZetaCorporations.getInstance().getCompanyData().companies.containsKey(ticker)) {
                        this.menuUtil.getOwner().sendMessage(ChatColor.RED + "A company with that ticker symbol already exists.");
                        return;
                    }
                    Company co = new Company(0D, companyName, ticker, "",
                            Lists.newArrayList(this.menuUtil.getOwner().getUniqueId()), this.menuUtil.getOwner().getUniqueId(), 0, new ArrayList<>(), new ArrayList<>(), 0);
                    ZetaCorporations.getInstance().getCompanyData().saveCompany(co);
                } else {
                    this.menuUtil.getOwner().sendMessage(ChatColor.RED + "Invalid ticker symbol. I'm pretty sure I already told you the rules.");
                }
            }
        }
    }

    public static boolean validTickerSymbol(String string) {
        return string.matches("[a-zA-Z0-9 ]{1,5}");
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
