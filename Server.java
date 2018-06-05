import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static Map <String, Socket> Astana = new HashMap<>();
    private static Map <String, Socket> Almaty = new HashMap<>();
    private static Vector<String> login = new Vector<>();

    public static class RunServer implements Runnable {

        private Socket clientSocket = null;
        private int connection = 0;
        private int inGroup = 0;
        private String user;
        private String groupName;

        public RunServer(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                while(true) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    String clientMessage;
                    clientMessage = in.readLine();
                    System.out.println("Client:" + clientMessage + " " + Thread.currentThread().getId());
                    if(connection == 1) {
                        if(clientMessage.equals("server exit")) {
                            out.println("Server:You're disconnected.");
                            login.remove(user);
                            if(Astana.containsKey(user)) {
                                Astana.remove(user);
                                inGroup = 0;
                            } else if(Almaty.containsKey(user)) {
                                Almaty.remove(user);
                                inGroup = 0;
                            }
                            connection = 0;
                        } else if(clientMessage.startsWith("server join")) {
                            String[] group = clientMessage.split(" ");
                            if(group.length < 3) {
                                out.println("Server:Please enter the name of group you want to join.");
                            } else if(group.length > 3){
                                out.println("Server:Inappropriate group name!");
                            } else {
                                if(group[2].equals("Almaty")) {
                                    Almaty.put(user, clientSocket);
                                    out.println("Server:You're joined Almaty group now.");
                                    inGroup = 1;
                                    groupName = "Almaty";
                                } else if(group[2].equals("Astana")) {
                                    Astana.put(user, clientSocket);
                                    out.println("Server:You're joined Astana group now.");
                                    inGroup = 1;
                                    groupName = "Astana";
                                } else {
                                    out.println("Server:Such group doesn't exist!");
                                }
                            }
                        } else if(clientMessage.equals("server groupslist")) {

                            String tse = "";
                            String ala = "";

                            for(String first: Astana.keySet()) {
                                tse = tse + " " + first.toString();
                            }

                            for(String second: Almaty.keySet()) {
                                ala = ala + " " + second.toString();
                            }
                            out.println("Server:Astana " + tse + " | Almaty " + ala);
                        } else if(clientMessage.equals("server members")) {
                            if(inGroup == 0) {
                                out.println("Server:You're not in any group.");
                            } else if(inGroup == 1) {
                                if(Astana.containsKey(user)) {
                                    String tse = "";
                                    for(String first: Astana.keySet()) {
                                        tse = tse + " " + first.toString();
                                    }
                                    out.println("Server:" + tse);
                                } else if(Almaty.containsKey(user)) {
                                    String ala = "";
                                    for(String second: Almaty.keySet()) {
                                        ala = ala + " " + second.toString();
                                        out.println("Server:" + ala);
                                    }
                                }
                            }
                        } else if(clientMessage.startsWith("toall")) {
                            String[] msg = clientMessage.split(" ", 2);
                            if(inGroup == 0) {
                                out.println("Server:You're not in any group.");
                            } else if(inGroup == 1) {
                                if(groupName.equals("Almaty")) {
                                    for(String second: Almaty.keySet()) {
                                        PrintWriter output = new PrintWriter(Almaty.get(second).getOutputStream(), true);
                                        output.println(user + ":" + msg[1]);
                                    }
                                } else if(groupName.equals("Astana")) {
                                    for(String first: Astana.keySet()) {
                                        PrintWriter output = new PrintWriter(Astana.get(first).getOutputStream(), true);
                                        output.println(user + ":" + msg[1]);
                                    }
                                }
                            }
                        } else if(clientMessage.startsWith("server leave")) {
                            String[] group = clientMessage.split(" ");

                            if(group.length < 3) {
                                out.println("Server:Please enter the name of group.");
                            } else if(group[2].equals("Astana")) {
                                if (inGroup == 0) {
                                    out.println("Server:You're not in any group.");
                                } else if (inGroup == 1) {
                                    out.println("Server:You have left Astana group.");
                                    Astana.remove(user);
                                    inGroup = 0;
                                }
                            } else if(group[2].equals("Almaty")) {
                                if (inGroup == 0) {
                                    out.println("Server:You're not in any group.");
                                } else if (inGroup == 1) {
                                    out.println("Server:You have left Almaty group.");
                                    Almaty.remove(user);
                                    inGroup = 0;
                                }
                            }se
                        } else {
                            String[] div = clientMessage.split(" ", 2);
                            if(div.length < 2) {
                                out.println("Server:Unexpected command!");
                                continue;
                            }
                            if(Astana.containsKey(div[0])) {
                                    PrintWriter output = new PrintWriter(Astana.get(div[0]).getOutputStream(), true);
                                    output.println(user + ":" + div[1]);
                            } else if(Almaty.containsKey(div[0])) {
                                    PrintWriter output = new PrintWriter(Almaty.get(div[0]).getOutputStream(), true);
                                    output.println(user + ":" + div[1]);
                            } else {
                                out.println("Server:Unexpected command!");
                            }

                        }

                    } else if(connection == 0) {
                        if (clientMessage.startsWith("server hello")) {
                            String[] div = clientMessage.split(" ");
                            if (div.length < 3) {
                                out.println("Server:Please enter your login.");
                                continue;
                            } else if(div.length > 3) {
                                out.println("Server:Your login should be one word.");
                                continue;
                            }
                            if (login.contains(div[2])) {
                                out.println("Server:Such username is already exists!");
                                continue;
                            }
                            out.println("Server:Hi " + div[2]);
                            user = div[2];
                            connection = 1;
                            login.add(div[2]);
                        } else {
                            out.println("Server:Please enter: server hello <username> to establish the connection.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);

        while(true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new RunServer(clientSocket)).start();
        }
    }
}
