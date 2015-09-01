package com.cyngn.chrono.api;

import com.cyngn.chrono.http.CacheUtil;
import com.cyngn.chrono.storage.ReportStorage;
import com.cyngn.chrono.storage.entity.MetricReport;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The API to support the client app reporting timing data back to us.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/11/14
 */
public class Report implements RestApi {
    private static final Logger logger = LoggerFactory.getLogger(Report.class);
    public final static String REPORT_API_V1 = "/api/v1/report";

    private final RestApiDescriptor [] supportApi = {new RestApi.RestApiDescriptor(HttpMethod.POST, REPORT_API_V1, this::add)};
    private final ReportStorage storage;


    public Report(ReportStorage storage) {
        this.storage = storage;
    }

    /**
     * Adds a api measurement to our reporting store
     */
    protected void add(RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        request.bodyHandler(body -> {
            MetricReport report = HttpHelper.attemptToParse(body.toString(), MetricReport.class, request.response());
            if(isValidRequest(report)) {
                report.created = DateTime.now(DateTimeZone.UTC).toDate();
                report.client_ip = context.request().remoteAddress().host();

                logger.info("add - report: {}", report);
                storage.createReport(report, success -> {
                    if(success) {
                        CacheUtil.setNoCacheHeaders(response.headers());
                        HttpHelper.processResponse(response);
                    } else {
                        HttpHelper.processErrorResponse("Failed to persist report to DB", response,
                                HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    }
                });
            } else {
                HttpHelper.processErrorResponse("Invalid report", response, HttpResponseStatus.BAD_REQUEST.code());
            }

        });
    }

    private boolean isValidRequest(MetricReport data) {
        return data != null && StringUtils.isNotEmpty(data.deviceId) &&
               data.measurements != null && data.measurements.size() > 0;
    }

    @Override
    public RestApiDescriptor[] supportedApi() {
        return supportApi;
    }
}
