/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing.vertx.sockjs;

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
   private BridgeOptions options;

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
      MessageConsumer<JsonObject> p = bus.consumer("mobilein",
            h -> {
               System.out.println(h.body());

               //bus.publish("mobileout", jo);
               JsonObject reply = new JsonObject();
               reply.put("replay", "You guy my guy received");
               h.reply(reply);
            });

      MessageConsumer<JsonObject> pl = bus.consumer("webin",
            h -> {
               JsonObject jo = h.body();

               System.out.println(h);
               bus.publish("webout", jo);
            });

   }

   private SockJSHandler eventBusHandler() {
      options = new BridgeOptions()
            .addOutboundPermitted(new PermittedOptions().setAddress("webout"))
            .addOutboundPermitted(new PermittedOptions().setAddress("mobileout"))
            .addInboundPermitted(new PermittedOptions().setAddress("webin"))
            .addInboundPermitted(new PermittedOptions().setAddress("mobilein"));
      return SockJSHandler.create(vertx).bridge(options, event -> {
         if (null != event.type()) {
            switch (event.type()) {
               case SOCKET_CREATED:
                  System.out.println("Created");
                  break;
               case RECEIVE:
                  System.out.println(event.getRawMessage());
                  break;
               case SEND:
               case PUBLISH:
                  System.out.println(event.type());
                  JsonObject rawMessage = event.getRawMessage();
                  System.out.println(rawMessage.encodePrettily());
                  break;
               default:
                  break;
            }
         }

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
