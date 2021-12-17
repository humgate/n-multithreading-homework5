import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    final static int CALC_TIME = 4000;

    public static void main(String[] args) throws IOException {
        //создаем серверный сокет на порту 23444, если порт занят падаем с исключением
        ServerSocket serverSocket = new ServerSocket(23444);

        while (true) {
            try (
                    /* сим говорим серверу ждать клиентского подключения, и как только оно поступит
                     * сохранить его сокет соединения на серверной стороне в переменной clientSocket */
                    Socket clientSocket = serverSocket.accept();

                    //Текстовый писатель в поток вывода сокета соединения с клиентом
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    //Буферизированный текстовый читатель из потока ввода сокета соединения с клиентом
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                System.out.println("Создано новое соединение c клиентом");
                final String message = in.readLine();
                System.out.println("От клиента получена строка: " + message);
                int entry = Integer.parseInt(message);
                int result = complicatedCalc(entry);
                out.println(result);
                System.out.println("Рассчитано и отправлено клиенту значение: " + result);
            } catch (IOException e) {
                System.err.println("Ошибка соединения: " + e);
            } catch (NumberFormatException e) {
                System.err.println("Строка переданная клиентом,не может быть преобразована в число: " + e);
            }
        }
    }

    /**
     * Жутко сложные и тайм консьюминг вычисления на сервере.
     * Поскольку целью задания является работа с блокирующими и неблокирующими клиент-серверными
     * типами взаимодействия, содержание данного метода не имеет значения и можно ограничиться
     * простейшей реализацией
     *
     * @param entry - входное значение для вычислений
     * @return - результат вычислений
     */
    public static int complicatedCalc(int entry) {
        try {
            Thread.sleep(CALC_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return entry + 1;
    }
}
