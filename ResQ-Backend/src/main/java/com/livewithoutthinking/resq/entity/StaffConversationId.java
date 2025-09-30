package com.livewithoutthinking.resq.entity;

import java.io.Serializable;
import java.util.Objects;

public class StaffConversationId implements Serializable {

    private Integer staff;
    private Integer conversation;

    // Constructor mặc định
    public StaffConversationId() {}

    public StaffConversationId(Integer staff, Integer conversation) {
        this.staff = staff;
        this.conversation = conversation;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaffConversationId)) return false;
        StaffConversationId that = (StaffConversationId) o;
        return Objects.equals(staff, that.staff) && Objects.equals(conversation, that.conversation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staff, conversation);
    }
}
