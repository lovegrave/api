package cn.yanss.m.cookhouse.api.dao.entity;

import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreResponse {

    private String token;
    private Integer storeId;
    private String storeName;
    private String meituanStoreId;
    private String dadaStoreId;
    private String bardStoreId;
    private String intro;
    private Double lng;
    private Double lat;
    private String linkman;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Integer status;
    private List<Busniss> businessTime;
    private String storeNotice;
    private String addressDetail;
    private Double sendPrice;
    private String location;
    private String sendType;
    private Integer bookDay;
    private Integer receiveTime;
    private String storeLogo;
    private int permissions;

    public StoreResponse(String token) {
        this.token = token;
    }

    public StoreResponse() {
    }

    public List<Busniss> getBusinessTime() {
        return businessTime;
    }

    public void setBusinessTime(String businessTime) throws IOException {
        this.businessTime = MapperUtils.json2list(businessTime,Busniss.class);
    }

    @Data
    public static class Busniss {
        @JsonFormat(pattern = "HH:mm")
        private Date startTime;
        @JsonFormat(pattern = "HH:mm")
        private Date endTime;
    }
}
