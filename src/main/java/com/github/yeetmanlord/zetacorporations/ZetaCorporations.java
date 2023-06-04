package com.github.yeetmanlord.zetacorporations;

import com.github.yeetmanlord.reflection_api.Version;
import com.github.yeetmanlord.reflection_api.exceptions.VersionFormatException;
import com.github.yeetmanlord.zeta_core.ZetaCore;
import com.github.yeetmanlord.zeta_core.ZetaPlugin;
import com.github.yeetmanlord.zeta_core.api.util.ItemUtils;
import com.github.yeetmanlord.zetacorporations.api.Company;
import com.github.yeetmanlord.zetacorporations.commands.TradingGuide;
import com.github.yeetmanlord.zetacorporations.data.CompanyDataStorer;
import com.github.yeetmanlord.zetacorporations.data.GeneralConfig;
import com.github.yeetmanlord.zetacorporations.data.PortfolioDataStorer;
import com.github.yeetmanlord.zetacorporations.menu.ConfigMenu;
import com.github.yeetmanlord.zetacorporations.menu.company.CompanyConfigMenu;
import com.github.yeetmanlord.zetacorporations.menu.portfolio.MyPortfolio;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;

public final class ZetaCorporations extends ZetaPlugin {

    private static final Version MIN_ZETA_CORE_VERSION = new Version(2, 0);

    private static Economy econ = null;

    private static ZetaCorporations instance;

    private OrderManager orderManager;

    private CompanyDataStorer companyDataStorer;
    private PortfolioDataStorer portfolioDataStorer;

    private GeneralConfig generalConfig;

    private boolean dataRead = false;

    public static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("###,###.##");
    public static final DecimalFormat INT_FORMAT = new DecimalFormat("###,###");

    @Override
    public void onEnable() {

        try {
            Version zetaCoreVersion = new Version(ZetaCore.getInstance().getDescription().getVersion());
            if (zetaCoreVersion.isOlder(MIN_ZETA_CORE_VERSION)) {
                getLogger().severe("ZetaCore version " + zetaCoreVersion + " is too old. Please update to version " + MIN_ZETA_CORE_VERSION + " or newer.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } catch(VersionFormatException exc){
            getLogger().severe("ZetaCore version is not in correct format. Please download a valid plugin or nag the developer to fix it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;
        this.getPluginLogger().setColor(ChatColor.GOLD);

        orderManager = new OrderManager();
        getPluginLogger().info("OrderManager initialized. Loading data...");

        super.onEnable();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public String getPluginName() {
        return "ZetaCorp";
    }

    @Override
    public void onDataReadFinish() {
        getPluginLogger().info("Data loaded. Plugin enabled and ready to use.");
        dataRead = true;

        getPluginLogger().debug("Starting order execution tasks...");
        Bukkit.getScheduler().runTaskTimer(this, orderManager.orderExecutionTask, 0, 20);

        for (Company company : companyDataStorer.companies.values()) {
            company.updateSharesOutstanding();
        }

        getCommand("corporation").setExecutor((sender, command, label, args) ->  {
            if (sender instanceof Player p) {
                new CompanyConfigMenu(ZetaCore.getInstance().getPlayerMenuUtility(p)).open();
            }
            return true;
        });

        getCommand("portfolio").setExecutor((sender, command, label, args) ->  {
            if (sender instanceof Player p) {
                new MyPortfolio(ZetaCore.getInstance().getPlayerMenuUtility(p)).open();
            }
            return true;
        });

        getCommand("trading_guide").setExecutor(new TradingGuide(this));
        getCommand("zeta_corporations").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player p) {
                new ConfigMenu(ZetaCore.getInstance().getPlayerMenuUtility(p)).open();
            }
            return true;
        });
    }

    @Override
    public boolean initializedFinished() {
        return dataRead;
    }

    @Override
    protected void registerDataStorers() {
        companyDataStorer = new CompanyDataStorer(this);
        companyDataStorer.setup();

        portfolioDataStorer = new PortfolioDataStorer(this);
        portfolioDataStorer.setup();

        generalConfig = new GeneralConfig(this);
        generalConfig.setup();
    }

    @Override
    public ItemStack getIcon() {
        return ItemUtils.makeItem(Material.GOLD_BLOCK, "&6ZetaCorp", "&7Manage your company and invest in others!");
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static ZetaCorporations getInstance() {
        return instance;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    public CompanyDataStorer getCompanyData() {
        return companyDataStorer;
    }

    public PortfolioDataStorer getPortfolioData() {
        return portfolioDataStorer;
    }

    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }
}
