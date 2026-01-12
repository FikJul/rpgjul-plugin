package com.fikjul.rpgjul.data;

import java.util.UUID;

/**
 * Represents a pending party invitation
 */
public class PartyInvitation {
    private final UUID partyId;
    private final UUID inviterId;
    private final UUID inviteeId;
    private final long expiresAt;

    public PartyInvitation(UUID partyId, UUID inviterId, UUID inviteeId, int expireSeconds) {
        this.partyId = partyId;
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
        this.expiresAt = System.currentTimeMillis() + (expireSeconds * 1000L);
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getInviterId() {
        return inviterId;
    }

    public UUID getInviteeId() {
        return inviteeId;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public long getTimeRemaining() {
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
}
