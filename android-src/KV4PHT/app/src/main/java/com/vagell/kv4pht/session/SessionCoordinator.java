package com.vagell.kv4pht.session;

/** One transmit owner at a time. Releasing or unkeying always clears that owner. */
public final class SessionCoordinator {
    public enum Owner { NONE, VOICE, PACKET }

    private Owner owner = Owner.NONE;
    private boolean unkeyed;

    public synchronized boolean tryAcquire(Owner next) {
        if (next == null || next == Owner.NONE) {
            return false;
        }
        if (owner != Owner.NONE && owner != next) {
            return false;
        }
        owner = next;
        unkeyed = false;
        return true;
    }

    public synchronized void release(Owner who) {
        if (owner == who) {
            owner = Owner.NONE;
        }
    }

    public synchronized void forceUnkey() {
        owner = Owner.NONE;
        unkeyed = true;
    }

    public synchronized Owner owner() {
        return owner;
    }

    public synchronized boolean unkeyRequested() {
        return unkeyed;
    }
}
