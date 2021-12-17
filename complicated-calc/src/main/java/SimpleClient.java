import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Простая реализация клиента, который коннектится к серверу,
 * читает ввод пользователя, валидирует его, отправляет его на сервер и отображает результат,
 * возвращаемый сервером.
 */
public class SimpleClient {
    static final String SERVER_ADRESS = "localhost";
    static final short SERVER_PORT = 23444;



    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //Создаем сокет соединения с сервером
        try (Socket socket = new Socket(SERVER_ADRESS, SERVER_PORT);

             //Текстовый писатель в поток вывода сокета соединения с сервером
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

             //Буферизированный текстовый читатель из потока ввода сокета соединения с сервером
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            //читаем ввод пользователя
            Integer value;
            do {
                System.out.println("Введите целое число...");
                value = parseInt(scanner.nextLine());
            } while (value == null);

            //отправляем на сервер, здесь поток блокируется пока все не запишет в стрим
            out.println(value);

            //здесь строка точно отправлена на сервер
            System.out.println("Запрос отправлен на сервер, ожидание результата от сервера...");

            //читаем сообщение от сервера, здесь поток блокируется пока все не считает из стрима
            String response = in.readLine();

            //здесь ответ сервера точно полностью зачитан
            System.out.println("SERVER: " + response);


        } catch (IOException e) {
            System.err.println("Ошибка соединения: " + e.getMessage());
        }
    }

    /**
     * Валидирует и парсит ввод строки пользователя в целое число
     * @param entry ввод пользователя
     * @return результат парсинга или null если ввод некорректный
     */
    public static Integer parseInt(String entry) {
        try {
            return Integer.parseInt(entry);
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод");
            return null;
        }
    }
}
