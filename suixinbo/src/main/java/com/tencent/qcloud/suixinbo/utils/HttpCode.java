package com.tencent.qcloud.suixinbo.utils;

/**
 * 网络交互处理
 */
public class HttpCode {

        public static String getContentByCode(int code) {
            String ret = "网络异常";
            switch (code) {
                case 10001:
                    ret = "请求有误";
                    break;
                case 10002:
                    ret = "请求json错误";
                    break;
                case 10003:
                    ret = "请求数据错误";
                    break;
                case 10004:
                    ret = "用户已经注册";
                    break;
                case 10005:
                    ret = "用户不存在";
                    break;
                case 10006:
                    ret = "密码有误";
                    break;
                case 10007:
                    ret = "重复登录";
                    break;
                case 10008:
                    ret = "重复退出";
                    break;
                case 10009:
                    ret = "token过期";
                    break;
                case 10010:
                    ret = "直播房间不存在";
                    break;
                case 20001:
                    ret = "用户没有av房间ID";
                    break;
                case 20002:
                    ret = "用户没有在直播";
                    break;
                case 30001:
                    ret = "验证码过期";
                    break;
                case 30002:
                    ret = "验证码错误";
                    break;
                case 30003:
                    ret = "沒有进行code验证";
                    break;
                case 30004:
                    ret = "无权限创建code";
                    break;
                case 40001:
                    ret = "无权限创建房间";
                    break;
                case 50001:
                    ret = "PPT已经存在";
                    break;
                case 50002:
                    ret = "没有权限上传/删除PPT";
                    break;
                case 90000:
                    ret = "服务器内部错误";
                    break;

            }
            return ret;
        }


}
