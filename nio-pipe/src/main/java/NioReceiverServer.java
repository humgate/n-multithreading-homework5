import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioReceiverServer {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;
    static final int BUFFER_SIZE = 2 << 20;

    public static void main(String[] args) throws IOException {
        // Занимаем порт, определяя серверный сокет
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(IP_ADDRESS, PORT));
        System.out.println("Сервер запущен...");

        while (true) {
            // Ждем подключения клиента и получаем потоки для дальнейшей работы
            try (SocketChannel socketChannel = serverChannel.accept()) {

                //пробуем как будет себя вести сервер при true и false
                socketChannel.configureBlocking(true);

                System.out.println("Подключился клиент...");

                // Определяем буфер для получения данных
                final ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);

                //счетчик полного количества считанных из канала байт
                int totalRead = 0;

                while (socketChannel.isConnected()) {
                    /*
                     * Если мы установили параметр socketChannel.configureBlocking(true), то на этом месте поток main
                     * заблокируется до тех пор, пока полностью не зачитает из канала данные в буфер.
                     * Если же, мы установили параметр socketChannel.configureBlocking(false), то в этом месте поток
                     * запросит канал выполнить чтение и сразу побежит дальше. А канал зачитает только какую-то часть
                     * данных из буфера, возможно 0 и передвинет указатель записи буфера на первый свободный его байт.
                     */
                    int bytesCount = socketChannel.read(inputBuffer);

                    System.out.println("Прочитано байт от клиента во входной буфер " + bytesCount);

                    // если в потоке больше нечего читать, перестаем работать с этим клиентом
                    if (bytesCount == -1) {
                        System.out.println("Из потока нечего читать bytesCount == -1");
                        break;
                    }

                    /*
                     * Переносим данные от клиента из буфера в строку msg в нужной кодировке.
                     * В этом примере строка msg в данном месте и есть кусок полезной информации,
                     * которую передает NioSenderClient, зачитанный в текущей попытке чтения.
                     */
                    final String msg = new String(inputBuffer.array(), 0, bytesCount,
                            StandardCharsets.UTF_8);

                    /*
                     * Здесь хорошо вида суть Non-blocking I/0:
                     * c этим куском строки msg уже можно что-то делать, не дожидаясь пока скачается вся строка.
                     * Например, если у сервера стоит задача обрабатывать строку, скажем убирать все пробелы,
                     * то можно уже в этом месте, удалять пробелы из полученной части строки и складывать
                     * в обработанный кусок (StringBuffer)
                     */
                    final String processedMsg = msg.replaceAll("\\s+", "");

                    /*
                     * Текстовый файл, который мы используем в данном примере, большой около 10М и сделан так,
                     * что каждые 10К его содержание меняется. Для наглядности, чтобы было понятно какая часть этого
                     * большого файла в данный момент зачитывается и обрабатывается, мы будем выводить длинну куска,
                     * а так же несколько (10) символов из начала куска и несколько символов (10) из конца куска.
                     * Плюс накопительным итогом будем считать и выводить общую длинну обработанных байт
                     * к данному моменту
                     */
                    System.out.println("Строка зачитана из буфера." +
                            " Длина: " + msg.length() +
                            " Начало: " + (msg.isEmpty() ? "" : msg.substring(0, 10) +
                            " Окончание: " + (msg.isEmpty() ? "" : msg.substring(msg.length()-10)) +
                            " Суммарно зачитано: " + (totalRead+=msg.length())));

                    /*
                     * Поскольку мы после каждой операции чтения в буфер, мы переносим из буфера в строку
                     * не весь буфер, а только часть данных от начала буфера до числа, равного зачитанному
                     * во время этой операции кол-ву байт, нам нужно очистить буфер после получения из него строки,
                     * для того чтобы при следующей операции переноса из буфера в строку перенеслась именно
                     * та часть стоки, которая будет зачитана во время следующей итерации чтения в буфер.
                     */
                    inputBuffer.clear();
                    System.out.println("Очищен буфер");

                    /*
                     * Ждать здесь в принципе не нужно. В случае если мы установили выше
                     * socketChannel.configureBlocking(false) то, при нулевом ожидании будет видно как много
                     * попыток чтения делается с результатом чтения 0.
                     */
                    Thread.sleep(3);
                }
            } catch (IOException err) {
                System.out.println(err.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
