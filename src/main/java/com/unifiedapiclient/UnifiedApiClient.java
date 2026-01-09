package com.unifiedapiclient;

import com.unifiedapiclient.internal.ClientConfiguration;
import com.unifiedapiclient.internal.HttpRequestExecutor;
import com.unifiedapiclient.internal.RequestSpecFactory;
import com.unifiedapiclient.internal.RestrictedBuilder;
import com.unifiedapiclient.internal.StringUtils;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * HTTP client with multi-source configuration, correlation IDs, and reusable request specifications.
 */
public class UnifiedApiClient {
    // Public for internal package access
    public static final String DEFAULT_VERSION = "0.1.0";
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    public static final boolean DEFAULT_RELAXED_SSL = true;
    public static final String DEFAULT_USER_AGENT = "unifiedapiclient/1.0";
    public static final int DEFAULT_MAX_LOG_ENDPOINT_LEN = 2000;
    public static final String DEFAULT_SERVICE_NAME = "api";

    private final Supplier<String> versionSupplier;
    protected final RequestSpecification requestSpec;
    private final String serviceName;
    private static final String UNCONFIGURED_SERVICE_NAME = "unconfigured";

    public UnifiedApiClient() {
        this.versionSupplier = () -> DEFAULT_VERSION;
        this.requestSpec = null;
        this.serviceName = UNCONFIGURED_SERVICE_NAME;
    }

    public UnifiedApiClient(Supplier<String> versionSupplier) {
        this.versionSupplier = Objects.requireNonNull(versionSupplier, "versionSupplier cannot be null");
        this.requestSpec = null;
        this.serviceName = UNCONFIGURED_SERVICE_NAME;
    }

    protected UnifiedApiClient(UnifiedApiClientBuilder builder) {
        Objects.requireNonNull(builder, "builder cannot be null");

        ClientConfiguration.ResolvedConfiguration config = ClientConfiguration.resolve(builder);

        this.serviceName = config.serviceName();
        this.versionSupplier = config.versionSupplier();

        this.requestSpec = RequestSpecFactory.create(
                config.serviceName(),
                config.baseUrl(),
                config.connectTimeoutMs(),
                config.readTimeoutMs(),
                config.relaxedSsl(),
                config.userAgent(),
                config.maxLogEndpointLen(),
                config.customHeaders(),
                config.authorization(),
                config.loggingEnabled()
        );
    }

    public String version() {
        return versionSupplier.get();
    }

    public String version(Function<String, String> mapper) {
        return Objects.requireNonNull(mapper, "mapper cannot be null").apply(version());
    }

    public static UnifiedApiClient create(String serviceName) {
        return forService(serviceName).build();
    }

    public static ServiceClientBuilder service(String serviceName) {
        return forService(serviceName);
    }

    public static ServiceClientBuilder forService(String serviceName) {
        return new RestrictedBuilder(StringUtils.requireNonBlank(serviceName, "serviceName"));
    }

    public static UnifiedApiClientBuilder builder() {
        return new UnifiedApiClientBuilder();
    }

    public RequestSpecification getRequestSpecification() {
        return requireRequestSpec();
    }

    public String getServiceName() {
        return serviceName;
    }

    public Response get(String path, Object... pathParams) {
        return HttpRequestExecutor.executeGet(requireRequestSpec(), path, pathParams);
    }

    public Response post(String path, Object body) {
        return HttpRequestExecutor.executePost(requireRequestSpec(), path, body);
    }

    public Response put(String path, Object body) {
        return HttpRequestExecutor.executePut(requireRequestSpec(), path, body);
    }

    public Response delete(String path) {
        return HttpRequestExecutor.executeDelete(requireRequestSpec(), path);
    }

    public Response patch(String path, Object body) {
        return HttpRequestExecutor.executePatch(requireRequestSpec(), path, body);
    }

    public Response head(String path) {
        return HttpRequestExecutor.executeHead(requireRequestSpec(), path);
    }

    public Response options(String path) {
        return HttpRequestExecutor.executeOptions(requireRequestSpec(), path);
    }


    private RequestSpecification requireRequestSpec() {
        if (requestSpec == null) {
            throw new IllegalStateException("UnifiedApiClient is not configured. Use builder() or create(serviceName).");
        }
        return requestSpec;
    }

}
