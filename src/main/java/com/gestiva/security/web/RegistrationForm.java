package com.gestiva.security.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Il nome tenant è obbligatorio")
    @Size(max = 120, message = "Il nome tenant non può superare 120 caratteri")
    private String tenantName;

    @NotBlank(message = "L'email tenant è obbligatoria")
    @Email(message = "L'email tenant non è valida")
    @Size(max = 180, message = "L'email tenant non può superare 180 caratteri")
    private String tenantEmail;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 120, message = "Il nome non può superare 120 caratteri")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(max = 120, message = "Il cognome non può superare 120 caratteri")
    private String lastName;

    @NotBlank(message = "L'email utente è obbligatoria")
    @Email(message = "L'email utente non è valida")
    @Size(max = 180, message = "L'email utente non può superare 180 caratteri")
    private String userEmail;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 8, max = 100, message = "La password deve avere tra 8 e 100 caratteri")
    private String password;

    @NotBlank(message = "La conferma password è obbligatoria")
    private String confirmPassword;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}