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

import de.mash1t.kickstart.counters.Counters;
import de.mash1t.networklib.methods.TCP;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import de.mash1t.networklib.packets.Packet;
import de.mash1t.networklib.packets.PacketType;
import de.mash1t.networklib.packets.ConnectPacket;
import de.mash1t.networklib.packets.KickPacket;

/**
 * Class for rejecting clients politely if server has already maxClients
 *
 * @author Manuel Schmid
 */
public final class RejectionThread extends Thread {

    protected TCP conLib;
    protected SocketAddress ip;

    /**
     * Constructor
     *
     * @param clientSocket Sochet where the connection was accepted
     * @throws java.io.IOException
     */
    public RejectionThread(Socket clientSocket) throws IOException {
        conLib = new TCP(clientSocket);
    }

    /**
     * Called when thread is started
     */
    @Override
    public void run() {

        Packet clientAnswer = conLib.read();
        PacketType pType = clientAnswer.getType();

        if (pType == PacketType.Connect) {
            String name = ((ConnectPacket) clientAnswer).getName();
            conLib.send(new KickPacket("Sorry \"" + name + "\", too many clients. Please try later."));
        } else {
            conLib.send(new KickPacket("Security breach: Please do not use a modified client"));
        }

        conLib.close();
        Counters.rejected();
    }
}
