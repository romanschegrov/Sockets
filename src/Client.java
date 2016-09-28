import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by ramon on 15.09.2016.
 */
public class Client {

    public static void main(String[] args) throws IOException { new Client(args[0], args[1]); }

    public Client(String host, String port) throws IOException {
        try (Socket socket = new Socket(host, Integer.valueOf(port));
             InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()
        ) {

            Scanner sis = new Scanner(System.in);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            DataInputStream dis = new DataInputStream(inputStream);
            BufferedReader brs  = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter out = new PrintWriter(outputStream, true);

            System.out.print("Введите команду (введите -h для справки): ");

            while (sis.hasNextLine()) {
                String sysLine = sis.nextLine();

                if (sysLine.equalsIgnoreCase("-E")) break;

                out.println(sysLine);

                String command = brs.readLine();
                if (command.equals("text")){
                    out.println("go text");
                    System.out.println(brs.readLine());
                    while (brs.ready()){
                        System.out.println(brs.readLine());
                    }
                } else if (command.equals("file")){
                    out.println("get length");
                    int dataLength = (int) dis.readLong();
                    out.println("go file");

                    File file = new File(sysLine);
                    File newFile = new File(getDir(),file.getName());

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        System.out.print("Копируем");
                        int length;
                        int all = 0;
                        byte[] buffer = new byte[1024];
                        while (true){
                            length = bis.read(buffer);
                            System.out.print(".");
                            fos.write(buffer, 0, length);
                            all = all + length;
                            if (all >= dataLength) break;
                        }
                        fos.flush();
                        System.out.println("Готово\nФайл скопирован успешно " + newFile.toPath().toAbsolutePath().normalize().toString());
                    }
                }
                System.out.print("Введите команду (введите -h для справки): ");
            }
        }
    }

    private File dir;

    private File getDir() {
        Scanner scanner = new Scanner(System.in);
        if (dir == null) {
            dir = setDir(scanner);
        }
        else {
            System.out.print("Текущий каталог " + dir.toPath().toAbsolutePath().normalize().toString() + ", хотите изменить (y/n):");
//            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("Y")) {
                    dir = setDir(scanner);
                    break;
                } else if (answer.equalsIgnoreCase("N")) {
                    break;
                } else {
                    System.out.print("Текущий каталог " + dir.toPath().toAbsolutePath().normalize().toString() + ", хотите изменить (y/n):");
                }
            }
        }
        return dir;
    }

    private File setDir(Scanner scanner){
        System.out.print("Введите каталог для копирования файла (Например C:\\abc или ./abc или ../../abc):");
//        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String pathname = scanner.nextLine();
            dir = new File(pathname);
            if (dir.exists() && dir.isDirectory()) {
                System.out.println("Каталог установлен " + dir.toPath().toAbsolutePath().normalize().toString());
                break;
            }
            System.out.println("Ошибка при вводе каталога '" + pathname + "'");
        }
        return dir;
    }
}
