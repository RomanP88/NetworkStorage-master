package ru.gb.storage.client.handlers;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import ru.gb.storage.client.Client;
import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message instanceof ErrorMessage) {
            Platform.runLater(() -> client.getMainController().showErrorMessage(((ErrorMessage) message).getErrorMessage()));
        } else {
            if (message instanceof AuthResponseMessage) {
                processAuthMessage((AuthResponseMessage) message);
            }
            if (message instanceof RegisterResponseMessage) {
                processRegisterMessage((RegisterResponseMessage) message);
            }
            if (message instanceof FileResponseMessage) {
                processFileResponseMessage((FileResponseMessage) message);
            }
            if (message instanceof FileRequestMessage) {
                processFileRequestMessage(ctx, (FileRequestMessage) message);
            }
            if (message instanceof FileListResponseMessage) {
                processFileListResponseMessage((FileListResponseMessage) message);
            }
        }

    }

    private void processFileListResponseMessage(FileListResponseMessage message) {
        Platform.runLater(() -> client.getMainController().fillServerTable(message.getFileList(), message.getPath()));
    }

    private void processRegisterMessage(RegisterResponseMessage message) {
        if (message.getStatus() != MessageStatus.SUCCESS) {
            Platform.runLater(() -> client.getLoginController().registerError(message.getText()));
        } else {
            Platform.runLater(() -> client.getLoginController().registerSuccess());
        }
    }

    private void processAuthMessage(AuthResponseMessage message) {
        if (message.getStatus() == MessageStatus.ERROR) {
            Platform.runLater(() -> client.getLoginController().authenticateError(message.getText()));
        } else if (message.getStatus() == MessageStatus.SUCCESS) {
            Platform.runLater(() -> client.getLoginController().authenticateSuccess());
        }
    }

    private void processFileResponseMessage(FileResponseMessage message) {
        Path uploadFile = Paths.get(message.getPathToSave()).resolve(message.getFilename());
        double percent = (((double) message.getCurrentPart() * 100) / message.getAllParts());
        try (RandomAccessFile file = new RandomAccessFile(uploadFile.toFile(), "rw")) {
            file.seek(message.getStartPosition());
            file.write(message.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> client.getMainController().showProgressBar(message.getFilename(), percent));
        if (message.getCurrentPart() == message.getAllParts()) {
            Platform.runLater(() -> client.getMainController().fillClientTable());
        }
    }

    private void processFileRequestMessage(ChannelHandlerContext ctx, FileRequestMessage message) throws IOException {
        Path file = Paths.get(message.getFilename());
        if (file.toFile().exists()) {
            sendFile(ctx, new FileTransferHelper(file, message.getPathToSave()));
        }
    }

    private void sendFile(ChannelHandlerContext ctx, FileTransferHelper helper) throws IOException {
        FileResponseMessage response = helper.getNextPart();
        ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
            if (helper.hasNextPart()) {
                sendFile(ctx, helper);
            } else {
                helper.close();
            }
        });
    }
}
