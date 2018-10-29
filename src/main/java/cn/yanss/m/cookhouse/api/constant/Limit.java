package cn.yanss.m.cookhouse.api.constant;

import java.util.concurrent.Semaphore;

public class Limit {

    private Limit() {
    }

    /**
     * 信号量控制
     */
    public static final Semaphore semLimit = new Semaphore(10);

    public static final Semaphore hisLimit = new Semaphore(3);
}
