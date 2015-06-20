/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mash1t.kickstart.server;

import de.mash1t.kickstart.counters.Counters;
import de.mash1t.networklib.packets.Packet;

/**
 *
 * @author Manuel Schmid
 */
public class Helper {

    /**
     * Writes a Packet to a specific ObjectOutputStream
     *
     * @param packet stands for itself
     * @param thread ClientThread to send obj to
     * @return result of sending
     */
    public static boolean send(Packet packet, ClientThread thread) {
        try {
            Counters.connection();
            return thread.conLib.send(packet);
        } catch (Exception ex) {
            Counters.exception();
            return false;
        }
    }
}
