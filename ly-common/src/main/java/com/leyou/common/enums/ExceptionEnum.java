package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400, "商品价格不能为空"),
    CATEGORY_NOT_FOUND(404, "商品分类不存在"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404, "商品规格参数不存在"),
    GOODS_NOT_FOUND(404, "商品不存在"),
    GOODS_DETAIL_NOT_FOUND(404, "商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404, "商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404, "商品库存不存在"),
    BRAND_NOT_FOUND(500, "品牌不存在"),
    BRAND_SAVE_ERROR(500, "新增品牌失败"),
    UPLOAD_FILE_ERROR(500, "文件上传失败"),
    INVALID_FILE_TYPE(500, "校验文件类型不匹配"),
    GOODS_SAVE_ERROR(500, "新增商品失败"),
    GOODS_UPDATE_ERROR(500, "更新商品失败"),
    INVALID_USER_DATA_TYPE_ERROR(400,"用户数据类型不正确"),
    GOODS_ID_CANNOT_BE_NULL(400, "商品ID不能为空"),
    INVALID_VERIFY_CODE_ERROR(400,"验证码不正确"),
    INVALID_USERNAME_PASSWORD_ERROR(400,"用户名或密码错误"),
    CREATE_TOKEN_ERROR(500,"token生成失败"),
    UNAUTHORIZED(403,"未授权"),
    CART_NOT_FOUND(404,"购物车信息未找到"),
    RECEIVER_ADDRESS_NOT_FOUND(404,"收获地址不存在"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    CREATE_ORDER_ERROR(500,"创建订单失败"),
    CREATE_ORDER_DETAIL_ERROR(500,"创建订单详情失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    ORDER_STATUS_EXCEPTION(500,"订单状态异常"),
    CREATE_PAY_URL_ERROR(500,"创建支付链接异常"),
    WX_PAY_SIGN_INVALID(500,"微信支付签名失败"),
    WX_PAY_NOTIFY_PARAM_ERROR(500,"微信支付回调数据不正确"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败");
    private int code;
    private String msg;
}
