/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package africa.finserve.sockjs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author steve-nganga
 */
public class NewClass extends AbstractVerticle {

    private EventBus bus;

    @Override
    public void start() {

        Router router = Router.router(vertx);

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Access-Control-Allow-Origin");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.DELETE);

        router.route().handler(io.vertx.ext.web.handler.CorsHandler.create("*")
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods));
        router.route("/eventbus/*").handler(eventBusHandler());
        router.route().handler(BodyHandler.create());
        router.route().handler(staticHandler());

        router.route().failureHandler(errorHandler());

        vertx.createHttpServer().requestHandler(router::accept).listen(9999);
        bus = vertx.eventBus();
        MessageConsumer<JsonObject> p = bus.consumer("in",
                h -> {
                    JsonObject jo = h.body();
                    System.out.println(h);
                    bus.publish("out", jo);
                });
        
        MessageConsumer<JsonObject> pl = bus.consumer("in2",
                h -> {
                    JsonObject jo = h.body();
                    jo.put("oga", Boolean.FALSE);
                    System.out.println(h);
                    bus.publish("out2", jo);
                });


    }

    private SockJSHandler eventBusHandler() {
        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("out"))
                .addOutboundPermitted(new PermittedOptions().setAddress("out2"))
                .addInboundPermitted(new PermittedOptions().setAddress("in"))
                .addInboundPermitted(new PermittedOptions().setAddress("in2"));
        return SockJSHandler.create(vertx).bridge(options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.println("Created");

            } else if (event.type() == BridgeEventType.RECEIVE) {
                System.out.println(event.getRawMessage());

            } else if (event.type() == BridgeEventType.SEND || event.type() == BridgeEventType.PUBLISH) {
                System.out.println(event.type());
                JsonObject rawMessage = event.getRawMessage();
                rawMessage.put("address_", "one");

            }
            System.out.println(event.type());
            event.complete(true);

        }
        );
    }

    private Router
            auctionApiRouter() {

        Router router
                = Router
                        .router(vertx
                        );
        router
                .route().handler(BodyHandler
                        .create());

        router
                .route().consumes("application/json");
        router
                .route().produces("application/json");

        return router;

    }

    private ErrorHandler
            errorHandler() {
        return ErrorHandler
                .create(true);

    }

    private StaticHandler
            staticHandler() {
        return StaticHandler
                .create()
                .setCachingEnabled(false);
    }

}
