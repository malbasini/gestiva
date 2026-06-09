package com.gestiva.billing.paypal;

import java.util.List;

public class PaypalCreateOrderResponse {
    private String id;
    private String status;
    private List<Link> links;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Link> getLinks() { return links; }
    public void setLinks(List<Link> links) { this.links = links; }

    public static class Link {
        private String href;
        private String rel;
        private String method;

        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }

        public String getRel() { return rel; }
        public void setRel(String rel) { this.rel = rel; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }
}
