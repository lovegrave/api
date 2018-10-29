package cn.yanss.m.cookhouse.api.entity;

import lombok.Data;

import java.util.Date;

@Data
public class HistoryRequest {

    private String deliver;

    private String total;

    private String refund;

    private Date start;

    private Date end;

    private Integer storeId;

    private Integer pageNum;

    private Integer pageSize;

    public HistoryRequest(String deliver, String total, String refund, Date start, Date end, Integer storeId, Integer pageNum, Integer pageSize) {
        this.deliver = deliver;
        this.total = total;
        this.refund = refund;
        this.start = start;
        this.end = end;
        this.storeId = storeId;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public HistoryRequest() {
    }
}
