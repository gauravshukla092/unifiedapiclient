package com.unifiedapiclient.internal;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Executes HTTP requests using RestAssured.
 */
public final class HttpRequestExecutor {
    private HttpRequestExecutor() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Response executeGet(RequestSpecification requestSpec, String path, Object... pathParams) {
        String resolvedPath = StringUtils.requireNonBlank(path, "path");
        RequestSpecification request = given(requestSpec);
        PathParameterHandler.configure(request, resolvedPath, pathParams);
        return request.get(resolvedPath).then().extract().response();
    }

    public static Response executePost(RequestSpecification requestSpec, String path, Object body) {
        return executeWithBody(requestSpec, path, body, HttpMethod.POST);
    }

    public static Response executePut(RequestSpecification requestSpec, String path, Object body) {
        return executeWithBody(requestSpec, path, body, HttpMethod.PUT);
    }

    public static Response executePatch(RequestSpecification requestSpec, String path, Object body) {
        return executeWithBody(requestSpec, path, body, HttpMethod.PATCH);
    }

    public static Response executeDelete(RequestSpecification requestSpec, String path) {
        return executeWithoutBody(requestSpec, path, HttpMethod.DELETE);
    }

    public static Response executeHead(RequestSpecification requestSpec, String path) {
        return executeWithoutBody(requestSpec, path, HttpMethod.HEAD);
    }

    public static Response executeOptions(RequestSpecification requestSpec, String path) {
        return executeWithoutBody(requestSpec, path, HttpMethod.OPTIONS);
    }

    private static Response executeWithBody(RequestSpecification requestSpec, String path, Object body, HttpMethod method) {
        String resolvedPath = StringUtils.requireNonBlank(path, "path");
        RequestSpecification request = given(requestSpec).body(body);
        
        Response response = switch (method) {
            case POST -> request.post(resolvedPath);
            case PUT -> request.put(resolvedPath);
            case PATCH -> request.patch(resolvedPath);
            default -> throw new IllegalArgumentException("Unsupported method with body: " + method);
        };
        
        return response.then().extract().response();
    }

    private static Response executeWithoutBody(RequestSpecification requestSpec, String path, HttpMethod method) {
        String resolvedPath = StringUtils.requireNonBlank(path, "path");
        RequestSpecification request = given(requestSpec);
        
        Response response = switch (method) {
            case DELETE -> request.delete(resolvedPath);
            case HEAD -> request.head(resolvedPath);
            case OPTIONS -> request.options(resolvedPath);
            default -> throw new IllegalArgumentException("Unsupported method without body: " + method);
        };
        
        return response.then().extract().response();
    }

    private enum HttpMethod {
        GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
    }
}

