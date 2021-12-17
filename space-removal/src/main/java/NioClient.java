import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class NioClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Определяем сокет сервера
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",
                23334);

        try (final SocketChannel socketChannel = SocketChannel.open()) {
            // подключаемся к серверу
            socketChannel.connect(socketAddress);
            socketChannel.configureBlocking(false);
            System.out.println("socketChannel.isBlocking()==" + socketChannel.isBlocking());

            // Определяем буфер для получения и отправки данных
            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 25);
            ByteBuffer outputBuffer;// = ByteBuffer.allocate(2 << 25);

            String msg;
            System.out.println("Начало...");
            msg = readTextFile("space-removal//largetextfile");
            System.out.println("Прочитали текстовый файл в переменную. Длина строки: " + msg.length());

            //помещаем считанную строку в выходной байтовый буффер
            outputBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
            System.out.println("Поместили строку в inputbuffer. Размер данных inputbuffer " + outputBuffer.remaining());

            int writtenBytes = socketChannel.write(outputBuffer);
            System.out.println("Запустили запись выходного буфера в выходной канал");
            System.out.println(outputBuffer.remaining());
            System.out.println("Записано байт: " + writtenBytes);

            scanner.nextLine();
            int bytesCount = socketChannel.read(inputBuffer);

            System.out.println(new String(inputBuffer.array(), 0, bytesCount,
                    StandardCharsets.UTF_8).trim());
            inputBuffer.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readTextFile (String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
           //чтение построчно
            StringBuilder stringBuffer = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
            return stringBuffer.toString();
        } catch (IOException ex) {
            System.out.println("Ошибка ввода-вывода" + ex.getMessage());
            return null;
        }
    }
}
