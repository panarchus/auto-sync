package realm.every.io.autosync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import realm.every.io.autosync.actions.Action;

public class Server implements Runnable {

    public final Path localRepo;
    public final Path remoteRepo;
    public final String remotePrefix;
    public final int port;

    private boolean running = true;
    private Thread myThread;

    public final Map<String, DirWatcher> dMap;

    public Server(Path localRepo, Path remoteRepo, String remote, int port) {
        this.localRepo = localRepo;
        this.remoteRepo = remoteRepo;
        this.remotePrefix = remote + ":/";
        this.port = port;
        this.dMap = new HashMap<>();
    }

    @Override
    public void run() {
        myThread = Thread.currentThread();
        try {
            // Setup server
            Selector selector = Selector.open();
            ServerSocketChannel socket = ServerSocketChannel.open();
            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            socket.bind(addr);
            socket.configureBlocking(false);

            socket.register(selector, socket.validOps(), null);

            while (running) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (SelectionKey k : keys) {
                    if (k.isAcceptable()) {
                        SocketChannel client = socket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    } else if (k.isReadable()) {
                        SocketChannel client = (SocketChannel)k.channel();
                        Action action = Action.decodeAndCreateAction(
                                ChannelIOHelper.readFromSocket(client));
                        try {
                            action.runAction(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                            writeToClient(client, false, e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        shutdown();
    }

    private void writeToClient(SocketChannel client, boolean success, Exception e) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(success?1:0);
        if (!success) {
            if (e != null) {
                baos.write(e.getMessage().getBytes());
            } else {
                baos.write("Unknown failure. Check server log.".getBytes());
            }
        }
        client.write(ByteBuffer.wrap(baos.toByteArray()));
    }

    private void shutdown() {
        for (Entry<String, DirWatcher> e : dMap.entrySet()) {
            e.getValue().stop();
        }

        for (Entry<String, DirWatcher> e : dMap.entrySet()) {
            try {
                e.getValue().getRunningThread().join();
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * Computes the absolute remote path that corresponds with the
     * given absolute local path
     * @param absoluteLocalPath local
     * @return remote as String (can't be path if not on this filesystem)
     */
    public String computeRemotePathFromLocal(Path absoluteLocalPath) {
        Path p = localRepo.relativize(absoluteLocalPath);
        return remoteRepo.resolve(p).toString();
        /*
        String local = absoluteLocalPath.toString();
        if (local.contains(localRepoString)) {
            return remoteRepoString +
                    local.substring(
                            local.indexOf(localRepoString) +
                            localRepoString.length());
        }
        */
    }

    public String getRemotePrefix() {
        return remotePrefix;
    }

    public synchronized void stop() {
        running = false;
        myThread.interrupt();
    }
}
