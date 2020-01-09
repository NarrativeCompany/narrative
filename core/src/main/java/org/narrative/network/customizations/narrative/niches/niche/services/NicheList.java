package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IsNullOrder;
import org.narrative.common.persistence.hibernate.criteria.CriteriaList;
import org.narrative.common.persistence.hibernate.criteria.CriteriaSort;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/7/18
 * Time: 2:09 PM
 */
public class NicheList extends AreaTaskImpl<List<Niche>> implements CriteriaList<Niche, NicheList.SortField> {
    public NicheList() {
        super(false);
    }

    private NicheStatus status;
    private NicheStatus notStatus;

    private Boolean purchased;
    private Boolean pendingPurchase;
    private Boolean forSale;

    private SortField sort;

    private int page = 1;
    private Integer rowsPerPage;

    private Integer count;

    private boolean sortAsc;

    private boolean doCount;

    private Criteria nicheCriteria;

    protected Criteria getNicheCriteria() {
        if (nicheCriteria == null) {
            nicheCriteria = Niche.dao().getGSession().getSession().createCriteria(Niche.class, "niche");
        }
        return nicheCriteria;
    }

    private Criteria activeAuctionCriteria;

    protected Criteria getActiveAuctionCriteria(boolean leftJoin) {
        if (activeAuctionCriteria == null) {
            Criteria criteria = getNicheCriteria();
            if (leftJoin) {
                activeAuctionCriteria = criteria.createCriteria(HibernateUtil.makeName(criteria, Niche.FIELD__ACTIVE_AUCTION__NAME), "aa", Criteria.LEFT_JOIN);

            } else {
                activeAuctionCriteria = criteria.createCriteria(HibernateUtil.makeName(criteria, Niche.FIELD__ACTIVE_AUCTION__NAME), "aa");
            }
        }
        return activeAuctionCriteria;
    }

    private Criteria activeAuctionInvoiceCriteria;

    protected Criteria getActiveAuctionInvoiceCriteria() {
        if (activeAuctionInvoiceCriteria == null) {
            Criteria criteria = getActiveAuctionCriteria(false);
            activeAuctionInvoiceCriteria = criteria.createCriteria(HibernateUtil.makeName(criteria, NicheAuction.FIELD__ACTIVE_INVOICE__NAME), "aa_ai");
        }
        return activeAuctionInvoiceCriteria;
    }

    private Criteria activeInvoiceCriteria;

    protected Criteria getActiveInvoiceCriteria() {
        if (activeInvoiceCriteria == null) {
            Criteria criteria = getActiveAuctionInvoiceCriteria();
            activeInvoiceCriteria = criteria.createCriteria(HibernateUtil.makeName(criteria, NicheAuctionInvoice.Fields.invoice), "aa_ai_i");
        }
        return activeInvoiceCriteria;
    }

