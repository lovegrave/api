package cn.yanss.m.cookhouse.api.websocket.netty;


import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.websocket.handler.ChildChannelHandler;
import cn.yanss.m.util.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * ClassName:NettyServer 注解式随spring启动
 *
 * @author hl
 */
@Component
public class NettyServer {
    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8888"));

    private final RedisService redisService;

    @Autowired
    public NettyServer(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostConstruct
    public void initNetty() {
        new Thread() {
            @Override
            public void run() {
                try {
                    new NettyServer(redisService).start();
                } catch (SSLException e) {
                    LogUtils.create().methodName("initNetty").message(e.getMessage()).error();
                } catch (CertificateException e) {
                    LogUtils.create().methodName("initNetty").message(e.getMessage()).error();
                } catch (Exception e) {
                    LogUtils.create().methodName("initNetty").message(e.getMessage()).error();
                }
            }
        }.start();
    }

    public void start() throws CertificateException, SSLException {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChildChannelHandler(sslCtx, redisService));
            Channel ch = b.bind(PORT).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            LogUtils.create().methodName("initNetty-->start").message(e.getMessage()).error();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
