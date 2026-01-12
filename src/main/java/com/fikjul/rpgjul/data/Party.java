package com.fikjul.rpgjul.data;

import java.util.*;

/**
 * Represents a party of players
 */
public class Party {
    private final UUID partyId;
    private UUID leaderId;
    private final Set<UUID> members;
    private final long createdAt;

    public Party(UUID partyId, UUID leaderId) {
        this.partyId = partyId;
        this.leaderId = leaderId;
        this.members = new HashSet<>();
        this.members.add(leaderId);
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public void addMember(UUID playerId) {
        members.add(playerId);
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public boolean isMember(UUID playerId) {
        return members.contains(playerId);
    }

    public boolean isLeader(UUID playerId) {
        return leaderId.equals(playerId);
    }

    public int getSize() {
        return members.size();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Promote a new leader when current leader leaves
     */
    public void promoteNewLeader() {
        if (members.isEmpty()) {
            return;
        }
        
        // Remove old leader if still in set
        members.remove(leaderId);
        
        if (!members.isEmpty()) {
            // Promote first member
            leaderId = members.iterator().next();
        }
    }
}
