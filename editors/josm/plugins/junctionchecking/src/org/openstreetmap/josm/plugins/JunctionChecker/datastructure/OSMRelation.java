// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.ArrayList;

/**
 * @author  joerg
 */
public class OSMRelation extends OSMEntity {

    private ArrayList<Member> members = new ArrayList<>();

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    /**
     * gibt den Member mit der Rolle zurück, wenn vorhanden. Sonst null
     */
    public OSMEntity getMember(String role) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getRole().equals(role)) {
                return members.get(i).getMember();
            }
        }
        return null;
    }

    public void addMember(OSMEntity member, String role) {
        members.add(new Member(member, role));
    }

    public String getRelationType(String key) {
        return this.getValue(key);
    }

    @Override
    public String toString() {
        String s = ("Relation-ID: " + this.getId() + " Relation-Type: " + this.getRelationType("type") +"\n");
        for (Member member : members) {
            s += member.toString();
        }
        return s;
    }


    /**
     * Klasse dient zum Verwalten eines Member-Objektes. Muß ein Objekt vom Typ OSMEntitysein.
     * @author  joerg
     */
    static class Member {

        private OSMEntity member;
        private String role;

        Member(OSMEntity member, String role) {
            this.member = member;
            this.setRole(role);
        }

        public Class<? extends OSMEntity> getType() {
            return member.getClass();
        }

        public void setMember(OSMEntity member) {
            this.member = member;
        }

        public OSMEntity getMember() {
            return member;
        }

        public Long getId() {
            return member.getId();
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }

        @Override
        public String toString() {
            return "Member: " + getRole() + ", ref:" + getId() + ", type:" + getType().getName();
        }
    }
}
