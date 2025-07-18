package com.kardex.infrastructure.adapters.output.exception.handler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRuleException extends BaseException {

  public BusinessRuleException(int status, String message) {
    super(status, message);
  }
}
