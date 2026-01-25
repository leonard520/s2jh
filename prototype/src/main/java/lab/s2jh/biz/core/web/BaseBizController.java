package lab.s2jh.biz.core.web;

import lab.s2jh.biz.core.entity.BaseBizEntity;
import lab.s2jh.web.mvc.BaseMvcController;

public abstract class BaseBizController<T extends BaseBizEntity> extends BaseMvcController<T, Long> {

}
