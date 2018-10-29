package cn.yanss.m.cookhouse.api.disruptor.exception;

import cn.yanss.m.util.LogUtils;
import com.lmax.disruptor.ExceptionHandler;

/**
 * @author hl
 * @desc disruptor 异常
 */

public class NotifyEventHandlerException implements ExceptionHandler {
    @Override
    public void handleEventException(Throwable throwable, long sequence, Object event) {
        throwable.fillInStackTrace();
        LogUtils.create().methodName("handleEventException").message(throwable.getMessage()).error();
    }

    @Override
    public void handleOnStartException(Throwable throwable) {
        LogUtils.create().methodName("handleOnStartException").message(throwable.getMessage()).error();
    }

    @Override
    public void handleOnShutdownException(Throwable throwable) {
        LogUtils.create().methodName("handleOnShutdownException").message(throwable.getMessage()).error();
    }
}
