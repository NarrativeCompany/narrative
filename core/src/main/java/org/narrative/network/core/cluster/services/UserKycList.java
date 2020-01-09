package org.narrative.network.core.cluster.services;

import org.narrative.common.persistence.PersistenceUtil;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IsNullOrder;
import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKyc_;
import org.narrative.network.core.user.User_;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public class UserKycList extends GlobalTaskImpl<List<UserKyc>> {
    public enum SortOrder implements NameForDisplayProvider {
        NONE,
        LAST_UPDATE_TIMESTAMP;

        public String getNameForDisplay() {
            return wordlet("userKycList.sortOrder." + this);
        }

        public boolean isLastUpdateTimestamp() {
            return this==LAST_UPDATE_TIMESTAMP;
        }
    }

    private UserKycStatus kycStatus;
    private String username;

    private SortOrder sortOrder = SortOrder.LAST_UPDATE_TIMESTAMP;

    private int page = 1;
    private int maxResults = 25;

    private Integer totalResultCount;

    private boolean fetchTotalResultCount;

    public UserKycList() {
        super(false);
    }

    public void validate(ValidationHandler handler, String paramPrefix) {
    }

    private Criteria userKycCriteria;
    private Criteria userNameCriteria;

    public Criteria getRootCriteria() {
        if (userKycCriteria == null) {
            userKycCriteria = getNetworkContext().getGlobalSession().getSession().createCriteria(UserKyc.class, "ukcRoot");
        }
        return userKycCriteria;
    }

    protected Criteria getUserNameCriteria() {
        if (userNameCriteria == null) {
            Criteria root = getRootCriteria();
            userNameCriteria = root.createCriteria(HibernateUtil.makeName(root,UserKyc_.user.getName()), "un");
        }
        return userNameCriteria;
    }

    @Override
    protected List<UserKyc> doMonitoredTask() {
        Criteria uc = getRootCriteria();

        Criteria curCrit;
        if (kycStatus != null) {
            uc.add(Restrictions.eq(UserKyc_.kycStatus.getName(), kycStatus));
        }

        if (StringUtils.isNotEmpty(username)) {
            curCrit = getUserNameCriteria();
            curCrit.add(Restrictions.eq(HibernateUtil.makeName(curCrit, User_.username.getName()), username));
        }

        // jw: before we do any ordering or anything, lets get a result count if necessary
        if (fetchTotalResultCount) {
            // jw: since some joins are many to one, we need to do the count equivelent of the DISTINCT_ROOT_ENTITY result transformer we use for fetching results.
            uc.setProjection(Projections.countDistinct(HibernateUtil.makeName(uc, UserKyc.FIELD__OID__NAME)));
            totalResultCount = Math.toIntExact((Long) uc.uniqueResult());
            uc.setProjection(null);
        }

        // jw: now, lets setup the ordering prior to fetching actual results!
        if(sortOrder!=null) {
            if(sortOrder.isLastUpdateTimestamp()) {
                // bl: we want nulls last and this will do it
                uc.addOrder(IsNullOrder.asc(HibernateUtil.makeName(uc,UserKyc_.lastUpdated.getName())));
                uc.addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(uc,UserKyc_.lastUpdated.getName()), false));
            }
        }

        // User name is default
        uc.addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(getUserNameCriteria(), User_.username.getName()), true));

        // jw: we want distinct root entities!
        uc.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        // jw: calculate the first result, and limit to the specified number of results!
        int pageIndex = Math.max(page, 1) - 1;
        uc.setFirstResult(pageIndex * maxResults);
        uc.setMaxResults(maxResults);

        // jw: finally, we can run the criteria!
        return uc.list();
    }

    public void doSetPage(int page) {
        this.page = page;
    }

    public void doSetMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public UserKycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(UserKycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getTotalResultCount() {
        return totalResultCount;
    }

    public void setTotalResultCount(Integer totalResultCount) {
        this.totalResultCount = totalResultCount;
    }

    public boolean isFetchTotalResultCount() {
        return fetchTotalResultCount;
    }

    public void setFetchTotalResultCount(boolean fetchTotalResultCount) {
        this.fetchTotalResultCount = fetchTotalResultCount;
    }
}
