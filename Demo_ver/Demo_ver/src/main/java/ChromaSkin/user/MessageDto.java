package ChromaSkin.user;

import org.springframework.web.bind.annotation.RequestMethod;

public class MessageDto {
    private String message;
    private String redirectUrl;
    private RequestMethod method;
    private Object data;

    // 생성자
    public MessageDto(String message, String redirectUrl, RequestMethod method, Object data) {
        this.message = message;
        this.redirectUrl = redirectUrl;
        this.method = method;
        this.data = data;
    }

    // Getter 및 Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

