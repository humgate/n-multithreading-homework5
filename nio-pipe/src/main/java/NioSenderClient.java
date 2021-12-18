import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NioSenderClient {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;

    public static void main(String[] args) {
        // Определяем сокет сервера
        InetSocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS,
                PORT);

        try (final SocketChannel socketChannel = SocketChannel.open()) {
            // подключаемся к серверу
            socketChannel.connect(socketAddress);

            /*
            * Неблокирующее взаимодействие false, блокирующее - true.
            * В данном примере разницу в работе хорошо видно. См System.out а так же комментарии
            * ниже по тексту
             */
            socketChannel.configureBlocking(false);
            System.out.println("socketChannel.isBlocking()==" + socketChannel.isBlocking());

            System.out.println("Начало...");
            //читаем большой файл
            String msg = readTextFile("text.txt");
            System.out.println("Прочитали текстовый файл в переменную. Длина строки: " + msg.length());

            //помещаем считанную строку в выходной байтовый буфер
            ByteBuffer outputBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
            System.out.println("Поместили строку в outputBuffer. Размер буфере " + outputBuffer.remaining());

            while (socketChannel.isConnected()) {

                /*
                 * В этот момент выполнения кода хорошо видна разница между блокирующим и неблокирующим IO.
                 * Размер строки, которую мы передаем в этом примере равен 10114000 байт.
                 * Если мы установили параметр socketChannel.configureBlocking(true), то на этом месте поток main
                 * заблокируется до тех пор, пока полностью не запишет содержание буфера 10114000 байт в канал.
                 *
                 * Если же, мы установили параметр socketChannel.configureBlocking(false), то в этом месте поток
                 * запросит канал выполнить запись и сразу побежит дальше. А канал запишет только какую-то часть
                 * данных из буфера, в нашем примере это 2883562 байт и передвинет указатель буфера на первый
                 * незаписанный за эту попытку записи байт в буфере, то есть 2883563-тий
                 */
                int writtenBytes = socketChannel.write(outputBuffer);
                System.out.println("Запустили запись выходного буфера в выходной канал");
                System.out.println("Записано байт: " + writtenBytes);
                System.out.println("Осталось в буфере не записано байт: " + outputBuffer.remaining());

                // если в поток больше нечего писать из буфера, завершаем цикл попыток записи
                if (outputBuffer.remaining() == 0) {
                    System.out.println("Из буфера все записано outputBuffer.remaining() == 0");
                    break;
                }

                /*
                 * Важно, что если мы здесь в нашем потоке main снова вызовем операцию записи в канал
                 * из буфера, то она может ничего не записать в канал, если запись в канал вызванная
                 * во время предыдущего вызова, еще не закончилась. В нашем примере если мы
                 * подождем 2000 мсекунд, то этого уже достаточно, чтобы предыдущая запись закончилась
                 * и начала записываться новая порция.
                 */
                Thread.sleep(2000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * загрузчик текстового файла
     * @param fileName -имя файла
     * @return зачитанная строка или null в случае ошибки
     */
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