    @Override
    protected List<Niche> doMonitoredTask() {
        Criteria criteria = getNicheCriteria();

        criteria.add(Restrictions.eq(Niche.FIELD__PORTFOLIO__NAME, getAreaContext().getPortfolio()));

        if (status != null) {
            criteria.add(Restrictions.eq(Niche.FIELD__STATUS__NAME, status));
        }
        if (notStatus != null) {
            criteria.add(Restrictions.ne(Niche.FIELD__STATUS__NAME, notStatus));
        }
        if (purchased != null) {
            assert forSale == null : "The 'purchased' parameter should never be used in conjunction with 'forSale', due to how they join to NicheAuction differently.";
            assert purchased : "The 'purchased' parameter should only ever be used positively!";

            Disjunction disjunction = Restrictions.disjunction();
            // jw: if the niche is approved (owned) then it should be considered purchased!
            disjunction.add(Restrictions.eq(HibernateUtil.makeName(criteria, Niche.FIELD__STATUS__NAME), NicheStatus.ACTIVE));
            // jw: or if the niche if for sale, and its activeAuction is over, then we are just waiting on payment.
            disjunction.add(Restrictions.lt(HibernateUtil.makeName(getActiveAuctionCriteria(true), NicheAuction.FIELD__END_DATETIME__NAME), now()));

            criteria.add(disjunction);
        }
        if (forSale != null) {
            assert forSale : "The 'forSale' parameter should only ever be used positively!";

            // jw: first, the niche must be "FOR_SALE"
            criteria.add(Restrictions.eq(Niche.FIELD__STATUS__NAME, NicheStatus.FOR_SALE));

            Criteria auctionCriteria = getActiveAuctionCriteria(false);

            Disjunction disjunction = Restrictions.disjunction();
            // jw: or if the niche if for sale, and its activeAuction is over, then we are just waiting on payment.
            disjunction.add(Restrictions.isNull(HibernateUtil.makeName(auctionCriteria, NicheAuction.FIELD__END_DATETIME__NAME)));
            disjunction.add(Restrictions.gt(HibernateUtil.makeName(auctionCriteria, NicheAuction.FIELD__END_DATETIME__NAME), now()));

            criteria.add(disjunction);
        }

        if (pendingPurchase != null) {
            assert pendingPurchase : "The 'pendingPurchase' parameter should only ever be used positively!";

            criteria.add(Restrictions.eq(Niche.FIELD__STATUS__NAME, NicheStatus.PENDING_PAYMENT));

            // jw: next, the niche's active auction will have a active invoice flagged as invoiced.
            Criteria invoiceCriteria = getActiveInvoiceCriteria();
            invoiceCriteria.add(Restrictions.eq(HibernateUtil.makeName(invoiceCriteria, Invoice.Fields.status), InvoiceStatus.INVOICED));
        }

        if (doCount) {
            criteria.setProjection(Projections.rowCount());
            count = ((Number) criteria.uniqueResult()).intValue();
            criteria.setProjection(null);
        }

        if (sort != null) {
            switch (sort) {
                case LAST_STATUS_UPDATE_DATETIME:
                    criteria.addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(criteria, Niche.FIELD__LAST_STATUS_CHANGE_DATETIME__NAME), sortAsc));
                    break;
                case ACTIVE_AUCTION_EXPIRATION_DATE:
                    String endDatetimeField = HibernateUtil.makeName(getActiveAuctionCriteria(false), NicheAuction.FIELD__END_DATETIME__NAME);

                    criteria.addOrder(IsNullOrder.sort(endDatetimeField, sortAsc));
                    criteria.addOrder(HibernateUtil.getOrder(endDatetimeField, sortAsc));
                    criteria.addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(getActiveAuctionCriteria(false), NicheAuction.FIELD__START_DATETIME__NAME), sortAsc));
                    break;
                default:
                    throw UnexpectedError.getRuntimeException("Encountered unhandled SortOrder/" + sort);
            }
        }

        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        assert rowsPerPage != null && rowsPerPage > 0 : "Should never use AreaUserList without first setting rowsPerPage!";

        // jw: establish limits
        criteria.setFirstResult(Math.max(page - 1, 0) * rowsPerPage);
        criteria.setMaxResults(rowsPerPage);

        return criteria.list();
    }

    public NicheStatus getStatus() {
        return status;
    }

    public void setStatus(NicheStatus status) {
        this.status = status;
    }

    public NicheStatus getNotStatus() {
        return notStatus;
    }

    public void setNotStatus(NicheStatus notStatus) {
        this.notStatus = notStatus;
    }

    public Boolean getPurchased() {
        return purchased;
    }

    public void setPurchased(Boolean purchased) {
        this.purchased = purchased;
    }

    public Boolean getPendingPurchase() {
        return pendingPurchase;
    }

    public void setPendingPurchase(Boolean pendingPurchase) {
        this.pendingPurchase = pendingPurchase;
    }

    public Boolean getForSale() {
        return forSale;
    }

    public void setForSale(Boolean forSale) {
        this.forSale = forSale;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public boolean isSortAsc() {
        return sortAsc;
    }

    @Override
    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    @Override
    public int getRowsPerPage() {
        assert rowsPerPage != null : "The rows per page must be set before this method is called!";

        return rowsPerPage;
    }

    @Override
    public void doSetRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    @Override
    public SortField getSort() {
        return sort;
    }

    public void doSortByField(SortField sort) {
        this.sort = sort;
    }

    @Override
    public void doCount(boolean doCount) {
        this.doCount = doCount;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    public enum SortField implements CriteriaSort {
        LAST_STATUS_UPDATE_DATETIME,
        ACTIVE_AUCTION_EXPIRATION_DATE;
    }
}
