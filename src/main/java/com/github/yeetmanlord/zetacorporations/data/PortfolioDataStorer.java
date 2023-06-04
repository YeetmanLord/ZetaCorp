package com.github.yeetmanlord.zetacorporations.data;

import com.github.yeetmanlord.zeta_core.data.DataStorer;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import com.github.yeetmanlord.zetacorporations.api.Company;
import com.github.yeetmanlord.zetacorporations.api.Portfolio;
import com.github.yeetmanlord.zetacorporations.api.Share;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Stores all portfolios.
 * Must be loaded after companies.
 */
public class PortfolioDataStorer extends DataStorer {

    /**
     * Stores all portfolios. Mapped by UUID.
     */
    protected HashMap<UUID, Portfolio> portfolios = new HashMap<>();

    public PortfolioDataStorer(ZetaCorporations instanceIn) {
        super(instanceIn, "portfolios");
    }

    @Override
    public void setDefaults() {
    }

    @Override
    public void read() {
        this.portfolios = new HashMap<>();
        CompanyDataStorer companyData = ((ZetaCorporations) this.instance).getCompanyData();
        this.reload();

        for (String uuidString : this.config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);
            ConfigurationSection section = this.config.getConfigurationSection(uuidString);
            if (section == null) continue;
            Portfolio portfolio = new Portfolio(new HashMap<>(), uuid);
            ConfigurationSection positionsSection = section.getConfigurationSection("positions");
            if (positionsSection != null) {
                for (String tickerSymbol : positionsSection.getKeys(false)) {
                    Company company = companyData.companies.get(tickerSymbol);
                    ConfigurationSection shareSection = positionsSection.getConfigurationSection(tickerSymbol);
                    if (shareSection == null) continue;
                    long quantity = shareSection.getLong("quantity");
                    double averagePrice = shareSection.getDouble("averagePrice");
                    portfolio.addPosition(new Share(quantity, averagePrice, company, portfolio));
                }
            }
            this.portfolios.put(uuid, portfolio);
        }
    }

    @Override
    public void write() {
        for (UUID uuid : portfolios.keySet()) {
            Portfolio portfolio = portfolios.get(uuid);
            this.savePortfolio(portfolio);
        }

        this.save();
    }

    public void savePortfolio(Portfolio portfolio) {
        ConfigurationSection section = this.config.createSection(portfolio.getOwner().toString());
        // Balance is calculated on the fly so don't store it

        ConfigurationSection postionsSection = section.createSection("positions");
        for (Map.Entry<String, Share> shareEntry : portfolio.getPositions().entrySet()) {
            ConfigurationSection shareSection = postionsSection.createSection(shareEntry.getKey());
            shareSection.set("quantity", shareEntry.getValue().getQuantity());
            shareSection.set("averagePrice", shareEntry.getValue().getAveragePrice());
        }

        this.save();
    }

    public List<Portfolio> getShareholders(Company company) {
        List<Portfolio> shareholders = new ArrayList<>();
        for (Portfolio portfolio : portfolios.values()) {
            if (portfolio.getPositions().containsKey(company.getTicker())) {
                shareholders.add(portfolio);
            }
        }

        return shareholders;
    }

    public Portfolio getPortfolio(UUID uniqueId) {
        if (portfolios.containsKey(uniqueId)) {
            return portfolios.get(uniqueId);
        } else {
            Portfolio portfolio = new Portfolio(new HashMap<>(), uniqueId);
            portfolios.put(uniqueId, portfolio);
            this.savePortfolio(portfolio);
            return portfolio;
        }
    }
}
