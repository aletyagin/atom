package ru.atom.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPool {
    private final static Logger log = LogManager.getLogger(ConnectionPool.class);
    private static final int PARALLELISM_LEVEL = 4;
    private static ConcurrentHashMap<Session, Player> pool = new ConcurrentHashMap<>();

    public static void send(@NotNull Session session, @NotNull String msg) {
        if (session.isOpen()) {
            try {
                session.getRemote().sendString(msg);
            } catch (IOException ignored) { }
        }
    }

    public static void broadcast(@NotNull String msg) {
        pool.forEachKey(PARALLELISM_LEVEL, session -> send(session, msg));
    }

    public static void shutdown() {
        pool.forEachKey(PARALLELISM_LEVEL, session -> {
            if (session.isOpen()) {
                session.close();
            }
        });
    }

    public static Player get(Session session) {
        return pool.get(session);
    }

    public static void add(Session session, Player player) {
        if (pool.putIfAbsent(session, player) == null) {
            log.info("{} joined", player.getName());
        }
    }

    public static void remove(Session session) {
        pool.remove(session);
    }
}
