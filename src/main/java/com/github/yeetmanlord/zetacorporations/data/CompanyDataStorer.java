package com.github.yeetmanlord.zetacorporations.data;

import com.github.yeetmanlord.zeta_core.api.util.ItemSerializer;
import com.github.yeetmanlord.zeta_core.data.DataStorer;
import com.github.yeetmanlord.zetacorporations.api.Company;
import com.github.yeetmanlord.zetacorporations.api.Order;
import com.github.yeetmanlord.zetacorporations.api.OrderPriceConfiguration;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CompanyDataStorer extends DataStorer {

    /**
     * Stores all companies. Mapped by ticker symbol.
     */
    public HashMap<String, Company> companies = new HashMap<>();

    /**
     * Used on load to temporarily store orders that need to be loaded into portfolios
     */
    public HashMap<UUID, List<Order>> portfolioOrders = new HashMap<>();

    public CompanyDataStorer(ZetaCorporations instanceIn) {
        super(instanceIn, "companies");
    }

    @Override
    public void setDefaults() {
    }

    @Override
    public void read() {
        this.portfolioOrders = new HashMap<>();
        this.companies = new HashMap<>();
        this.reload();
        for (String tickerSymbol : this.config.getKeys(false)) {
            ConfigurationSection section = this.config.getConfigurationSection(tickerSymbol);
            if (section == null) continue;
            String name = section.getString("name");
            String description = section.getString("description");
            List<String> ownerIds = section.getStringList("owners");
            List<UUID> owners = new ArrayList<>();
            for (String ownerId : ownerIds) {
                owners.add(UUID.fromString(ownerId));
            }
            double balance = section.getDouble("balance");
            double lastPrice = section.getDouble("lastPrice");
            double shareholderReserve = section.getDouble("shareholderReservedBalance");
            List<Order> openSellOrders = new ArrayList<>();
            List<Order> openBuyOrders = new ArrayList<>();

            UUID ceo = UUID.fromString(Objects.requireNonNull(section.getString("ceo")));
            long sharesIssued = section.getLong("sharesIssued");
            Company company = new Company(balance, name, tickerSymbol, description, owners, ceo, lastPrice, openBuyOrders, openSellOrders, sharesIssued);
            company.setShouldSave(false);
            company.setShareholderReservedBalance(shareholderReserve);

            ConfigurationSection buyOrdersSection = section.getConfigurationSection("buyOrders");
            if (buyOrdersSection != null) {
                for (String orderId : buyOrdersSection.getKeys(false)) {
                    UUID orderID = UUID.fromString(orderId);

                    ConfigurationSection orderSection = buyOrdersSection.getConfigurationSection(orderId);
                    if (orderSection == null) continue;
                    String ownerId = orderSection.getString("owner");
                    UUID owner = null;

                    if (ownerId != null) {
                        owner = UUID.fromString(ownerId);
                    }

                    Order order = new Order(company, orderSection.getLong("quantity"),
                            new OrderPriceConfiguration(OrderPriceConfiguration.OrderType.valueOf(orderSection.getString("orderType")
                            ), orderSection.getDouble("price"), OrderPriceConfiguration.TransactionType.valueOf(orderSection.getString("transactionType"))), owner, orderID);

                    if (owner != null) {
                        if (portfolioOrders.containsKey(owner)) {
                            portfolioOrders.get(owner).add(order);
                        } else {
                            List<Order> orders = new ArrayList<>();
                            orders.add(order);
                            portfolioOrders.put(owner, orders);
                        }
                    }

                    openBuyOrders.add(order);
                }
            }

            ConfigurationSection sellOrdersSection = section.getConfigurationSection("sellOrders");
            if (sellOrdersSection != null) {
                for (String orderId : sellOrdersSection.getKeys(false)) {
                    UUID orderID = UUID.fromString(orderId);

                    ConfigurationSection orderSection = sellOrdersSection.getConfigurationSection(orderId);
                    if (orderSection == null) continue;
                    String ownerId = orderSection.getString("owner");

                    UUID owner = null;
                    if (ownerId != null) {
                        owner = UUID.fromString(ownerId);
                    }

                    Order order = new Order(company, orderSection.getLong("quantity"),
                            new OrderPriceConfiguration(OrderPriceConfiguration.OrderType.valueOf(orderSection.getString("orderType")
                            ), orderSection.getDouble("price"), OrderPriceConfiguration.TransactionType.valueOf(orderSection.getString("transactionType"))), owner, orderID);

                    if (owner != null) {
                        if (portfolioOrders.containsKey(owner)) {
                            portfolioOrders.get(owner).add(order);
                        } else {
                            List<Order> orders = new ArrayList<>();
                            orders.add(order);
                            portfolioOrders.put(owner, orders);
                        }
                    }

                    openSellOrders.add(order);
                }
            }

            ItemStack icon = ItemSerializer.deserialize(section.getString("icon"));
            company.setIcon(icon);
            company.setShouldSave(true);
            companies.put(tickerSymbol, company);
        }
    }

    @Override
    public void write() {
        for (Company company : companies.values()) {
            this.saveCompany(company);
        }

        this.save();
    }

    public void saveCompany(Company company) {
        ConfigurationSection section = this.config.createSection(company.getTicker());
        section.set("name", company.getName());
        section.set("description", company.getDescription());
        List<String> ownerIds = new ArrayList<>();
        for (UUID owner : company.getOwners()) {
            ownerIds.add(owner.toString());
        }
        section.set("owners", ownerIds);
        section.set("balance", company.getBalance());
        section.set("lastPrice", company.getLastPrice());

        ConfigurationSection buyOrdersSection = section.createSection("buyOrders");
        for (Order order : company.getOpenBuyOrders()) {
            ConfigurationSection orderSection = buyOrdersSection.createSection(order.getOrderID().toString());

            UUID owner = order.getPortfolioOwner();
            if (owner != null) {
                orderSection.set("owner", owner.toString());
            }
            else {
                orderSection.set("owner", null);
            }

            orderSection.set("quantity", order.getQuantity());
            orderSection.set("orderType", order.getOrderConfig().getOrderType().toString());
            orderSection.set("price", order.getOrderConfig().getPrice());
            orderSection.set("transactionType", order.getOrderConfig().getTransactionType().toString());
        }

        ConfigurationSection sellOrdersSection = section.createSection("sellOrders");
        for (Order order : company.getOpenSellOrders()) {
            ConfigurationSection orderSection = sellOrdersSection.createSection(order.getOrderID().toString());

            UUID owner = order.getPortfolioOwner();
            if (owner != null) {
                orderSection.set("owner", owner.toString());
            }
            else {
                orderSection.set("owner", null);
            }

            orderSection.set("quantity", order.getQuantity());
            orderSection.set("orderType", order.getOrderConfig().getOrderType().toString());
            orderSection.set("price", order.getOrderConfig().getPrice());
            orderSection.set("transactionType", order.getOrderConfig().getTransactionType().toString());
        }

        section.set("ceo", company.getCeo().toString());
        section.set("icon", ItemSerializer.serialize(company.getIcon()));
        section.set("sharesIssued", company.getSharesIssued());
        section.set("shareholderReservedBalance", company.getShareholderReservedBalance());

        this.save();
    }

    /**
     * Gets a list of companies that a player has an ownership stake in.
     * @param ownerId The UUID of the player to check.
     * @return A list of companies that the player has an ownership stake in.
     */
    public List<Company> getCompanies(UUID ownerId) {
        List<Company> companies = new ArrayList<>();
        for (Company company : this.companies.values()) {
            if (company.getOwners().contains(ownerId)) {
                companies.add(company);
            }
        }
        return companies;
    }

    public void deleteCompany(Company company) {
        this.companies.remove(company.getTicker());
        this.config.set(company.getTicker(), null);
        this.save();
    }

    public List<Company> searchCompanies(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return new ArrayList<>(this.companies.values());
        }
        List<Company> companies = new ArrayList<>();
        for (Company company : this.companies.values()) {
            if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', company.getName())).toLowerCase().contains(searchTerm.toLowerCase()) || company.getTicker().toLowerCase().contains(searchTerm.toLowerCase())) {
                companies.add(company);
            }
        }
        return companies;
    }
}
