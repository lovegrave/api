package cn.yanss.m.cookhouse.api.websocket.handler;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * @author hl
 */
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    private final RedisService redisService;

    public ChildChannelHandler(SslContext sslCtx, RedisService redisService) {
        this.sslCtx = sslCtx;
        this.redisService = redisService;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast("http-codec",
                new HttpServerCodec());
        pipeline.addLast("aggregator",
                new HttpObjectAggregator(65536));
        ch.pipeline().addLast("http-chunked",
                new ChunkedWriteHandler());
        pipeline.addLast("handler",
                new MyWebSocketServerHandler(redisService));
    }
}
