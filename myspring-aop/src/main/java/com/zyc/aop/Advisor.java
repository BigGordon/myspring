package com.zyc.aop;

import java.util.List;

/**
 * @author Gordon
 * Advisor记录一次AOP行为，针对特定的切口（Pointcut），实施特定的增强方法（Advice）
 * @since 2019/3/5
 */
public class Advisor {
    /** 特定的切口 */
    private Pointcut pointcut;
    /** 特定的增强方法 */
    private List<Advice> advices;

    public Pointcut getPointcut() {
        return pointcut;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    public List<Advice> getAdvices() {
        return advices;
    }

    public void setAdvices(List<Advice> advices) {
        this.advices = advices;
    }
}
