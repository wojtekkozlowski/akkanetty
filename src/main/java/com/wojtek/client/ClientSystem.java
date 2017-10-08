package com.wojtek.client;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import scala.PartialFunction;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.wojtek.client.ClientSystem.NIO_EVENT_LOOP_GROUP;

public class ClientSystem {

    static final NioEventLoopGroup NIO_EVENT_LOOP_GROUP = new NioEventLoopGroup();

    public static void main(String[] args) {
        new ClientSystem().start();
    }

    private void start() {

        ActorSystem system = ActorSystem.create();

//        IntStream
//                .range(1,10)
//                .mapToObj(i -> system.actorOf(ConnectionActor.props(), "actor"+i))
//                .forEach(a -> a.tell("hello", ActorRef.noSender()));

//        ActorRef actor = system.actorOf(ConnectionActor.props());

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(
                () -> IntStream.range(0,10).forEach(
                        (i) -> system.actorOf(ConnectionActor.props())), 0, 200, TimeUnit.MILLISECONDS);

//        ActorRef throttler = system.actorOf(Props.create(TimerBasedThrottler.class,
//                new Throttler.Rate(2, Duration.create(1, TimeUnit.SECONDS))));

//        throttler.tell(new Throttler.SetTarget(actor), ActorRef.noSender());

//        throttler.tell("1",  ActorRef.noSender());
//        throttler.tell("2",  ActorRef.noSender());
//        throttler.tell("3",  ActorRef.noSender());
//        throttler.tell("4",  ActorRef.noSender());
//        throttler.tell("5",  ActorRef.noSender());
//
//        ActorMaterializer materializer = ActorMaterializer.create(system);
//
//        final ActorRef throttler =
//                Source.actorRef(1000, OverflowStrategy.dropNew())
//                        .throttle(100,  FiniteDuration.create(1, TimeUnit.SECONDS), 10, ThrottleMode.shaping())
//                        .to(Sink.actorRef(actor, NotUsed.getInstance()))
//                        .run(materializer);
//
//        throttler.tell("1", ActorRef.noSender());
//        throttler.tell("2", ActorRef.noSender());
//        throttler.tell("3", ActorRef.noSender());
//        throttler.tell("4", ActorRef.noSender());
//        throttler.tell("5", ActorRef.noSender());


    }
}

class ConnectionActor extends AbstractActor {

    private final Client client;

    private ConnectionActor() {
        client = new Client(NIO_EVENT_LOOP_GROUP);
    }

    static Props props(){
        return Props.create(ConnectionActor.class, ConnectionActor::new);
    }

    public Receive createReceive() {
        client.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush("akka1!\n");

            } else {
                System.out.println("didn't connect");
            }
        });
        return new ReceiveBuilder().build();
    }
}
