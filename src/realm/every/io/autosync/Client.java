package realm.every.io.autosync;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import realm.every.io.autosync.actions.Action;

public class Client implements Runnable {

    final int port;
    final Action action;

    public Client(int port, Action action) {
        this.port = port;
        this.action = action;
    }

    @Override
    public void run() {
        InetSocketAddress addr = new InetSocketAddress("localhost", port);
        try {
            SocketChannel socket = SocketChannel.open(addr);
            socket.write(ByteBuffer.wrap(action.encode()));
            byte[] in = ChannelIOHelper.readFromSocket(socket);
            ByteBuffer bb = ByteBuffer.wrap(in);
            if (bb.getInt() == 0) {
                // report error
                byte[] b = new byte[bb.remaining()];
                bb.get(b);
                System.out.println("ERROR: " + new String(b));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
