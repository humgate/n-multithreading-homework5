import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioServer {
    public static void main(String[] args) throws IOException {
        // Занимаем порт, определяя серверный сокет
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("localhost", 23334));
        System.out.println("Сервер запущен...");

        while (true) {
            // Ждем подключения клиента и получаем потоки для дальнейшей работы
            try (SocketChannel socketChannel = serverChannel.accept()) {
                socketChannel.configureBlocking(false);
                System.out.println("Подключился клиент...");
                // Определяем буфер для получения данных
                final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 25);

                while (socketChannel.isConnected()) {
                    // читаем данные из канала в буфер
                    int bytesCount = socketChannel.read(inputBuffer);
                    System.out.println("Прочитано байт от клиента во входной буфер " + bytesCount);

                    // если в потоке больше нечего читать, перестаем работать с этим клиентом
                    if (bytesCount == -1) {
                        System.out.println("Из потока нечего читать bytesCount == -1");
                        break;
                    }

                    // переносим данные клиента из буфера в строку в нужной кодировке
                    final String msg = new String(inputBuffer.array(), 0, bytesCount,
                            StandardCharsets.UTF_8);
                    System.out.println("Строка зачитана из буфера. Длина: " + msg.length());

                    //очищаем буфер
                    inputBuffer.clear();
                    System.out.println("Очищен буфер");

                    // обрабатываем зачитанную строку
                    final String processedMsg = msg.replaceAll("\\s+", "");
                    System.out.println("Получена обработанная строка без пробелов. Длинна: " + processedMsg.length());

                    //заносим строку в выходной буфер
                    final ByteBuffer outputBuffer = ByteBuffer.wrap((processedMsg).getBytes(StandardCharsets.UTF_8));
                    System.out.println("Строка помещена в выходной буфер. Длина: " + outputBuffer.remaining());

                    //пишем из буфера в канал
                    bytesCount = socketChannel.write(outputBuffer);
                    System.out.println("Записано в выходной канал байт: " + bytesCount);

                    Thread.sleep(1);
                }
            } catch (IOException err) {
                System.out.println(err.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
