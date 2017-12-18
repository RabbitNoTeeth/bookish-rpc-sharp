package cn.booklish.sharp.remoting.netty4.core

import cn.booklish.sharp.remoting.netty4.api.ClientChannelInOperator
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.apache.log4j.Logger
import java.net.InetSocketAddress
import java.util.concurrent.Semaphore


object Client {

    private val logger: Logger = Logger.getLogger(this.javaClass)

    private val eventLoopGroup = NioEventLoopGroup()
    private val bootstrap = Bootstrap()

    fun init() {
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(ClientChannelInitializer(ClientChannelInOperator()))
    }

    fun newChannel(address: InetSocketAddress):Channel?{
        //设置信号量,最多允许重试3次
        val semaphore = Semaphore(3)
        do {
            try {
                val channelFuture = bootstrap.clone().connect(address).sync()
                return channelFuture.channel()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                //重试
                logger.info("[SharpRpc-client]: 客户端channel连接失败,重新尝试连接...")
            } catch (e: Exception) {
                e.printStackTrace()
                //重试
                logger.info("[SharpRpc-client]: 客户端channel连接失败,重新尝试连接...")
            }
        } while (semaphore.tryAcquire())
       return null
    }

}