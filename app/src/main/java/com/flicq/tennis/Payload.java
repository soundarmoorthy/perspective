package com.flicq.tennis;

import java.nio.ByteBuffer;

public class Payload {
    public ByteBuffer bb = null;
    int index;
    boolean inUse;

    Payload(int index) {
        this.bb = ByteBuffer.allocateDirect(60); // 60 is larger than all packet sizes
        inUse = false;
        reclaim();
    }

    void reclaim() {
        clear();
        inUse = false;
    }

    void clear() {
        this.bb.position(0);
        this.bb.order(null); // Little endian
    }

    boolean claim() {
        if (!inUse) {
            inUse = true;
            return true;
        }
        return false;
    }

}


