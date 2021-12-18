import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class NioClient {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;
    static final int BUFFER_SIZE = 2 << 20;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Определяем сокет сервера
        InetSocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS, PORT);

        try (final SocketChannel socketChannel = SocketChannel.open()) {
            // подключаемся к серверу
            socketChannel.connect(socketAddress);
            //неблокирующее взаимодействие, в данном примере разницы не увидеть
            //socketChannel.configureBlocking(false);
            System.out.println("socketChannel.isBlocking()==" + socketChannel.isBlocking());

            // Определяем буфер для получения и отправки данных
            final ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            ByteBuffer outputBuffer;

            Thread readerThread = new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                int bytesCount = 0;
                try {
                    bytesCount = socketChannel.read(inputBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(threadName + ". Запустили чтение входного буфера во входной канал");
                System.out.println(threadName + ". Зачитано байт: " + bytesCount);
                System.out.println(threadName + ". Обработанная сервером строка: " +
                        new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim());

                inputBuffer.clear();
            }, "readerThread");

            readerThread.start();

            while (true) {
                System.out.println("Введите строку...");
                String msg = scanner.nextLine();
                if ("end".equals(msg)) break;

                //помещаем считанную строку в выходной байтовый буфер
                outputBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));

                //пишем в канал
                int writtenBytes = socketChannel.write(outputBuffer);
                System.out.println("Запустили запись выходного буфера в выходной канал");
                System.out.println("Записано байт: " + writtenBytes);

                Thread.sleep(1);
            }
            readerThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
