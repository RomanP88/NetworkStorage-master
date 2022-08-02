package ru.gb.storage.client.services;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import javafx.application.Platform;
import ru.gb.storage.client.Client;
import ru.gb.storage.client.handlers.ClientHandler;
import ru.gb.storage.client.services.interfaces.NetworkService;
import ru.gb.storage.commons.handlers.JsonDecoder;
import ru.gb.storage.commons.handlers.JsonEncoder;
import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.FileResponseMessage;
import ru.gb.storage.commons.messages.Message;

import java.io.IOException;

public class NetworkServiceImpl implements NetworkService {
    private Channel channel;
    private final NioEventLoopGroup group = new NioEventLoopGroup(1);
    private boolean connected;
    private Client client;

    public NetworkServiceImpl(Client client) {
        this.client = client;
    }

    @Override
    public void start() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                new LengthFieldPrepender(3),
                                new StringDecoder(),
                                new StringEncoder(),
                                new JsonDecoder(),
                                new JsonEncoder(),
                                new ClientHandler(client)
                        );
                    }
                });
        System.out.println("Client started");
        channel = bootstrap.connect("localhost", 9000).sync().channel();
    }

    @Override
    public void stop() {
        if (connected) {
            channel.close();
        }
        group.shutdownGracefully();
        connected = false;
    }

    @Override
    public void send(Message msg) {
        channel.writeAndFlush(msg);
    }

    public void send(FileTransferHelper helper) throws IOException {
        FileResponseMessage response = helper.getNextPart();
        double percent = ((double) response.getCurrentPart() * 100) / response.getAllParts();
        Platform.runLater(() -> client.getMainController().showProgressBar(response.getFilename(), percent));
        channel.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
            if (helper.hasNextPart()) {
                send(helper);
            } else {
                helper.close();
            }
        });
    }
}
