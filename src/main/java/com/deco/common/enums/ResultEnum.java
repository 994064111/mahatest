package com.deco.common.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
	SUCCESS(101,"SUCCESS"),
    FAILURE(102,"FAIL"),
    USER_NEED_AUTHORITIES(201,"User need Authorities"),
    USER_LOGIN_FAILED(202,"User account or password error"),
    USER_LOGIN_SUCCESS(203,"Login success"),
    USER_NO_ACCESS(204,"Users do not have access"),
    USER_LOGOUT_SUCCESS(205,"Logout Success"),
    TOKEN_IS_BLACKLIST(206,"Token on the blacklist"),
    LOGIN_IS_OVERDUE(207,"Expired token"),
    TOKEN_INVALID(208,"Token Invalid")
    ;
 
	private Integer code;
    
    public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
 
    private String message;
    
    public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message == null ? null : message.trim();
	}
 
    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    /**
     * @deprecation:通过code返回枚举
     * @param code
     * @return
     */
    public static ResultEnum parse(int code){
        ResultEnum[] values = values();
        for (ResultEnum value : values) {
            if(value.getCode() == code){
                return value;
            }
        }
        throw  new RuntimeException("Unknown code of ResultEnum");
    }

}
