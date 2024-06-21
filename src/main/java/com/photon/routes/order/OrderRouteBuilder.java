package com.photon.routes.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;
import org.apache.camel.saga.InMemorySagaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderRouteBuilder extends RouteBuilder {

    private final CamelContext camelContext;

    private final String ORDER_MGMT_DISCOVER_REGISTRY_SERVICE_NAME = "order-mgmt-service";

    private final String CART_SERVICE_NAME = "cart-service";

    private static final String API_GATEWAY_BASE_URL = "http://localhost:8083";

    @Value("${server.port}")
    private String portNumber;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet");
        rest("/startOrderFlow")
              .description("Save Order Partially FullFilled")
              .post()
              .consumes("application/json")
              .produces("application/json")
              .routeId("order-flow-route")
              .to("direct:cartUpdatedWithOrderDetails");

        from("direct:cartUpdatedWithOrderDetails")
                .log("Start of direct:cartUpdatedWithOrderDetails with body: ${body}")
                .removeHeader("CamelHttp*")
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .to(API_GATEWAY_BASE_URL + "/carts/orderFullFilled")
                .onCompletion()
                    .onCompleteOnly()
                        .to("direct:update-product-stock")
                    .onFailureOnly()
                        .to("direct:rollback-order")
                .convertBodyTo(String.class)
                .log("End of direct:update-account with body: ${body}");

        from("direct:update-product-stock")
                .log("Start of direct:update-product-stock with body: ${body}")
                .removeHeader("CamelHttp*")
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .to(API_GATEWAY_BASE_URL + "/products/updateProductStock")
                .onCompletion()
                    .onCompleteOnly()
                        .to("direct:productStockUpdated")
                .onFailureOnly()
                .to("direct:rollback-cart")
                .convertBodyTo(String.class)
                .log("End of direct:productStockUpdated with body: ${body}");

        from("direct:productStockUpdated")
                .log("updating product stock with body: ${body}")
                .removeHeader("CamelHttp*")
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .to(API_GATEWAY_BASE_URL + "/orders/fullFilled")
                .log("End of order Full Filled")
                .end();

        // on failure roll back sequence
        from("direct:rollback-product-stock")
                .log("Start of direct:rollback-product-stock with body: ${body}")
                .removeHeader("CamelHttp*")
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .to(API_GATEWAY_BASE_URL + "/products/rollbackStock")
                .onCompletion()
                .onCompleteOnly()
                .to("direct:rollback-cart")
                .convertBodyTo(String.class)
                .log("End of direct:rollback-product-stock with body: ${body}");

        from("direct:rollback-cart")
                .log("Start of direct:rollback-cart with body: ${body}")
                .removeHeader("CamelHttp*")
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .to(API_GATEWAY_BASE_URL + "/carts/rollback")
                .onCompletion()
                    .onCompleteOnly()
                        .to("direct:rollback-order")
                .convertBodyTo(String.class)
                .log("End of direct:rollback-cart with body: ${body}");

        from("direct:rollback-order")
                .log("Start of direct:rollback-order with body: ${body}")
                .removeHeader("CamelHttp*")
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .to(API_GATEWAY_BASE_URL + "/orders/rollback")
                .convertBodyTo(String.class)
                .log("End of direct:rollback-order with body: ${body}");
    }
}
