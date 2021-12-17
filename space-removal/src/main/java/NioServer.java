import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NioServer {
    public static void main(String[] args) throws IOException {
        // Занимаем порт, определяя серверный сокет
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost", 23334));

        while (true) {
            // Ждем подключения клиента и получаем потоки для дальнейшей работы
            try (SocketChannel socketChannel = serverChannel.accept()) {
                //socketChannel.configureBlocking(false);
                System.out.println("socketChannel.isBlocking()==" + socketChannel.isBlocking());
                System.out.println("Подключился клиент");
                // Определяем буфер для получения данных
                final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 25);

                // читаем данные из канала в буфер
                int bytesCount = socketChannel.read(inputBuffer);
                System.out.println("bytesCount входного буфера == " + bytesCount);

                // если из потока читать нельзя, перестаем работать с этим клиентом
                if (bytesCount == -1) {
                    System.out.println("Из потока читать нельзя bytesCount == -1");
                    break;
                }

                // получаем переданную от клиента строку в нужной кодировке и очищаем буфер
                final String msg = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8);
                System.out.println("Строка зачитана из буфера. Длинна: " + msg.length());
                inputBuffer.clear();
                System.out.println("Очищен буфер");

                // обрабатываем зачитанную строку и отправляем ее клиенту
                final String processedMsg = msg.replaceAll("\\s+", "");
                System.out.println("Получена обработанная строка без пробелов. Длинна: " + processedMsg.length());

                final ByteBuffer outputBuffer = ByteBuffer.wrap((processedMsg).getBytes(StandardCharsets.UTF_8));
                System.out.println("Строка помещена в выходной буфер. Длина: " + outputBuffer.remaining());

                socketChannel.write(outputBuffer);
                System.out.println("Выходной буфер начал записываться в выходной канал");

                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();

            } catch (IOException err) {
                System.out.println(err.getMessage());
            }
        }
    }

}
