package cn.yanss.m.cookhouse.api.websocket.link;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class Global {
    public final static ConcurrentHashMap<String, Channel> channel = new ConcurrentHashMap<>(32);

    private Global() {
    }

    public static void put(String id, Channel socketChannel) {
        channel.put(id, socketChannel);
    }

    public static Channel get(String id) {
        return channel.get(id);
    }

    public static void remove(Channel nioSocketChannel) {
        channel.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> channel.remove(entry.getKey()));
    }

    public static boolean exists(String id) {
        if (null == get(id)) {
            return false;
        } else {
            return get(id).isActive();
        }
    }
}
