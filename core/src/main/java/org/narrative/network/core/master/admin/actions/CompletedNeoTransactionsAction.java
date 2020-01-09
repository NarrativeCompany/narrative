package org.narrative.network.core.master.admin.actions;

import org.narrative.network.core.cluster.actions.ClusterAction;
import org.narrative.network.core.narrative.wallet.NeoTransaction;

import java.util.List;

/**
 * Date: 2019-06-12
 * Time: 07:43
 *
 * @author brian
 */
public class CompletedNeoTransactionsAction extends ClusterAction {
    public static final String ACTION_NAME = "completed-neo-transactions";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    public static final String PAGE_PARAM = "page";

    public static final int COMPLETED_ROWS_PER_PAGE = 50;

    private int page;

    private List<NeoTransaction> neoTransactions;
    private int countCompletedTransactions;

    public String input() throws Exception {
        if(page<=0) {
            page = 1;
        }
        neoTransactions = NeoTransaction.dao().getAllCompleted(page, COMPLETED_ROWS_PER_PAGE);
        countCompletedTransactions = NeoTransaction.dao().getCountCompleted();
        return INPUT;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<NeoTransaction> getNeoTransactions() {
        return neoTransactions;
    }

    public int getCountCompletedTransactions() {
        return countCompletedTransactions;
    }
}
