package com.gestiva.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        jdbcTemplate.execute("DELETE FROM sales_order_line");
        jdbcTemplate.execute("DELETE FROM sales_order");
        jdbcTemplate.execute("DELETE FROM quote_line");
        jdbcTemplate.execute("DELETE FROM quote");
        jdbcTemplate.execute("DELETE FROM document_sequence");
        jdbcTemplate.execute("DELETE FROM crm_note");
        jdbcTemplate.execute("DELETE FROM crm_activity");
        jdbcTemplate.execute("DELETE FROM opportunity");
        jdbcTemplate.execute("DELETE FROM crm_lead");
        jdbcTemplate.execute("DELETE FROM contact_person");
        jdbcTemplate.execute("DELETE FROM supplier");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM company_profile");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM permission");
        jdbcTemplate.execute("DELETE FROM role");
        jdbcTemplate.execute("DELETE FROM app_user");
        jdbcTemplate.execute("DELETE FROM tenant_module");
        jdbcTemplate.execute("DELETE FROM audit_log");
        jdbcTemplate.execute("DELETE FROM ai_conversation_log");
        jdbcTemplate.execute("DELETE FROM payment_reminder");
        jdbcTemplate.execute("DELETE FROM payment_schedule");
        jdbcTemplate.execute("DELETE FROM attachment");
        jdbcTemplate.execute("DELETE FROM document_template");
        jdbcTemplate.execute("DELETE FROM tenant");
    }
}
