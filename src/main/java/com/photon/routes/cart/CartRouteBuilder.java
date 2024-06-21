package com.photon.routes.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

//@Component
//@Slf4j
//@RequiredArgsConstructor
public class CartRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        rest("/carts")
                .put("/{id}/updateCartStatus")
                //.log("update cart status ${body}")
                .consumes("application/json")
                .to("");
    }
}
