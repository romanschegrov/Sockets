import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by ramon on 15.09.2016.
 */
public class Server extends Thread {

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(Integer.valueOf(args[0]))) {
            System.out.println("Сервер " + server);
            File rootDir = getDir();
            System.out.println("Каталог определен " + rootDir.toPath().toAbsolutePath().normalize().toString());
            while (true) {
                Socket client = server.accept();
                System.out.println("Клиент " + client);
                new Server(client, rootDir).start();
            }
        }
    }

    private Socket socket;
    private File rootDir;
    private InputStream inputStream;
    private Scanner scanner;
    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;
    private PrintWriter printWriter;

    public Server(Socket socket, File rootDir) throws IOException {
        this.socket = socket;
        this.rootDir = rootDir;
        inputStream = socket.getInputStream();
        scanner = new Scanner(inputStream);
        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);
        printWriter = new PrintWriter(outputStream, true);
    }

    private static File getDir() {
        File file = null;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите рабочий каталог сервера (Например C:\\abc или ./abc или ../../abc):");
        while (scanner.hasNextLine()) {
            String pathname = scanner.nextLine();
            file = new File(pathname);
            if (file.exists() && file.isDirectory()) break;
            System.out.print("Ошибка при вводе каталога '" + pathname + "'. Повторите ввод:");
        }
        return file;
    }

    @Override
    public void run() {

        try {
            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine();
                if (cmd.length() < 2) writeText("Ошибка команды '" + cmd + "'");
                else {
                    String flag = cmd.substring(0, 2);
                    if (flag.equalsIgnoreCase("-F")) {
                        if (cmd.length() < 4) writeText("Ошибка команды '" + cmd + "'");
                        else {
                            String pathname = cmd.substring(3);
                            File file = new File(pathname);
                            if (file.exists() && !file.isDirectory()) {
                                printWriter.println("file");
                                String cmdLength = scanner.nextLine();
                                if (cmdLength.equals("get length")) {
                                    dataOutputStream.writeLong(file.length());
                                    String cmdFileGo = scanner.nextLine();
                                    if (cmdFileGo.equals("go file")) {
                                        try (FileInputStream fis = new FileInputStream(file)) {
                                            System.out.print("Сохраняем файл в поток");
                                            int length = 0;
                                            byte[] buffer = new byte[1024];
                                            while ((length = fis.read(buffer)) != -1) {
                                                System.out.print(".");
                                                outputStream.write(buffer, 0, length);
                                            }
                                            outputStream.flush();
                                            System.out.println("Готово\nСохранили файл " + file);
                                        }
                                    }
                                }
                            } else {
                                writeText("Файл '" + pathname + "' не найден");
                            }
                        }
                    } else if (flag.equalsIgnoreCase("-L")) {
                        StringBuilder builder = new StringBuilder();
                        outputList(rootDir, builder);
                        writeText(builder.toString());
                        System.out.println("Показали список файлов " + socket);
                    } else if (flag.equalsIgnoreCase("-H")) {
                        writeText(outputHelp());
                        System.out.println("Показали справку " + socket);
                    } else {
                        writeText("Ошибка команды '" + cmd + "'");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeText(String text){
        printWriter.println("text");
        String cmdText = scanner.nextLine();
        if (cmdText.equals("go text")) {
            printWriter.println(text);
        }
    }

    private void outputList(File dir, StringBuilder builder) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory())
                outputList(file, builder);
            else
                builder.append("\t" + file + "\n");
        }
    }

    private String outputHelp() {
        StringBuilder builder = new StringBuilder();
        builder.append("|----------------------------------------------|");
        builder.append("\n|\tСправка                                |");
        builder.append("\n|----------------------------------------------|");
        builder.append("\n|\t-h Показать справку                    |");
        builder.append("\n|\t-l Показать список файлов              |");
        builder.append("\n|\t-f:filepath-with-filename Скачать файл |");
        builder.append("\n|\t-e Выйти                               |");
        builder.append("\n|----------------------------------------------|");
        return builder.toString();
    }
}
