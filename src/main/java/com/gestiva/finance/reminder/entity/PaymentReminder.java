package com.gestiva.finance.reminder.entity;

import com.gestiva.common.model.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reminder")
public class PaymentReminder extends TenantAwareEntity {

    @Column(name = "payment_schedule_id", nullable = false)
    private Long paymentScheduleId;

    @Column(nullable = false, length = 30)
    private String channel; // EMAIL, MANUAL

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, SENT, FAILED

    public Long getPaymentScheduleId() {
        return paymentScheduleId;
    }

    public void setPaymentScheduleId(Long paymentScheduleId) {
        this.paymentScheduleId = paymentScheduleId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}