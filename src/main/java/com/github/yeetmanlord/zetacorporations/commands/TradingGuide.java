package com.github.yeetmanlord.zetacorporations.commands;

import com.github.yeetmanlord.zeta_core.commands.Command;
import com.github.yeetmanlord.zeta_core.commands.HelpCommand;
import com.github.yeetmanlord.zeta_core.commands.ISubCommand;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class TradingGuide extends Command {
    static final int width = 51;
    static class TradingHelp implements ISubCommand {

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public String getName() {
            return "trading";
        }

        @Override
        public String getDesc() {
            return "How to perform trades and what different trade types mean.";
        }

        @Override
        public String getSyntax() {
            return "/trading_guide trading";
        }

        @Override
        public String getPermission() {
            return "zeta_corp.trading_guide";
        }

        @Override
        public void run(CommandSender sender, String[] args) {
            String[] content = new String[] {
                    "&e" + "-".repeat(width + 4),
                    "&6&lTrading Guide",
                    "&e&lTrading and Order Types",
                    "",
                    "&e&lPlacing Orders:",
                    "&fPlacing orders is quite simple, first open your        ",
                    "&aportfolio&f and open the search companies screen. Then,",
                    "&ffind the company you want to invest in. Click on it to",
                    "&fopen the company's info page. Finally, click on the Place",
                    "&fOrder button.",
                    "",
                    "&e&lSetting up your orders:",
                    "&fThe most important aspect of an order is the order type.",
                    "&fIt can be either &eMarket &for &eLimit&f. A &eMarket&f order",
                    "&fbuys the stock at any price, no matter what. This means",
                    "&fThat you may see a price of &e$&610&f initially, but you",
                    "may end up buying some shares for &e$&610&f and some more for &e$&615",
                    "&eLimit&f orders, as the name implies, limit the price that",
                    "&fYou buy shares at. When selling, a limit order will make",
                    "&fsure you get the price you want or better. This means that",
                    "&fyour stock will be sold only when the price is higher than",
                    "&fthe price you want. Buying is similar except the trade will",
                    "&fhappen only if the stock's price is lower than what you want",
                    "&fThink of it this way: &eMarket&f will buy/sell no matter and &eLimit&f",
                    "&fWill limit the price and won't always mean you get the stock.",
                    "&e" + "-".repeat(width + 4),
                    "&cLegal Note:",
                    "&fJust so you know this is not necessarily an accurate",
                    "&frepresentation of how these order work in the real world.",
                    "&fThere are some strong similarities, but if you want some more",
                    "&finfo on real-world stock orders you can read this article by",
                    "&fInvestopedia: &ehttps://www.investopedia.com/investing/basics-trading-stock-know-your-orders/",
                    "&fI am not a financial advisor. I'm a programmer :D"
            };
            sendMessage(content, sender);
        }
    }

    static class CompaniesHelp implements ISubCommand {

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public String getName() {
            return "companies";
        }

        @Override
        public String getDesc() {
            return "Get info on how to create and manage your companies";
        }

        @Override
        public String getSyntax() {
            return "/trading_guide companies";
        }

        @Override
        public String getPermission() {
            return "zeta_corp.trading_guide";
        }

        @Override
        public void run(CommandSender sender, String[] args) {
            String[] content = new String[] {
                    "&e" + "-".repeat(width + 4),
                    "&6&lTrading Guide",
                    "&e&lCompanies Guide",
                    "",
                    "&e&lCreating A Company:",
                    "&fTo create a company, first open the companies GUI",
                    "&e(/companies)&fThen click on create company.",
                    "&fFirst input the company's name and then its ticker",
                    "&fA ticker is the name investors use to refer to a",
                    "&fcompany. It must be 5 or less characters and only",
                    "&fnumbers and letters. It should be recognizable.",
                    "&fFor example, TSLA is Tesla and AAPL is Apple.",
                    "&e&lManaging your company:",
                    "&fThere are a few aspects of your company: &eOwners&f,",
                    "&eShares&f, &eDividends&f, and your &ecorporate bank",
                    "&eaccount&f.",
                    "&eOwners:",
                    "&fThese are players who own a part of your company",
                    "&fand help manage it. They can access your &ecorporate",
                    "&ebank account&f and &eissue shares&f, but cannot add new",
                    "&eowners&f.",
                    "&eShares:",
                    "&fPut simply, a share is a part of a company. By &eissuing",
                    "&eshares &fyou are allowing players to invest in your company.",
                    "&fInvestors will buy your shares and you receive money in",
                    "&fyour &ecorporate bank account&f. This can then be used",
                    "&fto expand or build things or pursue whatever the company's",
                    "&fgoal is.",
                    "&eDividend",
                    "&fA &edividend&f is a payout to all shareholders/investors.",
                    "&fOne can be given out in the corporate account page.",
                    "&cNote:",
                    "&fFor a real-world definition of a dividend see this article",
                    "&fby Investopedia: &ehttps://www.investopedia.com/terms/d/dividend.asp",
                    "&eCorporate Bank Account:",
                    "&fThe corporate bank account is divided into 2 sections: ",
                    "&acompany balance &fand &ashareholder reserved balance.",
                    "&aCompany balance &fis accessible to all owners and can be",
                    "&fwithdrawn from at any time. The &ashareholder reserved balance",
                    "&fis kept for shareholders in case the company is deleted or",
                    "&fa dividend is paid out. The amount that is reserved when buying shares",
                    "is determined by the server's admin. Additionally, when making any",
                    "deposit a percentage (20%) will be reserved for shareholders as well.",
                    "This can be disabled by the server's admin.",
                    "&cNote: &fThis is a feature of the plugin and not of real-world companies!!",
                    "&e" + "-".repeat(width + 4)
            };
            sendMessage(content, sender);
        }
    }

    static class DefinitionsHelp implements ISubCommand {

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public String getName() {
            return "definitions";
        }

        @Override
        public String getDesc() {
            return "Get a bunch of various trading definitions to understand more about trading in this plugin.";
        }

        @Override
        public String getSyntax() {
            return "/trading_guide definitions";
        }

        @Override
        public String getPermission() {
            return "zeta_corp.trading_guide";
        }

        @Override
        public void run(CommandSender sender, String[] args) {
            String[] content = new String[] {
                    "&e" + "-".repeat(width + 4),
                    "&6&lTrading Guide",
                    "&e&lDefinitions",
                    "",
                    "&e&lStock: &fA stock/share is a part of a company.",
                    "&e&lShareholder: &fA shareholder is someone who shares in a company",
                    "&e&lDividend: &fA dividend is a payout to shareholders",
                    "&e&lTicker: &fA ticker is a short name for a company. For example, TSLA is Tesla",
                    "&e&lMarket Cap: &fA market cap is the total value of a company's shares",
                    "&fIn this plugin, it is specifically, the last price of a share times the number of shares",
                    "&fin circulation.",
                    "&e&lBid: &fA bid is the highest price someone is willing to buy a share for",
                    "&e&lAsk: &fAn ask is the lowest price someone is willing to sell a share for",
                    "&e&lMid: &fThe mid is the average of the bid and ask",
                    "&e&lLimit Order: &fA limit order is an order to buy or sell a share at a specific price",
                    "&for better. See /trading_guide trading for more info.",
                    "&e&lMarket Order: &fA market order is an order to buy or sell a share at the current price.",
                    "&fThis is the default order type and does not guarentee a specific price.",
                    "See /trading_guide trading for more info.",
                    "&e&lOpen Order: &fAn open order is an order that has not been filled yet.",
                    "&fMeaning that an investor is still waiting to buy/sell shares.",
                    "&e&lOutstanding Shares: &fOutstanding shares are the number of shares in circulation.",
                    "&e&lIssued Shares: &fIssued shares are the number of shares that the company has sold",
                    "&for is in the process of selling to investors.",
                    "&e&lLast (purchase) price: &fThe last price is the price that the last share was bought for.",
                    "&e&lNet Worth: &fNet worth is your cash plus the value of your shares.",
                    "&cNote: &fAll of these definitions are in the context of this game and not necessarily",
                    "&fhow they are used/defined in the real world.",
                    "&e" + "-".repeat(width + 4),
            };
            sendMessage(content, sender);
        }
    }

    static class TipsHelp implements ISubCommand {

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public String getName() {
            return "tips";
        }

        @Override
        public String getDesc() {
            return "Get some tips on how to trade in this plugin.";
        }

        @Override
        public String getSyntax() {
            return "/trading_guide tips";
        }

        @Override
        public String getPermission() {
            return "zeta_corp.trading_guide";
        }

        @Override
        public void run(CommandSender sender, String[] args) {
            String[] content = new String[] {
                    "&e" + "-".repeat(width + 4),
                    "&6&lTrading Guide",
                    "&e&lTrading Tips",
                    "",
                    "&fIn general, you should look for companies who's market cap and balance",
                    "&fare similar because this indicates that the company could pay out shareholders",
                    "&fif deleted. Just a safety tip and a way to avoid scams.",
                    "&fTake advantage of the info you have. You can view the CEO and owners of a company",
                    "&fas well as how many shares are in circulation.",
                    "&fThe amount of open buy/sell orders can also be a good indicator of how active a company is.",
                    "&fA low amount of buy/sell orders could mean that you will have a hard time selling/buying shares",
                    "&fespecially at a stable price.",
                    "&fLastly, this is a game of the stock market. Have fun! But that also means that",
                    "&fmany aspects of this plug-in &cdon't reflect the real world.",
                    "&fPlease read the legal disclaimer for more info.",
                    "&fI'm a developer not a financial advisor! :D",
                    "&e" + "-".repeat(width + 4),
            };
            sendMessage(content, sender);
        }
    }

    static class LegalDisclaimer implements ISubCommand {

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public String getName() {
            return "legal_disclaimer";
        }

        @Override
        public String getDesc() {
            return "A small legal disclaimer because I don't want to get sued.";
        }

        @Override
        public String getSyntax() {
            return "/trading_guide legal_disclaimer";
        }

        @Override
        public String getPermission() {
            return "zeta_corp.trading_guide";
        }

        @Override
        public void run(CommandSender sender, String[] args) {
            String[] content = new String[]{
                    "&e" + "-".repeat(width + 4),
                    "&6&lTrading Guide",
                    "&c&lLegal Disclaimer",
                    "",
                    "&fThis plugin is a game and is not meant to be used as a real world stock market.",
                    "&fI am not a financial advisor and anything you see in this plugin should not be",
                    "&ftaken as financial advice.",
                    "&fThis is meant to be a &orecreation&f of the stock market in a more fun and intuitive way.",
                    "&fI would encourage you to do your own research if you are interested",
                    "&fin investing and the stock market.",
                    "&fI'm a developer not a financial advisor after all!",
                    "&e" + "-".repeat(width + 4),
            };
            sendMessage(content, sender);
        }
    }

    public TradingGuide(ZetaCorporations main) {
        super(new HelpCommand("trading_guide"), main);
        this.commands = new ArrayList<>();
        this.commands.add(new TradingHelp());
        this.commands.add(new CompaniesHelp());
        this.commands.add(new DefinitionsHelp());
        this.commands.add(new TipsHelp());
        this.commands.add(new LegalDisclaimer());
    }

    @Override
    protected String getName() {
        return "trading_guide";
    }

    @Override
    protected String getDesc() {
        return "Tips, tricks, and guides on how to interact with the stock market and companies.";
    }

    @Override
    protected String getSyntax() {
        return "/trading_guide help | trading | companies | definitions | tips | legal_disclaimer";
    }


    private static void sendMessage(String[] content, CommandSender sender) {
        for (String line : content) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
}
