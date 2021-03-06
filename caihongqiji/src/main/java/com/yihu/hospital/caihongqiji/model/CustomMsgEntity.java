package com.yihu.hospital.caihongqiji.model;

/**
 * Created by onekey on 2017/4/5.
 */

public class CustomMsgEntity {

    public static final String GuestGroupChat = "message/text/guestGroupChat";
    public static final String Guest2LiveGuest = "message/text/guest2LiveGuest";

    public static final String TopMessage = "message/control/topMessage";
    public static final String CancelTopMessage = "message/control/cancelTopMessage";

    public static final String AdminExitRoom = "message/control/adminexitroom";


    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public MsgBody getMsgbody() {
        return msgbody;
    }

    public void setMsgbody(MsgBody msgbody) {
        this.msgbody = msgbody;
    }

    private String cmd;

    @Override
    public String toString() {
        return "CustomMsgEntity{" +
                "cmd='" + cmd + '\'' +
                ", request=" + request +
                ", msgbody=" + msgbody +
                '}';
    }

    private Request request;

    private MsgBody msgbody;

    public class Request {
        String msgtype;
        String sequence;
        String version;
    };

    public class MsgBody {
        public String getMsgContent() {
            return msgContent;
        }

        public void setMsgContent(String msgContent) {
            this.msgContent = msgContent;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        @Override
        public String toString() {
            return "MsgBody{" +
                    "msgContent='" + msgContent + '\'' +
                    ", sender='" + sender + '\'' +
                    '}';
        }

        String msgContent;
        String sender;
    };

}
