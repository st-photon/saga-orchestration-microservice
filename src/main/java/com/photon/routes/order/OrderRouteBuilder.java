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

    @Value("${server.port}")
    private String portNumber;

    @Override
    public void configure() throws Exception {
        //camelContext.addService(new InMemorySagaService());
        //camelContext.getExecutorServiceManager().getDefaultThreadPoolProfile().setMaxQueueSize(-1); // default id is defaultThreadPoolProfile
        restConfiguration()
                //.contextPath("/saga/*")
                //.apiContextPath("/api-doc")
                //.apiProperty("api.title", "Spring Boot Camel Postgres Rest API.")
                //.apiProperty("api.version", "1.0")
                //.apiProperty("cors", "true")
                //.apiContextRouteId("doc-api")
                .component("servlet");
//        rest().description("Order mgmt service")
//                .consumes("application/json")
//                .produces("application/json")
//                .post("/placeOrder")
//                .description("Place an order")
//                .outType(String.class)
//                .type(Transaction.class)
//                .route()
//                .removeHeaders("CamelHttp*")
//                .saga()
//                .option("tnx", simple("${body}"))
//                .compensation("direct:cancel-transaction")
//                .completion("direct:complete-transaction")
//                .timeout(2, TimeUnit.MINUTES)
//                .multicast()
//                .parallelProcessing()
//                .to("direct:update-account")
//                .to("direct:add-transaction")
//                .end();
//        rest("/carts")
//                .put("/{id}/updateCartStatus")
//                .consumes("application/json")
//                .to("http://localhost:8083/carts/{id}/ordered");HHHHHHHH

//        from("direct:orderFullFilled")
//                .log("order full filled: ${body}")
//                .to("http://localhost:8083/carts/"+ UUID.randomUUID()+"/ordered")
//                .onCompletion()
//                .setBody();

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
                //.serviceCall(CART_SERVICE_NAME + "/carts/orderFullFilled")
                .to("http://localhost:8083/carts/orderFullFilled")
                //.onCompletion()
                    //.onCompleteOnly()
                        //.to("direct:cart-status-updated")
                    //.onFailureOnly()
                        //.to("direct:rollback-order")
                .convertBodyTo(String.class)
                .log("End of direct:update-account with body: ${body}");

//        from("direct:cartUpdatedWithOrderDetails")
//                .log("Start of direct:cartUpdatedWithOrderDetails with body: ${body}")
//                .removeHeader("CamelHttp*")
//                .removeHeader(Exchange.HTTP_PATH)
//                .removeHeader(Exchange.HTTP_URI)
//                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
//                .serviceCall(CART_SERVICE_NAME + "/carts/"+ UUID.randomUUID()+"orderFullFilled")
//                .onCompletion()
//                .onCompleteOnly()
//                .to("direct:cart-status-updated")
//                .onFailureOnly()
//                .to("direct:rollback-order")
//                .convertBodyTo(String.class)
//                .log("End of direct:update-account with body: ${body}");
//
//        from("direct:rollback-order")
//                .log("Start of direct:rollback-order with body: ${body}")
//                .removeHeader("CamelHttp*")
//                .removeHeader(Exchange.HTTP_PATH)
//                .removeHeader(Exchange.HTTP_URI)
//                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
//                .serviceCall(ORDER_MGMT_DISCOVER_REGISTRY_SERVICE_NAME + "/orders/"+ UUID.randomUUID()+"orderFullFilled")
//                .onCompletion()
//                .onCompleteOnly()
//                .to("direct:cart-status-updated")
//                .onFailureOnly()
//                .to("direct:rollback-order")
//                .convertBodyTo(String.class)
//                .log("End of direct:update-account with body: ${body}");
    }
}
