/*
 * The MIT License
 *
 * Copyright 2015 Manuel Schmid.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.mash1t.kickstart.server;

import de.mash1t.kickstart.config.ConfigController;
import de.mash1t.kickstart.config.ConfigParam;
import de.mash1t.kickstart.counters.Counters;
import de.mash1t.networklib.packets.KickPacket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class ChatServer initializes threads and accepts new clients
 */
public final class ChatServer {

    // Setting up client
    protected static final List<ClientThread> threads = new ArrayList<>();
    protected static List<String> userList = new ArrayList<>();
    
    // Config controller
    private static final ConfigController conf = new ConfigController("ServerConfig.ini");

    /**
     * Main method for server
     *
     * @param args
     */
    public static void main(String args[]) {
        System.out.println("Reading configuration file");
        if (conf.readConfigFile() && conf.validateConfig()) {
            runServer();
        } else {
            System.out.print("Server configuration file not found/readable/valid \nRestore default configuration? (y/n): ");
            Scanner sc = new Scanner(System.in);
            if (sc.nextLine().equals("y")) {
                if (conf.makeDefaultFile()) {
                    System.out.println("Restored default configuration");
                    conf.readConfigFile();
                    runServer();
                } else {
                    System.out.println("Error: Please check permissions");
                    System.out.println("Aborting Server");
                }
            } else {
                System.out.println("Aborting Server");
            }
        }
    }

    private static void runServer() {

        int portNumber = conf.getConfigValueInt(ConfigParam.Port);
        // maxClientsCount = 0 means infinite clients
        int maxClientsCount = conf.getConfigValueInt(ConfigParam.MaxClients);
        System.out.println("Server started on port " + portNumber);

        // Open a server socket on the portNumber (default 8000)
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);

            // Adding shutdown handle
            Runtime.getRuntime().addShutdownHook(new ShutdownHandle());

            Socket clientSocket = null;

            // Create client socket for each connection
            while (true) {
                try {
                    // Handle for new connection, put it into empty array-slot
                    clientSocket = serverSocket.accept();
                    Counters.connection();
                    // maxClientsCount = 0 means infinite clients
                    if (maxClientsCount == 0 || threads.size() < maxClientsCount) {
                        ClientThread clientThread = new ClientThread(clientSocket);
                        threads.add(clientThread);
                        clientThread.start();
                        Counters.login();
                    } else {
                        // Only when maxclients is reached        
                        RejectionThread fThread = new RejectionThread(clientSocket);
                        fThread.start();
                    }
                } catch (IOException ex) {
                    Counters.exception();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
            Counters.exception();
        }
    }

    /**
     * Getter for the userlist
     *
     * @return
     */
    public static List<String> getUserList() {
        return userList;
    }
}

class ShutdownHandle extends Thread {

    @Override
    public void run() {

        System.out.println("Shutting down Server");

        // Send closing of server to all clients
        for (ClientThread thread : ChatServer.threads) {
            if (thread.state == ConnectionState.Online) {
                Helper.send(new KickPacket("*** SERVER IS GOING DOWN ***"), thread);
            }
        }
        System.out.println("Shut down successfully");
    }
}
