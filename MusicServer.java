package AppendixA;

import java.io.*;
import java.net.*;
import java.util.*;


public class MusicServer {
    ArrayList clientOutputStreams;//List接口的可调整大小的阵列实现。

    public static void main(String[] args) {
        new MusicServer().go();
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket sock;

        public ClientHandler(Socket clientSOcket) {
            try {
                sock = clientSOcket;
                in = new ObjectInputStream(sock.getInputStream());
                //ObjectInputStream对先前使用ObjectOutputStream编写的原始数据和对象进行反序列化。

            } catch (Exception ex) {
                ex.printStackTrace();//将此throwable及其回溯打印到标准错误流。
            }
        }

        public void run() {
            Object o1;//类Object是类层次结构的根。 每个class都有Object作为超类。 所有对象（包括数组）都实现此类的方法。
            Object o2;
            try {
                while ((o1 = in.readObject()) != null) {//从ObjectInputStream中读取一个对象。
                    o2 = in.readObject();
                    System.out.println("read two objects");
                    tellEveryone(o1, o2);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void go() {
        clientOutputStreams = new ArrayList();
        try {
            ServerSocket serverSock = new ServerSocket(4242);
            while (true) {
                Socket clientSocket = serverSock.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                //ObjectOutputStream将Java对象的原始数据类型和图形写入OutputStream。
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("got a connection");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tellEveryone(Object one, Object two) {
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                ObjectOutputStream out = (ObjectOutputStream) it.next();
                out.writeObject(one);//将指定的对象写入ObjectOutputStream。
                out.writeObject(two);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
