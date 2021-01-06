package com.kevin.minirpc;

/**
 * @author kevin.lee
 * @date 2021/1/3 0003
 */
public class RpcReponse {
    public RpcReponse(String requestId, Object result) {
        this.requestId = requestId;
        this.result = result;
    }

    /**
     * 请求对象的ID
     */
    private String requestId;

    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
