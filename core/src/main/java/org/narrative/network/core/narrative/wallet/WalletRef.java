package org.narrative.network.core.narrative.wallet;

/**
 * Date: 2019-05-16
 * Time: 15:45
 *
 * @author jonmark
 */
public interface WalletRef {
    public static final String FIELD__WALLET__NAME = "wallet";
    public static final String FIELD__WALLET__COLUMN = FIELD__WALLET__NAME+"_"+ Wallet.FIELD__OID__NAME;

    Wallet getWallet();
    void setWallet(Wallet wallet);
}
