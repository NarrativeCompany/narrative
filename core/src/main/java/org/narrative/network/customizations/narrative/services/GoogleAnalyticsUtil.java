package org.narrative.network.customizations.narrative.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportData;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.util.NetworkLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This tool is handy for doing test queries: https://ga-dev-tools.appspot.com/query-explorer/
 * Find all of the dimensions and metrics here: https://ga-dev-tools.appspot.com/dimensions-metrics-explorer/
 * Date: 2019-06-18
 * Time: 09:21
 *
 * @author brian
 */
public class GoogleAnalyticsUtil {
    private static final NetworkLogger logger = new NetworkLogger(GoogleAnalyticsUtil.class);

    private static final String APPLICATION_NAME = "Narrative.org Platform";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static GoogleCredential credential;
    private static String viewId;

    public static void initGoogleAnalyticsApi(String apiJsonFile, String viewId) throws IOException {
        credential = GoogleCredential
                .fromStream(new FileInputStream(apiJsonFile))
                .createScoped(AnalyticsReportingScopes.all());
        GoogleAnalyticsUtil.viewId = viewId;
    }

    public static Long getMonthlyUniqueVisitors() {
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();

            GetReportsResponse response = getReportFor30DayActiveUsers(service);

            String value = getSingleValue(response);
            return value==null ? null : Long.parseLong(value);
        } catch (Exception e) {
            StatisticManager.recordException(e, false, null);
            if(logger.isErrorEnabled()) logger.error("Failed fetching monthly unique visitors from Google Analytics!", e);
            return null;
        }
    }

    private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Construct the Analytics Reporting service object.
        return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    private static GetReportsResponse getReportFor30DayActiveUsers(AnalyticsReporting service) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("today");
        dateRange.setEndDate("today");

        // bl: just looking for 30-day active users
        Metric sessions = new Metric().setExpression("ga:30dayUsers");

        // bl: we have to pull the date dimension. even though we're querying for just a single day,
        // this is required to use ga:30dayUsers
        Dimension date = new Dimension().setName("ga:date");

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(viewId)
                .setDateRanges(Collections.singletonList(dateRange))
                .setMetrics(Collections.singletonList(sessions))
                .setDimensions(Collections.singletonList(date));

        ArrayList<ReportRequest> requests = new ArrayList<>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        return service.reports().batchGet(getReport).execute();
    }

    private static String getSingleValue(GetReportsResponse response) {
        List<Report> reports = response.getReports();
        if(reports.isEmpty()) {
            return null;
        }
        // bl: just grab the first report
        Report report = reports.get(0);
        ReportData reportData = report.getData();
        if(reportData.getRowCount()<=0) {
            return null;
        }

        ReportRow reportRow = reportData.getRows().get(0);
        List<DateRangeValues> metrics = reportRow.getMetrics();
        if(metrics.isEmpty()) {
            return null;
        }
        DateRangeValues dateRangeValues = metrics.get(0);
        List<String> values = dateRangeValues.getValues();
        if(values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
