// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.geochat;

import java.time.Instant;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * One message.
 *
 * @author zverik
 */
public final class ChatMessage implements Comparable<ChatMessage> {
    private final LatLon pos;
    private final Instant time;
    private final String author;
    private String recipient;
    private final String message;
    private final long id;
    private boolean priv;
    private final boolean incoming;

    public ChatMessage(long id, LatLon pos, String author, boolean incoming, String message, Instant time) {
        this.id = id;
        this.author = author;
        this.message = message;
        this.pos = pos;
        this.time = time;
        this.incoming = incoming;
        this.priv = false;
        this.recipient = null;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setPrivate(boolean priv) {
        this.priv = priv;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * Is only set when the message is not incoming, that is, author is the current user.
     * @return recipient
     */
    public String getRecipient() {
        return recipient;
    }

    public long getId() {
        return id;
    }

    public LatLon getPosition() {
        return pos;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPrivate() {
        return priv;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public Instant getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ChatMessage other = (ChatMessage) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public int compareTo(ChatMessage o) {
        long otherId = o.id;
        return otherId < id ? 1 : otherId == id ? 0 : 1;
    }
}
