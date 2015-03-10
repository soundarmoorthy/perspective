package com.flicq.tennis;

import java.util.ArrayList;

public class PayloadPool {
    public int maxPayloads;
    int maxClaimed;
    ArrayList<Payload> payloadPool = new ArrayList<Payload>();

    PayloadPool(int size) {
        maxPayloads = size;
        int i;
        maxClaimed = -1;
        for (i = 0; i < size; i++) {
            payloadPool.add(new Payload(i));
        }
    }

    public Payload getInstance() {
        int size = payloadPool.size();
        Payload pl = null;
        for (int i = 0; i < size; i++) {
            pl = payloadPool.get(i);
            if (pl.claim()) {
                if (i > maxClaimed) {
                        maxClaimed = i; // track the maximum payload claimed.
                }
                return (pl);
            }
        }
        return (null);
    }
}


