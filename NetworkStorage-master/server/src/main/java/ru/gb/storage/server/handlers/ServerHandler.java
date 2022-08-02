package ru.gb.storage.server.handlers;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.helpers.FileInfo;
import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.*;
import ru.gb.storage.commons.messages.DeleteErrorMessage;
import ru.gb.storage.commons.messages.FileNotFoundMessage;
import ru.gb.storage.commons.messages.RenameErrorMessage;
import ru.gb.storage.commons.messages.AuthResponseMessage;
import ru.gb.storage.commons.messages.FileListResponseMessage;
import ru.gb.storage.commons.messages.FileResponseMessage;
import ru.gb.storage.commons.messages.RegisterResponseMessage;
import ru.gb.storage.server.services.AuthServiceImpl;
import ru.gb.storage.server.services.RegisterServiceImpl;
import ru.gb.storage.server.services.interfaces.AuthService;
import ru.gb.storage.server.services.interfaces.RegisterService;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private String username;
    private Path currentDir;
    private Path homeDir;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (username != null) {
            System.out.println(username + " disconnected");
        } else {
            System.out.println("Unknown user disconnected");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message instanceof RegisterRequestMessage) {
            processRegisterMessage(ctx, (RegisterRequestMessage) message);
        }
        if (message instanceof AuthRequestMessage) {
            processAuthMessage(ctx, (AuthRequestMessage) message);
        }
        if (message instanceof FileRequestMessage) {
            processFileRequestMessage(ctx, (FileRequestMessage) message);
        }
        if (message instanceof FileResponseMessage) {
            processFileResponseMessage(ctx, (FileResponseMessage) message);
        }
        if (message instanceof FileListRequestMessage) {
            processFileListRequestMessage(ctx);
        }
        if (message instanceof ChangeDirectoryMessage) {
            processChangeDirectoryMessage(ctx, (ChangeDirectoryMessage) message);
        }
        if (message instanceof CreateDirectoryMessage) {
            processCreateDirectoryMessage(ctx, (CreateDirectoryMessage) message);
        }
        if (message instanceof DeleteItemMessage) {
            processDeleteItemMessage(ctx, (DeleteItemMessage) message);
        }
        if(message instanceof FileRenameMessage){
            processFileRenameMessage(ctx, (FileRenameMessage) message);
        }
    }

    private void processFileRenameMessage(ChannelHandlerContext ctx, FileRenameMessage message) throws IOException {
        Path newPath = Paths.get(homeDir.toString(), message.getNewPath());
        Path oldPath = Paths.get(homeDir.toString(), message.getOldPath());
        if(Files.notExists(newPath) && !newPath.equals(oldPath)){
            try {
                Files.move(oldPath, newPath);
            } catch (IOException e) {
                ctx.writeAndFlush(new RenameErrorMessage("Ошибка переименования файла " + oldPath.getFileName()));
                e.printStackTrace();
            }
        }
        ctx.writeAndFlush(getFileListResponseMessage());
    }

    private void processDeleteItemMessage(ChannelHandlerContext ctx, DeleteItemMessage message) throws IOException {
        Path deleteItem = Paths.get(homeDir.toString(), message.getPathToDeleteItem());
        if(!deleteFiles(deleteItem.toAbsolutePath())){
            ctx.writeAndFlush(new DeleteErrorMessage("Не удалось удалить файл или директорию"));
        }
        ctx.writeAndFlush(getFileListResponseMessage());
    }

    private void processCreateDirectoryMessage(ChannelHandlerContext ctx, CreateDirectoryMessage message) throws IOException {
        Path newDir = Paths.get(homeDir.toString(), message.getPathToCreate());
        Files.createDirectory(newDir);
        ctx.writeAndFlush(getFileListResponseMessage());
    }

    private void processChangeDirectoryMessage(ChannelHandlerContext ctx, ChangeDirectoryMessage message) throws IOException {
        currentDir = Paths.get(homeDir.toString(), message.getDirName());
        ctx.writeAndFlush(getFileListResponseMessage());
    }

    private void processFileListRequestMessage(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(getFileListResponseMessage());
    }

    private FileListResponseMessage getFileListResponseMessage() throws IOException {
        FileListResponseMessage response = new FileListResponseMessage();
        response.setFileList(Files.list(currentDir).map(FileInfo::new).collect(Collectors.toList()));
        if (currentDir.getNameCount() > homeDir.getNameCount()) {
            response.setPath("\\" + currentDir.subpath(homeDir.getNameCount(), currentDir.getNameCount()));
        } else {
            response.setPath("\\");
        }
        return response;
    }

    private void processRegisterMessage(ChannelHandlerContext ctx, RegisterRequestMessage rrm) {
        RegisterResponseMessage response = new RegisterResponseMessage();
        RegisterService registerService = new RegisterServiceImpl();
        if (registerService.checkExistLogin(rrm.getLogin())) {
            response.setStatus(MessageStatus.ALREADY_EXISTS);
            response.setText("User with login `" + rrm.getLogin() + "' already exist");
        } else {
            registerService.register(rrm.getLogin(), rrm.getPassword());
            response.setStatus(MessageStatus.SUCCESS);
            response.setText("User with login `" + rrm.getLogin() + "' success register");
            username = rrm.getLogin();
            homeDir = Paths.get("files", username).toAbsolutePath();
            currentDir = Paths.get(String.valueOf(homeDir));
            createUserFolder();
        }
        ctx.writeAndFlush(response);
    }

    private void processAuthMessage(ChannelHandlerContext ctx, AuthRequestMessage arm) {
        AuthService authService = new AuthServiceImpl();
        AuthResponseMessage response = new AuthResponseMessage();
        if (authService.authenticate(arm.getLogin(), arm.getPassword())) {
            response.setStatus(MessageStatus.SUCCESS);
            response.setText("Authenticate successful");
            username = arm.getLogin();
            homeDir = Paths.get("files", username).toAbsolutePath();
            currentDir = Paths.get(String.valueOf(homeDir));
            createUserFolder();
        } else {
            response.setStatus(MessageStatus.ERROR);
            response.setText("Wrong login or password");
        }
        ctx.writeAndFlush(response);
    }

    private void processFileRequestMessage(ChannelHandlerContext ctx, FileRequestMessage message) throws IOException {
        Path file = currentDir.resolve(message.getFilename());
        if (Files.notExists(file)) {
            ctx.writeAndFlush(new FileNotFoundMessage("File " + file.getFileName() + " not found"));
        } else {
            sendFile(ctx, new FileTransferHelper(file, message.getPathToSave()));
        }
    }

    private void createUserFolder() {
        if(Files.notExists(homeDir)){
            try {
                Files.createDirectories(homeDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void processFileResponseMessage(ChannelHandlerContext ctx, FileResponseMessage message) throws IOException {
        Path newFile = Paths.get(homeDir.toString(), message.getPathToSave(), message.getFilename());
        try (RandomAccessFile file = new RandomAccessFile(newFile.toFile(), "rw")) {
            file.seek(message.getStartPosition());
            file.write(message.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message.getCurrentPart() == message.getAllParts()) {
            processFileListRequestMessage(ctx);
        }
    }

    private boolean deleteFiles(Path pathToDelete) throws IOException {
        if(Files.isDirectory(pathToDelete)) {
            for (Path file : Files.list(pathToDelete).collect(Collectors.toList())) {
                if (Files.isDirectory(file)) {
                    deleteFiles(file.toAbsolutePath());
                } else {
                    Files.deleteIfExists(file);
                }
            }
        }
        return Files.deleteIfExists(pathToDelete);
    }
}


