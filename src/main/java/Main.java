
import africa.finserve.sockjs.NewClass;
import io.vertx.core.Vertx;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author steve-nganga
 */
public class Main {
    public static void main(String args[])
    {
        Vertx vertx=Vertx.vertx();
        vertx.deployVerticle(new NewClass());
    }
}
