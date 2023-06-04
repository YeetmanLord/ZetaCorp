package com.github.yeetmanlord.zetacorporations.data;

import com.github.yeetmanlord.zeta_core.data.DataStorer;
import com.github.yeetmanlord.zetacorporations.ZetaCorporations;

public class GeneralConfig extends DataStorer {

    private double marketOrderMargin;
    private long maxSharesIssued;
    private long maxSharesIssuedAtOnce;
    private double maxShareIssuePrice;
    private double shareholderReservePercent;
    private boolean reservePortionOfDeposit;

    public GeneralConfig(ZetaCorporations instance) {
        super(instance, "config");
    }

    @Override
    public void setDefaults() {
        if (!this.config.contains("marketOrderMargin")) {
            this.config.set("marketOrderMargin", 0.1); // 2.5%
        }

        if (!this.config.contains("maxSharesIssued")) {
            this.config.set("maxSharesIssued", 2500000);
        }

        if (!this.config.contains("maxSharesIssuedAtOnce")) {
            this.config.set("maxSharesIssuedAtOnce", 1000000);
        }

        if (!this.config.contains("maxShareIssuePrice")) {
            this.config.set("maxShareIssuePrice", 1000);
        }

        if (!this.config.contains("shareholderReservePercent")) {
            this.config.set("shareholderReservePercent", 0.5);
        }

        if (!this.config.contains("reservePortionOfDeposit")) {
            this.config.set("reservePortionOfDeposit", true);
        }
    }

    @Override
    public void read() {
        this.marketOrderMargin = this.config.getDouble("marketOrderMargin");
        this.maxSharesIssued = this.config.getLong("maxSharesIssued");
        this.maxSharesIssuedAtOnce = this.config.getLong("maxSharesIssuedAtOnce");
        this.maxShareIssuePrice = this.config.getDouble("maxShareIssuePrice");
        this.shareholderReservePercent = this.config.getDouble("shareholderReservePercent");
        this.reservePortionOfDeposit = this.config.getBoolean("reservePortionOfDeposit");
    }

    @Override
    public void write() {
        this.config.set("marketOrderMargin", this.marketOrderMargin);
        this.config.set("maxSharesIssued", this.maxSharesIssued);
        this.config.set("maxSharesIssuedAtOnce", this.maxSharesIssuedAtOnce);
        this.config.set("maxShareIssuePrice", this.maxShareIssuePrice);
        this.config.set("shareholderReservePercent", this.shareholderReservePercent);
        this.config.set("reservePortionOfDeposit", this.reservePortionOfDeposit);

        this.save();
    }

    /**
     * The percentage of the company's ask price that must be accounted for when buying shares since the price is not guaranteed
     */
    public double getMarketOrderMargin() {
        if (marketOrderMargin > 1) {
            marketOrderMargin = 1;
        } else if (marketOrderMargin < 0) {
            marketOrderMargin = 0;
        }
        return marketOrderMargin;
    }

    public void setMarketOrderMargin(double marketOrderMargin) {
        if (marketOrderMargin > 1) {
            marketOrderMargin = 1;
        } else if (marketOrderMargin < 0) {
            marketOrderMargin = 0;
        }
        this.marketOrderMargin = marketOrderMargin;
        this.write();
    }

    /**
     * The maximum amount of shares that can be issued by a company
     */
    public long getMaxSharesIssued() {
        return maxSharesIssued;
    }

    public void setMaxSharesIssued(long maxSharesIssued) {
        this.maxSharesIssued = maxSharesIssued;
        this.write();
    }

    /**
     * The maximum amount of shares that can be issued at once by a company
     */
    public long getMaxSharesIssuedAtOnce() {
        return maxSharesIssuedAtOnce;
    }

    public void setMaxSharesIssuedAtOnce(long maxSharesIssuedAtOnce) {
        this.maxSharesIssuedAtOnce = maxSharesIssuedAtOnce;
        this.write();
    }

    /**
     * The maximum price that shares can be issued at by a company
     */
    public double getMaxShareIssuePrice() {
        return maxShareIssuePrice;
    }

    public void setMaxShareIssuePrice(double maxShareIssuePrice) {
        this.maxShareIssuePrice = maxShareIssuePrice;
        this.write();
    }

    /**
     * The percentage of the company's deposits that are reserved for shareholders (From issuing shares specifically)
     */
    public double getShareholderReservePercent() {
        if (shareholderReservePercent > 1) {
            shareholderReservePercent = 1;
        } else if (shareholderReservePercent < 0) {
            shareholderReservePercent = 0;
        }
        return shareholderReservePercent;
    }

    public void setShareholderReservePercent(double shareholderReservePercent) {
        if (shareholderReservePercent > 1) {
            shareholderReservePercent = 1;
        } else if (shareholderReservePercent < 0) {
            shareholderReservePercent = 0;
        }
        this.shareholderReservePercent = shareholderReservePercent;
        this.write();
    }

    /**
     * Determines if 20% of deposits should be reserved for shareholders.
     */
    public boolean doesReservePortionOfDeposit() {
        return this.reservePortionOfDeposit;
    }

    public void setReservePortionOfDeposit(boolean reservePortionOfDeposit) {
        this.reservePortionOfDeposit = reservePortionOfDeposit;
        this.write();
    }
}
